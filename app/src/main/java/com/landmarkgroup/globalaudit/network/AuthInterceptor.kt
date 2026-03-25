package com.landmarkgroup.globalaudit.network

import com.landmarkgroup.globalaudit.data.model.SharedAuthData
import com.landmarkgroup.globalaudit.utils.AuthConstants
import okhttp3.Interceptor
import okhttp3.Response
import android.util.Log
import okio.Buffer

class AuthInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val builder = originalRequest.newBuilder()
        
        val authData = SharedAuthData.getAuthData()
            ?: throw IllegalStateException("AuthData is not set. Please ensure it is initialized before making requests.")
        
        // Use ID token for all requests (NGINX and IBM accept it)
        val token: String? = authData.idTokenKey
        
        if (token != null) {
            builder.header("Authorization", "Bearer $token")
        }
        
        // Inject common user/session headers for ALL requests (IBM + NGINX)
        // Use employeeId for USER-ID only
        authData.employeeIdKey?.let { builder.header("USER-ID", it) }
        authData.selectedFacilityKey?.let { builder.header("FACILITY-ID", it) }

        // IBM API Gateway client id header required by both IBM and NGINX gateways
        builder.header(AuthConstants.API_CLIENT_ID_HEADER, AuthConstants.API_CLIENT_ID_VALUE)
        // Landmark client id header required by IBM gateway (parity with Xamarin)
        builder.header("x-landmark-client-id", AuthConstants.API_CLIENT_ID_VALUE)
        // Explicit id_token header (parity with Xamarin)
        token?.let { builder.header("id_token", it) }
        // Additional parity headers
        authData.usernameKey?.let { builder.header("user", it) }
        authData.warehouseCodeKey?.let { builder.header("location", it) }
        authData.selectedFacilityKey?.let { builder.header("facility", it) }
        
        val requestWithHeaders = builder.build()


        return chain.proceed(requestWithHeaders)
    }
}
