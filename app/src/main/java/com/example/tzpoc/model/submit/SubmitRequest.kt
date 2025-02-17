package com.example.tzpoc.model.submit


data class SubmitRequest(
    val UserName: String?,
    val VehicleId: Int?,
    val LocationType: String?,
    val GateContainerDetails: List<GateContainerDetails>
)

data class GateContainerDetails(
    val ContainerId: Int?,
    val TagNo: String?,

    )
