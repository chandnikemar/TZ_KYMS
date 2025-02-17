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
import com.example.tzpoc.databinding.ActivityEditBinding
import com.example.tzpoc.helper.Constants
import com.example.tzpoc.helper.RFIDHandler
import com.example.tzpoc.helper.Resource
import com.example.tzpoc.helper.SessionManager
import com.example.tzpoc.model.containerDetails.ContainerRequest
import com.example.tzpoc.model.submit.GateContainerDetails
import com.example.tzpoc.model.submit.SubmitRequest
import com.example.tzpoc.model.vehicalJobDetails.VehicleJobRequest
import com.example.tzpoc.repository.TzRepository
import com.example.tzpoc.viewmodel.vehicalJobDetail.EntryViewModel
import com.example.tzpoc.viewmodel.vehicalJobDetail.VehicleJobViewModelFactory
import com.google.android.material.textfield.TextInputEditText
import com.zebra.rfid.api3.TagData
import es.dmoral.toasty.Toasty
import java.util.HashMap

class ExitActivity : AppCompatActivity(), RFIDHandler.ResponseHandlerInterface {

    lateinit var binding: ActivityEditBinding
    private lateinit var session: SessionManager
    private var token: String? = ""
    private lateinit var progress: ProgressDialog
    var rfidHandler: RFIDHandler? = null
    private var antennaPower: String? = ""
    private lateinit var userDetails: HashMap<String, String?>
//    private var selectedContainerId1: String? = null
//    private var selectedContainerId2: String? = null
    private lateinit var viewModel: EntryViewModel
    private var userName: String? = ""
    private var locationType: String? = ""
    private var isJobAssigned = false

    private fun hideRemainingFields() {
        // Hide the remaining fields if job type is not "Import" or based on any other condition
        binding.clAdditionalFields2.visibility = View.GONE
        binding.clAdditionalFields3.visibility = View.GONE
    }

    // Initialize the RFID reader
    private fun initReader(antennaPower: Int) {
        rfidHandler = RFIDHandler()
        rfidHandler!!.init(this, this@ExitActivity, antennaPower)
    }

    // Pause RFID handling when activity is paused
    override fun onPause() {
        super.onPause()
        rfidHandler?.onPause() // Pause RFID handler if needed
    }

    override fun onPostResume() {
        super.onPostResume()
        val status = rfidHandler?.onResume() ?: ""
        Toast.makeText(this@ExitActivity, status, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroy() {
        super.onDestroy()
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
        setContentView(R.layout.activity_edit)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_edit)
        session = SessionManager(this)
        userDetails = session.getUserDetails()
        val Ctrno1 = findViewById<TextInputEditText>(R.id.edLocId3)
        val Ctrno2 = findViewById<TextInputEditText>(R.id.edLocId4)
        val
        userName = userDetails[Constants.KEY_USER_NAME].toString()
        if (userDetails.isEmpty()) {
            Toasty.error(this, "User details are missing.", Toasty.LENGTH_SHORT).show()
        } else {
            token = userDetails["jwtToken"]
        }
        binding.clAdditionalFields2.requestLayout()
        progress = ProgressDialog(this)
        progress.setMessage("Please Wait...")


        val tzRepository = TzRepository()
        val viewModelProviderFactory = VehicleJobViewModelFactory(application, tzRepository)
        viewModel = ViewModelProvider(this, viewModelProviderFactory)[EntryViewModel::class.java]

        setSupportActionBar(binding.exitToolbar)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.show()

        viewModel.vehicleJobMutableLiveData.observe(this) { response ->
            when (response) {
                is Resource.Success -> {
                    val jobDetails = response.data
                    isJobAssigned = true

                    val locationType = "Exit" // Default location type if not provided
                    val jobLength = jobDetails?.item2?.length
                    val containerDetails = jobDetails?.item2?.containerDetails ?: emptyList()


                    val responseMessage = jobDetails?.item1?.responseMessage
                    val errorMessage = jobDetails?.item1?.errorMessage

                    if (responseMessage != null) {
                        containerDetails.forEach { container ->
                            Log.d("VehicleJobDetails", "CTR No: ${container.ctrNo}, Tag No: ${container.tagNumber}")}
                        if (containerDetails.isNotEmpty()) {
                            // Iterate through the containerDetails and display ctrNo and tagNumber
                            containerDetails.forEachIndexed { index, container ->

                                when (index) {
                                    0 -> {

                                        binding.edLocId3.setText(container.ctrNo)
                                        binding.tlRfid1.setText(container.tagNumber)
                                        binding.clAdditionalFields2.visibility = View.VISIBLE
                                    }
                                    1 -> {

                                        binding.edLocId4.setText(container.ctrNo)
                                        binding.tlRfid2.setText(container.tagNumber)
                                        binding.clAdditionalFields3.visibility = View.VISIBLE
                                    }
                                    else -> {
                                        // Handle additional containers if necessary
                                    }
                                }
                            }
                        } else {
                            Log.d("VehicleJobDetails", "No container details available.")

                            Toasty.info(this, "No container details available", Toasty.LENGTH_SHORT).show()
                            binding.clAdditionalFields2.visibility = View.GONE
                            binding.clAdditionalFields3.visibility = View.GONE
                        }
                        Toasty.success(this, responseMessage, Toasty.LENGTH_SHORT).show()
                    } else if (errorMessage != null) {

                        Toasty.error(this, errorMessage, Toasty.LENGTH_SHORT).show()
                    } else {

                        Toasty.error(this, "Error: No message available", Toasty.LENGTH_SHORT).show()
                    }


                    containerDetails.forEach { container ->
                        Log.d("VehicleJobDetails", "CTR No: ${container.ctrNo}, Tag No: ${container.tagNumber}")
                    }

                    hideProgressBar()

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
                    val builder = AlertDialog.Builder(this)
                    builder.setMessage("Submission Successful!")
                        .setTitle("Information")
                        .setPositiveButton("OK") { dialog, id ->
                            dialog.dismiss()
                            finish()
                        }

                    val dialog = builder.create()
                    dialog.show()
                }

                is Resource.Error -> {
                    Toasty.error(this, "Submit Failed: ${response.message}", Toasty.LENGTH_SHORT).show()
                    hideProgressBar()
                }

                is Resource.Loading -> {
                    showProgressBar()
                }
            }
        }


        binding.btnSubmit.setOnClickListener {
            if (isJobAssigned) {
                val rfidTag = getRFIDTag()
                val jobDetails = viewModel.vehicleJobMutableLiveData.value?.data
                val jobId = jobDetails?.item2?.vehicleId ?: 0
                val locationType = "Exit"
                val containerDetails = jobDetails?.item2?.containerDetails ?: emptyList()

                if (rfidTag.isNullOrEmpty()) {
                    Toasty.warning(this, "RFID tag is missing or invalid", Toasty.LENGTH_SHORT).show()
                } else {
                    val containerDetailsList = mutableListOf<GateContainerDetails>()


                    containerDetails.forEach { container ->
                        containerDetailsList.add(
                            GateContainerDetails(
                                ContainerId = container.containerId ?: 0,
                                TagNo = container.tagNumber
                            )
                        )
                    }

                    // If container details are added to the list, proceed with the submit
                    if (containerDetailsList.isNotEmpty()) {
                        val submitRequest = SubmitRequest(
                            UserName = userName,
                            VehicleId = jobId,
                            LocationType = locationType,
                            GateContainerDetails = containerDetailsList
                        )
                        submit(submitRequest)
                        val responseMessage = jobDetails?.item1?.responseMessage
                        val errorMessage = jobDetails?.item1?.errorMessage
                        if (responseMessage != null){
                        Toasty.success(this, responseMessage, Toasty.LENGTH_SHORT).show()
                    } else if (errorMessage != null) {
                        Toasty.error(this, errorMessage, Toasty.LENGTH_SHORT).show()
                    } else {

                        Toasty.error(this, "Error: No message available", Toasty.LENGTH_SHORT).show()
                    }

                    Log.d("submitRequest","submitRequest:$submitRequest")
                    } else {
                        Toasty.warning(this, "No matching containers found", Toasty.LENGTH_SHORT).show()
                    }
                }
            } else {
                Toasty.warning(this, "Job is not assigned yet.", Toasty.LENGTH_SHORT).show()
            }
        }


        binding.btnClear.setOnClickListener {
            // Clear text fields
//            binding.edRfid3.setText("")
//            binding.tlRfid4.setText("")
//            binding.edAdditionalField.setText("")

            // Optionally, hide the keyboard
            val focusedView = currentFocus
            if (focusedView is EditText) {
                val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(focusedView.windowToken, 0)
            }
        }

        try {
            antennaPower = userDetails[Constants.SET_ANTENNA_POWER]
            initReader(antennaPower?.toInt() ?: 130) // Default antenna power
        } catch (e: Exception) {
            Toasty.warning(this, e.message.toString(), Toasty.LENGTH_SHORT).show()
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

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                return true
            }

            else -> return super.onOptionsItemSelected(item)
        }
    }
    private fun getVehicleJobDetails(vehicleJobRequest: VehicleJobRequest) {
        showProgressBar()
        val bearerToken = token ?: ""
        viewModel.getVehicleJobDetails(bearerToken, Constants.baseurl, vehicleJobRequest)
    }
    private fun getContainerDetails(containerRequest: ContainerRequest) {
        showProgressBar()
        val bearerToken = token ?: ""
        viewModel.getContainerDetails(bearerToken, Constants.baseurl, containerRequest )
    }

    private fun submit(submitRequest: SubmitRequest) {
        showProgressBar()
        val bearerToken = token ?: ""
        viewModel.submitVehicleJob(bearerToken, Constants.baseurl, submitRequest)
    }

    private fun showProgressBar() {
        progress.show()
    }

    private fun hideProgressBar() {
        if (progress.isShowing) {
            progress.dismiss()
        }
    }

    override fun handleTagdata(tagData: Array<TagData>) {
        if (tagData.size == 1) {
            val tagDataFromScan = tagData[0].tagID
            val focusedView = currentFocus

            runOnUiThread {
                when (focusedView?.id) {
                    R.id.edRfid -> {
                        if (binding.edRfid.text.toString().isEmpty()) {
                            binding.edRfid.setText(tagDataFromScan)
                            val vehicleJobRequest = VehicleJobRequest(tagDataFromScan)
                            getVehicleJobDetails(vehicleJobRequest)
                        }
                    }
//                    R.id.edRfid3 -> {
//                        if (binding.edRfid3.text.toString().isEmpty()) {
//                            binding.edRfid3.setText(tagDataFromScan)
//                            val containerRequest = ContainerRequest(tagDataFromScan)
//                            getContainerDetails(containerRequest)
//                        }
//                    }
//                    R.id.edAdditionalField -> {
//                    if (binding.edAdditionalField.text.toString().isEmpty()) {
//                        binding.edAdditionalField.setText(tagDataFromScan)
//                        val containerRequest = ContainerRequest(tagDataFromScan)
//                        getContainerDetails(containerRequest)
//                    }
//                }
                    else -> {
                        Toasty.warning(this@ExitActivity, "Please focus on the correct field before scanning.", Toasty.LENGTH_SHORT).show()
                    }
                }
            }


            stopInventory() // Stop scanning after processing
        } else if (tagData.size > 1) {
            Toasty.warning(this, "Please scan only one RFID tag at a time.", Toasty.LENGTH_SHORT).show()
            stopInventory()
        }
    }

    private fun fetchContainerDetails(tag1: String, tag2: String) {
        val bearerToken = token ?: ""
        val containerRequest1 = ContainerRequest(tag1)
        val containerRequest2 = ContainerRequest(tag2)

        // Call the API to fetch container details for both tags
        viewModel.getContainerDetails(bearerToken, Constants.baseurl, containerRequest1)
        viewModel.getContainerDetails(bearerToken, Constants.baseurl, containerRequest2)
    }

    override fun handleTriggerPress(pressed: Boolean) {
        if (pressed) {
            performInventory()
        } else {
            stopInventory()
        }
    }
}
