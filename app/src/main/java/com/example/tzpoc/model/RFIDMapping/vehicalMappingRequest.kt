package com.example.tzpoc.model.RFIDMapping

data class VehicleMappingRequest(
    val RequestId: String,
    val VRN: String,
    val RFIDTagNo: String,
    val ForceMap: Boolean
)
