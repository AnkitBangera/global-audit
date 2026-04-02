package com.landmarkgroup.globalaudit.utils

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.landmarkgroup.globalaudit.BuildConfig
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.landmarkgroup.globalaudit.data.model.AuthData
import java.io.File

/**
 * Settings manager for storing app preferences persistently.
 * Similar to Xamarin's Settings class that uses Plugin.Settings.
 * 
 * This class manages device ID and other app settings using SharedPreferences.
 */
object AppSettings {
    private const val PREFS_NAME = "GlobalAuditPrefs"
    private const val KEY_DEVICE_ID = "DeviceId"
    private const val TAG = "AppSettings"

    // Auth/session persistence
    private const val KEY_ACCESS_TOKEN = "AccessToken"
    private const val KEY_ID_TOKEN = "IdToken"
    private const val KEY_EXPIRES_ON = "ExpiresOn"
    private const val KEY_SELECTED_FACILITY = "SelectedFacility"
    private const val KEY_WAREHOUSE_CODE = "WarehouseCode"
    private const val KEY_USERNAME = "Username"
    private const val KEY_EMPLOYEE_ID = "EmployeeId"
    private const val KEY_PROFILE_PICTURE_URL = "ProfilePictureUrl"
    private const val KEY_PERMISSIBLE_WAREHOUSES = "PermissibleWarehouses"
    private const val KEY_PERMISSIBLE_FACILITIES = "PermissibleFacilities"
    private const val KEY_REFRESH_TOKEN = "RefreshToken"
    // Persisted feature flag for enabling Global Stock Audit across app restarts
    private const val KEY_GLOBAL_AUDIT_FLAG = "UI_FEATURE_GLOBAL_AUDIT_FACILITY_FLAG_ENABLED"

    private fun getSharedPreferences(context: Context): SharedPreferences {
        return try {
            val masterKey = MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()
            EncryptedSharedPreferences.create(
                context,
                PREFS_NAME,
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        } catch (e: Exception) {
            Log.e(TAG, "EncryptedSharedPreferences init failed. Clearing corrupted prefs and retrying: ${e.message}")
            // Attempt to delete the underlying shared_prefs file and retry
            try {
                // API 24+: preferred removal
                context.deleteSharedPreferences(PREFS_NAME)
            } catch (_: Throwable) {
                // Fallback: remove the XML file directly
                try {
                    val prefsFile = File(context.applicationInfo.dataDir + "/shared_prefs/$PREFS_NAME.xml")
                    if (prefsFile.exists()) {
                        prefsFile.delete()
                    }
                } catch (_: Throwable) { /* ignore */ }
            }
            // Recreate a fresh encrypted SharedPreferences
            val masterKey = MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()
            EncryptedSharedPreferences.create(
                context,
                PREFS_NAME,
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        }
    }

    /**
     * Gets the stored device ID.
     * If not set, retrieves and stores the actual device ID.
     * 
     * @param context Application context
     * @return Device ID string
     */
    fun getDeviceId(context: Context): String {
        val prefs = getSharedPreferences(context)
        var deviceId = prefs.getString(KEY_DEVICE_ID, null)
        
        if (deviceId.isNullOrEmpty()) {
            // Device ID not stored yet, get it from device and save it
            deviceId = DeviceUtils.getDeviceId(context)
            setDeviceId(context, deviceId)
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "Device ID initialized and stored: $deviceId")
            }
        } else {
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "Device ID retrieved from storage: $deviceId")
            }
        }
        
        return deviceId
    }

    /**
     * Sets and stores the device ID.
     * 
     * @param context Application context
     * @param deviceId Device ID to store
     */
    fun setDeviceId(context: Context, deviceId: String) {
        val prefs = getSharedPreferences(context)
        prefs.edit()
            .putString(KEY_DEVICE_ID, deviceId)
            .apply()
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "Device ID stored: $deviceId")
        }
    }

    /**
     * Clears the stored device ID.
     * Useful for logout or reset scenarios.
     * 
     * @param context Application context
     */
    fun clearDeviceId(context: Context) {
        val prefs = getSharedPreferences(context)
        prefs.edit()
            .remove(KEY_DEVICE_ID)
            .apply()
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "Device ID cleared")
        }
    }

    /**
     * Checks if device ID is already stored.
     * 
     * @param context Application context
     * @return true if device ID exists, false otherwise
     */
    fun hasDeviceId(context: Context): Boolean {
        val prefs = getSharedPreferences(context)
        val deviceId = prefs.getString(KEY_DEVICE_ID, null)
        return !deviceId.isNullOrEmpty()
    }

    fun saveAuthData(context: Context, authData: AuthData) {
        val prefs = getSharedPreferences(context)
        prefs.edit()
            .putString(KEY_ACCESS_TOKEN, authData.accessTokenKey)
            .putString(KEY_ID_TOKEN, authData.idTokenKey)
            .putLong(KEY_EXPIRES_ON, authData.expiresOnKey)
            .putString(KEY_SELECTED_FACILITY, authData.selectedFacilityKey)
            .putString(KEY_WAREHOUSE_CODE, authData.warehouseCodeKey)
            .putString(KEY_USERNAME, authData.usernameKey)
            .putString(KEY_EMPLOYEE_ID, authData.employeeIdKey)
            .putString(KEY_PROFILE_PICTURE_URL, authData.profilePictureUrl)
            .putStringSet(KEY_PERMISSIBLE_WAREHOUSES, authData.permissableWarehouses.toSet())
            .putStringSet(KEY_PERMISSIBLE_FACILITIES, authData.permissableFacilities.toSet())
            .putString(KEY_REFRESH_TOKEN, authData.refreshTokenKey)
            .apply()

        if (BuildConfig.DEBUG) {
            Log.d(TAG, "Auth data saved (expiresOn=${authData.expiresOnKey}, selectedFacility=${authData.selectedFacilityKey})")
        }
    }

    fun loadAuthData(context: Context): AuthData? {
        val prefs = getSharedPreferences(context)
        val idToken = prefs.getString(KEY_ID_TOKEN, null)
        val accessToken = prefs.getString(KEY_ACCESS_TOKEN, null)
        val expiresOn = prefs.getLong(KEY_EXPIRES_ON, 0L)

        // If we don't have the basics, treat as no cached session.
        if (idToken.isNullOrBlank() || accessToken.isNullOrBlank() || expiresOn <= 0L) {
            return null
        }

        val employeeId = prefs.getString(KEY_EMPLOYEE_ID, null)
            ?: JwtUtils.extractForKey(idToken, "SamAccountName")
        return AuthData(
            accessTokenKey = accessToken,
            idTokenKey = idToken,
            expiresOnKey = expiresOn,
            selectedFacilityKey = prefs.getString(KEY_SELECTED_FACILITY, null),
            warehouseCodeKey = prefs.getString(KEY_WAREHOUSE_CODE, null),
            usernameKey = prefs.getString(KEY_USERNAME, null),
            employeeIdKey = employeeId,
            profilePictureUrl = prefs.getString(KEY_PROFILE_PICTURE_URL, null),
            permissableWarehouses = prefs.getStringSet(KEY_PERMISSIBLE_WAREHOUSES, emptySet())?.toList() ?: emptyList(),
            permissableFacilities = prefs.getStringSet(KEY_PERMISSIBLE_FACILITIES, emptySet())?.toList() ?: emptyList(),
            refreshTokenKey = prefs.getString(KEY_REFRESH_TOKEN, null)
        )
    }
    
    // Persist/Load the Global Stock Audit feature flag
    fun setGlobalAuditEnabled(context: Context, enabled: Boolean) {
        val prefs = getSharedPreferences(context)
        prefs.edit()
            .putBoolean(KEY_GLOBAL_AUDIT_FLAG, enabled)
            .apply()
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "Global Audit feature flag stored: $enabled")
        }
    }
    
    fun getGlobalAuditEnabled(context: Context): Boolean {
        val prefs = getSharedPreferences(context)
        return prefs.getBoolean(KEY_GLOBAL_AUDIT_FLAG, false)
    }
    
    fun clearAuthData(context: Context) {
        val prefs = getSharedPreferences(context)
        prefs.edit()
            .remove(KEY_ACCESS_TOKEN)
            .remove(KEY_ID_TOKEN)
            .remove(KEY_EXPIRES_ON)
            .remove(KEY_SELECTED_FACILITY)
            .remove(KEY_WAREHOUSE_CODE)
            .remove(KEY_USERNAME)
            .remove(KEY_EMPLOYEE_ID)
            .remove(KEY_PROFILE_PICTURE_URL)
            .remove(KEY_PERMISSIBLE_WAREHOUSES)
            .remove(KEY_PERMISSIBLE_FACILITIES)
            .remove(KEY_REFRESH_TOKEN)
            .remove(KEY_GLOBAL_AUDIT_FLAG)
            .apply()
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "Auth data cleared")
        }
    }
}
