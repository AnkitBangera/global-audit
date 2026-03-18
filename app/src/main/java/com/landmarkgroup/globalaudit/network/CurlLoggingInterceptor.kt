package com.landmarkgroup.globalaudit.network

import android.util.Log
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okio.Buffer
import okio.IOException

private const val TAG = "CurlScanZone"

/**
 * Logs the equivalent curl command for scan-zone requests so you can copy from Logcat and run in terminal.
 */
class CurlLoggingInterceptor : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val url = request.url.toString()

        if (!url.contains("scan-zone")) {
            return chain.proceed(request)
        }

        val bodyString = request.body?.let { body ->
            val buffer = Buffer()
            try {
                body.writeTo(buffer)
                buffer.readUtf8()
            } catch (e: IOException) {
                "<failed to read body>"
            }
        }

        val contentType = request.body?.contentType()
        val newRequest = if (bodyString != null && contentType != null) {
            request.newBuilder()
                .method(request.method, bodyString.toRequestBody(contentType))
                .build()
        } else {
            request
        }

        val curl = buildCurlString(newRequest, bodyString)
        Log.d(TAG, "--- curl equivalent ---")
        Log.d(TAG, curl)
        Log.d(TAG, "--- end curl ---")

        return chain.proceed(newRequest)
    }

    private fun buildCurlString(request: Request, bodyString: String?): String {
        val url = request.url.toString()
        val sb = StringBuilder()
        sb.append("curl --location '").append(url).append("'")

        request.headers.forEach { (name, value) ->
            sb.append(" \\\n  --header '").append(name).append(": ").append(value).append("'")
        }

        if (!bodyString.isNullOrBlank()) {
            val escaped = bodyString.replace("'", "'\\''")
            sb.append(" \\\n  --data '").append(escaped).append("'")
        }

        return sb.toString()
    }
}
