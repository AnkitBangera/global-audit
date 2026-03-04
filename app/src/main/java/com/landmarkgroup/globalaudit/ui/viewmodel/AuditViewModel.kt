package com.landmarkgroup.globalaudit.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.landmarkgroup.globalaudit.data.model.AuditData
import com.landmarkgroup.globalaudit.data.model.LocationBin
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuditViewModel : ViewModel() {
    private val _auditData = MutableStateFlow<AuditData?>(null)
    val auditData: StateFlow<AuditData?> = _auditData.asStateFlow()
    
    private val _currentZoneId = MutableStateFlow<String>("")
    val currentZoneId: StateFlow<String> = _currentZoneId.asStateFlow()
    
    private val _currentLocationId = MutableStateFlow<String>("")
    val currentLocationId: StateFlow<String> = _currentLocationId.asStateFlow()
    
    private val _currentQuantity = MutableStateFlow<String>("")
    val currentQuantity: StateFlow<String> = _currentQuantity.asStateFlow()
    
    fun setZoneId(zoneId: String) {
        _currentZoneId.value = zoneId
        _auditData.value = AuditData(zoneId = zoneId)
    }
    
    fun setLocationId(locationId: String) {
        _currentLocationId.value = locationId
    }
    
    fun setQuantity(quantity: String) {
        _currentQuantity.value = quantity
    }
    
    fun addBin() {
        val locationId = _currentLocationId.value.trim()
        val quantity = _currentQuantity.value.trim().toIntOrNull() ?: 0
        
        if (locationId.isNotEmpty() && quantity > 0) {
            val currentData = _auditData.value ?: AuditData(zoneId = _currentZoneId.value)
            val newBin = LocationBin(locationId = locationId, quantity = quantity)
            val updatedBins = currentData.bins + newBin
            
            _auditData.value = currentData.copy(bins = updatedBins)
            
            // Clear current inputs
            _currentLocationId.value = ""
            _currentQuantity.value = ""
        }
    }
    
    fun clearAudit() {
        _auditData.value = null
        _currentZoneId.value = ""
        _currentLocationId.value = ""
        _currentQuantity.value = ""
    }
    
    fun submitAudit() {
        viewModelScope.launch {
            // Here you would typically save to database or send to API
            // For now, we'll just keep the data
        }
    }
}
