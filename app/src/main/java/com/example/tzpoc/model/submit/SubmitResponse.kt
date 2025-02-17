package com.example.tzpoc.model.submit

data class SubmitResponse(
    val errorMessage: String?,
    val exception: String?,
    val responseMessage: String?,
    val statusCode: Int
)
