package com.landmarkgroup.globalaudit.utils

import android.content.Context

/**
 * Application constants and utility methods.
 * Similar to Xamarin's Constants class.
 */
object Constants {
    const val IBM_URL = "https://apidev.landmarkgroup.com"
    const val NGINX_URL = "https://devapi1.landmarkgroup.com/warehouse-ops/"
    
    /**
     * Gets the device ID for the current device.
     * This is equivalent to Xamarin's Constants.DEVICE_ID.
     * 
     * The device ID is:
     * 1. Retrieved from persistent storage if already set
     * 2. Extracted from device (Settings.Secure.ANDROID_ID) if not stored
     * 3. Stored for future use
     * 
     * @param context Application context (required for first call)
     * @return Unique device identifier
     */
    fun getDeviceId(context: Context): String {
        return AppSettings.getDeviceId(context)
    }
    
    /**
     * Gets the device ID if context is available.
     * This is a convenience method that requires context to be passed.
     * 
     * Note: For use in ViewModels or classes that have access to Context.
     * For classes without context, use AppSettings.getDeviceId(context) directly.
     */
    val DEVICE_ID: String
        get() {
            throw IllegalStateException(
                "DEVICE_ID requires Context. Use Constants.getDeviceId(context) or AppSettings.getDeviceId(context) instead."
            )
        }
}
