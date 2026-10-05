package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
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
import androidx.compose.ui.window.Dialog
import com.example.data.*
import com.example.ui.components.GradeBadge
import com.example.ui.components.IslamicArchCard
import com.example.ui.components.IslamicGoldDivider
import com.example.ui.theme.*

@Composable
fun BooksProgressScreen(
    repository: AcademyRepository,
    currentUser: CurrentUser,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val allStudents by repository.students.collectAsState()
    val allBookProgress by repository.bookProgress.collectAsState()

    val visibleStudents = remember(allStudents, currentUser) {
        allStudents.filter { st ->
            if (currentUser.isFullAdmin) true else currentUser.allowedGroups.contains(st.groupType)
        }
    }

    var selectedStudentId by remember { mutableStateOf(visibleStudents.firstOrNull()?.studentId ?: "") }
    val currentStudent = visibleStudents.find { it.studentId == selectedStudentId } ?: visibleStudents.firstOrNull()

    val studentBooks = remember(allBookProgress, selectedStudentId) {
        allBookProgress.filter { it.studentId == selectedStudentId }
    }

    var editingProgress by remember { mutableStateOf<BookProgress?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "School Books Wise Progress",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = IslamicGreen,
            fontFamily = FontFamily.Serif
        )
        Text(
            text = "Apka Bacha Kis Book Me Acha Hai - Real-time Subject Evaluation",
            fontSize = 12.sp,
            color = IslamicGoldDark,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Student selector chips
        ScrollableTabRow(
            selectedTabIndex = visibleStudents.indexOfFirst { it.studentId == selectedStudentId }.coerceAtLeast(0),
            edgePadding = 0.dp,
            containerColor = Color.Transparent,
            divider = {},
            indicator = {}
        ) {
            visibleStudents.forEach { st ->
                val isSelected = st.studentId == selectedStudentId
                FilterChip(
                    selected = isSelected,
                    onClick = { selectedStudentId = st.studentId },
                    label = { Text("${st.name} (${st.className})", fontSize = 12.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = IslamicGreen,
                        selectedLabelColor = Color.White
                    ),
                    modifier = Modifier.padding(end = 6.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (currentStudent != null) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = SurfaceWhite,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "${currentStudent.name} S/o ${currentStudent.fatherName}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = TextPrimaryCharcoal
                        )
                        Text(
                            text = "Class: ${currentStudent.className}  •  Group: ${currentStudent.groupType}  •  Teacher: ${currentStudent.assignedTeacher}",
                            fontSize = 11.sp,
                            color = TextSecondaryGrey
                        )
                    }

                    Surface(shape = RoundedCornerShape(8.dp), color = IslamicGreenContainer) {
                        Text(
                            text = "Tap to Edit",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = IslamicGreen,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Subjects List
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(studentBooks, key = { it.id }) { bp ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { editingProgress = bp },
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.Brush.linearGradient(listOf(IslamicGold.copy(alpha = 0.3f), Color.Transparent)))
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
                                Spacer(modifier = Modifier.width(6.dp))
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Edit",
                                    tint = TextSecondaryGrey,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        LinearProgressIndicator(
                            progress = { bp.percentage / 100f },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(7.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = if (bp.percentage >= 85) ColorSuccess else if (bp.percentage >= 70) IslamicGreen else if (bp.percentage >= 50) IslamicGold else ColorError,
                            trackColor = CreamBackground
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = bp.remarks,
                            fontSize = 12.sp,
                            color = TextPrimaryCharcoal,
                            fontWeight = FontWeight.Medium
                        )

                        Text(
                            text = "Evaluated by: ${bp.updatedBy}  •  ${bp.updatedDate}",
                            fontSize = 10.sp,
                            color = TextSecondaryGrey
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }

    // Edit Dialog
    editingProgress?.let { bp ->
        var grade by remember { mutableStateOf(bp.grade) }
        var pctText by remember { mutableStateOf(bp.percentage.toString()) }
        var remarks by remember { mutableStateOf(bp.remarks) }

        Dialog(onDismissRequest = { editingProgress = null }) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceWhite)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "Update ${bp.subject} Progress",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = IslamicGreen
                    )
                    Text(
                        text = "Modify percentage, grade and teacher remarks",
                        fontSize = 12.sp,
                        color = TextSecondaryGrey
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = pctText,
                        onValueChange = { pctText = it },
                        label = { Text("Percentage (0 - 100)") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text("Grade:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = IslamicGreen)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        listOf("A+", "A", "B", "C", "Needs Improvement").forEach { g ->
                            FilterChip(
                                selected = grade == g,
                                onClick = { grade = g },
                                label = { Text(g, fontSize = 10.sp) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = remarks,
                        onValueChange = { remarks = it },
                        label = { Text("Teacher Remarks (Urdu / English)") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { editingProgress = null }) {
                            Text("Cancel")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                val updated = bp.copy(
                                    percentage = pctText.toIntOrNull()?.coerceIn(0, 100) ?: bp.percentage,
                                    grade = grade,
                                    remarks = remarks,
                                    updatedBy = currentUser.name
                                )
                                repository.updateBookProgress(updated)
                                editingProgress = null
                                Toast.makeText(context, "${bp.subject} progress updated!", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = IslamicGreen)
                        ) {
                            Text("Save")
                        }
                    }
                }
            }
        }
    }
}
