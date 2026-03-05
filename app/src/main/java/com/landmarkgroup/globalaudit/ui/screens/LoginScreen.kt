package com.landmarkgroup.globalaudit.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.landmarkgroup.globalaudit.R
import com.landmarkgroup.globalaudit.ui.components.LoginWebView
import com.landmarkgroup.globalaudit.ui.utils.safeAreaPadding
import com.landmarkgroup.globalaudit.ui.viewmodel.LoginUiState
import com.landmarkgroup.globalaudit.ui.viewmodel.LoginViewModel

@Composable
fun LoginScreen(
    viewModel: LoginViewModel,
    onLoginSuccess: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState) {
        when (uiState) {
            is LoginUiState.NavigateToMainApp -> {
                onLoginSuccess()
            }
            else -> {
                // Handle other states in UI
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        val currentState = uiState
        when (currentState) {
            is LoginUiState.Idle -> {
                LoginContent(
                    enabled = true,
                    errorMessage = null,
                    onLoginClick = { viewModel.initiateLogin() }
                )
            }
            
            is LoginUiState.Error -> {
                LoginContent(
                    enabled = true,
                    errorMessage = currentState.message,
                    onLoginClick = { viewModel.initiateLogin() }
                )
            }

            is LoginUiState.AdfsLoginInProgress -> {
                LoginContent(
                    enabled = false,
                    onLoginClick = { }
                )
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        CircularProgressIndicator()
                        Text("Redirecting to login page...")
                    }
                }
            }

            is LoginUiState.ShowWebView -> {
                LoginWebView(
                    url = currentState.url,
                    onAuthCodeReceived = { authCode ->
                        viewModel.handleAuthCode(authCode)
                    },
                    onAuthError = { error ->
                        viewModel.handleAuthFlowError(error)
                    }
                )
            }

            is LoginUiState.Authorizing -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        CircularProgressIndicator()
                        Text("Authorizing with server...")
                    }
                }
            }

            is LoginUiState.ShowFacilitySelection -> {
                FacilitySelectionDialog(
                    facilities = currentState.facilities,
                    onFacilitySelected = { facility ->
                        viewModel.onFacilitySelected(facility)
                    }
                )
            }

            is LoginUiState.NavigateToMainApp -> {
                // Navigation handled by LaunchedEffect
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        }
    }
}

@Composable
fun FacilitySelectionDialog(
    facilities: List<String>,
    onFacilitySelected: (String) -> Unit
) {
    AlertDialog(
        onDismissRequest = { /* Users cannot dismiss this */ },
        title = { Text("Select Facility") },
        text = {
            LazyColumn {
                items(facilities) { facility ->
                    Text(
                        text = facility,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onFacilitySelected(facility) }
                            .padding(16.dp)
                    )
                }
            }
        },
        confirmButton = { /* No confirm button, selection is immediate */ }
    )
}

@Composable
private fun LoginContent(
    enabled: Boolean,
    errorMessage: String? = null,
    onLoginClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
            .safeAreaPadding()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Spacer(modifier = Modifier.height(48.dp))
        
        // Logo Icon at the top
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFF4A90E2)),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.checklist),
                contentDescription = "Global Stock Audit Logo",
                modifier = Modifier.size(64.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Title
        Text(
            text = "Global Stock Audit",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF333333)
        )
        
        // Subtitle
        Text(
            text = "Warehouse Operations",
            fontSize = 16.sp,
            color = Color(0xFF666666)
        )
        
        Spacer(modifier = Modifier.weight(1f))
        
        // Login Card
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Color.White)
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Heading
            Text(
                text = "Sign in with AD",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF333333)
            )
            
            // Instructions
            Text(
                text = "Enter your credentials to access the system",
                fontSize = 14.sp,
                color = Color(0xFF666666)
            )
            
            // Error message
            errorMessage?.let { error ->
                Text(
                    text = error,
                    fontSize = 14.sp,
                    color = Color(0xFFD32F2F),
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Login Button
            Button(
                onClick = onLoginClick,
                enabled = enabled,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF4A90E2)
                )
            ) {
                Text(
                    text = "LOGIN",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Support Link
            Text(
                text = "Trouble signing in? Contact IT Support",
                fontSize = 14.sp,
                color = Color(0xFF4A90E2),
                modifier = Modifier.clickable { /* Handle support click */ }
            )
        }
        
        Spacer(modifier = Modifier.weight(1f))
    }
}
