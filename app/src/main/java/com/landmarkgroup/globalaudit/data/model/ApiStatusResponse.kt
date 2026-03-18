package com.landmarkgroup.globalaudit.data.model

/**
 * Standard response structure used by DigitalWMS audit APIs.
 * Many endpoints return this same shape: { "returnCode": "Y|N", "errorMessage": "..." }.
 */
data class ApiStatusResponse(
    val returnCode: String,
    val errorMessage: String
)

