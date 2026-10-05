package com.example.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import kotlinx.coroutines.delay

@Composable
fun WelcomeScreen(
    onContinue: () -> Unit
) {
    // Task 4: Auto-navigate to Home after 3 seconds
    LaunchedEffect(Unit) {
        delay(3000)
        onContinue()
    }

    val gradientBrush = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF0B6A3A),
            Color(0xFF14532D)
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(gradientBrush)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.08f)),
            border = CardDefaults.outlinedCardBorder().copy(
                brush = Brush.linearGradient(
                    listOf(Color(0xFFFFD700), Color(0xFFFFD700).copy(alpha = 0.2f))
                )
            ),
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Logo at top
                Surface(
                    shape = CircleShape,
                    color = Color.White,
                    shadowElevation = 8.dp,
                    modifier = Modifier
                        .size(100.dp)
                        .border(3.dp, Color(0xFFFFD700), CircleShape)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.alhadid_logo),
                        contentDescription = "Al Hadid Academy Logo",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Title: "Welcome to Al Hadid Academy"
                Text(
                    text = "Welcome to Al Hadid Academy",
                    fontSize = 23.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    fontFamily = FontFamily.Serif
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Subtitle: "Nasirabad Jatlam, Azad Kashmir"
                Text(
                    text = "Nasirabad Jatlam, Azad Kashmir",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFFFFE082),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(18.dp))

                // Gold Divider with Star Icon
                Row(
                    modifier = Modifier.fillMaxWidth(0.7f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    HorizontalDivider(modifier = Modifier.weight(1f), color = Color(0xFFFFD700).copy(alpha = 0.6f))
                    Icon(
                        Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = Color(0xFFFFD700),
                        modifier = Modifier.padding(horizontal = 8.dp).size(18.dp)
                    )
                    HorizontalDivider(modifier = Modifier.weight(1f), color = Color(0xFFFFD700).copy(alpha = 0.6f))
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Description:
                Text(
                    text = "Al Hadid Academy is a complete Islamic & Modern Education System. We provide Hifz-ul-Quran, Nazra, Tajweed, Tuition (Class 5-8), Computer Courses and full Fee & Attendance Management. Our mission is to provide quality Deeni & Dunyavi taleem under the supervision of Principal Awais Mustafa and Teacher Anas Mustafa.",
                    fontSize = 13.sp,
                    color = Color.White.copy(alpha = 0.95f),
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Highlights with gold icons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.MenuBook, contentDescription = null, tint = Color(0xFFFFD700), modifier = Modifier.size(22.dp))
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Quran & Hifz", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.School, contentDescription = null, tint = Color(0xFFFFD700), modifier = Modifier.size(22.dp))
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Tuition & Books", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Verified, contentDescription = null, tint = Color(0xFFFFD700), modifier = Modifier.size(22.dp))
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Attendance", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))

                // Button: "Continue to Dashboard →"
                Button(
                    onClick = onContinue,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFFD700),
                        contentColor = Color(0xFF14532D)
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                ) {
                    Text(
                        text = "Continue to Dashboard →",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Redirecting in 3 seconds...",
                    fontSize = 11.sp,
                    color = Color.White.copy(alpha = 0.6f)
                )
            }
        }
    }
}

/**
 * Reusable Welcome Card component for Home Screen
 */
@Composable
fun WelcomeCard(
    modifier: Modifier = Modifier,
    onContinueClick: (() -> Unit)? = null
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        modifier = modifier
            .fillMaxWidth()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFF0B6A3A), Color(0xFF14532D))
                ),
                shape = RoundedCornerShape(20.dp)
            )
            .border(
                1.5.dp,
                Brush.linearGradient(listOf(Color(0xFFFFD700), Color(0xFFFFD700).copy(alpha = 0.3f))),
                RoundedCornerShape(20.dp)
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                shape = CircleShape,
                color = Color.White,
                shadowElevation = 4.dp,
                modifier = Modifier
                    .size(60.dp)
                    .border(2.dp, Color(0xFFFFD700), CircleShape)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.alhadid_logo),
                    contentDescription = "Al Hadid Academy Logo",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Welcome to Al Hadid Academy",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center,
                fontFamily = FontFamily.Serif
            )

            Text(
                text = "Nasirabad Jatlam, Azad Kashmir",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFFFFE082),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Al Hadid Academy is a complete Islamic & Modern Education System. We provide Hifz-ul-Quran, Nazra, Tajweed, Tuition (Class 5-8), Computer Courses and full Fee & Attendance Management. Our mission is to provide quality Deeni & Dunyavi taleem under the supervision of Principal Awais Mustafa and Teacher Anas Mustafa.",
                fontSize = 12.sp,
                color = Color.White.copy(alpha = 0.9f),
                textAlign = TextAlign.Center,
                lineHeight = 17.sp
            )

            if (onContinueClick != null) {
                Spacer(modifier = Modifier.height(14.dp))
                Button(
                    onClick = onContinueClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFFD700),
                        contentColor = Color(0xFF14532D)
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth().height(40.dp)
                ) {
                    Text("Continue to Dashboard →", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        }
    }
}
