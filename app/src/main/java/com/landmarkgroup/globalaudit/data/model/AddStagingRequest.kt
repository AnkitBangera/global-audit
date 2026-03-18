package com.landmarkgroup.globalaudit.data.model

data class AddStagingRequest(
    val zone: String,
    val location: String,
    val userId: String,
    val unitQty: Int,
    val deviceId: String
)
