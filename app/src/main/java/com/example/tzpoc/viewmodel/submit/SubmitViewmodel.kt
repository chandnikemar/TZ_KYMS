package com.example.tzpoc.viewmodel.submit//package com.example.tzpoc.viewmodel.submit
//
//
//
//import android.app.Application
//import androidx.lifecycle.AndroidViewModel
//import androidx.lifecycle.MutableLiveData
//import androidx.lifecycle.viewModelScope
//import com.example.tzpoc.api.Utils
//import com.example.tzpoc.helper.Constants
//import com.example.tzpoc.helper.Resource
//import com.example.tzpoc.model.submit.SubmitRequest
//import com.example.tzpoc.model.submit.SubmitResponse
//import com.example.tzpoc.repository.TzRepository
//import kotlinx.coroutines.launch
//import org.json.JSONObject
//import retrofit2.Response
//
//class SubmitViewModel(
//    application: Application,
//    private val tzRepository: TzRepository
//) : AndroidViewModel(application) {
//
//    // LiveData to hold the submit response
//    private val submitMutableLiveData: MutableLiveData<Resource<SubmitResponse>> = MutableLiveData()
//
//    // Function to submit the data (e.g., vehicle details)
//    fun submitVehicleJob(
//        bearerToken: String,
//        baseUrl: String,
//        submitRequest: SubmitRequest
//    ) {
//        viewModelScope.launch {
//            safeAPICallSubmit(bearerToken, baseUrl, submitRequest)
//        }
//    }
//
//    // Handle the response from the submit API
//    private fun handleSubmitResponse(response: Response<SubmitResponse>): Resource<SubmitResponse> {
//        var errorMessage = ""
//        if (response.isSuccessful) {
//            response.body()?.let { submitResponse ->
//                return Resource.Success(submitResponse)
//            }
//        } else if (response.errorBody() != null) {
//            val errorObject = response.errorBody()?.let {
//                JSONObject(it.charStream().readText())
//            }
//            errorObject?.let {
//                errorMessage = it.getString(Constants.HTTP_ERROR_MESSAGE)
//            }
//        }
//        return Resource.Error(errorMessage)
//    }
//
//    // Perform the API call for the submit action
//    private suspend fun safeAPICallSubmit(
//        bearerToken: String,
//        baseUrl: String,
//        submitRequest: SubmitRequest
//    ) {
//        submitMutableLiveData.postValue(Resource.Loading())
//        try {
//            if (Utils.hasInternetConnection(getApplication())) {
//                // Pass bearerToken along with submitRequest to the repository method
//                val response = tzRepository.postSubitVehicalJob(bearerToken, baseUrl, submitRequest)
//                submitMutableLiveData.postValue(handleSubmitResponse(response))
//            } else {
//                submitMutableLiveData.postValue(Resource.Error(Constants.NO_INTERNET))
//            }
//        } catch (t: Throwable) {
//            when (t) {
//                is Exception -> {
//                    submitMutableLiveData.postValue(Resource.Error("${t.message}"))
//                }
//                else -> submitMutableLiveData.postValue(Resource.Error(Constants.CONFIG_ERROR))
//            }
//        }
//    }
//}
