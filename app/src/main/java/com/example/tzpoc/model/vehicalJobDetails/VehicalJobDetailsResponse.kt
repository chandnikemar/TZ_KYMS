package com.example.tzpoc.model.vehicalJobDetails

data class VehicleJobResponse(
    val item1: Item1,
    val item2: Item2
)

data class Item1(
    val errorMessage: String?,
    val exception: String?,
    val responseMessage: String,
    val statusCode: Int
)

data class Item2(
    val vehicleId: Int,
    val vrn: String,
    val length: Double,
    val containerIds: String,
    val jobNumber: String,
    val jobType: String,
    val containerDetails: List<ContainerDetail>
)

data class ContainerDetail(
    val containerId: Int,
    val ctrNo: String,
    val tagNumber: String,
    val length: Double,
    val shippingLine: String,
    val isoCode: String
)
