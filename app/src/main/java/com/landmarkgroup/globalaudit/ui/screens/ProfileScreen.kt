package com.landmarkgroup.globalaudit.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.landmarkgroup.globalaudit.R
import com.landmarkgroup.globalaudit.data.model.SharedAuthData
import com.landmarkgroup.globalaudit.ui.utils.safeAreaPadding
import com.landmarkgroup.globalaudit.utils.JwtUtils

@Composable
fun ProfileScreen(
    onBackClick: () -> Unit,
    onLogout: () -> Unit
) {
    val authData = SharedAuthData.getAuthData()
    val idToken = authData?.idTokenKey
    
    // Extract user details from JWT token
    val firstName = JwtUtils.extractForKey(idToken, "given_name") ?: ""
    val lastName = JwtUtils.extractForKey(idToken, "family_name") ?: ""
    val fullName = authData?.usernameKey ?: "$firstName $lastName".trim().ifEmpty { "N/A" }
    val email = JwtUtils.extractForKey(idToken, "email") ?: "N/A"
    
    // Get selected facility and roles
    val selectedFacility = authData?.selectedFacilityKey ?: "Not Selected"
    
    // Extract roles from backend (if available in future)
    val roles = listOf("User") // This would come from backend AuthorizationResponse
    
    // Generate initials for profile picture
    val initials = when {
        firstName.isNotEmpty() && lastName.isNotEmpty() -> 
            "${firstName.first().uppercase()}${lastName.first().uppercase()}"
        fullName.isNotEmpty() && fullName != "N/A" -> 
            fullName.split(" ").take(2).joinToString("") { it.first().uppercase() }
        else -> "U"
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
            .safeAreaPadding()
            .verticalScroll(rememberScrollState())
    ) {
        // Back button and header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = Color(0xFF333333)
                )
            }
        }
        
        // Profile Picture Section
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Profile Picture
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF4A90E2)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = initials,
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
            
            // User Name
            Text(
                text = fullName,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF333333)
            )
            
            // User Title/Role (if available)
            if (roles.isNotEmpty()) {
                Text(
                    text = roles.first(),
                    fontSize = 16.sp,
                    color = Color(0xFF666666)
                )
            }
        }
        
        // User Details Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Name
                ProfileDetailRow(
                    icon = Icons.Default.Person,
                    label = "Name",
                    value = fullName
                )
                
                // Email (if available)
                if (email != "N/A") {
                    ProfileDetailRow(
                        icon = Icons.Default.Person,
                        label = "Email",
                        value = email
                    )
                }
                
                // Selected Facility
                ProfileDetailRow(
                    icon = Icons.Default.CheckCircle,
                    label = "Selected Facility",
                    value = selectedFacility
                )
                
                // Roles
                if (roles.isNotEmpty()) {
                    ProfileDetailRow(
                        icon = Icons.Default.Lock,
                        label = "Roles",
                        value = roles.joinToString(", ")
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Logout Button
        Button(
            onClick = onLogout,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 8.dp)
                .height(56.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFFFEBEE),
                contentColor = Color(0xFFD32F2F)
            )
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "LOGOUT ACCOUNT",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun ProfileDetailRow(
    icon: ImageVector,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = Color(0xFF666666),
            modifier = Modifier.size(24.dp)
        )
        
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = label,
                fontSize = 14.sp,
                color = Color(0xFF999999),
                fontWeight = FontWeight.Normal
            )
            Text(
                text = value,
                fontSize = 16.sp,
                color = Color(0xFF333333),
                fontWeight = FontWeight.Medium
            )
        }
    }
}
