package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.*

@Composable
fun IslamicWatermarkBackground(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(CreamBackground)
            .drawBehind {
                // Subtle Islamic geometric arabesque star motifs drawn with 4% opacity
                val strokeColor = IslamicGreen.copy(alpha = 0.035f)
                val goldStroke = IslamicGold.copy(alpha = 0.04f)
                val step = 140.dp.toPx()
                val radius = 38.dp.toPx()

                var x = 0f
                while (x < size.width + step) {
                    var y = 0f
                    while (y < size.height + step) {
                        // 8-point geometric star
                        val path = Path()
                        for (i in 0 until 8) {
                            val angle1 = Math.toRadians((i * 45).toDouble())
                            val angle2 = Math.toRadians((i * 45 + 22.5).toDouble())
                            val rOuter = radius
                            val rInner = radius * 0.45f
                            val px1 = x + (rOuter * Math.cos(angle1)).toFloat()
                            val py1 = y + (rOuter * Math.sin(angle1)).toFloat()
                            val px2 = x + (rInner * Math.cos(angle2)).toFloat()
                            val py2 = y + (rInner * Math.sin(angle2)).toFloat()
                            if (i == 0) path.moveTo(px1, py1) else path.lineTo(px1, py1)
                            path.lineTo(px2, py2)
                        }
                        path.close()
                        drawPath(path, color = strokeColor, style = Stroke(width = 1.5f))
                        drawCircle(color = goldStroke, radius = 5f, center = Offset(x, y))
                        y += step
                    }
                    x += step
                }
            }
    ) {
        content()
    }
}

@Composable
fun IslamicLogoHeader(
    modifier: Modifier = Modifier,
    showSubtitle: Boolean = true,
    compact: Boolean = false,
    onLogoClick: (() -> Unit)? = null
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = if (compact) 8.dp else 16.dp, vertical = if (compact) 4.dp else 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(if (compact) 44.dp else 58.dp)
                .clip(CircleShape)
                .background(SurfaceWhite)
                .border(1.5.dp, IslamicGold, CircleShape)
                .padding(2.dp)
                .then(if (onLogoClick != null) Modifier.clickable { onLogoClick() } else Modifier),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.alhadid_logo),
                contentDescription = "Al Hadid Academy Logo",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Al Hadid Academy",
                color = IslamicGreen,
                fontSize = if (compact) 17.sp else 22.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Serif,
                letterSpacing = 0.5.sp
            )
            Text(
                text = "Nasirabad Jatlan, Azad Kashmir",
                color = IslamicGoldDark,
                fontSize = if (compact) 10.5.sp else 11.5.sp,
                fontWeight = FontWeight.SemiBold
            )
            if (showSubtitle) {
                Text(
                    text = "Hifz  •  Nazra  •  Tajweed  •  Tuition",
                    color = TextSecondaryGrey,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 0.5.sp
                )
            }
        }
    }
}

@Composable
fun IslamicArchCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    backgroundColor: Color = SurfaceWhite,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier),
        shape = RoundedCornerShape(
            topStart = 20.dp,
            topEnd = 20.dp,
            bottomStart = 14.dp,
            bottomEnd = 14.dp
        ),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.5.dp),
        border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(IslamicGold.copy(alpha = 0.6f), IslamicGreen.copy(alpha = 0.2f))))
    ) {
        // Islamic arch top accent line
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .background(
                    Brush.horizontalGradient(
                        listOf(
                            IslamicGreen,
                            IslamicGold,
                            IslamicGreen
                        )
                    )
                )
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            content()
        }
    }
}

@Composable
fun IslamicGoldDivider(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .height(1.dp)
                .background(Brush.horizontalGradient(listOf(Color.Transparent, IslamicGold.copy(alpha = 0.7f))))
        )
        Box(
            modifier = Modifier
                .padding(horizontal = 6.dp)
                .size(7.dp)
                .clip(CircleShape)
                .background(IslamicGold)
        )
        Box(
            modifier = Modifier
                .weight(1f)
                .height(1.dp)
                .background(Brush.horizontalGradient(listOf(IslamicGold.copy(alpha = 0.7f), Color.Transparent)))
        )
    }
}

@Composable
fun GradeBadge(
    grade: String,
    modifier: Modifier = Modifier
) {
    val (bg, textColor) = when (grade) {
        "A+" -> Pair(Color(0xFF10B981), Color.White)
        "A" -> Pair(Color(0xFF0B5D1E), Color.White)
        "B" -> Pair(Color(0xFF3B82F6), Color.White)
        "C" -> Pair(Color(0xFFF59E0B), Color.White)
        "Needs Improvement" -> Pair(Color(0xFF6B7280), Color.White)
        else -> Pair(Color(0xFFEF4444), Color.White)
    }

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = bg
    ) {
        Text(
            text = grade,
            color = textColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun IslamicMetricCard(
    title: String,
    value: String,
    subtitle: String,
    iconRes: String,
    accentColor: Color,
    modifier: Modifier = Modifier,
    progress: Float? = null
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = CardDefaults.outlinedCardBorder().copy(brush = Brush.verticalGradient(listOf(IslamicGold.copy(alpha = 0.4f), Color.Transparent)))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    color = TextSecondaryGrey,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(accentColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = iconRes,
                        fontSize = 12.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = value,
                color = IslamicGreen,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )

            if (progress != null) {
                Spacer(modifier = Modifier.height(6.dp))
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(5.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = accentColor,
                    trackColor = accentColor.copy(alpha = 0.15f),
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = subtitle,
                color = IslamicGoldDark,
                fontSize = 11.sp,
                fontWeight = FontWeight.Normal
            )
        }
    }
}
