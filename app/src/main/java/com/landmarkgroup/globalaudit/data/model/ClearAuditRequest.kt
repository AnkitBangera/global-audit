package com.landmarkgroup.globalaudit.data.model

data class ClearAuditRequest(
    val zone: String,
    val userId: String,
    val deviceId: String
)
