package com.landmarkgroup.globalaudit.data.model

import android.util.Log

object SharedAuthData {

    private var authData: AuthData? = null

    fun setAuthData(data: AuthData) {
        authData = data
    }

    fun getAuthData(): AuthData? {
        return authData
    }

    fun clearAuthData() {
        authData = null
    }

    fun getUserProfileImageUrl(): String? {
        val profileUrl = authData?.profilePictureUrl
        Log.d("UserImageURL", "SharedAuthData.getUserProfileImageUrl() returning: $profileUrl")
        return profileUrl
    }

    fun getUserDisplayName(): String? {
        return authData?.usernameKey
    }
}
