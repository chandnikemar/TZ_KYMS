package com.example.tzpoc.model.containerDetails

data class ContainerResponse(
    val containerId: Int,
    val ctrNo: String,
    val length: Double,
    val shippingLine: String,
    val isoCode: String
)