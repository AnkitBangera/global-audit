package com.landmarkgroup.globalaudit.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.outlined.CropFree
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.landmarkgroup.globalaudit.ui.utils.safeAreaPadding
import androidx.compose.ui.platform.LocalContext
import com.landmarkgroup.globalaudit.MainActivity

@Composable
fun ZoneSelectionScreen(
    isLoading: Boolean,
    errorMessage: String?,
    onBackClick: () -> Unit,
    onContinue: (String) -> Unit
) {
    var zoneId by remember { mutableStateOf("") }
    val context = LocalContext.current
    DisposableEffect(Unit) {
        val owner = "ZoneSelectionScreen"
        if (context is MainActivity) {
            MainActivity.setScanResultCallback({ barcode ->
                val scanned = barcode.trim()
                zoneId = scanned
                if (scanned.isNotBlank() && !isLoading) {
                    onContinue(scanned)
                }
            }, owner)
        }
        onDispose {
            if (context is MainActivity) {
                MainActivity.clearScanResultCallback(owner)
            }
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color(0xFFEFF2FF), Color.White)))
            .safeAreaPadding()
            .padding(24.dp)
    ) {
        // Back button
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Back",
                modifier = Modifier
                    .size(24.dp)
                    .clickable { onBackClick() },
                tint = Color(0xFF333333)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Header
        Text(
            text = "Select Zone",
            fontSize = 30.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF5B4AF7)
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = buildAnnotatedString {
                append("Scan or enter your assigned ")
                withStyle(SpanStyle(color = Color(0xFF5B4AF7), fontWeight = FontWeight.SemiBold)) {
                    append("zone ID")
                }
                append(" to begin the audit session.")
            },
            fontSize = 14.sp,
            color = Color(0xFF666666)
        )

        Spacer(modifier = Modifier.height(24.dp))
        
        // Input Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                Text(
                    text = "Scan/Enter Zone ID",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF333333)
                )
                
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "ZONE IDENTIFIER",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF5B4AF7),
                        letterSpacing = 1.0.sp
                    )
                    
                    OutlinedTextField(
                        value = zoneId,
                        onValueChange = { zoneId = it.replace(" ", "") },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("ZONE ID", color = Color(0xFF999999)) },
                        shape = RoundedCornerShape(8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF4A90E2),
                            unfocusedBorderColor = Color(0xFFE0E0E0)
                        ),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Text,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                if (zoneId.isNotBlank()) {
                                    onContinue(zoneId.trim())
                                }
                            }
                        ),
                        trailingIcon = {
                            Icon(
                                imageVector = Icons.Outlined.CropFree,
                                contentDescription = "Scan",
                                modifier = Modifier.size(22.dp),
                                tint = Color(0xFF666666)
                            )
                        },
                        singleLine = true
                    )
                }
                
                // Continue Button
                if (errorMessage != null) {
                    Text(
                        text = errorMessage ?: "",
                        color = Color(0xFFD32F2F),
                        fontSize = 14.sp
                    )
                }
                Button(
                    onClick = {
                        if (zoneId.isNotBlank()) {
                            onContinue(zoneId.trim())
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(28.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF5B4AF7)
                    ),
                    enabled = zoneId.isNotBlank() && !isLoading
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(color = Color.White, strokeWidth = 2.dp)
                    } else {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Continue",
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "→",
                                color = Color.White,
                                fontSize = 20.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
