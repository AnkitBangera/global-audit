package com.landmarkgroup.globalaudit.data.model

data class ScanLocationRequest(
    val zone: String,
    val location: String,
    val userId: String,
    val deviceId: String
)
