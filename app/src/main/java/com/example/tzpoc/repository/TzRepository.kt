    package com.example.tzpoc.repository


    import com.example.tzpoc.api.RetrofitInstance
    import com.example.tzpoc.helper.Constants
    import com.example.tzpoc.helper.SessionManager
    import com.example.tzpoc.model.LocationResponse
    import com.example.tzpoc.model.RFIDMapping.VehicleMappingRequest
    import com.example.tzpoc.model.containerDetails.ContainerRequest
    import com.example.tzpoc.model.containerDetails.ContainerResponse
    import com.example.tzpoc.model.login.LoginRequest
    import com.example.tzpoc.model.mapping.VehicleLocationRequest
    import com.example.tzpoc.model.mapping.VehicleLocationResponse
    import com.example.tzpoc.model.submit.SubmitRequest
    import com.example.tzpoc.model.submit.SubmitResponse
    import com.example.tzpoc.model.vehicalJobDetails.VehicleJobRequest
    import com.example.tzpoc.model.vehicalJobDetails.VehicleJobResponse
    import retrofit2.Response

    import retrofit2.http.Body
    import retrofit2.http.Header
    import retrofit2.http.Query


    class TzRepository {

        suspend fun login(
            baseUrl: String,
            loginRequest: LoginRequest,
        ) = RetrofitInstance.api(baseUrl).login(loginRequest)

        suspend fun getVehicleJobDetails(
            token: String,
            baseUrl: String,
            vehicleJobRequest: VehicleJobRequest
        ): Response<VehicleJobResponse> {
            val rfidTag = vehicleJobRequest.RFIDTag

            return RetrofitInstance.api(baseUrl).getVehicleJobDetails(token, rfidTag)
        }

        suspend fun getContainerDetails(
            token: String,
            baseUrl: String,
            containerRequest: ContainerRequest
        ): Response<ContainerResponse> {
            val rfidTag = containerRequest.RFIDTag

            return RetrofitInstance.api(baseUrl).getContainerDetailsOnTag(token, rfidTag)
        }

        suspend fun postSubitVehicalJob(
            token: String,
            baseUrl: String,
            submitRequest: SubmitRequest
        ) = RetrofitInstance.api(baseUrl).postSubmitVehicleJob(token, submitRequest)

        suspend fun getLocationList(
            token: String,
            baseUrl: String
        ): Response<List<LocationResponse>> {
            return RetrofitInstance.api(baseUrl).getLocationList(token) // Make sure this corresponds with your API
        }
        suspend fun getVehicleByLocation(
            token: String,
            baseUrl: String,
            vehicleLocationRequest: VehicleLocationRequest
        ): Response<List<VehicleLocationResponse>> {
            val devLocId = vehicleLocationRequest.DevLocId
            return RetrofitInstance.api(baseUrl).getVehicleByLocationDetail(token, devLocId)
        }

        suspend fun postRFIDVerifyMapping(
            token: String,
            baseUrl: String,
            vehicleMappingRequest: VehicleMappingRequest
        ) = RetrofitInstance.api(baseUrl).postRFIDVerifyMap(token, vehicleMappingRequest)
    }