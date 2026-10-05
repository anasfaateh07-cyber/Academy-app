package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.*
import com.example.service.NotificationService
import com.example.ui.components.*
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(
    repository: AcademyRepository,
    currentUser: CurrentUser,
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    var currentTab by remember { mutableIntStateOf(0) }
    var selectedStudentForDetail by remember { mutableStateOf<Student?>(null) }

    // Quick Action Dialogs
    var showAddStudentDialog by remember { mutableStateOf(false) }
    var showAddTeacherDialog by remember { mutableStateOf(false) }
    var showPostAnnouncementDialog by remember { mutableStateOf(false) }

    // Notification bell count
    val allNotifs by repository.notifications.collectAsState()
    val unreadCount = allNotifs.count { !it.isRead }
    var showNotificationDialog by remember { mutableStateOf(false) }

    val allStudents by repository.students.collectAsState()
    val allFees by repository.fees.collectAsState()
    val allAttendance by repository.attendance.collectAsState()

    // Screen navigation
    if (selectedStudentForDetail != null) {
        StudentDetailScreen(
            student = selectedStudentForDetail!!,
            repository = repository,
            currentUser = currentUser,
            onBack = { selectedStudentForDetail = null }
        )
        return
    }

    IslamicWatermarkBackground {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        IslamicLogoHeader(compact = true, showSubtitle = false)
                    },
                    actions = {
                        // In-App Notification Bell
                        IconButton(onClick = {
                            showNotificationDialog = true
                            repository.markNotificationsRead()
                        }) {
                            BadgedBox(
                                badge = {
                                    if (unreadCount > 0) {
                                        Badge(containerColor = ColorError) {
                                            Text("$unreadCount", color = Color.White)
                                        }
                                    }
                                }
                            ) {
                                Icon(Icons.Default.Notifications, contentDescription = "Alerts", tint = IslamicGreen)
                            }
                        }

                        // Logout
                        IconButton(onClick = onLogout) {
                            Icon(Icons.Default.Logout, contentDescription = "Logout", tint = ColorError)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = SurfaceWhite)
                )
            },
            bottomBar = {
                NavigationBar(
                    containerColor = SurfaceWhite,
                    contentColor = IslamicGreen,
                    tonalElevation = 8.dp
                ) {
                    val navItems = listOf(
                        Triple(0, "Home", Icons.Default.Mosque),
                        Triple(1, "Students", Icons.Default.People),
                        Triple(2, "Fees", Icons.Default.Receipt),
                        Triple(3, "Attendance", Icons.Default.CheckCircle),
                        Triple(4, "Quran", Icons.Default.MenuBook),
                        Triple(5, "Books", Icons.Default.AutoStories),
                        Triple(6, "Tests", Icons.Default.Assignment)
                    )

                    navItems.forEach { (index, title, icon) ->
                        val selected = currentTab == index
                        NavigationBarItem(
                            selected = selected,
                            onClick = { currentTab = index },
                            icon = { Icon(icon, contentDescription = title) },
                            label = { Text(title, fontSize = 10.sp, fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Color.White,
                                selectedTextColor = IslamicGreen,
                                indicatorColor = IslamicGreen,
                                unselectedIconColor = TextSecondaryGrey,
                                unselectedTextColor = TextSecondaryGrey
                            )
                        )
                    }
                }
            },
            containerColor = Color.Transparent
        ) { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                when (currentTab) {
                    0 -> {
                        // DASHBOARD OVERVIEW
                        AdminHomeContent(
                            currentUser = currentUser,
                            repository = repository,
                            onAddStudentClick = { showAddStudentDialog = true },
                            onAddTeacherClick = { showAddTeacherDialog = true },
                            onPostAnnouncementClick = { showPostAnnouncementDialog = true },
                            onNavigateToTab = { currentTab = it },
                            onStudentClick = { selectedStudentForDetail = it }
                        )
                    }
                    1 -> StudentsScreen(
                        repository = repository,
                        currentUser = currentUser,
                        onStudentClick = { selectedStudentForDetail = it }
                    )
                    2 -> FeesScreen(
                        repository = repository,
                        currentUser = currentUser
                    )
                    3 -> AttendanceScreen(
                        repository = repository,
                        currentUser = currentUser
                    )
                    4 -> QuranProgressScreen(
                        repository = repository,
                        currentUser = currentUser
                    )
                    5 -> BooksProgressScreen(
                        repository = repository,
                        currentUser = currentUser
                    )
                    6 -> TestsScreen(
                        repository = repository,
                        currentUser = currentUser
                    )
                }
            }
        }
    }

    if (showAddStudentDialog) {
        AddStudentDialog(
            currentUser = currentUser,
            onDismiss = { showAddStudentDialog = false },
            onAdd = { newStudent ->
                repository.addStudent(newStudent)
                showAddStudentDialog = false
                Toast.makeText(context, "Student ${newStudent.name} added successfully!", Toast.LENGTH_SHORT).show()
            }
        )
    }

    if (showAddTeacherDialog) {
        AddTeacherDialog(
            onDismiss = { showAddTeacherDialog = false },
            onAdd = { newTeacher ->
                repository.addTeacher(newTeacher)
                showAddTeacherDialog = false
                Toast.makeText(context, "Teacher ${newTeacher.name} added successfully!", Toast.LENGTH_SHORT).show()
            }
        )
    }

    if (showPostAnnouncementDialog) {
        PostAnnouncementDialog(
            currentUser = currentUser,
            onDismiss = { showPostAnnouncementDialog = false },
            onPost = { newAnn ->
                repository.postAnnouncement(newAnn)
                NotificationService.showPushNotification(
                    context = context,
                    title = "Al Hadid Academy - ${newAnn.title}",
                    body = newAnn.message
                )
                showPostAnnouncementDialog = false
                Toast.makeText(context, "Announcement broadcasted with push alert!", Toast.LENGTH_LONG).show()
            }
        )
    }

    if (showNotificationDialog) {
        AlertDialog(
            onDismissRequest = { showNotificationDialog = false },
            title = {
                Text(
                    text = "Notification Center",
                    fontWeight = FontWeight.Bold,
                    color = IslamicGreen
                )
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 400.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (allNotifs.isEmpty()) {
                        Text("No notifications yet.", color = TextSecondaryGrey)
                    } else {
                        allNotifs.forEach { notif ->
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = CreamBackground,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text(text = notif.title, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = IslamicGreen)
                                    Text(text = notif.body, fontSize = 11.5.sp, color = TextPrimaryCharcoal)
                                    Text(text = notif.timestamp, fontSize = 9.5.sp, color = TextSecondaryGrey)
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showNotificationDialog = false }) {
                    Text("Close")
                }
            }
        )
    }
}

@Composable
fun AdminHomeContent(
    currentUser: CurrentUser,
    repository: AcademyRepository,
    onAddStudentClick: () -> Unit,
    onAddTeacherClick: () -> Unit,
    onPostAnnouncementClick: () -> Unit,
    onNavigateToTab: (Int) -> Unit,
    onStudentClick: (Student) -> Unit
) {
    val allStudents by repository.students.collectAsState()
    val allFees by repository.fees.collectAsState()
    val allAttendance by repository.attendance.collectAsState()
    val allAnnouncements by repository.announcements.collectAsState()

    val totalStudents = allStudents.size
    val totalFeeCollected = allFees.filter { it.status == "Paid" }.sumOf { it.totalPaid }
    val totalPendingFee = allFees.filter { it.status != "Paid" }.sumOf { it.due }
    val presentCount = allAttendance.count { it.status == "Present" }

    val hifzCount = allStudents.count { it.groupType == "Hifz" }
    val nazraTajweedCount = allStudents.count { it.groupType in listOf("Nazra", "Tajweed") }
    val tuitionBoysCount = allStudents.count { it.groupType == "Tuition Boy" }
    val girlsPlaygroupCount = allStudents.count { it.groupType in listOf("Tuition Girl", "Playgroup") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Task 3 & 4: New Welcome Card replacing dummy section
        WelcomeCard(modifier = Modifier.fillMaxWidth())

        // Welcome Header
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = IslamicGreen,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Assalam-o-Alaikum,",
                            color = Color(0xFFFFE082),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = if (currentUser.name.isNotBlank()) currentUser.name else "Teacher Anas Mustafa",
                            style = TextStyle(
                                brush = Brush.horizontalGradient(
                                    colors = listOf(Color(0xFFFFD700), Color.White, Color(0xFF00FF87))
                                ),
                                fontWeight = FontWeight.Bold,
                                fontSize = 22.sp
                            )
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = IslamicGold
                    ) {
                        Text(
                            text = if (currentUser.isFullAdmin) "DUAL ADMIN" else "TEACHER",
                            color = Color(0xFF332501),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Al Hadid Academy • Nasirabad Jatlan, Azad Kashmir",
                    color = Color(0xFFFFEB3B),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = if (currentUser.isFullAdmin)
                        "Full administrative control over Hifz, Nazra, Tajweed, Tuition & Financials."
                    else
                        "Managing Girls Tuition & Playgroup classes and student assessments.",
                    color = Color.White.copy(alpha = 0.85f),
                    fontSize = 11.sp
                )
            }
        }

        // Top Action Buttons: [+ Add New Teacher] [+ Add New Student] [+ Post Announcement]
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (currentUser.isFullAdmin) {
                Button(
                    onClick = onAddTeacherClick,
                    colors = ButtonDefaults.buttonColors(containerColor = IslamicGreen),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(vertical = 10.dp)
                ) {
                    Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("+ Add Teacher", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }

            Button(
                onClick = onAddStudentClick,
                colors = ButtonDefaults.buttonColors(containerColor = IslamicGreen),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(vertical = 10.dp)
            ) {
                Icon(Icons.Default.GroupAdd, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("+ Add Student", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }

            if (currentUser.isFullAdmin) {
                Button(
                    onClick = onPostAnnouncementClick,
                    colors = ButtonDefaults.buttonColors(containerColor = IslamicGoldDark),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(vertical = 10.dp)
                ) {
                    Icon(Icons.Default.Campaign, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("+ Alert", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // 4 Big Metrics Cards
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            IslamicMetricCard(
                title = "Total Students",
                value = "$totalStudents / 100",
                subtitle = "Capacity target",
                iconRes = "👥",
                accentColor = IslamicGreen,
                progress = totalStudents / 100f,
                modifier = Modifier.weight(1f)
            )

            IslamicMetricCard(
                title = "Fee Collected",
                value = "Rs ${totalFeeCollected.toInt()}",
                subtitle = "October cycle",
                iconRes = "💵",
                accentColor = ColorSuccess,
                modifier = Modifier.weight(1f)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            IslamicMetricCard(
                title = "Pending Fees",
                value = "Rs ${totalPendingFee.toInt()}",
                subtitle = "Unpaid dues",
                iconRes = "⏳",
                accentColor = ColorError,
                modifier = Modifier.weight(1f)
            )

            IslamicMetricCard(
                title = "Present Today",
                value = "$presentCount",
                subtitle = "Marked attendance",
                iconRes = "✓",
                accentColor = IslamicGoldDark,
                modifier = Modifier.weight(1f)
            )
        }

        // 4 Group Summary Cards
        Text(
            text = "Academy Department Enrolment",
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = IslamicGreen
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            GroupBadgeCard(
                title = "Hifz Group",
                count = "$hifzCount Students",
                teacher = "Anas Mustafa",
                modifier = Modifier.weight(1f),
                onClick = { onNavigateToTab(1) }
            )

            GroupBadgeCard(
                title = "Nazra & Tajweed",
                count = "$nazraTajweedCount Students",
                teacher = "Awais & Anas",
                modifier = Modifier.weight(1f),
                onClick = { onNavigateToTab(1) }
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            GroupBadgeCard(
                title = "Tuition Boys",
                count = "$tuitionBoysCount Students",
                teacher = "Classes 3,7,8",
                modifier = Modifier.weight(1f),
                onClick = { onNavigateToTab(1) }
            )

            GroupBadgeCard(
                title = "Girls & Playgroup",
                count = "$girlsPlaygroupCount Students",
                teacher = "Teacher Isra",
                modifier = Modifier.weight(1f),
                onClick = { onNavigateToTab(1) }
            )
        }

        // Recent Activity Feed
        IslamicArchCard {
            Text(
                text = "Recent Students Enrolled",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = IslamicGreen
            )
            Spacer(modifier = Modifier.height(8.dp))

            allStudents.take(4).forEach { st ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onStudentClick(st) }
                        .padding(vertical = 5.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = st.name,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp,
                            color = TextPrimaryCharcoal
                        )
                        Text(
                            text = "Class ${st.className} • ${st.groupType} • ${st.assignedTeacher}",
                            fontSize = 11.sp,
                            color = TextSecondaryGrey
                        )
                    }

                    Icon(Icons.Default.ChevronRight, contentDescription = null, tint = IslamicGreen, modifier = Modifier.size(18.dp))
                }
                HorizontalDivider(color = DividerMuted.copy(alpha = 0.5f))
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}

@Composable
fun GroupBadgeCard(
    title: String,
    count: String,
    teacher: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier.clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.Brush.linearGradient(listOf(BorderGold.copy(alpha = 0.5f), Color.Transparent)))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(title, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = IslamicGreen)
            Text(count, fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = IslamicGoldDark)
            Text(teacher, fontSize = 10.5.sp, color = TextSecondaryGrey)
        }
    }
}
