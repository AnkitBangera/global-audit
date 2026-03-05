# Device ID Usage Guide

This document explains how to use the device ID functionality, which mirrors the Xamarin project's implementation.

## Overview

The device ID extraction follows the same pattern as the Xamarin project:
- **Xamarin**: Uses `Plugin.DeviceInfo.CrossDeviceInfo.Current.Id` → `Settings.DeviceID`
- **Android (Kotlin)**: Uses `Settings.Secure.ANDROID_ID` → `AppSettings.getDeviceId(context)`

## How It Works

1. **Device ID Extraction**: `DeviceUtils.getDeviceId(context)` extracts the Android device ID using `Settings.Secure.ANDROID_ID`
2. **Persistent Storage**: `AppSettings` stores the device ID in SharedPreferences (similar to Xamarin's `Settings.DeviceID`)
3. **Automatic Initialization**: Device ID is automatically initialized during login (in `LoginViewModel.init()`)

## Usage Examples

### In ViewModels (with Application context)

```kotlin
class MyViewModel(
    private val application: Application
) : ViewModel() {
    private val appContext = application.applicationContext
    
    fun makeApiCall() {
        val deviceId = AppSettings.getDeviceId(appContext)
        // Use deviceId in your API request
        val request = ApiRequest(
            deviceId = deviceId,
            // ... other fields
        )
    }
}
```

### In Activities/Fragments

```kotlin
class MyActivity : ComponentActivity() {
    fun makeApiCall() {
        val deviceId = AppSettings.getDeviceId(this)
        // Use deviceId in your API request
    }
}
```

### Using Constants (requires context)

```kotlin
// This requires passing context
val deviceId = Constants.getDeviceId(context)
```

## API Request Example

Similar to Xamarin's usage in `ScanStartLocationRequestModel`:

```kotlin
data class AuditRequest(
    val zoneId: String,
    val locationId: String,
    val quantity: Int,
    val deviceId: String  // Add device ID field
)

// Usage:
fun submitAudit(context: Context) {
    val request = AuditRequest(
        zoneId = zoneId,
        locationId = locationId,
        quantity = quantity,
        deviceId = AppSettings.getDeviceId(context)  // Get actual device ID
    )
    // Send request to API
}
```

## Comparison with Xamarin

| Xamarin | Android (Kotlin) |
|---------|------------------|
| `Plugin.DeviceInfo.CrossDeviceInfo.Current.Id` | `DeviceUtils.getDeviceId(context)` |
| `Settings.DeviceID` | `AppSettings.getDeviceId(context)` |
| `Settings.DeviceID = currentDeviceId` | `AppSettings.setDeviceId(context, deviceId)` |
| `DependencyService.Get<IDeviceService>().GetInfo()` | `DeviceUtils.getDeviceId(context)` |

## Notes

- Device ID is automatically initialized during app startup/login
- The device ID is stored persistently and reused across app sessions
- If device ID cannot be retrieved, a fallback ID is generated
- Device ID is cleared on logout (if needed, add `AppSettings.clearDeviceId(context)`)
