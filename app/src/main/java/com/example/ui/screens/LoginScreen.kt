package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.AcademyRepository
import com.example.ui.components.IslamicArchCard
import com.example.ui.components.IslamicGoldDivider
import com.example.ui.components.IslamicWatermarkBackground
import com.example.ui.theme.*

@Composable
fun LoginScreen(
    repository: AcademyRepository,
    onLoginSuccess: () -> Unit
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Staff/Admin, 1: Parent OTP
    var emailOrUsername by remember { mutableStateOf("awais@alhadid.com") }
    var password by remember { mutableStateOf("Alhadid@123") }
    var parentPhone by remember { mutableStateOf("+92 300 1234567") }
    var parentOtp by remember { mutableStateOf("123456") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    IslamicWatermarkBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Logo Top Center
            Box(
                modifier = Modifier
                    .size(90.dp)
                    .clip(CircleShape)
                    .background(SurfaceWhite)
                    .border(2.5.dp, IslamicGold, CircleShape)
                    .padding(4.dp),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.alhadid_logo),
                    contentDescription = "Al Hadid Academy Logo",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Al Hadid Academy",
                color = IslamicGreen,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Serif,
                letterSpacing = 0.5.sp
            )

            Text(
                text = "Nasirabad Jatlan, Azad Kashmir",
                color = IslamicGoldDark,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Tab Selector
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = SurfaceWhite,
                contentColor = IslamicGreen,
                modifier = Modifier
                    .clip(RoundedCornerShape(14.dp))
                    .border(1.dp, IslamicGold.copy(alpha = 0.3f), RoundedCornerShape(14.dp))
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0; errorMessage = null },
                    text = {
                        Text(
                            "Staff & Admin",
                            fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal,
                            color = if (selectedTab == 0) IslamicGreen else TextSecondaryGrey
                        )
                    }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1; errorMessage = null },
                    text = {
                        Text(
                            "Parent Portal",
                            fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal,
                            color = if (selectedTab == 1) IslamicGreen else TextSecondaryGrey
                        )
                    }
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Main Login Card
            IslamicArchCard {
                if (selectedTab == 0) {
                    Text(
                        text = "Dual Admin & Teacher Login",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = IslamicGreen
                    )
                    Text(
                        text = "Sign in as Principal Awais, Teacher Anas, or Teacher Isra",
                        fontSize = 12.sp,
                        color = TextSecondaryGrey
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = emailOrUsername,
                        onValueChange = { emailOrUsername = it },
                        label = { Text("Email Address") },
                        leadingIcon = {
                            Icon(Icons.Default.Person, contentDescription = null, tint = IslamicGreen)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = IslamicGreen,
                            unfocusedBorderColor = BorderGold
                        ),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Password") },
                        leadingIcon = {
                            Icon(Icons.Default.Lock, contentDescription = null, tint = IslamicGreen)
                        },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = IslamicGreen,
                            unfocusedBorderColor = BorderGold
                        ),
                        singleLine = true
                    )

                    if (errorMessage != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = errorMessage ?: "",
                            color = ColorError,
                            fontSize = 12.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = {
                            val success = repository.login(emailOrUsername, password)
                            if (success) {
                                onLoginSuccess()
                            } else {
                                errorMessage = "Invalid credentials. Tap quick demo buttons below."
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = IslamicGreen)
                    ) {
                        Text(
                            text = "Login to Management Portal",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                } else {
                    // Parent OTP Login
                    Text(
                        text = "Parent Portal Access",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = IslamicGreen
                    )
                    Text(
                        text = "Login with your registered WhatsApp phone number to view your child's data securely.",
                        fontSize = 12.sp,
                        color = TextSecondaryGrey
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = parentPhone,
                        onValueChange = { parentPhone = it },
                        label = { Text("Parent WhatsApp Phone") },
                        leadingIcon = {
                            Icon(Icons.Default.Phone, contentDescription = null, tint = IslamicGreen)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = IslamicGreen,
                            unfocusedBorderColor = BorderGold
                        ),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = parentOtp,
                        onValueChange = { parentOtp = it },
                        label = { Text("SMS / OTP Verification Code") },
                        leadingIcon = {
                            Icon(Icons.Default.Shield, contentDescription = null, tint = IslamicGreen)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = IslamicGreen,
                            unfocusedBorderColor = BorderGold
                        ),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = {
                            val success = repository.login(parentPhone, parentOtp)
                            if (success) {
                                onLoginSuccess()
                            } else {
                                errorMessage = "Could not authenticate phone number."
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = IslamicGreen)
                    ) {
                        Text(
                            text = "Verify OTP & View Child's Portal",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Quick Role Switcher / Demo Buttons
            Text(
                text = "⚡ Quick Switch & Test User Roles",
                color = IslamicGoldDark,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Role 1: Principal Awais Mustafa
                OutlinedButton(
                    onClick = {
                        emailOrUsername = "awais@alhadid.com"
                        password = "Alhadid@123"
                        repository.login(emailOrUsername, password)
                        onLoginSuccess()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.outlinedButtonColors(containerColor = SurfaceWhite)
                ) {
                    Icon(Icons.Default.Shield, contentDescription = null, tint = IslamicGreen, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Principal Awais Mustafa (Full Admin)",
                        color = IslamicGreen,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                // Role 2: Teacher Anas Mustafa
                OutlinedButton(
                    onClick = {
                        emailOrUsername = "anas@alhadid.com"
                        password = "Alhadid@123"
                        repository.login(emailOrUsername, password)
                        onLoginSuccess()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.outlinedButtonColors(containerColor = SurfaceWhite)
                ) {
                    Icon(Icons.Default.School, contentDescription = null, tint = IslamicGreen, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Teacher Anas Mustafa (Dual Admin - Full Access)",
                        color = IslamicGreen,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                // Role 3: Teacher Isra
                OutlinedButton(
                    onClick = {
                        emailOrUsername = "isra@alhadid.com"
                        password = "Isra@123"
                        repository.login(emailOrUsername, password)
                        onLoginSuccess()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.outlinedButtonColors(containerColor = SurfaceWhite)
                ) {
                    Icon(Icons.Default.Person, contentDescription = null, tint = Color(0xFF9333EA), modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Teacher Isra (Girls & Playgroup)",
                        color = Color(0xFF9333EA),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                // Role 4: Parent Portal
                OutlinedButton(
                    onClick = {
                        parentPhone = "+92 300 1234567"
                        repository.login(parentPhone, "123456")
                        onLoginSuccess()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.outlinedButtonColors(containerColor = SurfaceWhite)
                ) {
                    Icon(Icons.Default.Phone, contentDescription = null, tint = Color(0xFF25D366), modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Parent Portal (+92 300 1234567)",
                        color = Color(0xFF0F766E),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}
