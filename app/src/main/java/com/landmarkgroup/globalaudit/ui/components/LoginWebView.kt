package com.landmarkgroup.globalaudit.ui.components

import android.net.Uri
import android.util.Log
import android.view.ViewGroup
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.runtime.Composable
import androidx.compose.ui.viewinterop.AndroidView
import com.landmarkgroup.globalaudit.utils.AuthConstants
import com.landmarkgroup.globalaudit.BuildConfig

@Composable
fun LoginWebView(
    url: String,
    onAuthCodeReceived: (String) -> Unit,
    onAuthError: (String) -> Unit
) {
    AndroidView(factory = { context ->
        WebView(context).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            settings.apply {
                javaScriptEnabled = true
                domStorageEnabled = true
                allowFileAccess = false
                allowContentAccess = false
                loadWithOverviewMode = true
                useWideViewPort = true
            }

            webViewClient = object : WebViewClient() {

                override fun onPageFinished(view: WebView?, url: String?) {
                    super.onPageFinished(view, url)
                    Log.d("LoginWebView", "onPageFinished: Successfully loaded URL: $url")
                }

                override fun onReceivedError(
                    view: WebView?,
                    request: WebResourceRequest?,
                    error: WebResourceError?
                ) {
                    super.onReceivedError(view, request, error)
                    val failingUrl = request?.url?.toString() ?: "Unknown URL"
                    val errorCode = error?.errorCode ?: -1
                    val errorDescription = error?.description?.toString() ?: "Unknown error"
                    Log.e("LoginWebView", "onReceivedError: Failed to load URL: $failingUrl, Error Code: $errorCode, Description: $errorDescription")

                    when (errorCode) {
                        WebViewClient.ERROR_HOST_LOOKUP -> {
                            onAuthError("Network error: Unable to resolve host. Please check your internet connection and try again.")
                        }
                        WebViewClient.ERROR_TIMEOUT -> {
                            onAuthError("Connection timeout. Please try again.")
                        }
                        WebViewClient.ERROR_CONNECT -> {
                            onAuthError("Connection failed. Please check your internet connection.")
                        }
                        else -> {
                            onAuthError("WebView error ($errorCode): $errorDescription")
                        }
                    }
                }

                override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                    val url = request?.url?.toString() ?: return false
                    if (BuildConfig.DEBUG) {
                        Log.d("LoginWebView", "shouldOverrideUrlLoading: Intercepted URL: $url")
                    }

                    val uri = Uri.parse(url)
                    if (uri.scheme == AuthConstants.REDIRECT_URI.scheme && uri.host == AuthConstants.REDIRECT_URI.host) {
                        if (BuildConfig.DEBUG) {
                            Log.d("LoginWebView", "Redirect URI detected: $url")
                        }
                        val authCode = uri.getQueryParameter("code")
                        val error = uri.getQueryParameter("error")
                        val errorDescription = uri.getQueryParameter("error_description")

                        when {
                            authCode != null -> {
                                if (BuildConfig.DEBUG) {
                                    Log.d("LoginWebView", "Auth code found: $authCode")
                                }
                                onAuthCodeReceived(authCode)
                                return true
                            }
                            error != null -> {
                                val errorMsg = errorDescription ?: error
                                Log.e("LoginWebView", "ADFS Error: $errorMsg")
                                onAuthError("ADFS Error: $errorMsg")
                                return true
                            }
                        }
                    }

                    return false
                }
            }

            loadUrl(url)
        }
    })
}
