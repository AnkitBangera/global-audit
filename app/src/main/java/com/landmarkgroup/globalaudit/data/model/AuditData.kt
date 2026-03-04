package com.landmarkgroup.globalaudit.data.model

data class AuditData(
    val zoneId: String,
    val bins: List<LocationBin> = emptyList()
) {
    val totalQuantity: Int
        get() = bins.sumOf { it.quantity }
    
    val locationCount: Int
        get() = bins.size
}
