package com.example.tzpoc.viewmodel.VehicalMapping

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.tzpoc.api.Utils
import com.example.tzpoc.helper.Constants
import com.example.tzpoc.helper.Resource

import com.example.tzpoc.model.RFIDMapping.VehicleMappingRequest
import com.example.tzpoc.model.RFIDMapping.VehicleMappingResponse
import com.example.tzpoc.repository.TzRepository
import kotlinx.coroutines.launch
import org.json.JSONObject
import retrofit2.Response

class VehicalMappingViewModel(application: Application,
                              private val tzRepository: TzRepository
) : AndroidViewModel(application) {

    // LiveData for storing the response from RFID verification
    val vehicalVerifyMutableLiveData: MutableLiveData<Resource<VehicleMappingResponse>> = MutableLiveData()

    // Function to call API for RFID verification
    fun verifyRFID(
        bearerToken: String,
        baseUrl: String,
      vehicleMappingRequest: VehicleMappingRequest
    ) {
        viewModelScope.launch {
            safeAPICallRFIDVerify(bearerToken, baseUrl, vehicleMappingRequest)
        }
    }

    // Handle the response from the API call
    private fun handleRFIDVerifyResponse(response: Response<VehicleMappingResponse>): Resource<VehicleMappingResponse> {
        var errorMessage = ""

        if (response.isSuccessful) {
            response.body()?.let { verifyResponse ->
                // If the response is successful, return the response data
                return Resource.Success(verifyResponse)
            }
        } else if (response.errorBody() != null) {
            val errorBody = response.errorBody()?.string()

            if (!errorBody.isNullOrEmpty()) {
                val errorObject = try {
                    JSONObject(errorBody)
                } catch (e: Exception) {
                    null
                }

                errorObject?.let {

                    errorMessage = it.optString(response.message())
                } ?: run {
                    errorMessage = "Invalid JSON format"
                }
            } else {
                errorMessage = "Empty response body"
            }
        }

        // If the response was unsuccessful, return the statusMessage from the response
//        if (response.code() != 200 && response.isSuccessful) {
//            val errorResponse = response.body()
//            errorResponse?.let {
//                errorMessage = it.statusMessage
//            }
//        }

        // Return the error or success
        return Resource.Error(errorMessage)
    }


    private suspend fun safeAPICallRFIDVerify(
        bearerToken: String,
        baseUrl: String,
        vehicleMappingRequest: VehicleMappingRequest
    ) {
        vehicalVerifyMutableLiveData.postValue(Resource.Loading())
        try {
            if (Utils.hasInternetConnection(getApplication())) {
                val response = tzRepository.postRFIDVerifyMapping(bearerToken, baseUrl, vehicleMappingRequest)
                vehicalVerifyMutableLiveData.postValue(handleRFIDVerifyResponse(response))
            } else {
                vehicalVerifyMutableLiveData.postValue(Resource.Error(Constants.NO_INTERNET))
            }
        } catch (t: Throwable) {
            vehicalVerifyMutableLiveData.postValue(Resource.Error(t.message ?: Constants.CONFIG_ERROR))
        }
    }
}
