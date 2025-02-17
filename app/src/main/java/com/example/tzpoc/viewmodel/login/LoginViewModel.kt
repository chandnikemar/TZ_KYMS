package com.example.tzpoc.viewmodel.login

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.tzpoc.api.Utils
import com.example.tzpoc.helper.Constants
import com.example.tzpoc.helper.Resource
import com.example.tzpoc.model.login.LoginRequest
import com.example.tzpoc.model.login.LoginResponse
import com.example.tzpoc.repository.TzRepository
import kotlinx.coroutines.launch
import org.json.JSONObject
import retrofit2.Response

class   LoginViewModel(
    application: Application,
    private val tzRepository: TzRepository
) : AndroidViewModel(application) {

    // LiveData to hold the login response
    val loginMutableLiveData: MutableLiveData<Resource<LoginResponse>> = MutableLiveData()

    // Function to call the login API
    fun login(
        baseUrl: String,
        loginRequest: LoginRequest
    ) {
        viewModelScope.launch {
            safeAPICallDtmsLogin(baseUrl, loginRequest)
        }
    }

    // Handle the response from the login API
    private fun handleDtmsUserLoginResponse(response: Response<LoginResponse>): Resource<LoginResponse> {
        var errorMessage = ""
        if (response.isSuccessful) {
            response.body()?.let { loginResponse ->
                return Resource.Success(loginResponse)
            }
        } else if (response.errorBody() != null) {
            val errorObject = response.errorBody()?.let {
                JSONObject(it.charStream().readText())
            }
            errorObject?.let {
                errorMessage = it.getString(Constants.HTTP_ERROR_MESSAGE)
            }
        }
        return Resource.Error(errorMessage)
    }

    // Perform the API call for login
    private suspend fun safeAPICallDtmsLogin(baseUrl: String, loginRequest: LoginRequest) {
        loginMutableLiveData.postValue(Resource.Loading())
        try {
            if (Utils.hasInternetConnection(getApplication())) {
                val response = tzRepository.login(baseUrl, loginRequest)
                loginMutableLiveData.postValue(handleDtmsUserLoginResponse(response))
            } else {
                loginMutableLiveData.postValue(Resource.Error(Constants.NO_INTERNET))
            }
        } catch (t: Throwable) {
            when (t) {
                is Exception -> {
                    loginMutableLiveData.postValue(Resource.Error("${t.message}"))
                }
                else -> loginMutableLiveData.postValue(Resource.Error(Constants.CONFIG_ERROR))
            }
        }
    }
}
