package com.landmarkgroup.globalaudit

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.landmarkgroup.globalaudit.data.model.SharedAuthData
import com.landmarkgroup.globalaudit.navigation.NavGraph
import com.landmarkgroup.globalaudit.ui.theme.GlobalAuditTheme
import com.landmarkgroup.globalaudit.ui.viewmodel.AuditViewModel
import com.landmarkgroup.globalaudit.ui.viewmodel.LoginViewModel
import com.landmarkgroup.globalaudit.utils.AuthConstants
import androidx.lifecycle.ViewModelProvider

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Handle redirect URI from ADFS
        handleAuthRedirect(intent)
        
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
}
