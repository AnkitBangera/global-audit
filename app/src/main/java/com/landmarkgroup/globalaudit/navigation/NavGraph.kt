package com.landmarkgroup.globalaudit.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.landmarkgroup.globalaudit.ui.screens.*
import com.landmarkgroup.globalaudit.ui.viewmodel.AuditViewModel

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object GlobalStockAudit : Screen("global_stock_audit")
    object ZoneSelection : Screen("zone_selection")
    object LocationEntry : Screen("location_entry")
    object Summary : Screen("summary")
    object Success : Screen("success")
}

@Composable
fun NavGraph(
    navController: NavHostController,
    viewModel: AuditViewModel
) {
    // Ensure we start at Login screen on initial composition
    // This handles cases where saved state might restore to a different screen
    LaunchedEffect(Unit) {
        val backStackEntry = navController.currentBackStackEntry
        val currentRoute = backStackEntry?.destination?.route
        // Only navigate if we have a back stack entry and it's not Login
        // If back stack is empty, NavHost will use startDestination
        if (backStackEntry != null && currentRoute != Screen.Login.route) {
            navController.navigate(Screen.Login.route) {
                // Clear entire back stack and navigate to Login
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
                onLoginClick = {
                    navController.navigate(Screen.GlobalStockAudit.route)
                }
            )
        }
        
        composable(Screen.GlobalStockAudit.route) {
            GlobalStockAuditScreen(
                onGlobalStockAuditClick = {
                    navController.navigate(Screen.ZoneSelection.route)
                }
            )
        }
        
        composable(Screen.ZoneSelection.route) {
            ZoneSelectionScreen(
                onBackClick = {
                    navController.popBackStack()
                },
                onZoneSelected = { zoneId ->
                    viewModel.setZoneId(zoneId)
                    navController.navigate(Screen.LocationEntry.route)
                }
            )
        }
        
        composable(Screen.LocationEntry.route) {
            val auditData by viewModel.auditData.collectAsState()
            val zoneId by viewModel.currentZoneId.collectAsState()
            val locationId by viewModel.currentLocationId.collectAsState()
            val quantity by viewModel.currentQuantity.collectAsState()
            val binCount = auditData?.bins?.size ?: 0
            
            var showCancelDialog by remember { mutableStateOf(false) }
            
            LocationEntryScreen(
                zoneId = zoneId,
                locationId = locationId,
                quantity = quantity,
                binCount = binCount,
                onLocationIdChange = { viewModel.setLocationId(it) },
                onQuantityChange = { viewModel.setQuantity(it) },
                onAddBin = {
                    viewModel.addBin()
                },
                onCancel = {
                    showCancelDialog = true
                },
                onSummary = {
                    if (binCount > 0) {
                        navController.navigate(Screen.Summary.route)
                    }
                },
                showCancelDialog = showCancelDialog,
                onDismissCancelDialog = { showCancelDialog = false },
                onConfirmCancel = {
                    viewModel.clearAudit()
                    showCancelDialog = false
                    navController.navigate(Screen.ZoneSelection.route) {
                        popUpTo(Screen.ZoneSelection.route) { inclusive = true }
                    }
                }
            )
        }
        
        composable(Screen.Summary.route) {
            val auditDataState by viewModel.auditData.collectAsState()
            val auditData = auditDataState
            
            if (auditData != null) {
                var showCancelDialog by remember { mutableStateOf(false) }
                
                SummaryScreen(
                    auditData = auditData,
                    onEdit = {
                        showCancelDialog = true
                    },
                    onConfirm = {
                        viewModel.submitAudit()
                        navController.navigate(Screen.Success.route) {
                            popUpTo(Screen.GlobalStockAudit.route) { inclusive = false }
                        }
                    },
                    showCancelDialog = showCancelDialog,
                    onDismissCancelDialog = { showCancelDialog = false },
                    onConfirmCancel = {
                        viewModel.clearAudit()
                        showCancelDialog = false
                        navController.navigate(Screen.ZoneSelection.route) {
                            popUpTo(Screen.ZoneSelection.route) { inclusive = true }
                        }
                    }
                )
            }
        }
        
        composable(Screen.Success.route) {
            val auditDataState by viewModel.auditData.collectAsState()
            val auditData = auditDataState
            
            if (auditData != null) {
                SuccessScreen(
                    zoneId = auditData.zoneId,
                    binCount = auditData.bins.size,
                    onBackToDashboard = {
                        viewModel.clearAudit()
                        navController.navigate(Screen.GlobalStockAudit.route) {
                            popUpTo(Screen.GlobalStockAudit.route) { inclusive = true }
                        }
                    }
                )
            }
        }
    }
}
