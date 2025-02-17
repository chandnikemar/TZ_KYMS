package com.example.tzpoc

import android.app.ProgressDialog
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.example.tzpoc.databinding.ActivityContainerMappingBinding
import com.example.tzpoc.databinding.ActivityRfidMappingBinding
import com.example.tzpoc.helper.Constants
import com.example.tzpoc.helper.RFIDHandler
import com.example.tzpoc.helper.Resource
import com.example.tzpoc.helper.SessionManager
import com.example.tzpoc.model.RFIDMapping.VehicleMappingRequest
import com.example.tzpoc.model.submit.SubmitRequest
import com.example.tzpoc.model.vehicalJobDetails.VehicleJobRequest
import com.example.tzpoc.repository.TzRepository
import com.example.tzpoc.viewmodel.VehicalMapping.VehicalMappingViewModel
import com.example.tzpoc.viewmodel.VehicalMapping.VehicleMappingViewModelFactory
import com.example.tzpoc.viewmodel.mapping.MappingViewModel
import com.example.tzpoc.viewmodel.mapping.MappingViewModelFactory
import com.google.android.material.textfield.TextInputEditText
import com.zebra.rfid.api3.TagData
import es.dmoral.toasty.Toasty
import java.util.HashMap

class RfidMappingActivity : AppCompatActivity() ,RFIDHandler.ResponseHandlerInterface{
    lateinit var binding: ActivityRfidMappingBinding
    private lateinit var session: SessionManager
    private var token: String? = ""
    private var antennaPower: String? = ""
    private lateinit var progress: ProgressDialog
    var rfidHandler: RFIDHandler? = null
    private lateinit var viewModel: VehicalMappingViewModel

    private fun initReader(antennaPower: Int) {
        rfidHandler = RFIDHandler()
        rfidHandler!!.init(this, this@RfidMappingActivity, antennaPower)
    }

    // Pause RFID handling when activity is paused
    override fun onPause() {
        super.onPause()
        rfidHandler?.onPause() // Pause RFID handler if needed
    }

    override fun onPostResume() {
        super.onPostResume()
        val status = rfidHandler?.onResume() ?: ""
        Toast.makeText(this@RfidMappingActivity, status, Toast.LENGTH_SHORT).show()
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


    private lateinit var userDetails: HashMap<String, String?>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_rfid_mapping)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_rfid_mapping)
        session = SessionManager(this)
        userDetails = session.getUserDetails()

        if (userDetails.isEmpty()) {
            Toasty.error(this, "User details are missing.", Toasty.LENGTH_SHORT).show()
        } else {
            token = userDetails["jwtToken"]
        }

        progress = ProgressDialog(this)
        progress.setMessage("Please Wait...")

        val tzRepository = TzRepository()



        val viewModelProviderFactory = VehicleMappingViewModelFactory(application, tzRepository)
        viewModel = ViewModelProvider(this, viewModelProviderFactory)[VehicalMappingViewModel::class.java]
        setSupportActionBar(binding.entryToolbar)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.show()



            binding.btnSubmit.setOnClickListener {
                println("Success")
                val vehicleNo = binding.edVehicleNo.text.toString().trim()
                val rfidTag = binding.edRfid.text.toString().trim()


                if (vehicleNo.isEmpty() || rfidTag.isEmpty()) {
                    Toasty.error(this, "Please enter valid Vehicle No and RFID Tag.", Toasty.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                val vehicleMappingRequest = VehicleMappingRequest(
                    RequestId = "12345",
                    VRN = vehicleNo,
                    RFIDTagNo = rfidTag,
                    ForceMap = false
                )
                println("Vehicle Mapping Request: $vehicleMappingRequest")

                verify(vehicleMappingRequest)

                viewModel.vehicalVerifyMutableLiveData.observe(this) { response ->
                    when (response) {
                        is Resource.Loading -> {
                            showProgressBar()
                            // Show loading indicator
                        }
                        is Resource.Success -> {
                            val response = response.data

                            // Handle success
                            hideProgressBar()
                            val successMessage = response?.statusMessage ?: "Vehicle mapping successful"
                            Toasty.success(this, successMessage, Toasty.LENGTH_SHORT).show()
                            println("Success: ${response?.statusMessage}")
                        }
                        is Resource.Error -> {
                            val error = response.message
                            hideProgressBar()
                            val errorMessage = response.message ?: "Error occurred while mapping vehicle"
                            Toasty.error(this, errorMessage, Toasty.LENGTH_SHORT).show()
                            println("Error: $error")
                        }
                    }
                }

            }
            binding.btnClear.setOnClickListener {
                binding.edRfid.setText("")

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

    private fun verify(vehicleMappingRequest: VehicleMappingRequest) {
        showProgressBar()
        val bearerToken = token ?: ""
        viewModel.verifyRFID(bearerToken, Constants.baseurl, vehicleMappingRequest)
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
        if (tagData.size == 1) {
            val tagDataFromScan = tagData[0].tagID
            val focusedView = currentFocus

            runOnUiThread {
                // If the focused field is edRfid and it's empty, fill it with the scanned tag
                if (focusedView is TextInputEditText) {
                    if (focusedView.id == R.id.edRfid && focusedView.text.toString().isEmpty()) {
                        focusedView.setText(tagDataFromScan) // Set scanned tag to the EditText field
                        println("Scanned RFID Tag: $tagDataFromScan")  // Log to verify tag value
                    }
                }
            }

            stopInventory()
        } else if (tagData.size > 1) {
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