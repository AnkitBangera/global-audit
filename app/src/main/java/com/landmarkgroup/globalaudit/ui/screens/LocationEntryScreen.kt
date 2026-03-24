package com.landmarkgroup.globalaudit.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import com.landmarkgroup.globalaudit.ui.utils.safeAreaPadding
import androidx.compose.ui.platform.LocalContext
import com.landmarkgroup.globalaudit.MainActivity
import androidx.compose.foundation.BorderStroke

@Composable
fun LocationEntryScreen(
    zoneId: String,
    locationId: String,
    quantity: String,
    binCount: Int,
    isLocationValidated: Boolean,
    onLocationIdChange: (String) -> Unit,
    onQuantityChange: (String) -> Unit,
    onScanLocation: () -> Unit,
    onAddBin: () -> Unit,
    onCancel: () -> Unit,
    onSummary: () -> Unit,
    showCancelDialog: Boolean,
    onDismissCancelDialog: () -> Unit,
    onConfirmCancel: () -> Unit
) {
    // Location is only "valid" once scan-location returns returnCode=Y.
    val isLocationValid = isLocationValidated

    // Register hardware scanner callback (Zebra/Honeywell). Trim scanned data and auto-validate.
    val context = LocalContext.current
    DisposableEffect(Unit) {
        val owner = "LocationEntryScreen"
        if (context is MainActivity) {
            MainActivity.setScanResultCallback({ barcode ->
                val scanned = barcode.trim()
                if (scanned.isNotBlank()) {
                    onLocationIdChange(scanned)
                    onScanLocation()
                }
            }, owner)
        }
        onDispose {
            if (context is MainActivity) {
                MainActivity.clearScanResultCallback(owner)
            }
        }
    }
    
    // Cancel Confirmation Dialog
    if (showCancelDialog) {
        CancelConfirmationDialog(
            onDismiss = onDismissCancelDialog,
            onConfirm = onConfirmCancel
        )
    }
    
    val violet = Color(0xFF5B4AF7)
    val pageBgTop = Color(0xFFEFF2FF)
    val pageBgBottom = Color.White
    val labelGray = Color(0xFF9CA3AF)
    val textDark = Color(0xFF111827)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(pageBgTop, pageBgBottom)))
            .safeAreaPadding()
            .padding(24.dp)
    ) {
        // Header (ZONE + zoneId with accent bar + subtitle)
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(56.dp)
                    .background(violet, RoundedCornerShape(2.dp))
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = "ZONE",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = violet,
                    letterSpacing = 1.0.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = zoneId,
                    fontSize = 30.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = textDark
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Scan or enter location details",
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
                // Location ID Field
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "LOCATION ID",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = labelGray,
                        letterSpacing = 1.0.sp
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = locationId,
                            onValueChange = { onLocationIdChange(it.replace(" ", "")) },
                            modifier = Modifier
                                .weight(1f)
                                .onPreviewKeyEvent { event ->
                                    if (event.type == KeyEventType.KeyDown && event.nativeKeyEvent.keyCode == android.view.KeyEvent.KEYCODE_ENTER) {
                                        if (locationId.isNotBlank()) {
                                            onScanLocation()
                                        }
                                        true
                                    } else {
                                        false
                                    }
                                },
                            placeholder = { Text("Scan location...", color = Color(0xFFB0B7C3)) },
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = violet,
                                unfocusedBorderColor = if (isLocationValid) violet else Color(0xFFE0E0E0)
                            ),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Text,
                                imeAction = ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = {
                                    if (locationId.isNotBlank()) {
                                        onScanLocation()
                                    }
                                }
                            ),
                            singleLine = true,
                            trailingIcon = {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    if (isLocationValid) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = "Valid",
                                            tint = violet,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                    if (locationId.isNotBlank()) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Clear",
                                            modifier = Modifier
                                                .size(20.dp)
                                                .clickable { onLocationIdChange("") },
                                            tint = Color(0xFF666666)
                                        )
                                    }
                                }
                            }
                        )
                    }
                }
                
                AnimatedVisibility(
                    visible = isLocationValidated,
                    enter = expandVertically(expandFrom = Alignment.Top) + fadeIn(),
                    exit = shrinkVertically(shrinkTowards = Alignment.Top) + fadeOut()
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(24.dp)
                    ) {
                        // Quantity Field
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "QUANTITY",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = labelGray,
                                letterSpacing = 1.0.sp
                            )

                            OutlinedTextField(
                                value = quantity,
                                onValueChange = { newValue ->
                                    if (newValue.isEmpty() || newValue.all { it.isDigit() }) {
                                        onQuantityChange(newValue)
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                placeholder = { Text("0", color = Color(0xFFB0B7C3)) },
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = violet,
                                    unfocusedBorderColor = Color(0xFFE0E0E0)
                                ),
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Number,
                                    imeAction = ImeAction.Done
                                ),
                                keyboardActions = KeyboardActions(
                                    onDone = {
                                        if (locationId.isNotBlank() && quantity.isNotBlank()) {
                                            onAddBin()
                                        }
                                    }
                                ),
                                singleLine = true,
                                trailingIcon = {
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        IconButton(
                                            onClick = {
                                                val currentQty = quantity.toIntOrNull() ?: 0
                                                if (currentQty > 0) {
                                                    onQuantityChange((currentQty - 1).toString())
                                                }
                                            },
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.KeyboardArrowDown,
                                                contentDescription = "Decrease",
                                                tint = Color(0xFF666666),
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                        IconButton(
                                            onClick = {
                                                val currentQty = quantity.toIntOrNull() ?: 0
                                                onQuantityChange((currentQty + 1).toString())
                                            },
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.KeyboardArrowUp,
                                                contentDescription = "Increase",
                                                tint = Color(0xFF666666),
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                }
                            )
                        }

                        // Add Bin Button changes color when quantity is entered
                        val canAddBin = locationId.isNotBlank() && quantity.isNotBlank() && (quantity.toIntOrNull() ?: 0) > 0
                        if (canAddBin) {
                            Button(
                                onClick = onAddBin,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = violet)
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Text(
                                        text = "Add Bin (${binCount})",
                                        color = Color.White,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        } else {
                            OutlinedButton(
                                onClick = {},
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = Color(0xFF8A94A6)
                                ),
                                border = BorderStroke(1.dp, Color(0xFFDDE3EA)),
                                enabled = false
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = null,
                                        tint = Color(0xFF8A94A6),
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Text(
                                        text = "Add Bin (${binCount})",
                                        color = Color(0xFF8A94A6),
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.weight(1f))
        
        // Bottom Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedButton(
                onClick = onCancel,
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = Color(0xFF333333)
                ),
                border = ButtonDefaults.outlinedButtonBorder.copy(
                    width = 1.dp
                )
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = null,
                        tint = Color(0xFF333333),
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "Cancel",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            
            Button(
                onClick = onSummary,
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = violet
                ),
                enabled = binCount > 0
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Summary",
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
