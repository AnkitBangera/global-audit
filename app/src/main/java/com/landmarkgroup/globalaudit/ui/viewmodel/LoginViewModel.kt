package com.landmarkgroup.globalaudit.ui.viewmodel

import android.app.Application
import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.landmarkgroup.globalaudit.data.model.AuthData
import com.landmarkgroup.globalaudit.data.model.AuthorizationResponse
import com.landmarkgroup.globalaudit.data.model.SharedAuthData
import com.landmarkgroup.globalaudit.data.model.UserFacility
import com.landmarkgroup.globalaudit.network.NetworkModule
import com.landmarkgroup.globalaudit.utils.AppSettings
import com.landmarkgroup.globalaudit.utils.AuthConstants
import com.landmarkgroup.globalaudit.utils.DeviceUtils
import com.landmarkgroup.globalaudit.utils.JwtUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import net.openid.appauth.*

sealed class LoginUiState {
    object Idle : LoginUiState()
    object AdfsLoginInProgress : LoginUiState()
    data class ShowWebView(val url: String) : LoginUiState()
    object Authorizing : LoginUiState()
    data class ShowFacilitySelection(val facilities: List<String>) : LoginUiState()
    object NavigateToMainApp : LoginUiState()
    data class Error(val message: String) : LoginUiState()
}

class LoginViewModel(
    private val application: Application
) : ViewModel() {
    private val appContext: Context = application.applicationContext
    private val authService: AuthorizationService = AuthorizationService(appContext)
    private val apiService = NetworkModule.sahlaApiService

    private val _uiState = MutableStateFlow<LoginUiState>(LoginUiState.Idle)
    val uiState: StateFlow<LoginUiState> = _uiState

    private var lastAuthRequest: AuthorizationRequest? = null
    private lateinit var serviceConfig: AuthorizationServiceConfiguration

    init {
        // Initialize device ID on app startup (similar to Xamarin's ValidateDevice)
        initializeDeviceId()

        // Restore cached session (survives swipe-kill) if present
        val cachedAuth = AppSettings.loadAuthData(appContext)
        if (cachedAuth != null) {
            SharedAuthData.setAuthData(cachedAuth)
        }

        // If session is still valid, skip ADFS and go straight into the app
        if (isSessionValid(SharedAuthData.getAuthData())) {
            _uiState.value = LoginUiState.NavigateToMainApp
        }

        serviceConfig = AuthorizationServiceConfiguration(
            Uri.parse("https://sts.landmarkgroup.com/adfs/oauth2/authorize"),
            Uri.parse("https://sts.landmarkgroup.com/adfs/oauth2/token")
        )
    }

    /**
     * Initializes the device ID by retrieving it from device and storing it.
     * Similar to Xamarin's ValidateDevice() method which calls:
     * var currentDeviceId = DependencyService.Get<IDeviceService>().GetInfo();
     * Settings.DeviceID = currentDeviceId;
     */
    private fun initializeDeviceId() {
        try {
            val currentDeviceId = DeviceUtils.getDeviceId(appContext)
            AppSettings.setDeviceId(appContext, currentDeviceId)
            Log.d("LoginViewModel", "Device ID initialized: $currentDeviceId")
        } catch (e: Exception) {
            Log.e("LoginViewModel", "Error initializing device ID", e)
        }
    }

    fun initiateLogin() {
        _uiState.value = LoginUiState.AdfsLoginInProgress
        val authRequest = AuthorizationRequest.Builder(
            serviceConfig,
            AuthConstants.ADFS_CLIENT_ID,
            ResponseTypeValues.CODE,
            AuthConstants.REDIRECT_URI
        )
            .setScope(AuthConstants.SCOPES.joinToString(" "))
            .setNonce(null)
            .setAdditionalParameters(mapOf("resource" to AuthConstants.ADFS_RESOURCE_IDENTIFIER))
            .setPrompt("login")
            .build()
        lastAuthRequest = authRequest
        _uiState.value = LoginUiState.ShowWebView(authRequest.toUri().toString())
    }

    fun onLoginClick() {
        // If the session is still valid, do NOT trigger ADFS again.
        if (isSessionValid(SharedAuthData.getAuthData())) {
            _uiState.value = LoginUiState.NavigateToMainApp
            return
        }
        initiateLogin()
    }

    fun handleAuthCode(authCode: String) {
        Log.d("LoginViewModel", "Received authorization code: $authCode")
        val tokenRequest = TokenRequest.Builder(
            serviceConfig,
            AuthConstants.ADFS_CLIENT_ID
        )
            .setGrantType(GrantTypeValues.AUTHORIZATION_CODE)
            .setAuthorizationCode(authCode)
            .setRedirectUri(AuthConstants.REDIRECT_URI)
            .build()

        authService.performTokenRequest(tokenRequest) { tokenResponse, ex ->
            if (tokenResponse != null) {
                val accessToken = tokenResponse.accessToken
                val idToken = tokenResponse.idToken
                val expiresOn = tokenResponse.accessTokenExpirationTime ?: 0L

                Log.d("LoginViewModel", "ADFS Login successful - Access Token: $accessToken")
                Log.d("LoginViewModel", "ADFS Login successful - ID Token: $idToken")
                Log.d("UserImageURL", "Starting profile image URL construction in LoginViewModel")
                val userId = JwtUtils.extractUserId(idToken)
                Log.d("UserImageURL", "Extracted userId for profile image: $userId")
                // TODO: Implement profile image URL if needed
                // val profileImageUrl = Constants.getUserImageUrl(userId)
                
                val empIdFromJwt = JwtUtils.extractForKey(idToken, "SamAccountName")
                val displayName = (JwtUtils.extractForKey(idToken, "given_name") ?: "").trim() + " " +
                        (JwtUtils.extractForKey(idToken, "family_name") ?: "").trim()

                var sharedAuthData = SharedAuthData.getAuthData()
                if (sharedAuthData == null) {
                    sharedAuthData = AuthData(
                        accessTokenKey = accessToken,
                        idTokenKey = idToken,
                        expiresOnKey = expiresOn,
                        selectedFacilityKey = null,
                        usernameKey = displayName.trim(),
                        employeeIdKey = empIdFromJwt,
                        warehouseCodeKey = "",
                        profilePictureUrl = null, // Can be set if profile image URL is needed
                        permissableWarehouses = emptyList(),
                        permissableFacilities = emptyList()
                    )
                } else {
                    sharedAuthData.accessTokenKey = accessToken
                    sharedAuthData.idTokenKey = idToken
                    sharedAuthData.expiresOnKey = expiresOn
                    sharedAuthData.usernameKey = displayName.trim()
                    // Prefer SamAccountName if available
                    if (!empIdFromJwt.isNullOrEmpty()) {
                        sharedAuthData.employeeIdKey = empIdFromJwt
                    }
                    // Keep existing profilePictureUrl if already set
                }
                SharedAuthData.setAuthData(sharedAuthData)
                AppSettings.saveAuthData(appContext, sharedAuthData)

                if (accessToken != null && idToken != null) {
                    authorizeWithBackend()
                } else {
                    handleAuthFlowError("Missing access token or ID token")
                }
            } else {
                Log.e("LoginViewModel", "Token exchange failed", ex)
                handleAuthFlowError("Token exchange failed: ${ex?.errorDescription}")
            }
        }
    }

    private fun authorizeWithBackend() {
        _uiState.value = LoginUiState.Authorizing
        viewModelScope.launch {
            try {
                // Call the actual API endpoint to get user facilities
                val authorizationResponse = apiService.authorize()
                
                Log.d("LoginViewModel", "Authorization response received: ${authorizationResponse.id}")
                
                val sharedAuthData = SharedAuthData.getAuthData()
                val facilities = authorizationResponse.authorizations?.locations
                // Persist employeeId from backend authorizations response if present
                if (authorizationResponse.id != null) {
                    sharedAuthData?.employeeIdKey = authorizationResponse.id
                }
                if (facilities != null) {
                    sharedAuthData?.permissableWarehouses = facilities
                    sharedAuthData?.permissableFacilities = facilities.mapNotNull { 
                        UserFacility.fromId(it)?.name 
                    }
                    if (sharedAuthData != null) {
                        AppSettings.saveAuthData(appContext, sharedAuthData)
                    }
                }
                
                if (authorizationResponse.id != null && !facilities.isNullOrEmpty()) {
                    if (facilities.size == 1) {
                        val facilityName = UserFacility.fromId(facilities.first())?.name ?: facilities.first()
                        onFacilitySelected(facilityName)
                    } else {
                        _uiState.value = LoginUiState.ShowFacilitySelection(
                            facilities.mapNotNull { UserFacility.fromId(it)?.name }
                        )
                    }
                } else {
                    handleAuthFlowError("User not authorized by backend system.")
                }
            } catch (e: Exception) {
                Log.e("LoginViewModel", "Error calling authorize API", e)
                val errorMessage = when {
                    e.message?.contains("401") == true -> {
                        "Authentication failed. Please try logging in again."
                    }
                    e.message?.contains("403") == true -> {
                        "Access denied. Please contact your administrator."
                    }
                    e.message?.contains("404") == true -> {
                        "API endpoint not found. Please contact support."
                    }
                    e.message?.contains("500") == true -> {
                        "Server error. Please try again later."
                    }
                    else -> {
                        "Could not verify user with backend: ${e.message}"
                    }
                }
                handleAuthFlowError(errorMessage)
            }
        }
    }

    fun onFacilitySelected(facility: String) {
        viewModelScope.launch {
            val sharedAuthData = SharedAuthData.getAuthData()
            sharedAuthData?.selectedFacilityKey = facility
            sharedAuthData?.warehouseCodeKey = UserFacility.fromName(facility)?.id ?: ""
            if (sharedAuthData != null) {
                AppSettings.saveAuthData(appContext, sharedAuthData)
            }
            // Fire and store warehouse feature flags for the selected facility (numeric id like 1001000 for BU)
            try {
                val locationId = sharedAuthData?.warehouseCodeKey
                    ?: UserFacility.fromName(facility)?.id
                    ?: facility
                Log.d("LoginViewModel", "Calling eFulfill features endpoint for location: $locationId")
                val features = NetworkModule.efulfillApiService.getWarehouseFeatures(locationId)
                SharedAuthData.setWarehouseFeatures(features)
                val isAuditEnabled = features.any {
                    it.featureName.equals("UI_FEATURE_GLOBAL_AUDIT_FACILITY_FLAG", ignoreCase = true) && it.featureEnabledValue
                }
                AppSettings.setGlobalAuditEnabled(appContext, isAuditEnabled)
                Log.d("LoginViewModel", "Fetched ${features.size} warehouse feature flags for location $locationId; GlobalAuditEnabled=$isAuditEnabled")
            } catch (e: Exception) {
                Log.e("LoginViewModel", "Failed to fetch warehouse features for facility $facility", e)
                // Persist disabled if we couldn't fetch features to ensure safe default on next launch
                AppSettings.setGlobalAuditEnabled(appContext, false)
                // Non-blocking: proceed to app even if this call fails
            }
            _uiState.value = LoginUiState.NavigateToMainApp
        }
    }

    fun logout() {
        SharedAuthData.clearAuthData()
        AppSettings.clearAuthData(appContext)
        _uiState.value = LoginUiState.Idle
    }
    
    fun handleAuthFlowError(message: String) {
        Log.e("LoginViewModel", "Auth Flow Error: $message")
        SharedAuthData.clearAuthData()
        AppSettings.clearAuthData(appContext)
        _uiState.value = LoginUiState.Error(message)
    }

    private fun isSessionValid(authData: AuthData?): Boolean {
        return authData != null &&
            !authData.idTokenKey.isNullOrEmpty() &&
            !authData.accessTokenKey.isNullOrEmpty() &&
            authData.expiresOnKey > System.currentTimeMillis() &&
            !authData.selectedFacilityKey.isNullOrEmpty()
    }
}
