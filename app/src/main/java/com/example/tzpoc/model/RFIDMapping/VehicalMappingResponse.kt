package com.example.tzpoc.model.RFIDMapping

data class VehicleMappingResponse(
    val vrn: String?,
    val status: String,
    val statusMessage: String,
    val requestId: String
)