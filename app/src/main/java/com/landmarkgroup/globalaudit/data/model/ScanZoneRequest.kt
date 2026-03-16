package com.landmarkgroup.globalaudit.data.model

import com.squareup.moshi.Json

data class ScanZoneRequest(
    @Json(name = "zone")
    val zone: String
)
