package com.landmarkgroup.globalaudit.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.landmarkgroup.globalaudit.data.model.SharedAuthData
import com.landmarkgroup.globalaudit.ui.screens.*
import com.landmarkgroup.globalaudit.ui.viewmodel.AuditViewModel
import com.landmarkgroup.globalaudit.ui.viewmodel.LoginViewModel

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object GlobalStockAudit : Screen("global_stock_audit")
    object Profile : Screen("profile")
    object ZoneSelection : Screen("zone_selection")
    object LocationEntry : Screen("location_entry")
    object Summary : Screen("summary")
    object Success : Screen("success")
}

@Composable
fun NavGraph(
    navController: NavHostController,
    auditViewModel: AuditViewModel,
    loginViewModel: LoginViewModel
) {
    // Check auth state on startup
    LaunchedEffect(Unit) {
        val authData = SharedAuthData.getAuthData()
        val isAuthenticated = authData != null &&
                !authData.idTokenKey.isNullOrEmpty() &&
                authData.expiresOnKey > System.currentTimeMillis() &&
                !authData.selectedFacilityKey.isNullOrEmpty()
        
        if (!isAuthenticated) {
            navController.navigate(Screen.Login.route) {
                popUpTo(0) { inclusive = true }
            }
        } else {
            // If authenticated, navigate to main screen
            navController.navigate(Screen.GlobalStockAudit.route) {
                popUpTo(0) { inclusive = true }
            }
        }
    }
    
    NavHost(
        navController = navController,
        startDestination = Screen.Login.route
    ) {
        composable(Screen.Login.route) {
            LoginScreen(
                viewModel = loginViewModel,
                onLoginSuccess = {
                    navController.navigate(Screen.GlobalStockAudit.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }
        
        composable(Screen.GlobalStockAudit.route) {
            GlobalStockAuditScreen(
                onGlobalStockAuditClick = {
                    navController.navigate(Screen.ZoneSelection.route)
                },
                onProfileClick = {
                    navController.navigate(Screen.Profile.route)
                }
            )
        }
        
        composable(Screen.Profile.route) {
            ProfileScreen(
                onBackClick = {
                    navController.popBackStack()
                },
                onLogout = {
                    loginViewModel.logout()
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
        
        composable(Screen.ZoneSelection.route) {
            val isLoading by auditViewModel.isLoading.collectAsState()
            val errorMessage by auditViewModel.scanZoneError.collectAsState()
            val scope = rememberCoroutineScope()
            val toastEvent by auditViewModel.scanZoneToast.collectAsState()
            val context = androidx.compose.ui.platform.LocalContext.current

            // Clear any lingering scan-zone error when entering this screen
            LaunchedEffect(Unit) {
                auditViewModel.clearScanZoneError()
            }

            LaunchedEffect(toastEvent) {
                toastEvent?.let { pair ->
                    val success = pair.first
                    val msg = pair.second
                    val toast = android.widget.Toast.makeText(
                        context,
                        msg,
                        android.widget.Toast.LENGTH_SHORT
                    )
                    // Color the toast background: green for success, red for failure
                    val bgHex = if (success) "#388E3C" else "#D32F2F"
                    toast.view?.background =
                        android.graphics.drawable.ColorDrawable(android.graphics.Color.parseColor(bgHex))
                    val tv = toast.view?.findViewById<android.widget.TextView>(android.R.id.message)
                    tv?.setTextColor(android.graphics.Color.WHITE)
                    toast.setGravity(android.view.Gravity.BOTTOM, 0, 120)
                    toast.show()
                    auditViewModel.clearScanZoneToast()
                }
            }

            ZoneSelectionScreen(
                isLoading = isLoading,
                errorMessage = errorMessage,
                onBackClick = {
                    navController.popBackStack()
                },
                onContinue = { zoneId ->
                    scope.launch {
                        val ok = auditViewModel.scanZone(zoneId)
                        if (ok) {
                            auditViewModel.setZoneId(zoneId)
                            navController.navigate(Screen.LocationEntry.route)
                        }
                    }
                }
            )
        }
        
        composable(Screen.LocationEntry.route) {
            val auditData by auditViewModel.auditData.collectAsState()
            val zoneId by auditViewModel.currentZoneId.collectAsState()
            val locationId by auditViewModel.currentLocationId.collectAsState()
            val quantity by auditViewModel.currentQuantity.collectAsState()
            val binCount = auditData?.bins
                ?.map { it.locationId }
                ?.distinct()
                ?.size ?: 0
            val isLocationValidated by auditViewModel.isLocationValidated.collectAsState()
            val locationToast by auditViewModel.scanLocationToast.collectAsState()
            val addStagingToast by auditViewModel.addStagingToast.collectAsState()
            val scope = rememberCoroutineScope()
            val context = androidx.compose.ui.platform.LocalContext.current
            
            var showCancelDialog by remember { mutableStateOf(false) }

            LaunchedEffect(locationToast) {
                if (!locationToast.isNullOrBlank()) {
                    val toast = android.widget.Toast.makeText(
                        context,
                        locationToast,
                        android.widget.Toast.LENGTH_SHORT
                    )
                    toast.setGravity(android.view.Gravity.BOTTOM, 0, 120)
                    toast.show()
                    auditViewModel.clearScanLocationToast()
                }
            }

            LaunchedEffect(addStagingToast) {
                addStagingToast?.let { pair ->
                    val success = pair.first
                    val msg = pair.second
                    val toast = android.widget.Toast.makeText(
                        context,
                        msg,
                        android.widget.Toast.LENGTH_SHORT
                    )
                    val bgHex = if (success) "#388E3C" else "#D32F2F"
                    toast.view?.background =
                        android.graphics.drawable.ColorDrawable(android.graphics.Color.parseColor(bgHex))
                    val tv = toast.view?.findViewById<android.widget.TextView>(android.R.id.message)
                    tv?.setTextColor(android.graphics.Color.WHITE)
                    toast.setGravity(android.view.Gravity.BOTTOM, 0, 120)
                    toast.show()
                    auditViewModel.clearAddStagingToast()
                }
            }
            
            LocationEntryScreen(
                zoneId = zoneId,
                locationId = locationId,
                quantity = quantity,
                binCount = binCount,
                isLocationValidated = isLocationValidated,
                onLocationIdChange = { auditViewModel.setLocationId(it) },
                onQuantityChange = { auditViewModel.setQuantity(it) },
                onScanLocation = {
                    scope.launch {
                        auditViewModel.scanLocation(context)
                    }
                },
                onAddBin = {
                    scope.launch {
                        val ok = auditViewModel.addStaging(context)
                        if (ok) {
                            auditViewModel.addBin()
                        }
                    }
                },
                onCancel = {
                    showCancelDialog = true
                },
                onSummary = {
                    scope.launch {
                        val ok = auditViewModel.fetchSummary(context)
                        if (ok) {
                            navController.navigate(Screen.Summary.route)
                        }
                    }
                },
                showCancelDialog = showCancelDialog,
                onDismissCancelDialog = { showCancelDialog = false },
                onConfirmCancel = {
                    auditViewModel.launchClearAuditRemote(context, zoneId)
                    auditViewModel.clearAudit()
                    showCancelDialog = false
                    navController.navigate(Screen.ZoneSelection.route) {
                        popUpTo(Screen.ZoneSelection.route) { inclusive = true }
                    }
                }
            )
        }
        
        composable(Screen.Summary.route) {
            val auditDataState by auditViewModel.auditData.collectAsState()
            val auditData = auditDataState
            
            if (auditData != null) {
                val scope = rememberCoroutineScope()
                val context = androidx.compose.ui.platform.LocalContext.current
                var showCancelDialog by remember { mutableStateOf(false) }
                
                SummaryScreen(
                    auditData = auditData,
                    onEdit = {
                        showCancelDialog = true
                    },
                    onConfirm = {
                        scope.launch {
                            val (ok, msg) = auditViewModel.submitAuditRemote(context, auditData.zoneId)
                            val text = if (msg.isNotBlank()) msg else if (ok) "Submitted successfully" else "Submit failed"
                            val toast = android.widget.Toast.makeText(
                                context,
                                text,
                                android.widget.Toast.LENGTH_SHORT
                            )
                            val bgHex = if (ok) "#388E3C" else "#D32F2F"
                            toast.view?.background =
                                android.graphics.drawable.ColorDrawable(android.graphics.Color.parseColor(bgHex))
                            val tv = toast.view?.findViewById<android.widget.TextView>(android.R.id.message)
                            tv?.setTextColor(android.graphics.Color.WHITE)
                            toast.setGravity(android.view.Gravity.BOTTOM, 0, 120)
                            toast.show()
                            if (ok) {
                                navController.navigate(Screen.Success.route) {
                                    popUpTo(Screen.GlobalStockAudit.route) { inclusive = false }
                                }
                            }
                        }
                    },
                    showCancelDialog = showCancelDialog,
                    onDismissCancelDialog = { showCancelDialog = false },
                    onConfirmCancel = {
                        auditViewModel.launchClearAuditRemote(context, auditData.zoneId)
                        auditViewModel.clearAudit()
                        showCancelDialog = false
                        navController.navigate(Screen.ZoneSelection.route) {
                            popUpTo(Screen.ZoneSelection.route) { inclusive = true }
                        }
                    }
                )
            }
        }
        
        composable(Screen.Success.route) {
            val auditDataState by auditViewModel.auditData.collectAsState()
            val auditData = auditDataState
            
            if (auditData != null) {
                SuccessScreen(
                    zoneId = auditData.zoneId,
                    binCount = auditData.bins.map { it.locationId }.distinct().size,
                    onBackToDashboard = {
                        auditViewModel.clearAudit()
                        navController.navigate(Screen.GlobalStockAudit.route) {
                            popUpTo(Screen.GlobalStockAudit.route) { inclusive = true }
                        }
                    }
                )
            }
        }
    }
}
