package com.landmarkgroup.globalaudit.utils

import android.util.Log
import com.auth0.android.jwt.JWT

object JwtUtils {

    fun extractForKey(token: String?, key: String): String? {
        if (token.isNullOrBlank()) {
            return null
        }
        try {
            val jwt = JWT(token)
            val claimValue = jwt.getClaim(key).asString()

            if (claimValue.isNullOrBlank()) {
                Log.w("JwtUtils", "Claim '$key' is missing or empty in the JWT.")
                return null
            }
            Log.d("JwtUtils", "Extracted $key: $claimValue")
            return claimValue
        } catch (e: Exception) {
            Log.e("JwtUtils", "Error decoding JWT or extracting $key: ${e.message}", e)
            return null
        }
    }

    fun extractUserId(token: String?): String? {
        Log.d("UserImageURL", "extractUserId called with token: ${if (token.isNullOrBlank()) "null/blank" else "present"}")

        if (token.isNullOrBlank()) {
            Log.w("UserImageURL", "Token is null or blank, cannot extract user ID")
            return null
        }

        val possibleUserIdClaims = listOf(
            "sub",
            "upn",
            "unique_name",
            "preferred_username",
            "samaccountname",
            "sam_account_name",
            "sAMAccountName"
        )

        Log.d("UserImageURL", "Trying to extract user ID from claims: $possibleUserIdClaims")

        for (claimName in possibleUserIdClaims) {
            val userId = extractForKey(token, claimName)
            if (!userId.isNullOrBlank()) {
                Log.d("UserImageURL", "Successfully extracted User ID from claim '$claimName': $userId")
                return userId
            } else {
                Log.d("UserImageURL", "Claim '$claimName' is null or blank")
            }
        }

        Log.w("UserImageURL", "Could not extract user ID from any known claims")
        return null
    }
}
