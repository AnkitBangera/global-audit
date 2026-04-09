package com.landmarkgroup.globalaudit.network

import android.util.Log
import com.landmarkgroup.globalaudit.data.model.SharedAuthData
import com.landmarkgroup.globalaudit.utils.AuthConstants
import okhttp3.FormBody
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONObject
import java.util.concurrent.TimeUnit
import com.landmarkgroup.globalaudit.utils.SessionEvents

/**
 * Auth interceptor that:
 * - Attaches the current ID token and parity headers
 * - Proactively attempts a refresh if the token is expired before the request
 * - On 401, tries a synchronous refresh once and retries the request
 *
 * Note: Refresh here updates the in-memory SharedAuthData. Persisting to disk
 * is intentionally skipped to avoid needing an Android Context in networking layer.
 * The refreshed tokens remain valid for the app lifetime; persistence will happen
 * on the next ViewModel save.
 */
class AuthInterceptor : Interceptor {

    companion object {
        @Volatile
        private var isRefreshing = false
        private val refreshLock = Any()
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()

        val authData = SharedAuthData.getAuthData()
            ?: throw IllegalStateException("AuthData is not set. Please ensure it is initialized before making requests.")

        var token: String? = authData.idTokenKey

        // Pre-flight expiry check
        val now = System.currentTimeMillis()
        if (authData.expiresOnKey <= now) {
            if (tryRefreshTokenBlocking()) {
                token = SharedAuthData.getAuthData()?.idTokenKey
                Log.d("AuthInterceptor", "Pre-flight token refresh succeeded")
            } else {
                Log.w("AuthInterceptor", "Pre-flight token refresh failed; proceeding may yield 401")
            }
        }

        val requestWithHeaders = buildRequestWithHeaders(originalRequest, token, authData)
        var response = chain.proceed(requestWithHeaders)

        // If unauthorized, try a single refresh and retry once
        if (response.code == 401) {
            response.close()
            if (tryRefreshTokenBlocking()) {
                val newToken = SharedAuthData.getAuthData()?.idTokenKey
                Log.d("AuthInterceptor", "401-triggered refresh succeeded; retrying request")
                val retryRequest = buildRequestWithHeaders(originalRequest, newToken, authData)
                response = chain.proceed(retryRequest)
            } else {
                Log.w("AuthInterceptor", "401-triggered refresh failed; notifying session-expired")
                // Notify UI to redirect to login
                SessionEvents.notifySessionExpired()
                // Proceed again to return consistent response (likely 401)
                response = chain.proceed(requestWithHeaders)
            }
        }

        return response
    }

    private fun buildRequestWithHeaders(originalRequest: okhttp3.Request, token: String?, authData: com.landmarkgroup.globalaudit.data.model.AuthData): okhttp3.Request {
        val builder = originalRequest.newBuilder()

        // Use ID token for all requests (NGINX and IBM accept it)
        if (!token.isNullOrEmpty()) {
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

        return builder.build()
    }

    private fun tokenEndpoint(): String = "${AuthConstants.ADFS_AUTHORITY_URI}oauth2/token"

    /**
     * Attempts to refresh the token synchronously using the stored refresh token.
     * Updates SharedAuthData on success.
     */
    private fun tryRefreshTokenBlocking(): Boolean {
        val current = SharedAuthData.getAuthData() ?: return false
        val refresh = current.refreshTokenKey ?: return false

        synchronized(refreshLock) {
            if (isRefreshing) {
                // Another thread is already refreshing; wait a short moment by returning false
                // The caller will likely retry naturally on next request.
                return false
            }
            isRefreshing = true
        }

        return try {
            val client = OkHttpClient.Builder()
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(15, TimeUnit.SECONDS)
                .build()

            val body = FormBody.Builder()
                .add("grant_type", "refresh_token")
                .add("client_id", AuthConstants.ADFS_CLIENT_ID)
                .add("refresh_token", refresh)
                .build()

            val request = Request.Builder()
                .url(tokenEndpoint())
                .post(body)
                .build()

            val response = client.newCall(request).execute()
            if (!response.isSuccessful) {
                Log.w("AuthInterceptor", "Refresh token HTTP ${response.code}")
                return false
            }
            val payload = response.body?.string() ?: return false
            val json = JSONObject(payload)

            val access = json.optString("access_token", null)
            val id = json.optString("id_token", null)
            val newRefresh = if (json.has("refresh_token")) json.optString("refresh_token") else null
            val expiresIn = json.optLong("expires_in", 0L)
            val newExpiry = if (expiresIn > 0) System.currentTimeMillis() + expiresIn * 1000 else System.currentTimeMillis() + 3600_000

            val shared = SharedAuthData.getAuthData() ?: return false
            if (!access.isNullOrEmpty()) shared.accessTokenKey = access
            if (!id.isNullOrEmpty()) shared.idTokenKey = id
            shared.expiresOnKey = newExpiry
            if (!newRefresh.isNullOrEmpty()) shared.refreshTokenKey = newRefresh

            SharedAuthData.setAuthData(shared)
            true
        } catch (e: Exception) {
            Log.e("AuthInterceptor", "Token refresh exception", e)
            false
        } finally {
            synchronized(refreshLock) { isRefreshing = false }
        }
    }
}
