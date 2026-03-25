package com.landmarkgroup.globalaudit.data.model

data class AuthData(
    var accessTokenKey: String? = null,
    var idTokenKey: String? = null,
    var expiresOnKey: Long = 0L,
    var selectedFacilityKey: String? = null,
    var warehouseCodeKey: String? = null,
    var usernameKey: String? = null,
    var employeeIdKey: String? = null,
    var profilePictureUrl: String? = null,
    var permissableWarehouses: List<String> = emptyList(),
    var permissableFacilities: List<String> = emptyList(),
    // Refresh token for silent re-authentication via AppAuth
    var refreshTokenKey: String? = null
)
