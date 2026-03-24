package com.landmarkgroup.globalaudit.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.landmarkgroup.globalaudit.data.model.AuditData
import com.landmarkgroup.globalaudit.ui.utils.safeAreaPadding

@Composable
fun SummaryScreen(
    auditData: AuditData,
    onEdit: () -> Unit,
    onConfirm: () -> Unit,
    showCancelDialog: Boolean,
    onDismissCancelDialog: () -> Unit,
    onConfirmCancel: () -> Unit
) {
    // Cancel Confirmation Dialog
    if (showCancelDialog) {
        CancelConfirmationDialog(
            onDismiss = onDismissCancelDialog,
            onConfirm = onConfirmCancel
        )
    }

    // Colors close to the reference screenshot
    val primaryBlue = Color(0xFF2563EB)       // Blue for header and confirm
    val primaryBlueDark = Color(0xFF1E3A8A)
    val chipBlueBg = Color(0xFFE8F0FF)
    val chipBlueText = Color(0xFF1E3A8A)
    val chipGreenBg = Color(0xFFE8F5E9)
    val chipGreenText = Color(0xFF2E7D32)
    val chipOrangeBg = Color(0xFFFFF3E0)
    val chipOrangeText = Color(0xFFEF6C00)
    val pageBg = Color(0xFFF5F7FB)
    val cardBorder = Color(0xFFE5E7EB)
    val labelGray = Color(0xFF9CA3AF)
    val textDark = Color(0xFF1F2937)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(pageBg)
            .safeAreaPadding()
    ) {
        // Header with rounded bottom and gradient
        val headerShape = RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(headerShape)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(primaryBlueDark, primaryBlue)
                    )
                )
                .padding(vertical = 20.dp)
        ) {
            Text(
                text = "Confirm Audit",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.align(Alignment.Center)
            )
        }

        // Content
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp)
        ) {
            // Top chips row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatChip(
                    label = "ZONE",
                    value = auditData.zoneId,
                    icon = Icons.Default.Layers,
                    iconTint = chipBlueText,
                    valueColor = chipBlueText,
                    containerColor = chipBlueBg,
                    modifier = Modifier.weight(1f)
                )
                StatChip(
                    label = "LOCS",
                    value = auditData.locationCount.toString(),
                    icon = Icons.Default.LocationOn,
                    iconTint = chipGreenText,
                    valueColor = chipGreenText,
                    containerColor = chipGreenBg,
                    modifier = Modifier.weight(1f)
                )
                StatChip(
                    label = "TOTAL",
                    value = auditData.totalQuantity.toString(),
                    icon = Icons.Default.Layers,
                    iconTint = chipOrangeText,
                    valueColor = chipOrangeText,
                    containerColor = chipOrangeBg,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Inventory list header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "INVENTORY LIST",
                    fontSize = 12.sp,
                    letterSpacing = 1.2.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = labelGray
                )
                Surface(
                    color = chipBlueBg,
                    contentColor = chipBlueText,
                    shape = RoundedCornerShape(16.dp),
                    border = ButtonDefaults.outlinedButtonBorder
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Layers,
                            contentDescription = null,
                            tint = chipBlueText,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "${auditData.bins.size} ITEMS",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = chipBlueText
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // List container
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .verticalScroll(rememberScrollState())
                            .fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        auditData.bins.forEach { bin ->
                            LocationSummaryCard(
                                locationId = bin.locationId,
                                quantity = bin.quantity,
                                labelGray = labelGray,
                                textDark = textDark,
                                primaryBlue = primaryBlue,
                                cardBorder = cardBorder
                            )
                        }
                    }
                }
            }
        }

        // Bottom action bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(pageBg)
                .padding(horizontal = 16.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onEdit,
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = textDark
                ),
                border = ButtonDefaults.outlinedButtonBorder
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = null,
                        tint = textDark,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "EDIT",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Button(
                onClick = onConfirm,
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = primaryBlue
                )
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "CONFIRM",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
private fun StatChip(
    label: String,
    value: String,
    icon: ImageVector,
    iconTint: Color,
    valueColor: Color,
    containerColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.height(74.dp),
        shape = RoundedCornerShape(16.dp),
        color = containerColor,
        shadowElevation = 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color.White.copy(alpha = 0.8f),
                tonalElevation = 0.dp,
                shadowElevation = 0.dp
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier
                        .size(28.dp)
                        .padding(6.dp)
                )
            }
            Column(
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = label,
                    fontSize = 10.sp,
                    letterSpacing = 1.0.sp,
                    fontWeight = FontWeight.Medium,
                    color = valueColor.copy(alpha = 0.8f)
                )
                Text(
                    text = value,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = valueColor
                )
            }
        }
    }
}

@Composable
private fun LocationSummaryCard(
    locationId: String,
    quantity: Int,
    labelGray: Color,
    textDark: Color,
    primaryBlue: Color,
    cardBorder: Color
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = Color.White,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
        modifier = Modifier
            .fillMaxWidth()
            .border(width = 1.dp, color = cardBorder, shape = RoundedCornerShape(14.dp))
            .padding(horizontal = 14.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "LOCATION ID",
                    fontSize = 11.sp,
                    letterSpacing = 1.0.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = labelGray
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = locationId,
                    fontSize = 15.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.SemiBold,
                    color = textDark
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "QUANTITY",
                    fontSize = 11.sp,
                    letterSpacing = 1.0.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = labelGray
                )
                Spacer(modifier = Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = quantity.toString(),
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = primaryBlue
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "PCS",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = labelGray
                    )
                }
            }
        }
    }
}
