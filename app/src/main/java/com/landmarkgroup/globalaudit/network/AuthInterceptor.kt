package com.landmarkgroup.globalaudit.network

import com.landmarkgroup.globalaudit.data.model.SharedAuthData
import com.landmarkgroup.globalaudit.utils.AuthConstants
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val builder = originalRequest.newBuilder()
        
        val authData = SharedAuthData.getAuthData()
            ?: throw IllegalStateException("AuthData is not set. Please ensure it is initialized before making requests.")
        
        // For IBM API (apidev), use ID token; for NGINX/warehouse-ops, use Access token
        val token: String? = if (originalRequest.url.toString().contains("apidev")) {
            authData.idTokenKey
        } else {
            authData.accessTokenKey
        }
        
        if (token != null) {
            builder.header("Authorization", "Bearer $token")
        }
        
        // Inject common user/session headers for ALL requests (IBM + NGINX)
        // Use employeeId for USER-ID if available; fallback to display name
        if (!authData.employeeIdKey.isNullOrEmpty()) {
            builder.header("USER-ID", authData.employeeIdKey!!)
        } else {
            authData.usernameKey?.let { builder.header("USER-ID", it) }
        }
        authData.selectedFacilityKey?.let { builder.header("FACILITY-ID", it) }

        // IBM API Gateway client id header required by both IBM and NGINX gateways
        builder.header(AuthConstants.API_CLIENT_ID_HEADER, AuthConstants.API_CLIENT_ID_VALUE)
        
        val requestWithHeaders = builder.build()
        return chain.proceed(requestWithHeaders)
    }
}
