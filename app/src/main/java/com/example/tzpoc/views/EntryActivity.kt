package com.example.tzpoc.views

import android.app.AlertDialog
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
import androidx.lifecycle.ViewModelProvider
import com.example.tzpoc.R
import com.example.tzpoc.databinding.ActivityEntryBinding
import com.example.tzpoc.helper.Constants
import com.example.tzpoc.helper.RFIDHandler
import com.example.tzpoc.helper.Resource
import com.example.tzpoc.helper.SessionManager
import com.example.tzpoc.model.submit.GateContainerDetails

import com.example.tzpoc.model.submit.SubmitRequest

import com.example.tzpoc.model.vehicalJobDetails.VehicleJobRequest
import com.example.tzpoc.repository.TzRepository
import com.example.tzpoc.viewmodel.vehicalJobDetail.EntryViewModel


import com.example.tzpoc.viewmodel.vehicalJobDetail.VehicleJobViewModelFactory
import com.google.android.material.textfield.TextInputEditText
import es.dmoral.toasty.Toasty
import com.zebra.rfid.api3.TagData
import java.util.HashMap

class EntryActivity : AppCompatActivity(), RFIDHandler.ResponseHandlerInterface {


    lateinit var binding: ActivityEntryBinding
    private lateinit var session: SessionManager


    private var token: String? = ""
    private lateinit var progress: ProgressDialog
    var rfidHandler: RFIDHandler? = null
    private var antennaPower: String? = ""
    private lateinit var userDetails: HashMap<String, String?>
    private var selectedContainerId1: String? = null
    private var selectedContainerId2: String? = null
    private lateinit var viewModel: EntryViewModel
    private var userName: String? = ""
    private var locationType: String? = ""
    private var isJobAssigned = false
    private fun hideRemainingFields() {
        // Hide the remaining fields if job type is not "Import" or based on any other condition
        binding.clAdditionalFields.visibility = View.GONE
        binding.clAdditionalFields2.visibility = View.GONE
    }

    // Initialize the RFID reader
    private fun initReader(antennaPower: Int) {
        rfidHandler = RFIDHandler()
        rfidHandler!!.init(this, this@EntryActivity, antennaPower)
    }

    // Pause RFID handling when activity is paused
    override fun onPause() {
        super.onPause()
        rfidHandler?.onPause() // Pause RFID handler if needed
    }

    override fun onPostResume() {
        super.onPostResume()
        val status = rfidHandler?.onResume() ?: ""
        Toast.makeText(this@EntryActivity, status, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroy() {
        super.onDestroy()
//        binding.unbind()
        rfidHandler?.onDestroy()
    }

    fun performInventory() {
        rfidHandler!!.performInventory()
    }

    fun stopInventory() {
        rfidHandler!!.stopInventory()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_entry)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_entry)
        session = SessionManager(this)
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
        val viewModelProviderFactory = VehicleJobViewModelFactory(application, tzRepository)
        viewModel = ViewModelProvider(this, viewModelProviderFactory)[EntryViewModel::class.java]




        setSupportActionBar(binding.entryToolbar)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.show()



        viewModel.vehicleJobMutableLiveData.observe(this) { response ->
            when (response) {
                is Resource.Success -> {
                    val jobDetails = response.data
                    isJobAssigned = true


                    val jobType = "Import"

                    val locationType = "Entry" // Default location type if not provided

                    val jobLength = jobDetails?.item2?.length
//                   val jobLength = 40.0  // Assume this value comes from jobDetails or is hardcoded for testing

                    val containerDetails = jobDetails?.item2?.containerDetails ?: emptyList()
                    val ctrNos = containerDetails.map { it.ctrNo }
                    val containersID = containerDetails.map { it.containerId }
                    if (jobDetails?.item2 == null) {
                        // Show error message from item1 and stop further execution
                        val errorMessage = jobDetails?.item1?.errorMessage ?: "Unknown error occurred"

                        Toasty.error(this, " ${errorMessage}", Toasty.LENGTH_SHORT)
                            .show()
                        Toasty.error(this, errorMessage, Toasty.LENGTH_SHORT).show()
                        hideProgressBar()
                        return@observe
                    }

                    val ctrNosLength20 =
                        containerDetails.filter { it.length == 20.0 }.map { it.ctrNo }
                    val ctrNosLength40 =
                        containerDetails.filter { it.length == 40.0 }.map { it.ctrNo }

                    // Set default value for the dropdowns
                    val defaultCtrNo = "CTR No"


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


                    when (jobType) {
                        "Import" -> {
                            Toasty.success(this, "Job Type is Import", Toasty.LENGTH_SHORT).show()


                            binding.clAdditionalFields.visibility = View.VISIBLE


                            if (jobLength == 40.0) {

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


                            }


                            // If job length is 20, handle accordingly
                            else if (jobLength == 20.0) {
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
                            }

                        }

                        "Export" -> {
                            Toasty.success(this, "Job Type is Export", Toasty.LENGTH_SHORT).show()

                            // Create and show a custom popup (AlertDialog)
                            val builder = AlertDialog.Builder(this)
                            builder.setMessage("Already loaded")
                                .setTitle("Information")
                                .setPositiveButton("OK") { dialog, id ->
                                    dialog.dismiss()
                                    finish()
                                }

                            // Create and show the dialog
                            val dialog = builder.create()
                            dialog.show()

                            hideRemainingFields() // Hide the additional fields if job type is Export
                        }

                        else -> {
                            Toasty.warning(this, "Unknown Job Type: $jobType", Toasty.LENGTH_SHORT)
                                .show()
                        }
                    }

                    hideProgressBar()
                    Toasty.success(this, "Job details fetched successfully", Toasty.LENGTH_SHORT)
                        .show()
                }

                is Resource.Error -> {
                    Toasty.error(this, "Failed to fetch job details", Toasty.LENGTH_SHORT).show()
                    Log.e("VehicleJobDetails", "Error fetching details: ${response.message}")
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
                    val submitAPi = response.data
                    val errorMessage = submitAPi?.errorMessage ?: "Unknown error occurred"
                    val responseMessage=submitAPi?.responseMessage
                    val messageToShow = when {
                        !responseMessage.isNullOrEmpty() -> responseMessage  // Show responseMessage if it's not null
                        !errorMessage.isNullOrEmpty() -> errorMessage       // Otherwise, show errorMessage
                        else -> "Unknown error occurred"                    // Fallback message
                    }
                    val builder = AlertDialog.Builder(this)
                    builder.setMessage(messageToShow)
                        .setTitle("Information")
                        .setPositiveButton("OK") { dialog, id ->
                            dialog.dismiss()
                            finish()
                        }


                    val dialog = builder.create()
                    dialog.show()
                }

                is Resource.Error -> {
                    Toasty.error(this, "Submit Failed: ${response.message}", Toasty.LENGTH_SHORT)
                        .show()
                    hideProgressBar()
                    Log.e("VehicleJobDetails", "Error fetching details: ${response.message}")
                }

                is Resource.Loading -> {
                    showProgressBar()
                }
            }
        }

        binding.btnSubmit.setOnClickListener {
            if (isJobAssigned) {
                val rfidTag = getRFIDTag()  // Retrieve RFID tag value from the UI
                val jobDetails = viewModel.vehicleJobMutableLiveData.value?.data  // Get job details
                val jobId = jobDetails?.item2?.vehicleId ?: 0 // Get JobId, assuming 0 if not found
                val locationType = "Entry" // Define the location type
                val containerDetails = jobDetails?.item2?.containerDetails ?: emptyList() // Get container details

                Log.d("isJobAssigned", "Request: $rfidTag")
                Log.d("isJobAssigned", "Request: $jobId")
                Log.d("isJobAssigned", "Request: $containerDetails")

                if (rfidTag.isNullOrEmpty()) {
                    Toasty.warning(this, "RFID tag is missing or invalid", Toasty.LENGTH_SHORT).show()
                } else {
                    val containerDetailsList = mutableListOf<GateContainerDetails>()

                    // Retrieve RFID tag inputs
                    val tag1 = binding.edRfid2.text.toString().trim()
                    val tag2 = binding.edRfid3.text.toString().trim()

                    containerDetails.forEach { container ->
                        if (tag1.isNotEmpty() && selectedContainerId1 == container.containerId.toString()) {
                            containerDetailsList.add(
                                GateContainerDetails(
                                    ContainerId = container.containerId ?: 0,
                                    TagNo = tag1  // Add only if tag1 is not empty
                                )
                            )
                        }
                        if (tag2.isNotEmpty() && selectedContainerId2 == container.containerId.toString()) {
                            containerDetailsList.add(
                                GateContainerDetails(
                                    ContainerId = container.containerId ?: 0,
                                    TagNo = tag2  // Add only if tag2 is not empty
                                )
                            )
                        }
                    }

                    if (containerDetailsList.isNotEmpty()) {
                        val submitRequest = SubmitRequest(
                            UserName = userDetails["userName"] ?: "Unknown",
                            VehicleId = jobId,
                            LocationType = locationType,
                            GateContainerDetails = containerDetailsList
                        )

                        // Print the request (log it)
                        Log.d("SubmitRequest", "Request: $submitRequest") // This will print the SubmitRequest object to Logcat

                        // Call submit function
                        submit(submitRequest)
                    } else {
                        Toasty.warning(this, "No valid container details to submit", Toasty.LENGTH_SHORT).show()
                    }
                }
            } else {
                Toasty.warning(this, "Check the data", Toasty.LENGTH_SHORT).show()
            }
        }

        binding.btnClear.setOnClickListener {
            // Clear text fields

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
    }

    private fun getRFIDTag(): String? {
        val focusedView = currentFocus
        return if (focusedView is TextInputEditText) {
            focusedView.text.toString()
        } else {
            null
        }
    }

    private fun getVehicleJobDetails(vehicleJobRequest: VehicleJobRequest) {
        showProgressBar()
        val bearerToken = token ?: ""
        viewModel.getVehicleJobDetails(bearerToken, Constants.baseurl, vehicleJobRequest)
    }

    private fun submit(submitRequest: SubmitRequest) {
        showProgressBar()
        val bearerToken = token ?: ""
        viewModel.submitVehicleJob(bearerToken, Constants.baseurl, submitRequest)
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

    private fun hideKeyboard(editText: EditText) {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(editText.windowToken, 0)
    }

    fun showProgressBar() {
        progress.show()
    }

    fun hideProgressBar() {
        if (progress.isShowing) {
            progress.dismiss()
        }
    }

    override fun handleTagdata(tagData: Array<TagData>) {
        // Check if only one tag is scanned
        if (tagData.size == 1) {
            val tagDataFromScan = tagData[0].tagID
            val focusedView = currentFocus

            runOnUiThread {
                // If the focused field is edRfid and it's empty, fill it with the scanned tag
                if (focusedView is TextInputEditText) {
                    if (focusedView.id == R.id.edRfid && focusedView.text.toString().isEmpty()) {
                        focusedView.setText(tagDataFromScan)

                        // Call getVehicleJobDetails only when edRfid is not null or empty
                        val vehicleJobRequest =
                            VehicleJobRequest(tagDataFromScan)  // Assuming LocationId is 1 here
                        getVehicleJobDetails(vehicleJobRequest)
                    } else if (focusedView.id == R.id.edRfid2 || focusedView.id == R.id.edRfid3) {
                        // Do not call getVehicleJobDetails for edRfid2 and edRfid3
                        // Simply set the tag data in these fields if they are empty
                        if (focusedView.text.toString().isEmpty()) {
                            focusedView.setText(tagDataFromScan)
                        }
                    }
                }
            }

            // Optionally stop the RFID inventory after processing the single tag
            stopInventory()
        } else if (tagData.size > 1) {
            // If more than one RFID tag is scanned, stop the inventory and show a message
            Toasty.warning(this, "Please scan only one RFID tag.", Toasty.LENGTH_SHORT).show()
            stopInventory()
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
