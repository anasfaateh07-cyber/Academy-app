package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.*
import com.example.service.NotificationService
import com.example.ui.components.IslamicArchCard
import com.example.ui.components.IslamicGoldDivider
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun AnnouncementsScreen(
    repository: AcademyRepository,
    currentUser: CurrentUser,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val announcements by repository.announcements.collectAsState()
    var showPostDialog by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = Color.Transparent,
        floatingActionButton = {
            if (currentUser.isFullAdmin) {
                ExtendedFloatingActionButton(
                    onClick = { showPostDialog = true },
                    containerColor = IslamicGreen,
                    contentColor = Color.White,
                    shape = RoundedCornerShape(16.dp),
                    icon = { Icon(Icons.Default.Campaign, contentDescription = "Post Announcement") },
                    text = { Text("Post Announcement", fontWeight = FontWeight.Bold) }
                )
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            Text(
                text = "Announcements & High-Priority Alerts",
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = IslamicGreen,
                fontFamily = FontFamily.Serif
            )
            Text(
                text = "Announcements sent here trigger instant push notifications for parents",
                fontSize = 12.sp,
                color = TextSecondaryGrey
            )

            Spacer(modifier = Modifier.height(12.dp))

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(announcements, key = { it.id }) { ann ->
                    IslamicArchCard {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.NotificationsActive,
                                    contentDescription = null,
                                    tint = if (ann.priority == "High") ColorError else IslamicGoldDark,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = ann.title,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimaryCharcoal
                                )
                            }

                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (ann.priority == "High") Color(0xFFFEE2E2) else CreamBackground
                            ) {
                                Text(
                                    text = "${ann.type} (${ann.priority})",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (ann.priority == "High") Color(0xFF991B1B) else IslamicGreen,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = ann.message,
                            fontSize = 13.sp,
                            color = TextPrimaryCharcoal,
                            lineHeight = 18.sp
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Target: ${ann.targetGroup}",
                                fontSize = 11.sp,
                                color = IslamicGoldDark,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Posted by ${ann.createdBy} • ${ann.createdAt}",
                                fontSize = 10.5.sp,
                                color = TextSecondaryGrey
                            )
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }
    }

    if (showPostDialog) {
        PostAnnouncementDialog(
            currentUser = currentUser,
            onDismiss = { showPostDialog = false },
            onPost = { newAnn ->
                repository.postAnnouncement(newAnn)
                // Trigger system high priority notification
                NotificationService.showPushNotification(
                    context = context,
                    title = "Al Hadid Academy - ${newAnn.title}",
                    body = newAnn.message
                )
                showPostDialog = false
                Toast.makeText(context, "Announcement broadcasted to parents with High-Priority Push Notification!", Toast.LENGTH_LONG).show()
            }
        )
    }
}

@Composable
fun PostAnnouncementDialog(
    currentUser: CurrentUser,
    onDismiss: () -> Unit,
    onPost: (Announcement) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("Important") }
    var targetGroup by remember { mutableStateOf("All") }
    var priority by remember { mutableStateOf("High") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceWhite)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "Post Important Announcement",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = IslamicGreen
                )
                Text(
                    text = "Broadcasted to parents with loud heads-up push alert",
                    fontSize = 11.sp,
                    color = TextSecondaryGrey
                )

                IslamicGoldDivider(modifier = Modifier.padding(vertical = 8.dp))

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title (e.g. Academy Holiday, PTM, Fee Alert)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = message,
                    onValueChange = { message = it },
                    label = { Text("Announcement Message *") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text("Category:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = IslamicGreen)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    listOf("Important", "Fee Reminder", "Holiday", "Stationary Required").forEach { t ->
                        FilterChip(
                            selected = type == t,
                            onClick = { type = t },
                            label = { Text(t, fontSize = 10.sp) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text("Target Group:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = IslamicGreen)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    listOf("All", "Hifz", "Tuition Boy", "Tuition Girl", "Playgroup").forEach { g ->
                        FilterChip(
                            selected = targetGroup == g,
                            onClick = { targetGroup = g },
                            label = { Text(g, fontSize = 10.sp) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("Cancel") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (title.isNotBlank() && message.isNotBlank()) {
                                val timeStr = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US).format(Date())
                                onPost(
                                    Announcement(
                                        id = "ANN-${System.currentTimeMillis()}",
                                        title = title.trim(),
                                        message = message.trim(),
                                        type = type,
                                        targetGroup = targetGroup,
                                        createdBy = currentUser.name,
                                        createdAt = timeStr,
                                        priority = priority
                                    )
                                )
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = IslamicGreen)
                    ) {
                        Text("Post & Send Alert", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
