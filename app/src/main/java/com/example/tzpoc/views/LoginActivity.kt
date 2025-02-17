package com.example.tzpoc.views

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.example.tzpoc.R
import com.example.tzpoc.api.Utils
import com.example.tzpoc.databinding.ActivityLoginBinding
import com.example.tzpoc.helper.Constants
import com.example.tzpoc.helper.SessionManager
import com.example.tzpoc.repository.TzRepository
import com.example.tzpoc.viewmodel.login.LoginViewModel
import com.example.tzpoc.viewmodel.login.LoginViewModelFactory
import com.example.tzpoc.model.login.LoginRequest
import com.example.tzpoc.helper.Resource
import java.util.HashMap

class LoginActivity : AppCompatActivity() {

    // Make binding private
    private lateinit var binding: ActivityLoginBinding
    private lateinit var viewModel: LoginViewModel
    private lateinit var session: SessionManager
    private var isActivityRunning = false
    private lateinit var userDetails: HashMap<String, String?>
    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_login)
        supportActionBar?.hide()

        // Initialize the ProgressBar using data binding (binding.progressBar)
        progressBar = binding.progressBar

        // Initialize ViewModel and SessionManager
        val tzRepository = TzRepository()
        val viewModelProviderFactory = LoginViewModelFactory(application, tzRepository)
        viewModel = ViewModelProvider(this, viewModelProviderFactory)[LoginViewModel::class.java]
        session = SessionManager(this)

        // Observe the login response from ViewModel
        viewModel.loginMutableLiveData.observe(this) { response ->
            if (!isActivityRunning) return@observe
            when (response) {
                is Resource.Success -> {
                    progressBar.visibility = View.GONE
                    response.data?.let { resultResponse ->
                        session.createLoginSession(
                            resultResponse.firstName,
                            resultResponse.lastName,
                            resultResponse.email,
                            resultResponse.mobileNumber,
                            resultResponse.isVerified.toString(),
                            resultResponse.userName,
                            resultResponse.jwtToken,
                            resultResponse.refreshToken,
                            resultResponse.roleName,
                            resultResponse.id,
                            resultResponse.coordinates,
                            resultResponse.locationId.toString()
                        )
                        Utils.setSharedPrefsBoolean(this@LoginActivity, Constants.LOGGEDIN, true)
                        navigateToHome()
                    }
                }
                is Resource.Error -> {
                    progressBar.visibility = View.GONE
                    response.message?.let { errorMessage ->
                        Toast.makeText(this, "Login failed - $errorMessage", Toast.LENGTH_SHORT).show()
                    }
                }
                is Resource.Loading -> {
                    progressBar.visibility = View.VISIBLE
                }
            }
        }

        // Set the login button click listener
        binding.btnLogin.setOnClickListener {
            login()
        }
    }

    override fun onStart() {
        super.onStart()
        isActivityRunning = true
    }

    override fun onStop() {
        super.onStop()
        isActivityRunning = false
        // Ensure the progress bar is hidden when the activity stops
        progressBar.visibility = View.GONE
    }

    private fun login() {
        try {
            val userId=
                "superadmin"
            val password= "Pass@123"
//            val userId = binding.edUsername.text.toString().trim()
//            val password = binding.edPass.text.toString().trim()

            if (userId.isNotEmpty() && password.isNotEmpty()) {
                val loginRequest = LoginRequest(password, userId, Utils.getDeviceId(this@LoginActivity))
                viewModel.login(Constants.baseurl, loginRequest)
            } else {
                showErrorMessage("Please enter valid credentials")
            }
        } catch (e: Exception) {
            showErrorMessage("An error occurred: ${e.message}")
        }
    }

    private fun showErrorMessage(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun navigateToHome() {
        startActivity(Intent(this@LoginActivity, HomeActivity::class.java))
        finish()
    }
}
