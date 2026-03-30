package com.landmarkgroup.globalaudit

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.landmarkgroup.globalaudit.data.model.SharedAuthData
import com.landmarkgroup.globalaudit.navigation.NavGraph
import com.landmarkgroup.globalaudit.ui.theme.GlobalAuditTheme
import com.landmarkgroup.globalaudit.ui.viewmodel.AuditViewModel
import com.landmarkgroup.globalaudit.ui.viewmodel.LoginViewModel
import com.landmarkgroup.globalaudit.utils.AuthConstants
import android.os.Bundle as OsBundle
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.analytics.logEvent
import com.google.firebase.ktx.Firebase
import com.landmarkgroup.globalaudit.BuildConfig

class MainActivity : ComponentActivity() {

    companion object {
        private var scanResultCallback: ((String) -> Unit)? = null
        private var callbackOwner: String? = null

        fun setScanResultCallback(callback: (String) -> Unit, owner: String = "unknown") {
            scanResultCallback = callback
            callbackOwner = owner
            Log.d("MainActivity", "Scan callback set by: $owner")
        }

        fun clearScanResultCallback(owner: String = "unknown") {
            if (callbackOwner == null || callbackOwner == owner || owner == "force") {
                scanResultCallback = null
                callbackOwner = null
                Log.d("MainActivity", "Scan callback cleared by: $owner")
            } else {
                Log.d(
                    "MainActivity",
                    "Scan callback clear ignored - owner mismatch. Current: $callbackOwner, Requested: $owner"
                )
            }
        }

        fun hasScanResultCallback(): Boolean = scanResultCallback != null
    }

    private var zebraReceiverRegistered = false
    private var honeywellReceiverRegistered = false

    private val dataWedgeResultReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            var resultInfo = ""

            // Zebra DataWedge data (direct extra)
            if (intent.hasExtra("com.symbol.datawedge.data_string")) {
                resultInfo = intent.getStringExtra("com.symbol.datawedge.data_string")?.trim() ?: ""
            } else {
                when (intent.action) {
                    // Zebra DataWedge broadcast result
                    "com.symbol.datawedge.api.RESULT_ACTION" -> {
                        if (intent.hasExtra("com.symbol.datawedge.data_string")) {
                            resultInfo =
                                (intent.getStringExtra("com.symbol.datawedge.data_string") ?: "").trim()
                            Log.d("MainActivity", "Zebra DataWedge scan: $resultInfo")
                        }
                    }

                    // Honeywell (primary action for our app package)
                    "com.landmarkgroup.globalaudit.HONEYWELL_BARCODE_DATA" -> {
                        // Default version to 1 if not provided by firmware
                        val version = intent.getIntExtra("version", 1)
                        // Try multiple extras to be robust across firmware variants
                        val dataRaw = intent.getStringExtra("data")
                            ?: intent.getStringExtra("com.honeywell.aidc.extra.BARCODE_DATA")
                            ?: intent.getStringExtra("barcode_data")
                            ?: ""
                        val aimId = intent.getStringExtra("aimId") ?: ""
                        val codeId = intent.getStringExtra("codeId") ?: ""
                        val charset = intent.getStringExtra("charset") ?: ""
                        val timestamp = intent.getStringExtra("timestamp") ?: ""
                        resultInfo = dataRaw.trim()
                        Log.d(
                            "MainActivity",
                            "Honeywell scan - Data: $dataRaw, AimId: $aimId, CodeId: $codeId, Charset: $charset, Timestamp: $timestamp, Version: $version"
                        )
                    }

                    // Honeywell AIDC variants
                    "com.honeywell.aidc.action.ACTION_BARCODE_DATA",
                    "com.honeywell.aidc.action.ACTION_BARCODE" -> {
                        resultInfo = when {
                            intent.hasExtra("com.honeywell.aidc.extra.BARCODE_DATA") ->
                                intent.getStringExtra("com.honeywell.aidc.extra.BARCODE_DATA")
                                    ?.trim() ?: ""
                            intent.hasExtra("data") ->
                                intent.getStringExtra("data")?.trim() ?: ""
                            else -> ""
                        }
                        Log.d("MainActivity", "Honeywell AIDC scan: $resultInfo")
                    }

                    // Honeywell decode
                    "com.honeywell.decode.intent.action.SCAN_RESULT" -> {
                        resultInfo =
                            intent.getStringExtra("com.honeywell.decode.intent.extra.BARCODE_DATA")
                                ?.trim() ?: ""
                        Log.d("MainActivity", "Honeywell decode scan: $resultInfo")
                    }

                    // Generic fallback
                    "android.intent.action.DECODE_DATA" -> {
                        resultInfo =
                            intent.getStringExtra("android.intent.extra.TEXT")?.trim() ?: ""
                        Log.d("MainActivity", "Generic decode scan: $resultInfo")
                    }
                }
            }

            if (resultInfo.isNotBlank() && scanResultCallback != null) {
                scanResultCallback?.invoke(resultInfo)
            } else {
                Log.d(
                    "MainActivity",
                    "Scan result received but callback is null or empty. Value: $resultInfo"
                )
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (BuildConfig.DEBUG) {
            Firebase.analytics.logEvent("debug_app_open") {
                param("source", "MainActivity")
            }
        }

        // Handle redirect URI from ADFS
        handleAuthRedirect(intent)

        // If not authenticated, ensure Login flow is triggered via NavGraph; we keep the old behavior
        val idToken = SharedAuthData.getAuthData()?.idTokenKey
        if (idToken == null) {
            Log.d("MainActivity", "IdToken missing at startup - login will be required via NavGraph")
        }

        // Configure scanner by device type
        val manufacturer = Build.MANUFACTURER.lowercase()
        if (manufacturer.contains("zebra")) {
            configZebraScannerAndReceiver()
        } else if (manufacturer.contains("honeywell")) {
            configureHoneywellScanner()
            configHoneywellScannerReceiver()
        }

        enableEdgeToEdge()
        setContent {
            GlobalAuditTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    val auditViewModel: AuditViewModel = viewModel()
                    val loginViewModel: LoginViewModel = viewModel(
                        factory = object : ViewModelProvider.Factory {
                            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                                @Suppress("UNCHECKED_CAST")
                                return LoginViewModel(this@MainActivity.application) as T
                            }
                        }
                    )
                    NavGraph(
                        navController = navController,
                        auditViewModel = auditViewModel,
                        loginViewModel = loginViewModel
                    )
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleAuthRedirect(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            if (zebraReceiverRegistered || honeywellReceiverRegistered) {
                unregisterReceiver(dataWedgeResultReceiver)
                zebraReceiverRegistered = false
                honeywellReceiverRegistered = false
            }
        } catch (e: Exception) {
            Log.e("MainActivity", "Failed to unregister scanner receiver: ${e.message}")
        }
        releaseHoneywellScanner()
    }

    private fun handleAuthRedirect(intent: Intent?) {
        val data: Uri? = intent?.data
        if (data != null) {
            Log.d("MainActivity", "Received intent with data: $data")
            if (data.scheme == AuthConstants.REDIRECT_URI.scheme &&
                data.host == AuthConstants.REDIRECT_URI.host
            ) {
                val authCode = data.getQueryParameter("code")
                val error = data.getQueryParameter("error")
                val errorDescription = data.getQueryParameter("error_description")

                if (authCode != null) {
                    Log.d("MainActivity", "Auth code received: $authCode")
                    // The LoginViewModel will handle this through the NavGraph
                } else if (error != null) {
                    Log.e("MainActivity", "Auth error: $error - $errorDescription")
                }
            }
        }
    }

    // Zebra setup
    private fun configZebraScannerAndReceiver() {
        configureZebraDataWedgeProfile()

        val filter = IntentFilter().apply {
            // Zebra DataWedge intents
            addAction("com.symbol.datawedge.api.RESULT_ACTION")
            addAction("com.symbol.datawedge.scanner_status")
            addAction("com.symbol.datawedge.api.ACTION")

            // Generic barcode scan intent (fallback)
            addAction("android.intent.action.DECODE_DATA")
        }
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                registerReceiver(dataWedgeResultReceiver, filter, Context.RECEIVER_EXPORTED)
            } else {
                @Suppress("DEPRECATION")
                registerReceiver(dataWedgeResultReceiver, filter)
            }
            zebraReceiverRegistered = true
            Log.d("MainActivity", "Zebra receiver registered")
        } catch (e: Exception) {
            Log.e("MainActivity", "Failed to register Zebra receiver: ${e.message}")
        }
    }

    private fun configureZebraDataWedgeProfile() {
        val profileName = "GlobalAuditProfile"
        val packageName = packageName

        // Create profile
        val createProfileIntent = Intent().apply {
            action = "com.symbol.datawedge.api.ACTION"
            putExtra("com.symbol.datawedge.api.CREATE_PROFILE", profileName)
            putExtra("SEND_RESULT", "COMPLETE_RESULT")
            putExtra("COMMAND_IDENTIFIER", "CREATE_PROFILE")
        }
        sendBroadcast(createProfileIntent)

        // Configure profile
        val configBundle = OsBundle().apply {
            putString("PROFILE_NAME", profileName)
            putString("PROFILE_ENABLED", "true")
            putString("CONFIG_MODE", "UPDATE")

            // App association
            val appConfig = OsBundle().apply {
                putString("PACKAGE_NAME", packageName)
                putStringArray("ACTIVITY_LIST", arrayOf("*"))
            }
            putParcelableArray("APP_LIST", arrayOf(appConfig))

            // Intent output configuration
            val intentConfig = OsBundle().apply {
                putString("ENABLED", "true")
                putString("intent_output_enabled", "true")
                putString("intent_action", "com.symbol.datawedge.api.RESULT_ACTION")
                putString("intent_delivery", "2") // Broadcast
                putString("intent_category", "android.intent.category.DEFAULT")
            }
            putBundle("INTENT", intentConfig)

            // Barcode input configuration
            val barcodeConfig = OsBundle().apply {
                putString("ENABLED", "true")
                putString("scanner_selection", "auto")
                putString("scanner_input_enabled", "true")
                putString("aim_type", "trigger")
                putString("beam_timer", "1500")
                putString("Laser_on_time", "1500")
                putString("linear_security_level", "1")
                putString("picklist", "disabled")
                putString("aim_mode", "on")
                putString("aim_timer", "1000")
                putString("same_barcode_timeout", "500")
                putString("different_barcode_timeout", "500")
            }
            putBundle("BARCODE", barcodeConfig)

            // Keystroke output (optional)
            val keystrokeConfig = OsBundle().apply {
                putString("ENABLED", "true")
                putString("keystroke_output_enabled", "true")
                putString("keystroke_action_char", "9") // Tab
                putString("keystroke_delay_extended_ascii", "0")
                putString("keystroke_delay_control_chars", "0")
            }
            putBundle("KEYSTROKE", keystrokeConfig)
        }

        val setConfigIntent = Intent().apply {
            action = "com.symbol.datawedge.api.ACTION"
            putExtra("com.symbol.datawedge.api.SET_CONFIG", configBundle)
            putExtra("SEND_RESULT", "COMPLETE_RESULT")
            putExtra("COMMAND_IDENTIFIER", "SET_CONFIG")
        }
        sendBroadcast(setConfigIntent)

        Log.d("MainActivity", "Zebra DataWedge profile configured: $profileName")
    }

    // Honeywell setup
    private fun configureHoneywellScanner() {
        try {
            val properties = OsBundle().apply {
                // Ensure barcode data is delivered via broadcast intent
                putBoolean("DPR_DATA_INTENT", true)
                putString("DPR_DATA_INTENT_ACTION", "com.landmarkgroup.globalaudit.HONEYWELL_BARCODE_DATA")

                // Attempt to raise/override max length for common symbologies to avoid 26-char truncation
                // (Unsupported keys are ignored by the scanner service)
                putString("DEC_CODE39_MAX_LENGTH", "100")
                putString("DEC_CODE128_MAX_LENGTH", "100")
                putString("DEC_QR_MAX_LENGTH", "100")
                putString("DEC_PDF417_MAX_LENGTH", "300")
                putString("DEC_DATAMATRIX_MAX_LENGTH", "300")
                putString("DEC_EAN13_MAX_LENGTH", "50")
                putString("DEC_EAN8_MAX_LENGTH", "50")
                putString("DEC_UPCA_MAX_LENGTH", "50")
                putString("DEC_UPCE0_MAX_LENGTH", "50")
                putString("DEC_UPCE1_MAX_LENGTH", "50")
                putString("DEC_INTERLEAVED_2OF5_MAX_LENGTH", "100")
            }

            val claimIntent = Intent().apply {
                action = "com.honeywell.aidc.action.ACTION_CLAIM_SCANNER"
                // Keep default imager; if device does not support this identifier it will be ignored
                putExtra("com.honeywell.aidc.extra.EXTRA_SCANNER", "dcs.scanner.imager")
                putExtra("com.honeywell.aidc.extra.EXTRA_PROFILE", "DEFAULT")
                putExtra("com.honeywell.aidc.extra.EXTRA_PROPERTIES", properties)
            }

            sendBroadcast(claimIntent)
            Log.d("MainActivity", "Honeywell scanner claim intent sent with extended properties for max-length overrides")
        } catch (e: Exception) {
            Log.e("MainActivity", "Failed to configure Honeywell scanner: ${e.message}")
        }
    }

    private fun configHoneywellScannerReceiver() {
        val filter = IntentFilter().apply {
            // Honeywell intents
            addAction("com.landmarkgroup.globalaudit.HONEYWELL_BARCODE_DATA")
            addAction("com.honeywell.aidc.action.ACTION_BARCODE_DATA")
            addAction("com.honeywell.aidc.action.ACTION_BARCODE")
            addAction("com.honeywell.decode.intent.action.SCAN_RESULT")
            addAction("com.honeywell.aidc.action.ACTION_SCAN_RESULT")
            addAction("com.honeywell.aidc.action.ACTION_SCANNER_STATUS")

            // Generic fallback
            addAction("android.intent.action.DECODE_DATA")
        }
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                registerReceiver(dataWedgeResultReceiver, filter, Context.RECEIVER_EXPORTED)
            } else {
                @Suppress("DEPRECATION")
                registerReceiver(dataWedgeResultReceiver, filter)
            }
            honeywellReceiverRegistered = true
            Log.d("MainActivity", "Honeywell receiver registered")
        } catch (e: Exception) {
            Log.e("MainActivity", "Failed to register Honeywell receiver: ${e.message}")
        }
    }

    private fun releaseHoneywellScanner() {
        try {
            val releaseIntent = Intent().apply {
                action = "com.honeywell.aidc.action.ACTION_RELEASE_SCANNER"
            }
            sendBroadcast(releaseIntent)
            Log.d("MainActivity", "Honeywell scanner release intent sent")
        } catch (e: Exception) {
            Log.e("MainActivity", "Failed to release Honeywell scanner: ${e.message}")
        }
    }
}
