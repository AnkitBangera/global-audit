package com.landmarkgroup.globalaudit.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import com.landmarkgroup.globalaudit.ui.utils.safeAreaPadding

@Composable
fun SuccessScreen(
    zoneId: String,
    binCount: Int,
    onBackToDashboard: () -> Unit
) {
    LaunchedEffect(Unit) {
        delay(1500)
        onBackToDashboard()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .safeAreaPadding()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Spacer(modifier = Modifier.weight(1f))
        
        // Success Icon
        Box(
            modifier = Modifier
                .size(120.dp)
                .background(
                    Color(0xFFE8F5E9),
                    CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "Success",
                tint = Color(0xFF4CAF50),
                modifier = Modifier.size(64.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Title
        Text(
            text = "Audit Submitted!",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF333333)
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Description
        Text(
            text = "Zone $zoneId audit with $binCount bins has been recorded.",
            fontSize = 16.sp,
            color = Color(0xFF666666),
            modifier = Modifier.padding(horizontal = 32.dp)
        )
        
        Spacer(modifier = Modifier.weight(1f))
        
        // Auto navigate after 1.5s – no button
        Text(
            text = "Returning...",
            fontSize = 14.sp,
            color = Color(0xFF666666)
        )
        
        Spacer(modifier = Modifier.height(32.dp))
    }
}
