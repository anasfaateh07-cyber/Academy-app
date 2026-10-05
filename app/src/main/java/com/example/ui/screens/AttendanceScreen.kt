package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
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
import com.example.ui.components.IslamicArchCard
import com.example.ui.components.IslamicGoldDivider
import com.example.ui.components.IslamicMetricCard
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun AttendanceScreen(
    repository: AcademyRepository,
    currentUser: CurrentUser,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val allStudents by repository.students.collectAsState()
    val allAttendance by repository.attendance.collectAsState()

    val todayDate = remember { SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date()) }
    var selectedDate by remember { mutableStateOf(todayDate) }

    val availableGroups = if (currentUser.isFullAdmin) {
        listOf("Hifz", "Nazra", "Tajweed", "Tuition Boy", "Tuition Girl", "Playgroup")
    } else {
        currentUser.allowedGroups
    }

    var selectedGroup by remember { mutableStateOf(availableGroups.firstOrNull() ?: "Tuition Girl") }

    val groupStudents = remember(allStudents, selectedGroup) {
        allStudents.filter { it.groupType == selectedGroup }
    }

    // State of attendance map: studentId -> "Present" or "Absent"
    val attendanceStatusMap = remember(groupStudents, allAttendance, selectedDate) {
        mutableStateMapOf<String, String>().apply {
            groupStudents.forEach { st ->
                val existing = allAttendance.find { it.date == selectedDate && it.studentId == st.studentId }
                put(st.studentId, existing?.status ?: "Present")
            }
        }
    }

    val presentCount = attendanceStatusMap.values.count { it == "Present" }
    val absentCount = attendanceStatusMap.values.count { it == "Absent" }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Top Overview
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            IslamicMetricCard(
                title = "Present Today",
                value = "$presentCount",
                subtitle = "Active in $selectedGroup",
                iconRes = "✓",
                accentColor = ColorSuccess,
                modifier = Modifier.weight(1f)
            )

            IslamicMetricCard(
                title = "Absent Today",
                value = "$absentCount",
                subtitle = "Alert sent to parents",
                iconRes = "✗",
                accentColor = ColorError,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Group Selector Tabs
        ScrollableTabRow(
            selectedTabIndex = availableGroups.indexOf(selectedGroup).coerceAtLeast(0),
            edgePadding = 0.dp,
            containerColor = Color.Transparent,
            divider = {},
            indicator = {}
        ) {
            availableGroups.forEach { grp ->
                val isSelected = selectedGroup == grp
                FilterChip(
                    selected = isSelected,
                    onClick = { selectedGroup = grp },
                    label = { Text(grp, fontSize = 12.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = IslamicGreen,
                        selectedLabelColor = Color.White
                    ),
                    modifier = Modifier.padding(end = 6.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Action Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Students in $selectedGroup (${groupStudents.size})",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = IslamicGreen
            )

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                TextButton(
                    onClick = {
                        groupStudents.forEach { st -> attendanceStatusMap[st.studentId] = "Present" }
                    },
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text("All Present", fontSize = 11.sp, color = IslamicGreen)
                }

                Button(
                    onClick = {
                        val recordsToSave = groupStudents.map { st ->
                            val status = attendanceStatusMap[st.studentId] ?: "Present"
                            AttendanceRecord(
                                id = "ATT-${selectedDate}-${st.studentId}",
                                date = selectedDate,
                                studentId = st.studentId,
                                studentName = st.name,
                                className = st.className,
                                groupType = st.groupType,
                                status = status,
                                markedBy = currentUser.name
                            )
                        }
                        repository.markAttendance(recordsToSave)
                        Toast.makeText(context, "Attendance saved! Parents of absent students notified.", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = IslamicGreen),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Text("Save Attendance", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Students Attendance List
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(groupStudents, key = { it.studentId }) { st ->
                val currentStatus = attendanceStatusMap[st.studentId] ?: "Present"
                val isPresent = currentStatus == "Present"

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.Brush.horizontalGradient(listOf(if (isPresent) ColorSuccess.copy(alpha = 0.4f) else ColorError.copy(alpha = 0.4f), Color.Transparent)))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = st.name,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = TextPrimaryCharcoal
                            )
                            Text(
                                text = "Class ${st.className}  •  ${st.studentId}",
                                fontSize = 11.sp,
                                color = TextSecondaryGrey
                            )
                        }

                        // Present / Absent Toggle Buttons
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            // Present Button
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isPresent) ColorSuccess else Color(0xFFE5E7EB),
                                modifier = Modifier
                                    .clickable { attendanceStatusMap[st.studentId] = "Present" }
                                    .clip(RoundedCornerShape(8.dp))
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Present",
                                        tint = if (isPresent) Color.White else TextSecondaryGrey,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Present",
                                        color = if (isPresent) Color.White else TextSecondaryGrey,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            // Absent Button
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (!isPresent) ColorError else Color(0xFFE5E7EB),
                                modifier = Modifier
                                    .clickable { attendanceStatusMap[st.studentId] = "Absent" }
                                    .clip(RoundedCornerShape(8.dp))
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Absent",
                                        tint = if (!isPresent) Color.White else TextSecondaryGrey,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Absent",
                                        color = if (!isPresent) Color.White else TextSecondaryGrey,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }
}
