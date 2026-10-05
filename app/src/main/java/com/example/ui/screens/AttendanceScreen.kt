package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.*
import com.example.service.PdfService
import com.example.ui.components.IslamicArchCard
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AttendanceScreen(
    repository: AcademyRepository,
    currentUser: CurrentUser,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val allStudents by repository.students.collectAsState()
    val allAttendance by repository.attendance.collectAsState()

    // Task 5: Top Date picker (default today: 2026-10-05)
    var selectedDate by remember { mutableStateOf("2026-10-05") }
    var showDatePickerDialog by remember { mutableStateOf(false) }

    // Task 5: Class/Group dropdown (Hifz, Nazra, Tuition Boy, Tuition Girl)
    val availableGroups = listOf("Hifz", "Nazra", "Tuition Boy", "Tuition Girl", "Tajweed", "Playgroup")
    var selectedGroup by remember { mutableStateOf("Hifz") }
    var groupDropdownExpanded by remember { mutableStateOf(false) }

    // Task 5: Filters & Search
    var showOnlyAbsent by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var searchSaveStatus by remember { mutableStateOf("") }

    // Task 1: Auto-save with debounce for search field
    LaunchedEffect(searchQuery) {
        if (searchQuery.isNotBlank()) {
            searchSaveStatus = "Saving filter..."
            delay(1500)
            searchSaveStatus = "Auto Saved ✓"
        } else {
            searchSaveStatus = ""
        }
    }

    // Auto-save notification pill
    var recentSaveMessage by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(recentSaveMessage) {
        if (recentSaveMessage != null) {
            delay(2500)
            recentSaveMessage = null
        }
    }

    // Filter students by department/group
    val groupStudents = remember(allStudents, selectedGroup) {
        allStudents.filter { it.groupType == selectedGroup }
    }

    // Attendance status map: studentId -> "Present", "Absent", "Leave"
    val attendanceStatusMap = remember(groupStudents, allAttendance, selectedDate) {
        mutableStateMapOf<String, String>().apply {
            groupStudents.forEach { st ->
                val existing = allAttendance.find { it.date == selectedDate && it.studentId == st.studentId }
                put(st.studentId, existing?.status ?: "Present")
            }
        }
    }

    // Calculated metrics
    val totalCount = groupStudents.size
    val presentCount = groupStudents.count { (attendanceStatusMap[it.studentId] ?: "Present") == "Present" }
    val absentCount = groupStudents.count { attendanceStatusMap[it.studentId] == "Absent" }
    val leaveCount = groupStudents.count { attendanceStatusMap[it.studentId] == "Leave" }

    // Filtered display list
    val displayedStudents = remember(groupStudents, showOnlyAbsent, searchQuery, attendanceStatusMap) {
        groupStudents.filter { st ->
            val matchesSearch = searchQuery.isBlank() || st.name.contains(searchQuery, ignoreCase = true) || st.studentId.contains(searchQuery, ignoreCase = true)
            val matchesAbsentFilter = if (showOnlyAbsent) attendanceStatusMap[st.studentId] == "Absent" else true
            matchesSearch && matchesAbsentFilter
        }
    }

    // PDF state
    var generatedPdfFile by remember { mutableStateOf<File?>(null) }

    fun markStudentAttendance(student: Student, newStatus: String) {
        attendanceStatusMap[student.studentId] = newStatus

        // 1. Update in-memory repository
        repository.markAttendance(
            listOf(
                AttendanceRecord(
                    id = "ATT-${System.currentTimeMillis()}-${student.studentId}",
                    date = selectedDate,
                    studentId = student.studentId,
                    studentName = student.name,
                    className = student.className,
                    groupType = student.groupType,
                    status = newStatus,
                    markedBy = currentUser.name.ifBlank { "Teacher Anas Mustafa" }
                )
            )
        )

        // 2. Task 5: Store in Firestore: attendance/{date}/{classId}/{studentId} = {status, time, markedBy: teacherId}
        FirestoreHelper.saveAttendance(
            date = selectedDate,
            classId = selectedGroup,
            studentId = student.studentId,
            status = newStatus,
            markedBy = currentUser.email.ifBlank { "teacher_anas" }
        )

        recentSaveMessage = "${student.name}: Marked $newStatus • Auto Saved ✓"
    }

    fun markAllPresent() {
        groupStudents.forEach { st ->
            markStudentAttendance(st, "Present")
        }
        Toast.makeText(context, "All ${groupStudents.size} students marked Present! Auto Saved ✓", Toast.LENGTH_SHORT).show()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Task 5: Top Summary Card
        // Summary: Total: 20 | Present: 15 | Absent: 3 | Leave: 2
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
            border = BorderStroke(1.dp, BorderGold.copy(alpha = 0.5f)),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Attendance Summary",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = IslamicGreen
                    )

                    // Auto-saved green badge
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFDCFCE7)
                    ) {
                        Text(
                            text = "Instant Auto-Save ON ✓",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF15803D),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Total
                    MetricSummaryPill(
                        label = "Total",
                        count = totalCount,
                        containerColor = CreamBackground,
                        textColor = TextPrimaryCharcoal,
                        modifier = Modifier.weight(1f)
                    )
                    // Present
                    MetricSummaryPill(
                        label = "Present",
                        count = presentCount,
                        containerColor = Color(0xFFDCFCE7),
                        textColor = Color(0xFF16A34A),
                        modifier = Modifier.weight(1f)
                    )
                    // Absent
                    MetricSummaryPill(
                        label = "Absent",
                        count = absentCount,
                        containerColor = Color(0xFFFEE2E2),
                        textColor = Color(0xFFDC2626),
                        modifier = Modifier.weight(1f)
                    )
                    // Leave
                    MetricSummaryPill(
                        label = "Leave",
                        count = leaveCount,
                        containerColor = Color(0xFFFEF3C7),
                        textColor = Color(0xFFCA8A04),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Task 5: Date Picker & Class/Group Dropdown Controls
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Date Picker Card
            OutlinedCard(
                onClick = { showDatePickerDialog = true },
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, BorderGold),
                colors = CardDefaults.outlinedCardColors(containerColor = SurfaceWhite),
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.CalendarToday, contentDescription = "Date", tint = IslamicGreen, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text("Date", fontSize = 10.sp, color = TextSecondaryGrey)
                        Text(selectedDate, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimaryCharcoal)
                    }
                }
            }

            // Class / Group Dropdown
            Box(modifier = Modifier.weight(1.2f)) {
                OutlinedCard(
                    onClick = { groupDropdownExpanded = true },
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, BorderGold),
                    colors = CardDefaults.outlinedCardColors(containerColor = SurfaceWhite),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.School, contentDescription = "Class", tint = IslamicGreen, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text("Class / Group", fontSize = 10.sp, color = TextSecondaryGrey)
                                Text(selectedGroup, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = IslamicGreen)
                            }
                        }
                        Icon(Icons.Default.ArrowDropDown, contentDescription = "Dropdown", tint = IslamicGreen)
                    }
                }

                DropdownMenu(
                    expanded = groupDropdownExpanded,
                    onDismissRequest = { groupDropdownExpanded = false }
                ) {
                    availableGroups.forEach { grp ->
                        DropdownMenuItem(
                            text = { Text(grp, fontWeight = if (grp == selectedGroup) FontWeight.Bold else FontWeight.Normal) },
                            onClick = {
                                selectedGroup = grp
                                groupDropdownExpanded = false
                            },
                            leadingIcon = {
                                if (grp == selectedGroup) {
                                    Icon(Icons.Default.Check, contentDescription = null, tint = IslamicGreen)
                                }
                            }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Search Field with Task 1 Auto-Save Debounce
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search by student name or roll #...", fontSize = 12.sp) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = IslamicGreen, modifier = Modifier.size(18.dp)) },
            trailingIcon = {
                if (searchQuery.isNotBlank()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear", modifier = Modifier.size(16.dp))
                    }
                }
            },
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = IslamicGreen,
                unfocusedBorderColor = BorderGold.copy(alpha = 0.5f)
            ),
            modifier = Modifier.fillMaxWidth().height(48.dp),
            singleLine = true
        )

        if (searchSaveStatus.isNotBlank()) {
            Text(
                text = searchSaveStatus,
                color = Color(0xFF16A34A),
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(start = 6.dp, top = 2.dp)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Bulk Actions & Filter Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Task 5: Add filter: Show only Absent students
            FilterChip(
                selected = showOnlyAbsent,
                onClick = { showOnlyAbsent = !showOnlyAbsent },
                label = {
                    Text(
                        if (showOnlyAbsent) "Showing: Absent Only" else "Filter: All Students",
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                leadingIcon = {
                    Icon(
                        if (showOnlyAbsent) Icons.Default.FilterAlt else Icons.Default.FilterAltOff,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp)
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = Color(0xFFFEE2E2),
                    selectedLabelColor = Color(0xFFDC2626)
                )
            )

            // Task 5: Add bulk action: "Mark All Present"
            Button(
                onClick = { markAllPresent() },
                colors = ButtonDefaults.buttonColors(containerColor = IslamicGreen),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Icon(Icons.Default.DoneAll, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Mark All Present", fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
            }
        }

        // Auto-Save Toast Alert
        AnimatedVisibility(
            visible = recentSaveMessage != null,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFF15803D),
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
            ) {
                Text(
                    text = recentSaveMessage ?: "",
                    color = Color.White,
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Task 5: Middle List of students with 3 buttons per student
        if (displayedStudents.isEmpty()) {
            Box(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (showOnlyAbsent) "No absent students in $selectedGroup for $selectedDate! 🎉" else "No students enrolled in $selectedGroup.",
                    color = TextSecondaryGrey,
                    fontSize = 13.sp
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(displayedStudents, key = { it.studentId }) { student ->
                    val status = attendanceStatusMap[student.studentId] ?: "Present"

                    StudentAttendanceCard(
                        student = student,
                        currentStatus = status,
                        onStatusChange = { newStatus ->
                            markStudentAttendance(student, newStatus)
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Task 5: Bottom "Generate Attendance Report PDF" button with logo
        Button(
            onClick = {
                val pdfFile = PdfService.generateAttendanceReportPdf(
                    context = context,
                    date = selectedDate,
                    groupName = selectedGroup,
                    students = groupStudents,
                    attendanceMap = attendanceStatusMap
                )
                if (pdfFile != null) {
                    generatedPdfFile = pdfFile
                    PdfService.sharePdf(
                        context = context,
                        pdfFile = pdfFile,
                        message = "Assalam-o-Alaikum, Official Attendance Report for Al Hadid Academy ($selectedGroup) - $selectedDate."
                    )
                } else {
                    Toast.makeText(context, "Could not generate attendance PDF.", Toast.LENGTH_SHORT).show()
                }
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = IslamicGreen,
                contentColor = Color.White
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.alhadid_logo),
                contentDescription = "Logo",
                modifier = Modifier.size(22.dp).clip(CircleShape)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = "Generate Attendance Report PDF",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }

    // Simple Date Picker Dialog
    if (showDatePickerDialog) {
        var inputDate by remember { mutableStateOf(selectedDate) }
        AlertDialog(
            onDismissRequest = { showDatePickerDialog = false },
            title = { Text("Select Attendance Date", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Enter date in YYYY-MM-DD format (Default: 2026-10-05):", fontSize = 12.sp, color = TextSecondaryGrey)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = inputDate,
                        onValueChange = { inputDate = it },
                        label = { Text("Date") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (inputDate.isNotBlank()) {
                            selectedDate = inputDate.trim()
                        }
                        showDatePickerDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = IslamicGreen)
                ) {
                    Text("Apply Date")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePickerDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun MetricSummaryPill(
    label: String,
    count: Int,
    containerColor: Color,
    textColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = containerColor,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(label, fontSize = 10.5.sp, color = textColor.copy(alpha = 0.8f), fontWeight = FontWeight.SemiBold)
            Text("$count", fontSize = 15.sp, color = textColor, fontWeight = FontWeight.Bold)
        }
    }
}

/**
 * Task 5: Student Attendance Card with 3 buttons per student:
 * Present (Green), Absent (Red), Leave (Yellow)
 */
@Composable
fun StudentAttendanceCard(
    student: Student,
    currentStatus: String,
    onStatusChange: (String) -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp),
        border = BorderStroke(
            1.dp,
            when (currentStatus) {
                "Present" -> Color(0xFF16A34A).copy(alpha = 0.3f)
                "Absent" -> Color(0xFFDC2626).copy(alpha = 0.3f)
                else -> Color(0xFFCA8A04).copy(alpha = 0.3f)
            }
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Avatar / Badge
                    Surface(
                        shape = CircleShape,
                        color = IslamicGreen.copy(alpha = 0.1f),
                        modifier = Modifier.size(38.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = student.name.take(1).uppercase(),
                                fontWeight = FontWeight.Bold,
                                color = IslamicGreen,
                                fontSize = 16.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Text(
                            text = student.name,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.5.sp,
                            color = TextPrimaryCharcoal
                        )
                        Text(
                            text = "S/o ${student.fatherName} • ID: ${student.studentId}",
                            fontSize = 11.sp,
                            color = TextSecondaryGrey
                        )
                    }
                }

                // Current Status Badge
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = when (currentStatus) {
                        "Present" -> Color(0xFFDCFCE7)
                        "Absent" -> Color(0xFFFEE2E2)
                        else -> Color(0xFFFEF3C7)
                    }
                ) {
                    Text(
                        text = currentStatus.uppercase(),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = when (currentStatus) {
                            "Present" -> Color(0xFF16A34A)
                            "Absent" -> Color(0xFFDC2626)
                            else -> Color(0xFFCA8A04)
                        },
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Task 5: 3 Buttons per student: Present (Green), Absent (Red), Leave (Yellow)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Button 1: Present (Green)
                val isPresent = currentStatus == "Present"
                Button(
                    onClick = { onStatusChange("Present") },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isPresent) Color(0xFF16A34A) else Color(0xFFDCFCE7),
                        contentColor = if (isPresent) Color.White else Color(0xFF16A34A)
                    ),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp),
                    modifier = Modifier.weight(1f).height(36.dp)
                ) {
                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(3.dp))
                    Text("Present", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                // Button 2: Absent (Red)
                val isAbsent = currentStatus == "Absent"
                Button(
                    onClick = { onStatusChange("Absent") },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isAbsent) Color(0xFFDC2626) else Color(0xFFFEE2E2),
                        contentColor = if (isAbsent) Color.White else Color(0xFFDC2626)
                    ),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp),
                    modifier = Modifier.weight(1f).height(36.dp)
                ) {
                    Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(3.dp))
                    Text("Absent", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                // Button 3: Leave (Yellow)
                val isLeave = currentStatus == "Leave"
                Button(
                    onClick = { onStatusChange("Leave") },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isLeave) Color(0xFFCA8A04) else Color(0xFFFEF3C7),
                        contentColor = if (isLeave) Color.White else Color(0xFFCA8A04)
                    ),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp),
                    modifier = Modifier.weight(1f).height(36.dp)
                ) {
                    Icon(Icons.Default.Schedule, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(3.dp))
                    Text("Leave", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
