package com.landmarkgroup.globalaudit.utils

import android.net.Uri

object AuthConstants {
    const val ADFS_CLIENT_ID = "5a71b107-a456-4a68-b2d2-c1c8d09d7757"
    const val ADFS_AUTHORITY_URI = "https://sts.landmarkgroup.com/adfs/"
    val ADFS_DISCOVERY_URI: Uri = Uri.parse("$ADFS_AUTHORITY_URI/.well-known/openid-configuration")
    val ADFS_AUTHORIZATION_ENDPOINT_URI: Uri? = null
    val ADFS_TOKEN_ENDPOINT_URI: Uri? = null
    // TODO: Once http://global-audit is registered in ADFS, change back to:
    // val REDIRECT_URI: Uri = Uri.parse("http://global-audit")
    // For now, using the same redirect URI as efulfill-android for testing
    val REDIRECT_URI: Uri = Uri.parse("http://sahlaapp")
    const val REDIRECT_URI_SCHEME = "http"
    const val REDIRECT_URI_HOST = "sahlaapp"
    const val ADFS_RESOURCE_IDENTIFIER = "https://SahlaWebApi"
    val SCOPES = listOf("openid", "profile", "email")
    const val AUTH_STATE_PREFS_KEY = "authStateJson"
    const val AUTH_PREFS_NAME = "authPrefs"

    // IBM API Gateway client id for apidev/retail-sit (same as efulfill-android SIT)
    const val API_CLIENT_ID_HEADER = "x-ibm-client-id"
    const val API_CLIENT_ID_VALUE = "612f0b51-8594-4a44-93f0-265113649943"
}
