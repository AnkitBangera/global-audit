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
        
        // For IBM API (apidev), use access token; for other APIs, use ID token
        val token: String? = if (originalRequest.url.toString().contains("apidev")) {
            authData.accessTokenKey
        } else {
            authData.idTokenKey
        }
        
        if (token != null) {
            builder.header("Authorization", "Bearer $token")
        }
        
        // For apidev/retail-sit, use IBM API Gateway client id (not ADFS client id)
        if (originalRequest.url.toString().contains("apidev")) {
            builder.header(AuthConstants.API_CLIENT_ID_HEADER, AuthConstants.API_CLIENT_ID_VALUE)
            authData.usernameKey?.let { builder.header("USER-ID", it) }
            authData.selectedFacilityKey?.let { builder.header("FACILITY-ID", it) }
        }
        
        val requestWithHeaders = builder.build()
        return chain.proceed(requestWithHeaders)
    }
}
