package com.example.tzpoc.viewmodel.mapping

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.tzpoc.api.Utils
import com.example.tzpoc.helper.Constants
import com.example.tzpoc.helper.Resource
import com.example.tzpoc.model.LocationResponse
import com.example.tzpoc.model.containerDetails.ContainerRequest
import com.example.tzpoc.model.containerDetails.ContainerResponse
import com.example.tzpoc.model.mapping.VehicleLocationRequest
import com.example.tzpoc.model.mapping.VehicleLocationResponse
import com.example.tzpoc.model.submit.SubmitRequest
import com.example.tzpoc.model.submit.SubmitResponse
import com.example.tzpoc.repository.TzRepository
import kotlinx.coroutines.launch
import org.json.JSONObject
import retrofit2.Response

class MappingViewModel(
    application: Application,
    private val tzRepository: TzRepository
) : AndroidViewModel(application) {

    val locationMutableLiveData: MutableLiveData<Resource<List<LocationResponse>>> = MutableLiveData()

    val vehicleLocationMutableLiveData: MutableLiveData<Resource<List<VehicleLocationResponse>>> = MutableLiveData()

    val containerDetailsMutableLiveData: MutableLiveData<Resource<ContainerResponse>> =
        MutableLiveData()

    val submitMutableLiveData: MutableLiveData<Resource<SubmitResponse>> = MutableLiveData()

    // Function to get the location list
    fun getLocation(bearerToken: String, baseUrl: String) {
        viewModelScope.launch {
            safeAPICallLocationList(bearerToken, baseUrl)
        }
    }

    fun getVehicleByLocation(bearerToken: String, baseUrl: String, vehicleLocationRequest: VehicleLocationRequest) {
        viewModelScope.launch {
            safeAPICallVehicleByLocation(bearerToken, baseUrl, vehicleLocationRequest)
        }
    }

    fun getContainerDetails(
        bearerToken: String,
        baseUrl: String,
        containerRequest: ContainerRequest
    ) {
        viewModelScope.launch {
            safeAPICallContainerDetailsDetails(bearerToken, baseUrl, containerRequest)
        }
    }
    fun submit(
        bearerToken: String,
        baseUrl: String,
        submitRequest: SubmitRequest
    ) {
        viewModelScope.launch {
            safeAPICallSubmit(bearerToken, baseUrl, submitRequest)
        }
    }

    private fun handleLocationListResponse(response: Response<List<LocationResponse>>): Resource<List<LocationResponse>> {
        var errorMessage = ""
        if (response.isSuccessful) {
            response.body()?.let { locationList ->
                return Resource.Success(locationList)
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

    private suspend fun safeAPICallLocationList(bearerToken: String, baseUrl: String) {
        locationMutableLiveData.postValue(Resource.Loading())
        try {
            if (Utils.hasInternetConnection(getApplication())) {
                val response = tzRepository.getLocationList(bearerToken, baseUrl)
                locationMutableLiveData.postValue(handleLocationListResponse(response))
            } else {
                locationMutableLiveData.postValue(Resource.Error(Constants.NO_INTERNET))
            }
        } catch (t: Throwable) {
            locationMutableLiveData.postValue(Resource.Error(t.message ?: Constants.CONFIG_ERROR))
        }
    }

    private fun handleVehicleLocationResponse(response: Response<List<VehicleLocationResponse>>): Resource<List<VehicleLocationResponse>> {
        var errorMessage = ""
        if (response.isSuccessful) {
            response.body()?.let { vehicleList ->
                return Resource.Success(vehicleList)
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

    private suspend fun safeAPICallVehicleByLocation(
        bearerToken: String,
        baseUrl: String,
        vehicleLocationRequest: VehicleLocationRequest
    ) {
        vehicleLocationMutableLiveData.postValue(Resource.Loading())
        try {
            if (Utils.hasInternetConnection(getApplication())) {
                val response = tzRepository.getVehicleByLocation(bearerToken, baseUrl, vehicleLocationRequest)
                vehicleLocationMutableLiveData.postValue(handleVehicleLocationResponse(response))
            } else {
                vehicleLocationMutableLiveData.postValue(Resource.Error(Constants.NO_INTERNET))
            }
        } catch (t: Throwable) {
            vehicleLocationMutableLiveData.postValue(Resource.Error(t.message ?: Constants.CONFIG_ERROR))
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



    //submit Response

    private fun handleSubmitResponse(response: Response<SubmitResponse>): Resource<SubmitResponse> {
        var errorMessage = "Unknown error occurred" // Default error message

        if (response.isSuccessful) {
            response.body()?.let { vehicleJobResponse ->
                return Resource.Success(vehicleJobResponse)
            } ?: run {
                errorMessage = "Response body is null"
            }
        } else if (response.errorBody() != null) {
            val errorBody = response.errorBody()?.string()
            errorMessage = if (errorBody.isNullOrEmpty()) {
                "Empty response body"
            } else {
                val errorObject = try {
                    JSONObject(errorBody)
                } catch (e: Exception) {
                    null
                }
                errorObject?.optString(Constants.HTTP_ERROR_MESSAGE, "Unknown server error") ?: "Invalid JSON format"
            }
        }

        Log.e("API Response Error", errorMessage) // Log for debugging
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
}
