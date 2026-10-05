package com.example.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
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
import com.example.ui.components.IslamicGoldDivider
import com.example.ui.theme.IslamicGold
import com.example.ui.theme.IslamicGreen
import com.example.ui.theme.IslamicGreenDark
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onFinished: () -> Unit
) {
    val alphaAnim = remember { Animatable(0f) }
    val scaleAnim = remember { Animatable(0.75f) }

    LaunchedEffect(Unit) {
        alphaAnim.animateTo(1f, animationSpec = tween(1000))
        scaleAnim.animateTo(1f, animationSpec = tween(900))
        delay(1400)
        onFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFF0B5D1E),
                        Color(0xFF064315),
                        Color(0xFF042D0E)
                    )
                )
            )
            .clickable { onFinished() },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .padding(32.dp)
                .alpha(alphaAnim.value)
                .scale(scaleAnim.value)
        ) {
            // Large Centered Logo with double border
            Box(
                modifier = Modifier
                    .size(160.dp)
                    .clip(CircleShape)
                    .background(Color.White)
                    .border(3.dp, IslamicGold, CircleShape)
                    .padding(6.dp),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.alhadid_logo),
                    contentDescription = "Al Hadid Academy Logo",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            Text(
                text = "Al Hadid Academy",
                color = Color.White,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Serif,
                letterSpacing = 1.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Hifz  |  Nazra  |  Tajweed  |  Tuition  |  Computer",
                color = IslamicGold,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = 0.8.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            Box(
                modifier = Modifier
                    .width(180.dp)
                    .height(1.5.dp)
                    .background(
                        Brush.horizontalGradient(
                            listOf(Color.Transparent, IslamicGold, Color.Transparent)
                        )
                    )
            )

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Dual Admin & Student Management System",
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 12.sp,
                letterSpacing = 0.5.sp,
                textAlign = TextAlign.Center
            )
        }

        // Bottom Location Label
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 36.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Nasirabad Jatlan, Azad Kashmir",
                color = IslamicGold.copy(alpha = 0.9f),
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Tap to enter",
                color = Color.White.copy(alpha = 0.4f),
                fontSize = 10.sp
            )
        }
    }
}
