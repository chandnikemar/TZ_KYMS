package com.example.tzpoc.model.mapping

data class VehicleLocationResponse(
    val vehicleId: Int,
    val vrn: String,
    val length: Double,
    val containerDetails: List<Container>
)

data class Container(
    val containerId: Int,
    val ctrNo: String,
    val length: Double,
    val shippingLine: String,
    val isoCode: String
)
