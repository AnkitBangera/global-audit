package com.landmarkgroup.globalaudit.data.model

data class AuditData(
    val zoneId: String,
    val bins: List<LocationBin> = emptyList()
) {
    val totalQuantity: Int
        get() = bins.sumOf { it.quantity }
    
    // Count unique locations, not total entries, for proper "LOCS" semantics
    val locationCount: Int
        get() = bins.map { it.locationId }.distinct().size
}
