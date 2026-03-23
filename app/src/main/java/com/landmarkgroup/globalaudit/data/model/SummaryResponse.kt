package com.landmarkgroup.globalaudit.data.model

data class SummaryResponse(
    val scannedLocations: List<ScannedLocation>? = null
)

data class ScannedLocation(
    val locationId: String,
    val unitQty: Double
)
