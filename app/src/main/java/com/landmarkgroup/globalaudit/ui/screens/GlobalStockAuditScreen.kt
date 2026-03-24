package com.landmarkgroup.globalaudit.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.landmarkgroup.globalaudit.R
import com.landmarkgroup.globalaudit.data.model.SharedAuthData
import com.landmarkgroup.globalaudit.ui.utils.safeAreaPadding
import androidx.compose.ui.platform.LocalContext
import android.widget.Toast

@Composable
fun GlobalStockAuditScreen(
    onGlobalStockAuditClick: () -> Unit,
    onProfileClick: () -> Unit
) {
    val authData = SharedAuthData.getAuthData()
    val userName = authData?.usernameKey ?: "User"
    val context = LocalContext.current
    val features = SharedAuthData.getWarehouseFeatures()
    val isAuditEnabled = features?.any {
        it.featureName.equals("UI_FEATURE_GLOBAL_AUDIT_FACILITY_FLAG", ignoreCase = true) && it.featureEnabledValue
    } ?: false
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
            .safeAreaPadding()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Welcome Header
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Welcome back",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF333333)
            )
            Text(
                text = "Ready for today's audit?",
                fontSize = 16.sp,
                color = Color(0xFF666666)
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Main Operations Section
        Text(
            text = "MAIN OPERATIONS",
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF999999),
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        // Global Stock Audit Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(if (isAuditEnabled) Color(0xFF4A90E2) else Color(0xFFCFD8DC))
                .clickable {
                    if (isAuditEnabled) {
                        onGlobalStockAuditClick()
                    } else {
                        Toast.makeText(context, "This feature is not available", Toast.LENGTH_SHORT).show()
                    }
                }
                .alpha(if (isAuditEnabled) 1f else 0.6f),
            contentAlignment = Alignment.Center
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.checklist),
                        contentDescription = "Checklist",
                        modifier = Modifier.size(48.dp)
                    )
                    Text(
                        text = "Global Stock Audit",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "Start a new zone audit",
                        fontSize = 14.sp,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                }
                
                // Background decorative icons
                Box(
                    modifier = Modifier.size(120.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.checklist),
                        contentDescription = null,
                        modifier = Modifier
                            .size(100.dp)
                            .alpha(0.1f)
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // My Profile Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Color.White)
                .border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(16.dp))
                .clickable { onProfileClick() },
            contentAlignment = Alignment.Center
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Profile",
                        modifier = Modifier.size(40.dp),
                        tint = Color(0xFF666666)
                    )
                    Column(
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "My Profile",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF333333)
                        )
                        Text(
                            text = "View and edit your account",
                            fontSize = 14.sp,
                            color = Color(0xFF666666)
                        )
                    }
                }
                
                // Background decorative icon
                Box(
                    modifier = Modifier.size(80.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        modifier = Modifier
                            .size(60.dp)
                            .alpha(0.05f),
                        tint = Color(0xFF666666)
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.weight(1f))
    }
}
