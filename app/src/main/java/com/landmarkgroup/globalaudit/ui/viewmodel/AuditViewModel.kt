package com.landmarkgroup.globalaudit.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.landmarkgroup.globalaudit.data.model.AuditData
import com.landmarkgroup.globalaudit.data.model.LocationBin
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.landmarkgroup.globalaudit.network.NetworkModule
import com.landmarkgroup.globalaudit.data.model.ScanZoneRequest
import com.landmarkgroup.globalaudit.data.model.ScanLocationRequest
import com.landmarkgroup.globalaudit.data.model.AddStagingRequest
import com.landmarkgroup.globalaudit.data.model.ClearAuditRequest
import com.landmarkgroup.globalaudit.data.model.SharedAuthData
import com.landmarkgroup.globalaudit.utils.DeviceUtils
import com.landmarkgroup.globalaudit.utils.JwtUtils
import com.landmarkgroup.globalaudit.utils.AppSettings
import android.content.Context
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.analytics.logEvent
import com.google.firebase.crashlytics.ktx.crashlytics
import com.google.firebase.ktx.Firebase
import org.json.JSONObject

class AuditViewModel : ViewModel() {
    private val _auditData = MutableStateFlow<AuditData?>(null)
    val auditData: StateFlow<AuditData?> = _auditData.asStateFlow()
    
    private val _currentZoneId = MutableStateFlow<String>("")
    val currentZoneId: StateFlow<String> = _currentZoneId.asStateFlow()
    
    private val _currentLocationId = MutableStateFlow<String>("")
    val currentLocationId: StateFlow<String> = _currentLocationId.asStateFlow()
    
    private val _currentQuantity = MutableStateFlow<String>("")
    val currentQuantity: StateFlow<String> = _currentQuantity.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _scanZoneError = MutableStateFlow<String?>(null)
    val scanZoneError: StateFlow<String?> = _scanZoneError.asStateFlow()

    // Toast-like event for scan-zone result (success/failure + message)
    private val _scanZoneToast = MutableStateFlow<Pair<Boolean, String>?>(null)
    val scanZoneToast: StateFlow<Pair<Boolean, String>?> = _scanZoneToast.asStateFlow()

    private val _isLocationValidated = MutableStateFlow(false)
    val isLocationValidated: StateFlow<Boolean> = _isLocationValidated.asStateFlow()

    private val _scanLocationToast = MutableStateFlow<String?>(null)
    val scanLocationToast: StateFlow<String?> = _scanLocationToast.asStateFlow()

    // Toast for add-staging result (success/failure + message)
    private val _addStagingToast = MutableStateFlow<Pair<Boolean, String>?>(null)
    val addStagingToast: StateFlow<Pair<Boolean, String>?> = _addStagingToast.asStateFlow()
    
    fun setZoneId(zoneId: String) {
        _currentZoneId.value = zoneId
        _auditData.value = AuditData(zoneId = zoneId)
    }
    
    fun setLocationId(locationId: String) {
        _currentLocationId.value = locationId
        // Any edit invalidates the previous scan result.
        _isLocationValidated.value = false
        _currentQuantity.value = ""
    }
    
    fun setQuantity(quantity: String) {
        _currentQuantity.value = quantity
    }

    suspend fun scanZone(zoneId: String): Boolean {
        if (_isLoading.value) return false
        _isLoading.value = true
        _scanZoneError.value = null
        return try {
            val response = NetworkModule.auditApiService.scanZone(ScanZoneRequest(zone = zoneId))
            if (response.isSuccessful) {
                val body = response.body()
                val success = body?.returnCode.equals("Y", true)
                val message = body?.errorMessage ?: ""
                val finalMessage = if (success) if (message.isNotBlank()) message else "Zone accepted" else message
                _scanZoneToast.value = Pair(success, finalMessage)
                if (!success) {
                    _scanZoneError.value = finalMessage
                }
                // Analytics: log scan zone result
                Firebase.analytics.logEvent("scan_zone_result") {
                    param("zone_id", zoneId)
                    param("success", if (success) "1" else "0")
                }
                if (!success) {
                    Firebase.crashlytics.setCustomKey("zone_id", zoneId)
                    Firebase.crashlytics.log("scanZone failed: $finalMessage")
                }
                success
            } else {
                val msg = "Scan zone failed"
                _scanZoneError.value = msg
                _scanZoneToast.value = Pair(false, msg)
                false
            }
        } catch (e: Exception) {
            val msg = e.message ?: "Unknown error"
            _scanZoneError.value = msg
            _scanZoneToast.value = Pair(false, msg)
            Firebase.crashlytics.setCustomKey("zone_id", zoneId)
            Firebase.crashlytics.recordException(e)
            false
        } finally {
            _isLoading.value = false
        }
    }
    
    suspend fun scanLocation(context: Context): Boolean {
        if (_isLoading.value) return false
        _isLoading.value = true
        _scanZoneError.value = null
        return try {
            val auth = SharedAuthData.getAuthData()
            var userId: String = auth?.employeeIdKey ?: ""
            if (userId.isBlank()) {
                val idToken = auth?.idTokenKey
                val fromJwt = if (!idToken.isNullOrBlank()) JwtUtils.extractForKey(idToken, "SamAccountName") else null
                if (!fromJwt.isNullOrBlank()) {
                    userId = fromJwt
                    // Persist for future calls and headers
                    auth?.employeeIdKey = fromJwt
                    if (auth != null) {
                        AppSettings.saveAuthData(context, auth)
                        SharedAuthData.setAuthData(auth)
                    }
                }
            }
            Firebase.crashlytics.setUserId(userId)
            val request = ScanLocationRequest(
                zone = _currentZoneId.value,
                location = _currentLocationId.value,
                userId = userId,
                deviceId = DeviceUtils.getDeviceId(context)
            )
            val response = NetworkModule.auditApiService.scanLocation(request)
            if (response.isSuccessful) {
                val body = response.body()
                val success = body?.returnCode.equals("Y", true)
                val message = body?.errorMessage ?: ""
                val finalMessage = if (success) if (message.isNotBlank()) message else "Location accepted" else message
                if (success) {
                    _isLocationValidated.value = true
                    if (_currentQuantity.value.isBlank()) {
                        _currentQuantity.value = "0"
                    }
                } else {
                    _isLocationValidated.value = false
                    _scanLocationToast.value = finalMessage
                }
                success
            } else {
                val errorBody = try { response.errorBody()?.string() } catch (_: Exception) { null }
                val backendMsg = try {
                    if (!errorBody.isNullOrBlank()) JSONObject(errorBody).optString("errorMessage") else ""
                } catch (_: Exception) { "" }
                val msg = if (backendMsg.isNotBlank()) backendMsg else "Scan location failed: ${response.code()}"
                _scanZoneError.value = msg
                _isLocationValidated.value = false
                _scanLocationToast.value = msg
                false
            }
        } catch (e: Exception) {
            val msg = e.message ?: "Unknown error"
            _scanZoneError.value = msg
            _isLocationValidated.value = false
            _scanLocationToast.value = msg
            Firebase.crashlytics.setCustomKey("zone_id", _currentZoneId.value)
            Firebase.crashlytics.setCustomKey("location_id", _currentLocationId.value)
            Firebase.crashlytics.recordException(e)
            false
        } finally {
            _isLoading.value = false
        }
    }
    
    suspend fun addStaging(context: Context): Boolean {
        _isLoading.value = true
        _scanZoneError.value = null
        return try {
            val auth = SharedAuthData.getAuthData()
            var userId: String = auth?.employeeIdKey ?: ""
            if (userId.isBlank()) {
                val idToken = auth?.idTokenKey
                val fromJwt = if (!idToken.isNullOrBlank()) JwtUtils.extractForKey(idToken, "SamAccountName") else null
                if (!fromJwt.isNullOrBlank()) {
                    userId = fromJwt
                    auth?.employeeIdKey = fromJwt
                    if (auth != null) {
                        AppSettings.saveAuthData(context, auth)
                        SharedAuthData.setAuthData(auth)
                    }
                }
            }
            val qty = _currentQuantity.value.trim().toIntOrNull() ?: 0
            val request = AddStagingRequest(
                zone = _currentZoneId.value,
                location = _currentLocationId.value,
                userId = userId,
                unitQty = qty,
                deviceId = DeviceUtils.getDeviceId(context)
            )
            val response = NetworkModule.auditApiService.addStaging(request)
            if (response.isSuccessful) {
                val body = response.body()
                val success = body?.returnCode.equals("Y", true)
                val message = body?.errorMessage ?: ""
                val finalMessage = if (success) if (message.isNotBlank()) message else "Added to staging" else message
                _addStagingToast.value = Pair(success, finalMessage)
                if (!success) {
                    _scanZoneError.value = finalMessage
                }
                success
            } else {
                val msg = "Add staging failed: ${response.code()}"
                _scanZoneError.value = msg
                _addStagingToast.value = Pair(false, msg)
                false
            }
        } catch (e: Exception) {
            val msg = e.message ?: "Unknown error"
            _scanZoneError.value = msg
            _addStagingToast.value = Pair(false, msg)
            Firebase.crashlytics.setCustomKey("zone_id", _currentZoneId.value)
            Firebase.crashlytics.setCustomKey("location_id", _currentLocationId.value)
            Firebase.crashlytics.recordException(e)
            false
        } finally {
            _isLoading.value = false
        }
    }
    
    suspend fun fetchSummary(context: Context): Boolean {
        _isLoading.value = true
        _scanZoneError.value = null
        return try {
            val auth = SharedAuthData.getAuthData()
            var userId: String = auth?.employeeIdKey ?: ""
            if (userId.isBlank()) {
                val idToken = auth?.idTokenKey
                val fromJwt = if (!idToken.isNullOrBlank()) JwtUtils.extractForKey(idToken, "SamAccountName") else null
                if (!fromJwt.isNullOrBlank()) {
                    userId = fromJwt
                    auth?.employeeIdKey = fromJwt
                    if (auth != null) {
                        AppSettings.saveAuthData(context, auth)
                        SharedAuthData.setAuthData(auth)
                    }
                }
            }
            val zone = _currentZoneId.value
            val deviceId = DeviceUtils.getDeviceId(context)
            
            val response = NetworkModule.auditApiService.getAuditSummary(
                zone = zone,
                userId = userId,
                deviceId = deviceId
            )
            if (response.isSuccessful) {
                val body = response.body()
                val bins = body?.scannedLocations.orEmpty().map { item ->
                    LocationBin(
                        locationId = item.locationId,
                        quantity = item.unitQty.toInt()
                    )
                }
                _auditData.value = AuditData(zoneId = zone, bins = bins)
                true
            } else {
                val msg = "Fetch summary failed: ${response.code()}"
                _scanZoneError.value = msg
                false
            }
        } catch (e: Exception) {
            val msg = e.message ?: "Unknown error"
            _scanZoneError.value = msg
            Firebase.crashlytics.setCustomKey("zone_id", _currentZoneId.value)
            Firebase.crashlytics.recordException(e)
            false
        } finally {
            _isLoading.value = false
        }
    }
    
    suspend fun clearAuditRemote(context: Context, zone: String) {
        try {
            val auth = SharedAuthData.getAuthData()
            var userId: String = auth?.employeeIdKey ?: ""
            if (userId.isBlank()) {
                val idToken = auth?.idTokenKey
                val fromJwt = if (!idToken.isNullOrBlank()) JwtUtils.extractForKey(idToken, "SamAccountName") else null
                if (!fromJwt.isNullOrBlank()) {
                    userId = fromJwt
                    auth?.employeeIdKey = fromJwt
                    if (auth != null) {
                        AppSettings.saveAuthData(context, auth)
                        SharedAuthData.setAuthData(auth)
                    }
                }
            }
            val req = ClearAuditRequest(
                zone = zone,
                userId = userId,
                deviceId = DeviceUtils.getDeviceId(context)
            )
            // Fire-and-forget; do not block navigation and ignore errors/result
            NetworkModule.auditApiService.clearAudit(req)
        } catch (_: Exception) {
            // Swallow errors: user has already confirmed cancel/edit
        }
    }
    
    fun launchClearAuditRemote(context: Context, zone: String) {
        viewModelScope.launch {
            clearAuditRemote(context, zone)
        }
    }
    
    fun addBin() {
        val locationId = _currentLocationId.value.trim()
        val quantity = _currentQuantity.value.trim().toIntOrNull() ?: 0
        
        if (locationId.isNotEmpty()) {
            val currentData = _auditData.value ?: AuditData(zoneId = _currentZoneId.value)
            val newBin = LocationBin(locationId = locationId, quantity = quantity)
            // Upsert: keep only the latest record per locationId
            val updatedBins = currentData.bins.filter { it.locationId != locationId } + newBin
            
            _auditData.value = currentData.copy(bins = updatedBins)
            
            // Clear current inputs
            _currentLocationId.value = ""
            _currentQuantity.value = ""
            _isLocationValidated.value = false
        }
    }
    
    fun clearAudit() {
        _auditData.value = null
        _currentZoneId.value = ""
        _currentLocationId.value = ""
        _currentQuantity.value = ""
    }
    
    fun clearScanZoneError() {
        _scanZoneError.value = null
    }
    
    fun clearScanZoneToast() {
        _scanZoneToast.value = null
    }

    fun clearScanLocationToast() {
        _scanLocationToast.value = null
    }

    fun clearAddStagingToast() {
        _addStagingToast.value = null
    }

    suspend fun submitAuditRemote(context: Context, zone: String): Pair<Boolean, String> {
        _isLoading.value = true
        return try {
            val auth = SharedAuthData.getAuthData()
            var userId: String = auth?.employeeIdKey ?: ""
            if (userId.isBlank()) {
                val idToken = auth?.idTokenKey
                val fromJwt = if (!idToken.isNullOrBlank()) JwtUtils.extractForKey(idToken, "SamAccountName") else null
                if (!fromJwt.isNullOrBlank()) {
                    userId = fromJwt
                    auth?.employeeIdKey = fromJwt
                    if (auth != null) {
                        AppSettings.saveAuthData(context, auth)
                        SharedAuthData.setAuthData(auth)
                    }
                }
            }
            val req = ClearAuditRequest(
                zone = zone,
                userId = userId,
                deviceId = DeviceUtils.getDeviceId(context)
            )
            val response = NetworkModule.auditApiService.submitAudit(req)
            if (response.isSuccessful) {
                val body = response.body()
                val success = body?.returnCode.equals("Y", true)
                val message = body?.errorMessage ?: ""
                val finalMessage = if (success) if (message.isNotBlank()) message else "Submitted successfully" else message
                Pair(success, finalMessage)
            } else {
                Pair(false, "Submit failed: ${response.code()}")
            }
        } catch (e: Exception) {
            Pair(false, e.message ?: "Unknown error")
        } finally {
            _isLoading.value = false
        }
    }
}
