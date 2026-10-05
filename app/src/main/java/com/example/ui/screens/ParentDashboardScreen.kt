package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.*
import com.example.service.AudioService
import com.example.service.PdfService
import com.example.ui.components.*
import com.example.ui.theme.*
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ParentDashboardScreen(
    repository: AcademyRepository,
    currentUser: CurrentUser,
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    val allStudents by repository.students.collectAsState()
    val allFees by repository.fees.collectAsState()
    val allQuran by repository.quranProgress.collectAsState()
    val allBooks by repository.bookProgress.collectAsState()
    val allAttendance by repository.attendance.collectAsState()
    val allTests by repository.tests.collectAsState()
    val allTestMarks by repository.testMarks.collectAsState()
    val allAnnouncements by repository.announcements.collectAsState()

    // Find parent's children (Data Mahfooz / Private to this phone)
    val myChildren = remember(allStudents, currentUser.phone) {
        repository.getStudentsForParent(currentUser.phone)
    }

    var selectedChildId by remember { mutableStateOf(myChildren.firstOrNull()?.studentId ?: "") }
    val activeChild = myChildren.find { it.studentId == selectedChildId } ?: myChildren.firstOrNull()

    // Navigation Tabs in Parent View
    var selectedTab by remember { mutableIntStateOf(0) }
    // 0: Overview, 1: Books Progress, 2: Quran Sabaq, 3: Fees & Receipts, 4: Tests & Reports

    // Audio Playback
    var isPlayingAudio by remember { mutableStateOf(false) }

    // PDF Preview
    var generatedPdfFile by remember { mutableStateOf<File?>(null) }
    var pdfTitle by remember { mutableStateOf("") }
    var pdfShareMsg by remember { mutableStateOf("") }

    IslamicWatermarkBackground {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        IslamicLogoHeader(compact = true, showSubtitle = false)
                    },
                    actions = {
                        IconButton(onClick = onLogout) {
                            Icon(Icons.Default.Logout, contentDescription = "Logout", tint = IslamicGreen)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = SurfaceWhite)
                )
            },
            containerColor = Color.Transparent
        ) { padding ->
            if (activeChild == null) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No registered student found for this phone number.", color = TextSecondaryGrey)
                }
            } else {
                val childQuran = allQuran.find { it.studentId == activeChild.studentId }
                val childBooks = allBooks.filter { it.studentId == activeChild.studentId }
                val childFees = allFees.filter { it.studentId == activeChild.studentId }
                val childAttendance = allAttendance.filter { it.studentId == activeChild.studentId }
                val childMarks = allTestMarks.filter { it.studentId == activeChild.studentId }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(horizontal = 16.dp)
                ) {
                    // Child Switcher (if parent has multiple children)
                    if (myChildren.size > 1) {
                        ScrollableTabRow(
                            selectedTabIndex = myChildren.indexOfFirst { it.studentId == selectedChildId }.coerceAtLeast(0),
                            edgePadding = 0.dp,
                            containerColor = Color.Transparent,
                            divider = {},
                            indicator = {}
                        ) {
                            myChildren.forEach { ch ->
                                val isSelected = ch.studentId == selectedChildId
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { selectedChildId = ch.studentId },
                                    label = { Text("${ch.name} (Class ${ch.className})", fontSize = 12.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = IslamicGreen,
                                        selectedLabelColor = Color.White
                                    ),
                                    modifier = Modifier.padding(end = 6.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    // Parent Nav Tabs
                    ScrollableTabRow(
                        selectedTabIndex = selectedTab,
                        edgePadding = 0.dp,
                        containerColor = SurfaceWhite,
                        contentColor = IslamicGreen,
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .border(1.dp, BorderGold.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                    ) {
                        listOf("Overview", "Books Progress", "Quran Sabaq", "Fees & Receipts", "Tests & Reports").forEachIndexed { index, title ->
                            Tab(
                                selected = selectedTab == index,
                                onClick = { selectedTab = index },
                                text = {
                                    Text(
                                        text = title,
                                        fontSize = 11.5.sp,
                                        fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                                        color = if (selectedTab == index) IslamicGreen else TextSecondaryGrey
                                    )
                                }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Tab Contents
                    when (selectedTab) {
                        0 -> {
                            // OVERVIEW TAB
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .verticalScroll(rememberScrollState()),
                                verticalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                // Child Summary Card
                                IslamicArchCard {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(54.dp)
                                                .clip(CircleShape)
                                                .background(if (activeChild.gender == "Girl") Color(0xFFFCE7F3) else IslamicGreenContainer)
                                                .border(2.dp, IslamicGold, CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = activeChild.name.take(2).uppercase(),
                                                fontWeight = FontWeight.Bold,
                                                color = if (activeChild.gender == "Girl") Color(0xFFBE185D) else IslamicGreen,
                                                fontSize = 20.sp
                                            )
                                        }

                                        Spacer(modifier = Modifier.width(12.dp))

                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = activeChild.name,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 17.sp,
                                                color = TextPrimaryCharcoal
                                            )
                                            Text(
                                                text = "S/o ${activeChild.fatherName}  •  Class ${activeChild.className}",
                                                fontSize = 12.sp,
                                                color = TextSecondaryGrey
                                            )
                                            Text(
                                                text = "Group: ${activeChild.groupType} | Teacher: ${activeChild.assignedTeacher}",
                                                fontSize = 11.5.sp,
                                                color = IslamicGreen,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        }
                                    }

                                    if (activeChild.stationaryNeeded != null) {
                                        Spacer(modifier = Modifier.height(10.dp))
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = Color(0xFFFFFBEB),
                                            border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.Brush.linearGradient(listOf(Color(0xFFF59E0B), Color(0xFFF59E0B))))
                                        ) {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(10.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(Icons.Default.MenuBook, contentDescription = null, tint = Color(0xFFD97706), modifier = Modifier.size(16.dp))
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(
                                                    text = "Stationary Alert: Apke bache ko kal ${activeChild.stationaryNeeded} chahiye - Al Hadid Academy",
                                                    fontSize = 11.5.sp,
                                                    color = Color(0xFFB45309),
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                    }
                                }

                                // Quick Status Metrics
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    IslamicMetricCard(
                                        title = "Attendance",
                                        value = "96%",
                                        subtitle = "Regular & Punctual",
                                        iconRes = "📅",
                                        accentColor = ColorSuccess,
                                        modifier = Modifier.weight(1f)
                                    )

                                    val latestFee = childFees.firstOrNull()
                                    IslamicMetricCard(
                                        title = "Fee Status",
                                        value = latestFee?.status ?: "Paid",
                                        subtitle = "Rs ${activeChild.monthlyFee.toInt()} / mo",
                                        iconRes = "💳",
                                        accentColor = if (latestFee?.status == "Paid") ColorSuccess else ColorError,
                                        modifier = Modifier.weight(1f)
                                    )
                                }

                                // Latest Announcements
                                IslamicArchCard {
                                    Text(
                                        text = "📢 Latest Academy Announcements",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = IslamicGreen
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    allAnnouncements.take(2).forEach { ann ->
                                        Column(modifier = Modifier.padding(vertical = 4.dp)) {
                                            Text(text = ann.title, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = TextPrimaryCharcoal)
                                            Text(text = ann.message, fontSize = 11.5.sp, color = TextSecondaryGrey)
                                            Text(text = ann.createdAt, fontSize = 10.sp, color = IslamicGoldDark)
                                        }
                                        HorizontalDivider(color = DividerMuted.copy(alpha = 0.5f))
                                    }
                                }

                                // Quick Download Buttons
                                Button(
                                    onClick = {
                                        val pdf = PdfService.generateCharacterCertificatePdf(context, activeChild)
                                        generatedPdfFile = pdf
                                        pdfTitle = "Character Certificate - ${activeChild.name}"
                                        pdfShareMsg = "Official Character Certificate for ${activeChild.name} from Al Hadid Academy."
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = IslamicGoldDark),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(Icons.Default.WorkspacePremium, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Download Official Character Certificate (PDF)", fontWeight = FontWeight.Bold)
                                }

                                Spacer(modifier = Modifier.height(30.dp))
                            }
                        }

                        1 -> {
                            // BOOKS WISE PROGRESS TAB ("Apka Bacha Kis Book Me Acha Hai")
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .verticalScroll(rememberScrollState()),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Text(
                                    text = "Apka Bacha Kis Book Me Acha Hai",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = IslamicGreen,
                                    fontFamily = FontFamily.Serif
                                )
                                Text(
                                    text = "Subject-wise performance, grades & evaluation comments by teachers",
                                    fontSize = 12.sp,
                                    color = TextSecondaryGrey
                                )

                                childBooks.forEach { bp ->
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(14.dp),
                                        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                                    ) {
                                        Column(modifier = Modifier.padding(14.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = bp.subject,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 15.sp,
                                                    color = TextPrimaryCharcoal
                                                )
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Text(
                                                        text = "${bp.percentage}%",
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 13.sp,
                                                        color = IslamicGreen
                                                    )
                                                    Spacer(modifier = Modifier.width(8.dp))
                                                    GradeBadge(grade = bp.grade)
                                                }
                                            }

                                            Spacer(modifier = Modifier.height(6.dp))

                                            LinearProgressIndicator(
                                                progress = { bp.percentage / 100f },
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(7.dp)
                                                    .clip(RoundedCornerShape(4.dp)),
                                                color = if (bp.percentage >= 80) ColorSuccess else if (bp.percentage >= 60) IslamicGold else ColorError,
                                                trackColor = CreamBackground
                                            )

                                            Spacer(modifier = Modifier.height(6.dp))

                                            Text(
                                                text = bp.remarks,
                                                fontSize = 12.sp,
                                                color = TextPrimaryCharcoal,
                                                fontWeight = FontWeight.Medium
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(30.dp))
                            }
                        }

                        2 -> {
                            // QURAN SABAQ TAB
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .verticalScroll(rememberScrollState()),
                                verticalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                Text(
                                    text = "Quranic Lessons & Recitation (Sabaq / Sabaqi)",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = IslamicGreen,
                                    fontFamily = FontFamily.Serif
                                )

                                val q = childQuran ?: QuranProgress("0", activeChild.studentId, "Current", "Surah Yaseen Ayat 1-15", "Para 22 Quarter 1", "Para 1-4", "Good pronunciation", "MashaAllah regular", activeChild.assignedTeacher, true, 28)

                                IslamicArchCard {
                                    Text("📖 Sabaq (Daily Lesson):", fontSize = 12.sp, color = IslamicGreen, fontWeight = FontWeight.Bold)
                                    Text(q.sabaq, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimaryCharcoal)

                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text("🔄 Sabaqi (Recent Revision):", fontSize = 12.sp, color = TextSecondaryGrey)
                                    Text(q.sabaqi, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = TextPrimaryCharcoal)

                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text("📚 Manzil (Old Revision):", fontSize = 12.sp, color = TextSecondaryGrey)
                                    Text(q.manzil, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = TextPrimaryCharcoal)

                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text("✨ Tajweed Guidance:", fontSize = 12.sp, color = IslamicGoldDark)
                                    Text(q.tajweedMistakes, fontSize = 12.sp, color = ColorError)

                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text("💬 Teacher Remarks: ${q.remarks}", fontSize = 12.sp, color = TextSecondaryGrey)

                                    IslamicGoldDivider(modifier = Modifier.padding(vertical = 10.dp))

                                    // Play Recitation Button
                                    Button(
                                        onClick = {
                                            if (isPlayingAudio) {
                                                AudioService.stopPlaying()
                                                isPlayingAudio = false
                                            } else {
                                                isPlayingAudio = true
                                                AudioService.playAudio(context) {
                                                    isPlayingAudio = false
                                                }
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = if (isPlayingAudio) ColorError else IslamicGreen),
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (isPlayingAudio) Icons.Default.Stop else Icons.Default.PlayArrow,
                                            contentDescription = null,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(if (isPlayingAudio) "Playing Recitation..." else "Listen to Child's Recorded Sabaq (${q.audioDurationSec}s)")
                                    }
                                }

                                Spacer(modifier = Modifier.height(30.dp))
                            }
                        }

                        3 -> {
                            // FEES & RECEIPTS TAB
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .verticalScroll(rememberScrollState()),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text(
                                    text = "Official Fee Status & PDF Receipts",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = IslamicGreen,
                                    fontFamily = FontFamily.Serif
                                )

                                childFees.forEach { fee ->
                                    IslamicArchCard {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column {
                                                Text(
                                                    text = "${fee.month} ${fee.year} (${fee.receiptNo})",
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 14.sp,
                                                    color = TextPrimaryCharcoal
                                                )
                                                Text(
                                                    text = "Amount: Rs ${fee.totalPaid.toInt()}  |  Method: ${fee.paymentMethod}",
                                                    fontSize = 11.5.sp,
                                                    color = TextSecondaryGrey
                                                )
                                            }

                                            Button(
                                                onClick = {
                                                    val pdf = PdfService.generateFeeReceiptPdf(context, fee, activeChild)
                                                    generatedPdfFile = pdf
                                                    pdfTitle = "Fee Receipt - ${fee.receiptNo}"
                                                    pdfShareMsg = "Official Fee Receipt for ${activeChild.name} (${fee.month} ${fee.year}) from Al Hadid Academy."
                                                },
                                                colors = ButtonDefaults.buttonColors(containerColor = IslamicGreen),
                                                shape = RoundedCornerShape(8.dp)
                                            ) {
                                                Icon(Icons.Default.Receipt, contentDescription = null, modifier = Modifier.size(14.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("Download Receipt", fontSize = 11.sp)
                                            }
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(30.dp))
                            }
                        }

                        4 -> {
                            // TESTS & REPORTS TAB
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .verticalScroll(rememberScrollState()),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text(
                                    text = "Exam Results & Comprehensive Reports",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = IslamicGreen,
                                    fontFamily = FontFamily.Serif
                                )

                                Button(
                                    onClick = {
                                        val pdf = PdfService.generateWeeklyReportPdf(context, activeChild, childQuran, childBooks, childFees.firstOrNull())
                                        generatedPdfFile = pdf
                                        pdfTitle = "Weekly Progress Report - ${activeChild.name}"
                                        pdfShareMsg = "Al Hadid Academy Weekly Progress Report for ${activeChild.name}."
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = IslamicGreen),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(Icons.Default.Assessment, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Download Weekly Progress Report (PDF)", fontWeight = FontWeight.Bold)
                                }

                                Text("Test Results:", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = IslamicGreen)

                                childMarks.forEach { m ->
                                    val test = allTests.find { it.testId == m.testId } ?: TestRecord(m.testId, "Academic Test", "General", "Tuition", "7", "2026-10-01", 100, "Teacher")
                                    IslamicArchCard {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column {
                                                Text(text = test.title, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = TextPrimaryCharcoal)
                                                Text(text = "Subject: ${test.subject} • Date: ${test.date}", fontSize = 11.sp, color = TextSecondaryGrey)
                                                Text(text = "Score: ${m.obtainedMarks.toInt()}/${m.totalMarks.toInt()} (Pos: ${m.position})", fontSize = 12.sp, color = IslamicGreen, fontWeight = FontWeight.Bold)
                                            }

                                            Button(
                                                onClick = {
                                                    val pdf = PdfService.generateResultCardPdf(context, test, m, activeChild)
                                                    generatedPdfFile = pdf
                                                    pdfTitle = "Result Card - ${test.title}"
                                                    pdfShareMsg = "Test Result for ${activeChild.name}: ${m.obtainedMarks.toInt()}/${m.totalMarks.toInt()} (Grade ${m.grade})."
                                                },
                                                colors = ButtonDefaults.buttonColors(containerColor = IslamicGoldDark),
                                                shape = RoundedCornerShape(8.dp)
                                            ) {
                                                Text("Result Card PDF", fontSize = 10.sp)
                                            }
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(30.dp))
                            }
                        }
                    }
                }
            }
        }
    }

    generatedPdfFile?.let { file ->
        PdfPreviewDialog(
            pdfFile = file,
            documentTitle = pdfTitle,
            shareMessage = pdfShareMsg,
            parentPhone = activeChild?.parentPhone,
            onDismiss = { generatedPdfFile = null }
        )
    }
}
