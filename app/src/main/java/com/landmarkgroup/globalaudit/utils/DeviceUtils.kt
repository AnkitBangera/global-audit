package com.landmarkgroup.globalaudit.utils

import android.content.Context
import android.provider.Settings
import android.util.Log

/**
 * Utility class for device-related operations.
 * Similar to Xamarin's DeviceService.GetInfo() which uses Plugin.DeviceInfo.CrossDeviceInfo.Current.Id
 * On Android, this uses Settings.Secure.ANDROID_ID which is the equivalent.
 */
object DeviceUtils {
    private const val TAG = "DeviceUtils"

    /**
     * Gets the unique Android device ID.
     * This is equivalent to Xamarin's Plugin.DeviceInfo.CrossDeviceInfo.Current.Id
     * 
     * @param context Application context
     * @return Unique device identifier (ANDROID_ID)
     */
    fun getDeviceId(context: Context): String {
        return try {
            val androidId = Settings.Secure.getString(
                context.contentResolver,
                Settings.Secure.ANDROID_ID
            )
            if (androidId.isNullOrEmpty()) {
                Log.w(TAG, "ANDROID_ID is null or empty, using fallback")
                // Fallback: Generate a unique ID based on device info
                generateFallbackDeviceId(context)
            } else {
                Log.d(TAG, "Device ID retrieved: $androidId")
                androidId
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting device ID", e)
            generateFallbackDeviceId(context)
        }
    }

    /**
     * Fallback method to generate a device ID if ANDROID_ID is not available.
     * This should rarely be needed, but provides a backup.
     */
    private fun generateFallbackDeviceId(context: Context): String {
        // Use a combination of device properties as fallback
        val deviceInfo = android.os.Build.MANUFACTURER +
                android.os.Build.MODEL +
                android.os.Build.SERIAL
        return deviceInfo.hashCode().toString()
    }
}
