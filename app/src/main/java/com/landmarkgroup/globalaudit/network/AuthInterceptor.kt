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

        // Build and print a curl command for scan-zone API for backend verification
        try {
            val urlStr = requestWithHeaders.url.toString()
            if (urlStr.contains("/audit/scan-zone") || urlStr.contains("/audit/scan-location") || urlStr.contains("/audit/add-staging")) {
                val sb = StringBuilder()
                sb.append("curl --location \\").append("\n")
                    .append("  '").append(urlStr).append("' \\").append("\n")

                val headers = requestWithHeaders.headers
                for (name in headers.names()) {
                    val value = headers[name]
                    sb.append("  --header '").append(name).append(": ").append(value).append("' \\").append("\n")
                }

                val body = requestWithHeaders.body
                if (body != null && (requestWithHeaders.method == "POST" || requestWithHeaders.method == "PUT" || requestWithHeaders.method == "PATCH")) {
                    val buffer = Buffer()
                    body.writeTo(buffer)
                    val bodyStr = buffer.readUtf8()
                    if (bodyStr.isNotEmpty()) {
                        // Escape single quotes for shell safety: ' -> '"'"'
                        val escaped = bodyStr.replace("'", "'\"'\"'")
                        sb.append("  --data '").append(escaped).append("'")
                    } else {
                        // remove trailing backslash if present
                        if (sb.endsWith("\\\n")) {
                            sb.setLength(sb.length - 2)
                        }
                    }
                } else {
                    // remove trailing backslash if present
                    if (sb.endsWith("\\\n")) {
                        sb.setLength(sb.length - 2)
                    }
                }

                Log.d("Curl", "Audit curl:\n${sb}")
            }
        } catch (e: Exception) {
            Log.e("Curl", "Failed to build curl for request", e)
        }

        return chain.proceed(requestWithHeaders)
    }
}
