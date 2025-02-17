package com.example.tzpoc.views


import android.app.ProgressDialog
import android.content.Context

import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Toast

import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.tzpoc.R
import com.example.tzpoc.databinding.ActivityContainerMappingBinding
import com.example.tzpoc.helper.Constants
import com.example.tzpoc.helper.RFIDHandler
import com.example.tzpoc.helper.Resource
import com.example.tzpoc.helper.SessionManager
import com.example.tzpoc.model.LocationResponse
import com.example.tzpoc.model.mapping.VehicleLocationRequest
import com.example.tzpoc.model.submit.GateContainerDetails
import com.example.tzpoc.model.submit.SubmitRequest
import com.example.tzpoc.viewmodel.mapping.MappingViewModel
import com.example.tzpoc.repository.TzRepository
import com.example.tzpoc.viewmodel.mapping.MappingViewModelFactory
import com.google.android.material.textfield.TextInputEditText
import com.zebra.rfid.api3.TagData
import es.dmoral.toasty.Toasty
import java.util.HashMap

class ContainerMappingActivity : AppCompatActivity(), RFIDHandler.ResponseHandlerInterface {

    lateinit var binding: ActivityContainerMappingBinding
    private lateinit var session: SessionManager
    private var token: String? = ""

    private lateinit var progress: ProgressDialog
    var rfidHandler: RFIDHandler? = null
    private var selectedContainerId1: String? = null
    private var selectedContainerId2: String? = null
    private var antennaPower: String? = ""

    private lateinit var userDetails: HashMap<String, String?>
    private lateinit var viewModel: MappingViewModel
    private var isJobAssigned = false

    private fun hideRemainingFields() {
        // Hide the remaining fields if job type is not "Import" or based on any other condition
        binding.clAdditionalFields.visibility = View.GONE
        binding.clAdditionalFields2.visibility = View.GONE
    }

    private fun initReader(antennaPower: Int) {
        rfidHandler = RFIDHandler()
        rfidHandler!!.init(this, this@ContainerMappingActivity, antennaPower)
    }

    // Pause RFID handling when the activity is paused
    override fun onPause() {
        super.onPause()
        rfidHandler?.onPause() // Pause RFID handler if needed
    }

    override fun onPostResume() {
        super.onPostResume()
        val status = rfidHandler?.onResume() ?: ""
        Toast.makeText(this@ContainerMappingActivity, status, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroy() {
        super.onDestroy()
        rfidHandler?.onDestroy()
    }

    // Start inventory scanning
    fun performInventory() {
        rfidHandler!!.performInventory()
    }

    // Stop inventory scanning
    fun stopInventory() {
        rfidHandler!!.stopInventory()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_container_mapping)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_container_mapping)
        session = SessionManager(this)

        val sharedPreferences = getSharedPreferences("AppPrefs", MODE_PRIVATE)
        val savedLocationName = sharedPreferences.getString("selected_location_name", "")


        val savedLocationId = sharedPreferences.getString("selected_location_id", "")
        if (!savedLocationName.isNullOrEmpty() && !savedLocationId.isNullOrEmpty()) {
            // Use the saved location name and ID
            binding.edDropdown.setText(savedLocationName, false)
            Log.d(
                "ContainerMappingActivity",
                "Saved Location: $savedLocationName, ID: $savedLocationId"
            )
        }
        userDetails = session.getUserDetails()

        if (userDetails.isEmpty()) {
            Toasty.error(this, "User details are missing.", Toasty.LENGTH_SHORT).show()
        } else {
            token = userDetails["jwtToken"]
        }

        progress = ProgressDialog(this)
        progress.setMessage("Please Wait...")

        val tzRepository = TzRepository()
        val viewModelProviderFactory = MappingViewModelFactory(application, tzRepository)
        viewModel = ViewModelProvider(this, viewModelProviderFactory)[MappingViewModel::class.java]
        setSupportActionBar(binding.mappingToolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.show()




        viewModel.locationMutableLiveData.observe(this, Observer { resource ->
            when (resource) {
                is Resource.Loading -> {
                    // Show loading spinner while data is being fetched
                    progress.show()
                }

                is Resource.Success -> {
                    val response = resource.data
                    progress.dismiss()
                    resource.data?.let { locations ->

                        if (locations.isNotEmpty()) {
                            val locationNames = locations.map { it.locationName }


                            Log.d("ContainerMappingActivity", "Location names: $locationNames")

                            val adapter = ArrayAdapter(
                                this,
                                android.R.layout.simple_dropdown_item_1line,
                                locationNames
                            )
                            binding.edDropdown.setAdapter(adapter)

                            binding.edDropdown.setOnItemClickListener { parent, view, position, id ->
                                val selectedLocation = locations[position]
                                val selectedLocationId = selectedLocation.deviceLocationMappingId

                                // Save the selected location in SharedPreferences
                                val editor = sharedPreferences.edit()
                                editor.putString(
                                    "selected_location_name",
                                    selectedLocation.locationName
                                )
                                editor.putString(
                                    "selected_location_id",
                                    selectedLocationId.toString()
                                )
                                editor.apply()

                                // Save the selected location ID in session
                                session.saveSelectedLocation(selectedLocationId.toString())

                                fetchVehicleByLocation(selectedLocationId)
                            }
//                            val successMessage = resource.statusMessage ?: "Vehicle mapping successful"
                            Toasty.success(this, "Success", Toasty.LENGTH_SHORT).show()
//                            println("Success: ${resource?.statusMessage}")

                        } else {
                            Toasty.warning(this, "No locations available.", Toasty.LENGTH_SHORT)
                                .show()
                        }
                    }
                }

                is Resource.Error -> {
                    progress.dismiss()
                    Toasty.error(
                        this,
                        "Error fetching locations: ${resource.message}",
                        Toasty.LENGTH_SHORT
                    ).show()
                }
            }
        })

        fetchLocationList()
        viewModel.vehicleLocationMutableLiveData.observe(this, Observer { resource ->
            when (resource) {
                is Resource.Loading -> {
                    progress.show()
                }

                is Resource.Success -> {
                    progress.dismiss()
                    resource.data?.let { vehicleList ->

                        if (vehicleList.isNotEmpty()) {
                            isJobAssigned = true
                            val vehicleVRN = vehicleList[0].vrn
                            val vehicalLength = vehicleList[0].length
                            val containerDetails = vehicleList[0].containerDetails ?: emptyList()
                            val ctrNos = containerDetails.map { it.ctrNo }
                            val ctrNosLength20 =
                                containerDetails.filter { it.length == 20.0 }.map { it.ctrNo }
                            val ctrNosLength40 =
                                containerDetails.filter { it.length == 40.0 }.map { it.ctrNo }
                            val defaultCtrNo = "CTR No"
                            binding.edRfid.setText(vehicleVRN)
                            val adapter20 = ArrayAdapter(
                                this,
                                android.R.layout.simple_dropdown_item_1line,
                                ctrNosLength20
                            )
                            val adapter40 = ArrayAdapter(
                                this,
                                android.R.layout.simple_dropdown_item_1line,
                                ctrNosLength40
                            )
                            binding.autoCompleteSelectAssetType2.setAdapter(adapter20)
                            binding.autoCompleteSelectAssetType2.setText(defaultCtrNo, false)

                            binding.autoCompleteSelectAssetType3.setAdapter(adapter40)
                            binding.autoCompleteSelectAssetType3.setText(defaultCtrNo, false)

                            binding.clAdditionalFields.visibility = View.VISIBLE
                            if (vehicalLength == 40.0) {
                                binding.autoCompleteSelectAssetType2.setAdapter(
                                    ArrayAdapter(
                                        this,
                                        android.R.layout.simple_dropdown_item_1line,
                                        ctrNos
                                    )
                                )
                                binding.autoCompleteSelectAssetType2.setOnItemClickListener { _, _, _, _ ->
                                    val selectedCtrNo =
                                        binding.autoCompleteSelectAssetType2.text.toString()
                                    val selectedContainerLength = containerDetails.find {
                                        it.ctrNo == selectedCtrNo

                                    }?.length

                                    val selectedContainer =
                                        containerDetails.find { it.ctrNo == selectedCtrNo }
                                    selectedContainer?.let {
                                        selectedContainerId1 =
                                            it.containerId.toString() // Store containerId for later use
                                        Log.d(
                                            "Selected Container",
                                            "ContainerId: $selectedContainerId1"
                                        )
                                        Toast.makeText(
                                            this,
                                            "Selected ContainerId: $selectedContainerId1",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }

                                    if (selectedContainerLength == 20.0) {
                                        binding.clAdditionalFields2.visibility = View.VISIBLE
                                        val ctrNosLength20 =
                                            containerDetails.filter { it.length == 20.0 }
                                                .map { it.ctrNo }
                                        if (ctrNosLength20.size <= 1) {
                                            binding.clAdditionalFields2.visibility = View.GONE
                                            Toasty.warning(
                                                this,
                                                "No other containers with length 20 available",
                                                Toasty.LENGTH_SHORT
                                            ).show()
                                        } else {
                                            binding.autoCompleteSelectAssetType3.setAdapter(
                                                ArrayAdapter(
                                                    this,
                                                    android.R.layout.simple_dropdown_item_1line,
                                                    ctrNosLength20
                                                )
                                            )
                                        }
                                    } else if (selectedContainerLength == 40.0) {
                                        binding.clAdditionalFields2.visibility = View.GONE
                                    }
                                }

                                binding.autoCompleteSelectAssetType3.setOnItemClickListener { _, _, _, _ ->

                                    val selectedCtrNo =
                                        binding.autoCompleteSelectAssetType3.text.toString()
                                    //                                    val selectedContainer = containerDetails.find { it.ctrNo == selectedCtrNo }
                                    //                                    selectedContainer?.let {
                                    //                                        val containerId = it.containerId
                                    //                                        // Use this containerId in your submission logic or further processing
                                    //                                    }
                                    val selectedContainer =
                                        containerDetails.find { it.ctrNo == selectedCtrNo }
                                    selectedContainer?.let {
                                        selectedContainerId2 =
                                            it.containerId.toString() // Store containerId for later use
                                        Log.d(
                                            "Selected Container",
                                            "ContainerId: $selectedContainerId2"
                                        )
                                        Toast.makeText(
                                            this,
                                            "Selected ContainerId: $selectedContainerId2",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }


                                }
                            } else if (vehicalLength == 20.0) {
                                binding.clAdditionalFields.visibility = View.VISIBLE

                                binding.clAdditionalFields2.visibility =
                                    View.GONE  // Hide clAdditionalFields2 for jobLength 20
                                binding.autoCompleteSelectAssetType2.setAdapter(
                                    ArrayAdapter(
                                        this,
                                        android.R.layout.simple_dropdown_item_1line,
                                        ctrNosLength20
                                    )
                                )
                                binding.autoCompleteSelectAssetType2.setOnItemClickListener { _, _, _, _ ->
                                    val selectedCtrNo =
                                        binding.autoCompleteSelectAssetType2.text.toString()
                                    val selectedContainerLength = containerDetails.find {
                                        it.ctrNo == selectedCtrNo

                                    }?.length

                                    val selectedContainer =
                                        containerDetails.find { it.ctrNo == selectedCtrNo }
                                    selectedContainer?.let {
                                        selectedContainerId1 =
                                            it.containerId.toString() // Store containerId for later use
                                        Log.d(
                                            "Selected Container",
                                            "ContainerId: $selectedContainerId1"
                                        )
                                        Toast.makeText(
                                            this,
                                            "Selected ContainerId: $selectedContainerId1",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }

                                    if (selectedContainerLength == 20.0) {
                                        binding.clAdditionalFields2.visibility = View.VISIBLE
                                        val ctrNosLength20 =
                                            containerDetails.filter { it.length == 20.0 }
                                                .map { it.ctrNo }
                                        if (ctrNosLength20.size <= 1) {
                                            binding.clAdditionalFields2.visibility = View.GONE

                                        } else {
                                            binding.autoCompleteSelectAssetType3.setAdapter(
                                                ArrayAdapter(
                                                    this,
                                                    android.R.layout.simple_dropdown_item_1line,
                                                    ctrNosLength20
                                                )
                                            )
                                        }
                                    } else if (selectedContainerLength == 40.0) {
                                        binding.clAdditionalFields2.visibility = View.GONE
                                    }
                                }
                            } else {
                                Toasty.warning(this, "No vehicle VRN found", Toasty.LENGTH_SHORT)
                                    .show()
                            }
                        }
                    }
                }

                is Resource.Error -> {
                    progress.dismiss()
                    Toasty.error(
                        this,
                        "Error fetching vehicle location: ${resource.message}",
                        Toasty.LENGTH_SHORT
                    ).show()
                    Log.d(
                        "ContainerMappingActivity",
                        "Error fetching vehicle list: ${resource.message}"
                    )
                }
            }
        })
        viewModel.submitMutableLiveData.observe(this) { response ->
            if (isFinishing || isDestroyed) return@observe // Prevent UI update after activity is closed

            when (response) {
                is Resource.Success -> {
                    val successMessage = response.data?.responseMessage
                    val errorMessage =response.data?.errorMessage

                    if (!successMessage.isNullOrEmpty()) {
                        Toasty.success(this, successMessage, Toasty.LENGTH_SHORT).show()
                        finish()
                    } else {

                        Toasty.error(this, errorMessage.toString(), Toasty.LENGTH_SHORT).show()
                        finish()
                    }
                }
                is Resource.Error -> {
                    Toasty.error(this, response.message ?: "Submit Failed", Toasty.LENGTH_SHORT).show()
                }
                is Resource.Loading -> {
                    showProgressBar()
                }
            }
        }

        binding.btnSubmit.setOnClickListener {
            if (isJobAssigned) {
                val jobDetails = viewModel.vehicleLocationMutableLiveData.value?.data

                if (jobDetails != null) {
                    val rfidTag = getRFIDTag()
                    val jobId = jobDetails[0].vehicleId
                    val locationType = "Entry"
                    val containerDetails = jobDetails[0].containerDetails

                    // Fetch dropdown selections
                    val selectedContainer1 = binding.autoCompleteSelectAssetType2.text.toString()
                    val selectedContainer2 = binding.autoCompleteSelectAssetType3.text.toString()

                    Log.d("Submit", "Selected Container 1: $selectedContainerId1")
                    Log.d("Submit", "Selected Container 2: $selectedContainer2")
                    Log.d("containerDetails", "Selected Container 1: $containerDetails")

                    if (rfidTag.isNullOrEmpty()) {
                        Toasty.warning(this, "RFID tag is missing or invalid", Toasty.LENGTH_SHORT)
                            .show()
                        return@setOnClickListener
                    }

                    val containerDetailsList = mutableListOf<GateContainerDetails>()
                    Log.d(
                        "containerDetails------------>",
                        "Selected Container 1: $containerDetailsList"
                    )
                    // RFID tag inputs from UI
                    val tag1 = binding.edRfid2.text.toString()
                    val tag2 = binding.edRfid3.text.toString()
                    Log.d("RFID Capture", "Captured RFID Tags - Tag1: $tag1, Tag2: $tag2")
                    containerDetails.forEach { container ->
                        val containerIdString = container.containerId.toString() ?: ""

                        if (!tag1.isNullOrEmpty() && selectedContainerId1 == containerIdString) {
                            Log.d("Container Matching", "Adding Container1: ID=${container.containerId}, TagNo=$tag1")
                            containerDetailsList.add(GateContainerDetails(container.containerId ?: 0, tag1))
                        }
                        if (tag2.isNotEmpty() && selectedContainerId2 == containerIdString) {
                            containerDetailsList.add(
                                GateContainerDetails(
                                    ContainerId = container.containerId ?: 0,
                                    TagNo = tag2
                                )
                            )
                        }
                    }
                    if (containerDetailsList.isEmpty()) {
                        Log.e(
                            "Submit----------",
                            "❌ containerDetailsList is EMPTY! No valid containers selected."
                        )
                    } else {
                        Log.d(
                            "Submit----------",
                            "✅ containerDetailsList is READY for submission: $containerDetailsList"
                        )
                    }
                    if (containerDetails.isNotEmpty()) {
                        val submitRequest = SubmitRequest(
                            UserName = userDetails["userName"] ?: "Unknown",
                            VehicleId = jobId,
                            LocationType = locationType,
                            GateContainerDetails = containerDetailsList
                        )

                        // ✅ Log request data before calling API
                        Log.d("SubmitRequest", "Submitting Request: $submitRequest")

                        try {
                            submit(submitRequest)

                        } catch (e: Exception) {
                            Log.e("API Error", "Error while submitting request: ${e.message}")
                            Toasty.error(this, "Failed to submit request.", Toasty.LENGTH_SHORT)
                                .show()
                        }
                    } else {
                        Toasty.warning(this, "No valid containers selected.", Toasty.LENGTH_SHORT)
                            .show()
                    }
                } else {
                    Toasty.error(this, "Vehicle details not found.", Toasty.LENGTH_SHORT).show()
                }
            } else {
                Toasty.warning(this, "Check the data", Toasty.LENGTH_SHORT).show()
            }
        }



        binding.btnClear.setOnClickListener {

            //            binding.edRfid.setText("") // Assuming edRfid is a TextInputEditText for RFID
            binding.edRfid2.setText("") // Clear other text fields
            binding.edRfid3.setText("") // Clear other text fields

            // Reset the dropdown fields if necessary
            binding.autoCompleteSelectAssetType2.setText("", false) // Reset to default or empty
            binding.autoCompleteSelectAssetType3.setText("", false) // Reset to default or empty

            // Hide the additional fields
            binding.clAdditionalFields.visibility = View.GONE
            binding.clAdditionalFields2.visibility = View.GONE

            // Optionally, hide the keyboard
            val focusedView = currentFocus
            if (focusedView is EditText) {
                val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(focusedView.windowToken, 0)
            }
        }


        try {
            antennaPower = userDetails[Constants.SET_ANTENNA_POWER]
            println("Antenna Power: $antennaPower")
        } catch (e: Exception) {
            Toasty.warning(this, e.message.toString(), Toasty.LENGTH_SHORT).show()
        }

        initReader(antennaPower?.toInt() ?: 130) // Default antenna power
    } // Fetch the location list

    private fun fetchLocationList() {
        progress.show()
        viewModel.getLocation("$token", Constants.baseurl)
    }

    private fun fetchVehicleByLocation(devLocId: Int) {
        progress.show()
        val vehicleLocationRequest = VehicleLocationRequest(devLocId)
        viewModel.getVehicleByLocation("$token", Constants.baseurl, vehicleLocationRequest)
    }

    private fun submit(submitRequest: SubmitRequest) {

        viewModel.submit("$token", Constants.baseurl, submitRequest)


    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                return true
            }

            else -> return super.onOptionsItemSelected(item)
        }
    }


    private fun getRFIDTag(): String? {
        val focusedView = currentFocus
        return if (focusedView is TextInputEditText) {
            focusedView.text.toString()
        } else {
            null
        }
    }

    // Show progress bar
    private fun showProgressBar() {
        progress.show()
    }

    // Hide progress bar
    private fun hideProgressBar() {
        if (progress.isShowing) {
            progress.dismiss()
        }
    }

    // Handle scanned RFID tag data
    override fun handleTagdata(tagData: Array<TagData>) {
        if (tagData.size == 1) {
            val tagDataFromScan = tagData[0].tagID
            val focusedView = currentFocus

            runOnUiThread {
                if (focusedView is TextInputEditText) {
                    if (focusedView.id == R.id.edRfid2 || focusedView.id == R.id.edRfid3) {
                        if (focusedView.text.toString().isEmpty()) {
                            focusedView.setText(tagDataFromScan)
                        }
                    }
                }
            }
            // Optionally stop the RFID inventory after processing the single tag
            stopInventory()
        } else {
            Toasty.warning(this, "No RFID tags detected.", Toasty.LENGTH_SHORT).show()
        }
    }

    // Handle the RFID trigger press (start/stop inventory)
    override fun handleTriggerPress(pressed: Boolean) {
        if (pressed) {
            performInventory()
        } else {
            stopInventory()
        }
    }

//    private fun getLocationById(locationId: String): LocationResponse? {
//        // Replace with actual implementation of retrieving the location
//        // Example placeholder code:
//        val locations = listOf<LocationResponse>() // Replace with actual locations
//        return locations.find { it.deviceLocationMappingId.toString() == locationId}
//    }
}
