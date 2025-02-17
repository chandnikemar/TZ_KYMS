package com.example.tzpoc.model

data class LocationResponse(
    val deviceLocationMappingId: Int,
    val locationId: Int,
    val locationName: String,
    val locationCode: String,
    val lane: String,
    val deviceName: String,
    val direction: String,
    val deviceIP: String,
    val deviceType: String,
    val isActive: Boolean,
    val antenna: Int,
    val gpoManager: List<GpoManager>
)
data class GpoManager(val someField: String? = null)