package com.example.tzpoc.model.login

data class LoginResponse(
    val coordinates: String,
    val dealerCode: String,
    val dealerName: String,
    val email: String,
    val firstName: String,
    val id: Int,
    val isVerified: Boolean,
    val jwtToken: String,
    val lastName: String,
    val locationId: Int,
    val menuAccess: List<Any>,
    val mobileNumber: String,
    val refreshToken: String,
    val roleName: String,
    val userAccess: List<Any>,
    val userName: String



)