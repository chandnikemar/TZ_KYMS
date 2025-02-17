package com.example.tzpoc.viewmodel.vehicalJobDetail

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.tzpoc.api.Utils
import com.example.tzpoc.helper.Constants
import com.example.tzpoc.helper.Resource
import com.example.tzpoc.model.containerDetails.ContainerRequest
import com.example.tzpoc.model.containerDetails.ContainerResponse
import com.example.tzpoc.model.submit.SubmitRequest
import com.example.tzpoc.model.submit.SubmitResponse
import com.example.tzpoc.model.vehicalJobDetails.VehicleJobRequest
import com.example.tzpoc.model.vehicalJobDetails.VehicleJobResponse
import com.example.tzpoc.repository.TzRepository
import kotlinx.coroutines.launch
import org.json.JSONObject
import retrofit2.Response

class EntryViewModel(
    application: Application,
    private val tzRepository: TzRepository
) : AndroidViewModel(application) {

    // LiveData for vehicle job response
    val vehicleJobMutableLiveData: MutableLiveData<Resource<VehicleJobResponse>> = MutableLiveData()

    val containerDetailsMutableLiveData: MutableLiveData<Resource<ContainerResponse>> =
        MutableLiveData()

    // LiveData for submit response
    val submitMutableLiveData: MutableLiveData<Resource<SubmitResponse>> = MutableLiveData()


    // Function to call the vehicle job details API
    fun getVehicleJobDetails(
        bearerToken: String,
        baseUrl: String,
        vehicleJobRequest: VehicleJobRequest
    ) {
        viewModelScope.launch {
            safeAPICallVehicleJobDetails(bearerToken, baseUrl, vehicleJobRequest)
        }
    }
//container details
    fun getContainerDetails(
        bearerToken: String,
        baseUrl: String,
        containerRequest: ContainerRequest
    ) {
        viewModelScope.launch {
            safeAPICallContainerDetailsDetails(bearerToken, baseUrl, containerRequest)
        }
    }

    // Function to submit the vehicle job data
    fun submitVehicleJob(
        bearerToken: String,
        baseUrl: String,
        submitRequest: SubmitRequest
    ) {
        viewModelScope.launch {
            safeAPICallSubmit(bearerToken, baseUrl, submitRequest)
        }
    }


    // Handle the response from the vehicle job API
    private fun handleVehicleJobResponse(response: Response<VehicleJobResponse>): Resource<VehicleJobResponse> {
        var errorMessage = ""
        if (response.isSuccessful) {
            response.body()?.let { vehicleJobResponse ->
                return Resource.Success(vehicleJobResponse)
            }
        } else if (response.errorBody() != null) {
            val errorObject = try {
                JSONObject(response.errorBody()?.charStream()?.readText())
            } catch (e: Exception) {
                null
            }
            errorObject?.let {
                errorMessage = it.getString(Constants.HTTP_ERROR_MESSAGE)
            }
        }
        return Resource.Error(errorMessage)
    }

    // Perform the API call for vehicle job details
    private suspend fun safeAPICallVehicleJobDetails(
        bearerToken: String,
        baseUrl: String,
        vehicleJobRequest: VehicleJobRequest
    ) {
        vehicleJobMutableLiveData.postValue(Resource.Loading())
        try {
            if (Utils.hasInternetConnection(getApplication())) {
                val response =
                    tzRepository.getVehicleJobDetails(bearerToken, baseUrl, vehicleJobRequest)
                vehicleJobMutableLiveData.postValue(handleVehicleJobResponse(response))
            } else {
                vehicleJobMutableLiveData.postValue(Resource.Error(Constants.NO_INTERNET))
            }
        } catch (t: Throwable) {
            vehicleJobMutableLiveData.postValue(Resource.Error(t.message ?: Constants.CONFIG_ERROR))
        }
    }

    // Handle the submit response
    private fun handleSubmitResponse(response: Response<SubmitResponse>): Resource<SubmitResponse> {
        var errorMessage = ""
        if (response.isSuccessful) {
            response.body()?.let { vehicleJobResponse ->
                return Resource.Success(vehicleJobResponse)
            }
        } else if (response.errorBody() != null) {
            val errorBody = response.errorBody()?.string()
            if (errorBody.isNullOrEmpty()) {
                errorMessage = errorMessage
            } else {
                val errorObject = try {
                    JSONObject(errorBody)
                } catch (e: Exception) {
                    null
                }
                errorObject?.let {
                    errorMessage = it.getString(Constants.HTTP_ERROR_MESSAGE)
                } ?: run {
                    errorMessage = "Invalid JSON format"
                }
            }
        }
        return Resource.Error(errorMessage)
    }

    // Perform the API call for submitting the vehicle job
    private suspend fun safeAPICallSubmit(
        bearerToken: String,
        baseUrl: String,
        submitRequest: SubmitRequest
    ) {
        submitMutableLiveData.postValue(Resource.Loading())
        try {
            if (Utils.hasInternetConnection(getApplication())) {
                val response = tzRepository.postSubitVehicalJob(bearerToken, baseUrl, submitRequest)
                submitMutableLiveData.postValue(handleSubmitResponse(response))
            } else {
                submitMutableLiveData.postValue(Resource.Error(Constants.NO_INTERNET))
            }
        } catch (t: Throwable) {
            submitMutableLiveData.postValue(Resource.Error(t.message ?: Constants.CONFIG_ERROR))
        }
    }

    ///////container APi
    private fun handleContainerResponse(response: Response<ContainerResponse>): Resource<ContainerResponse> {
        var errorMessage = ""
        if (response.isSuccessful) {
            response.body()?.let { containerRequest ->
                return Resource.Success(containerRequest)
            }
        } else if (response.errorBody() != null) {
            val errorObject = try {
                JSONObject(response.errorBody()?.charStream()?.readText())
            } catch (e: Exception) {
                null
            }
            errorObject?.let {
                errorMessage = it.getString(Constants.HTTP_ERROR_MESSAGE)
            }
        }
        return Resource.Error(errorMessage)
    }

    // Perform the API call for vehicle job details
    private suspend fun safeAPICallContainerDetailsDetails(
        bearerToken: String,
        baseUrl: String,
        containerRequest: ContainerRequest
    ) {
        containerDetailsMutableLiveData.postValue(Resource.Loading())
        try {
            if (Utils.hasInternetConnection(getApplication())) {
                val response =
                    tzRepository.getContainerDetails(bearerToken, baseUrl, containerRequest)
                containerDetailsMutableLiveData.postValue(handleContainerResponse(response))
            } else {
                containerDetailsMutableLiveData.postValue(Resource.Error(Constants.NO_INTERNET))
            }
        } catch (t: Throwable) {
            containerDetailsMutableLiveData.postValue(Resource.Error(t.message ?: Constants.CONFIG_ERROR))
        }
    }
}
