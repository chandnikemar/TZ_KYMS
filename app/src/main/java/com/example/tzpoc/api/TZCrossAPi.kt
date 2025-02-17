package com.example.tzpoc.api
import com.example.tzpoc.helper.Constants
import com.example.tzpoc.helper.SessionManager
import com.example.tzpoc.model.LocationResponse
import com.example.tzpoc.model.RFIDMapping.VehicleMappingRequest
import com.example.tzpoc.model.RFIDMapping.VehicleMappingResponse
import com.example.tzpoc.model.containerDetails.ContainerResponse
import com.example.tzpoc.model.login.LoginRequest
import com.example.tzpoc.model.login.LoginResponse
import com.example.tzpoc.model.mapping.VehicleLocationResponse
import com.example.tzpoc.model.submit.SubmitRequest
import com.example.tzpoc.model.submit.SubmitResponse
import com.example.tzpoc.model.vehicalJobDetails.VehicleJobRequest
import com.example.tzpoc.model.vehicalJobDetails.VehicleJobResponse

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Query


interface TZCrossAPi {

    @POST(Constants.LOGIN_URL)
    suspend fun login(
        @Body
        loginRequest: LoginRequest
        ): Response<LoginResponse>

    @GET(Constants.GET_VEHICLE_JOB_DETAILS)
    suspend fun getVehicleJobDetails(
        @Header(Constants.HTTP_HEADER_AUTHORIZATION) bearerToken: String,
        @Query("RFIDTag") rfidTag: String,
    ): Response<VehicleJobResponse>



    @GET(Constants.GET_CONTAINER_DETAIL_ON_TAG)
    suspend fun getContainerDetailsOnTag(
        @Header(Constants.HTTP_HEADER_AUTHORIZATION) bearerToken: String,
        @Query("TagNo") tagNo: String
    ): Response<ContainerResponse>


    @POST(Constants.POST_GATE_ENTRY_EXIT)
    suspend fun postSubmitVehicleJob(
        @Header(Constants.HTTP_HEADER_AUTHORIZATION) bearerToken: String,
        @Body
        submitRequest: SubmitRequest
    ):Response<SubmitResponse>

    @GET(Constants.GET_ALL_LOCATION_DEVICE_MAPPING)
    suspend fun getLocationList(
        @Header(Constants.HTTP_HEADER_AUTHORIZATION) bearerToken: String
    ): Response<List<LocationResponse>>


    @GET(Constants.GET_VEHICLE_BY_LOCATION)
    suspend fun getVehicleByLocationDetail(
        @Header(Constants.HTTP_HEADER_AUTHORIZATION) bearerToken: String,
        @Query("DevLocId") devLocID: Int,
    ): Response<List<VehicleLocationResponse>>

    @POST(Constants.POST_RFID_VERIFY_MAP)
    suspend fun postRFIDVerifyMap(
        @Header(Constants.HTTP_HEADER_AUTHORIZATION) bearerToken: String,
        @Body vehicleMappingRequest: VehicleMappingRequest
    ): Response<VehicleMappingResponse>
}