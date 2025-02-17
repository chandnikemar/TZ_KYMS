package com.example.tzpoc

import android.app.AlertDialog
import android.app.ProgressDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.tzpoc.databinding.ActivityContainerDeMappingBinding
import com.example.tzpoc.databinding.ActivityContainerMappingBinding
import com.example.tzpoc.helper.Constants
import com.example.tzpoc.helper.RFIDHandler
import com.example.tzpoc.helper.Resource
import com.example.tzpoc.helper.SessionManager
import com.example.tzpoc.model.containerDetails.ContainerRequest
import com.example.tzpoc.model.mapping.VehicleLocationRequest
import com.example.tzpoc.model.submit.GateContainerDetails
import com.example.tzpoc.model.submit.SubmitRequest
import com.example.tzpoc.repository.TzRepository
import com.example.tzpoc.viewmodel.mapping.MappingViewModel
import com.example.tzpoc.viewmodel.mapping.MappingViewModelFactory
import com.google.android.material.textfield.TextInputEditText
import com.zebra.rfid.api3.TagData
import es.dmoral.toasty.Toasty
import java.util.HashMap

class ContainerDeMappingActivity : AppCompatActivity(), RFIDHandler.ResponseHandlerInterface {
    lateinit var binding: ActivityContainerDeMappingBinding
    private lateinit var session: SessionManager
    private var token: String? = ""
    private lateinit var progress: ProgressDialog
    var rfidHandler: RFIDHandler? = null
    private var selectedContainerId1: String? = null
    private var selectedContainerId2: String? = null
    private var antennaPower: String? = ""
    private var userName: String? = ""
    private lateinit var userDetails: HashMap<String, String?>
    private lateinit var viewModel: MappingViewModel
    private var isJobAssigned = false

    private fun hideRemainingFields() {
        binding.clAdditionalFields.visibility = View.GONE
        binding.clAdditionalFields2.visibility = View.GONE
    }

    private fun initReader(antennaPower: Int) {
        rfidHandler = RFIDHandler()
        rfidHandler!!.init(this, this@ContainerDeMappingActivity, antennaPower)
    }

    override fun onPause() {
        super.onPause()
        rfidHandler?.onPause()
    }

    override fun onPostResume() {
        super.onPostResume()
        val status = rfidHandler?.onResume() ?: ""
        Toast.makeText(this@ContainerDeMappingActivity, status, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroy() {
        super.onDestroy()
        rfidHandler?.onDestroy()
        viewModel.locationMutableLiveData.removeObservers(this)
    }

    fun performInventory() {
        rfidHandler!!.performInventory()
    }

    fun stopInventory() {
        rfidHandler!!.stopInventory()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_container_de_mapping)
        session = SessionManager(this)

        val sharedPreferences = getSharedPreferences("AppPrefs", MODE_PRIVATE)
        val savedLocationName = sharedPreferences.getString("selected_location_name", "")
        val savedLocationId = sharedPreferences.getString("selected_location_id", "")
        if (!savedLocationName.isNullOrEmpty() && !savedLocationId.isNullOrEmpty()) {
            binding.edDropdown.setText(savedLocationName, false)
        }

        userDetails = session.getUserDetails()
        userName = userDetails[Constants.KEY_USER_NAME].toString()
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

        viewModel.locationMutableLiveData.observe(this,
            Observer { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        progress.show()
                    }

                    is Resource.Success -> {
                        progress.dismiss()
                        resource.data?.let { locations ->
                            if (locations.isNotEmpty()) {
                                val locationNames = locations.map { it.locationName }
                                val adapter = ArrayAdapter(
                                    this,
                                    android.R.layout.simple_dropdown_item_1line,
                                    locationNames
                                )
                                binding.edDropdown.setAdapter(adapter)

                                binding.edDropdown.setOnItemClickListener { parent, view, position, id ->
                                    val selectedLocation = locations[position]
                                    val selectedLocationId =
                                        selectedLocation.deviceLocationMappingId
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

                                    session.saveSelectedLocation(selectedLocationId.toString())
                                    fetchVehicleByLocation(selectedLocationId)
                                }
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



                            binding.clAdditionalFields.visibility = View.VISIBLE
                            if (vehicalLength == 40.0) {
                                binding.clAdditionalFields.visibility = View.VISIBLE
                                binding.clAdditionalFields2.visibility = View.VISIBLE
                            } else if (vehicalLength == 20.0) {
                                binding.clAdditionalFields.visibility = View.VISIBLE
                                binding.clAdditionalFields2.visibility = View.GONE
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
        viewModel.containerDetailsMutableLiveData.observe(this) { response ->
            when (response) {
                is Resource.Success -> {
                    val containerDetails = response.data

                    val ctrNo = containerDetails?.ctrNo
                    val ctrLength = containerDetails?.length

                    if (containerDetails != null) {
                        // Populate the fields with container details
                        if (selectedContainerId1 == containerDetails.containerId.toString()) {
                            binding.edCtrNo.setText(containerDetails.ctrNo)
                        } else {
                            binding.edLocId3.setText(containerDetails.ctrNo)
                        }
                    }
                    if (ctrNo != null) {
                        // Set the ctrNo in the LocId field
                        if (binding.edCtrNo.text.toString().isEmpty()) {
                            binding.edCtrNo.setText(ctrNo)
                        }
                        if (binding.edLocId3.text.toString().isEmpty()) {
                            binding.edLocId3.setText(ctrNo) // Set ctrNo in edLocId4
                        }
//                        binding.edAdditionalField.setText(ctrNo) // Set ctrNo to other fields if needed
                    }
                    if (ctrLength != null) {
                        if (ctrLength == 40.0) {
                            // Hide third row if ctrLength is 40
                            binding.clAdditionalFields2.visibility = View.GONE
                        } else if (ctrLength == 20.0) {
                            // Show third row if ctrLength is 20
                            binding.clAdditionalFields2.visibility = View.VISIBLE
                        }
                    }
                    hideProgressBar()
                }

                is Resource.Error -> {
                    Toasty.error(this, "Failed to fetch container details", Toasty.LENGTH_SHORT)
                        .show()
                    hideProgressBar()
                }

                is Resource.Loading -> {
                    showProgressBar()
                }
            }
        }
            viewModel.submitMutableLiveData.observe(this) { response ->
            when (response) {
                is Resource.Success -> {
                    hideProgressBar()

                    // Check if responseMessage is null in the success case
                    val message = response.data
                    val successMessage = message?.responseMessage
                    val errorMessage =message?.errorMessage
                    if (successMessage != null) {
                        val builder = AlertDialog.Builder(this)
                        builder.setMessage(successMessage)
                            .setTitle("Information")
                            .setPositiveButton("OK") { dialog, id ->
                                dialog.dismiss()
                                finish() // Close the activity after success
                            }
                        val dialog = builder.create()
                        dialog.show()
                    }else { val builder = AlertDialog.Builder(this)
                        builder.setMessage(errorMessage)
                            .setTitle("Information")
                            .setPositiveButton("OK") { dialog, id ->
                                dialog.dismiss()
                                finish() // Close the activity after success
                            }
                        val dialog = builder.create()
                        dialog.show()}
                    // Show a success dialog with the appropriate message

                }

                is Resource.Error -> {
                    hideProgressBar()

                    // If errorMessage is null, fall back to the message from the response
                    val errorMessage =
                        response.message ?: "An unknown error occurred"

                    // Show the error message
                    Toasty.error(this, errorMessage, Toasty.LENGTH_SHORT).show()
                }

                is Resource.Loading -> {
                    showProgressBar()
                }
            }
        }

        binding.btnSubmit.setOnClickListener {
            if (isJobAssigned) {

                val jobDetails =
                    viewModel.vehicleLocationMutableLiveData.value?.data
                if (jobDetails != null) {

                    val rfidTag = getRFIDTag()
                    val jobId = jobDetails[0]?.vehicleId
                    val locationType = "Exit"

                    val containerDetails =
                        jobDetails[0].containerDetails
                    if (rfidTag.isNullOrEmpty()) {
                        Toasty.warning(this, "RFID tag is missing or invalid", Toasty.LENGTH_SHORT)
                            .show()
                    } else {
                        val containerDetailsList = mutableListOf<GateContainerDetails>()

                        val tag1 = binding.edCtrNo.text.toString()
                        val tag2 = binding.edLocId3.text.toString()

                        containerDetails.forEach { container ->
                            if (tag1.isNotEmpty() && selectedContainerId1 == container.containerId.toString()) {
                                containerDetailsList.add(
                                    GateContainerDetails(
                                        ContainerId = container.containerId ?: 0,
                                        TagNo = tag1
                                    )
                                )
                            }
                            if (tag2.isNotEmpty() || tag2.isEmpty() && selectedContainerId2 == container.containerId.toString()) {
                                containerDetailsList.add(
                                    GateContainerDetails(
                                        ContainerId = container.containerId ?: 0,
                                        TagNo = tag2
                                    )
                                )
                            }
                        }

                        if (containerDetailsList.isNotEmpty()) {
                            val submitRequest = SubmitRequest(
                                UserName = userName,
                                VehicleId = jobId,
                                LocationType = locationType,
                                GateContainerDetails = containerDetailsList
                            )
                            submit(submitRequest)
                        }
                    }


                }


            } else {
                Toasty.warning(this, "Job is not assigned yet.", Toasty.LENGTH_SHORT).show()
            }
        }
        antennaPower = userDetails[Constants.SET_ANTENNA_POWER] ?: "130"
        initReader(antennaPower!!.toInt())
    }

    private fun getRFIDTag(): String? {
        val focusedView = currentFocus
        return if (focusedView is TextInputEditText) {
            focusedView.text.toString()
        } else {
            null
        }
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

    private fun submit(submitRequest: SubmitRequest) {
        showProgressBar()
        val bearerToken = token ?: ""
        viewModel.submit(bearerToken, Constants.baseurl, submitRequest)
    }

    private fun showProgressBar() {
        progress.show()
    }

    private fun hideProgressBar() {
        if (progress.isShowing) {
            progress.dismiss()
        }
    }

    private fun fetchLocationList() {
        if (!token.isNullOrEmpty()) {
            progress.show()
            viewModel.getLocation(token!!, Constants.baseurl)
        } else {
            Toasty.error(this, "Token is missing.", Toasty.LENGTH_SHORT).show()
        }
    }

    private fun fetchVehicleByLocation(devLocId: Int) {
        progress.show()
        val vehicleLocationRequest = VehicleLocationRequest(devLocId)
        viewModel.getVehicleByLocation("$token", Constants.baseurl, vehicleLocationRequest)
    }

    private fun getContainerDetails(containerRequest: ContainerRequest) {
        showProgressBar()
        val bearerToken = token ?: ""
        viewModel.getContainerDetails(bearerToken, Constants.baseurl, containerRequest)
    }

    private fun fetchContainerDetails(tag1: String, tag2: String) {
        val bearerToken = token ?: ""
        val containerRequest1 = ContainerRequest(tag1)
        val containerRequest2 = ContainerRequest(tag2)


        viewModel.getContainerDetails(bearerToken, Constants.baseurl, containerRequest1)
        viewModel.getContainerDetails(bearerToken, Constants.baseurl, containerRequest2)
    }

    override fun handleTagdata(tagData: Array<TagData>) {
        if (tagData.isNotEmpty()) {
            val tagDataFromScan = tagData[0].tagID
            val focusedView = currentFocus

            runOnUiThread {
                if (focusedView is TextInputEditText) {
                    if (focusedView.id == R.id.edRfid2 || focusedView.id == R.id.edRfid3) {
                        if (focusedView.text.isNullOrEmpty()) {
                            focusedView.setText(tagDataFromScan)

                            val containerRequest = ContainerRequest(tagDataFromScan)
                            getContainerDetails(containerRequest)
                        }
                    }
                }
            }
            stopInventory()
        } else {
            Toasty.warning(this, "No RFID tags detected.", Toasty.LENGTH_SHORT).show()
        }
    }

    override fun handleTriggerPress(pressed: Boolean) {
        if (pressed) {
            performInventory()
        } else {
            stopInventory()
        }
    }
}
