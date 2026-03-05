package com.landmarkgroup.globalaudit.utils

import android.content.Context
import android.content.SharedPreferences
import android.util.Log

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

    private fun getSharedPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
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
            Log.d(TAG, "Device ID initialized and stored: $deviceId")
        } else {
            Log.d(TAG, "Device ID retrieved from storage: $deviceId")
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
        Log.d(TAG, "Device ID stored: $deviceId")
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
        Log.d(TAG, "Device ID cleared")
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
}
