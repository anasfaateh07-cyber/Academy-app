package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import com.example.service.PdfService
import com.example.ui.components.GradeBadge
import com.example.ui.components.IslamicArchCard
import com.example.ui.components.IslamicGoldDivider
import com.example.ui.components.PdfPreviewDialog
import com.example.ui.theme.*
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun TestsScreen(
    repository: AcademyRepository,
    currentUser: CurrentUser,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val allTests by repository.tests.collectAsState()
    val allMarks by repository.testMarks.collectAsState()
    val allStudents by repository.students.collectAsState()

    var showCreateDialog by remember { mutableStateOf(false) }
    var enteringMarksForTest by remember { mutableStateOf<TestRecord?>(null) }

    // PDF Preview
    var generatedPdfFile by remember { mutableStateOf<File?>(null) }
    var pdfTitle by remember { mutableStateOf("") }
    var pdfShareMsg by remember { mutableStateOf("") }
    var parentPhone by remember { mutableStateOf<String?>(null) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = Color.Transparent,
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showCreateDialog = true },
                containerColor = IslamicGreen,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp),
                icon = { Icon(Icons.Default.Add, contentDescription = "Create Test") },
                text = { Text("Create Test / Exam", fontWeight = FontWeight.Bold) }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            Text(
                text = "Assessments & Examination Center",
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = IslamicGreen,
                fontFamily = FontFamily.Serif
            )
            Text(
                text = "Manage Weekly Tests, Monthly Tests & Generate Result Card PDFs with Logo",
                fontSize = 12.sp,
                color = TextSecondaryGrey
            )

            Spacer(modifier = Modifier.height(12.dp))

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(allTests, key = { it.testId }) { test ->
                    val marksForTest = allMarks.filter { it.testId == test.testId }

                    IslamicArchCard {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = test.title,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimaryCharcoal
                                )
                                Text(
                                    text = "${test.subject}  •  ${test.date}",
                                    fontSize = 12.sp,
                                    color = TextSecondaryGrey
                                )
                            }

                            Surface(shape = RoundedCornerShape(8.dp), color = CreamBackground) {
                                Text(
                                    text = "Total: ${test.totalMarks} Marks",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = IslamicGreen,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Class/Group: ${test.groupType} (${test.className})",
                                fontSize = 11.sp,
                                color = IslamicGoldDark,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "Created by: ${test.createdBy}",
                                fontSize = 11.sp,
                                color = TextSecondaryGrey
                            )
                        }

                        IslamicGoldDivider(modifier = Modifier.padding(vertical = 8.dp))

                        // Marks Entered
                        Text(
                            text = "Result Records (${marksForTest.size} submitted)",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = IslamicGreen
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        marksForTest.forEach { m ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 3.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = m.studentName,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 12.sp,
                                        color = TextPrimaryCharcoal
                                    )
                                    Text(
                                        text = "Marks: ${m.obtainedMarks.toInt()}/${m.totalMarks.toInt()}  |  Pos: ${m.position}",
                                        fontSize = 10.5.sp,
                                        color = TextSecondaryGrey
                                    )
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    GradeBadge(grade = m.grade)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    IconButton(
                                        onClick = {
                                            val st = allStudents.find { it.studentId == m.studentId } ?: Student(
                                                studentId = m.studentId,
                                                name = m.studentName,
                                                fatherName = "Guardian",
                                                gender = "Boy",
                                                dateOfBirth = "2014-01-01",
                                                className = "7",
                                                groupType = "Tuition Boy",
                                                parentPhone = "+92 300 1234567",
                                                assignedTeacher = "Anas Mustafa"
                                            )
                                            val pdf = PdfService.generateResultCardPdf(context, test, m, st)
                                            generatedPdfFile = pdf
                                            pdfTitle = "Result Card - ${test.title}"
                                            parentPhone = st.parentPhone
                                            pdfShareMsg = "Assalam-o-Alaikum, Al Hadid Academy Test Result for ${st.name}: Obtained ${m.obtainedMarks.toInt()}/${m.totalMarks.toInt()} (Grade ${m.grade})."
                                        },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(Icons.Default.Receipt, contentDescription = "PDF Result", tint = IslamicGreen, modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Button(
                            onClick = { enteringMarksForTest = test },
                            colors = ButtonDefaults.buttonColors(containerColor = IslamicGreen),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.EditNote, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Enter / Update Marks for Students", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }
    }

    if (showCreateDialog) {
        CreateTestDialog(
            currentUser = currentUser,
            onDismiss = { showCreateDialog = false },
            onCreate = { newTest ->
                repository.addTest(newTest)
                showCreateDialog = false
                Toast.makeText(context, "Test created successfully!", Toast.LENGTH_SHORT).show()
            }
        )
    }

    enteringMarksForTest?.let { test ->
        val groupStudents = allStudents.filter { it.groupType == test.groupType || test.groupType == "All" }
        EnterMarksDialog(
            test = test,
            students = groupStudents,
            existingMarks = allMarks.filter { it.testId == test.testId },
            onDismiss = { enteringMarksForTest = null },
            onSave = { marksList ->
                repository.saveTestMarks(marksList)
                enteringMarksForTest = null
                Toast.makeText(context, "Test marks saved and results updated!", Toast.LENGTH_SHORT).show()
            }
        )
    }

    generatedPdfFile?.let { file ->
        PdfPreviewDialog(
            pdfFile = file,
            documentTitle = pdfTitle,
            shareMessage = pdfShareMsg,
            parentPhone = parentPhone,
            onDismiss = { generatedPdfFile = null }
        )
    }
}

@Composable
fun CreateTestDialog(
    currentUser: CurrentUser,
    onDismiss: () -> Unit,
    onCreate: (TestRecord) -> Unit
) {
    var title by remember { mutableStateOf("Monthly Test - October 2026") }
    var subject by remember { mutableStateOf("Mathematics") }
    var groupType by remember { mutableStateOf("Tuition Boy") }
    var className by remember { mutableStateOf("7, 8") }
    var totalMarksText by remember { mutableStateOf("100") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceWhite)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "Create New Test / Exam",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = IslamicGreen
                )

                IslamicGoldDivider(modifier = Modifier.padding(vertical = 8.dp))

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Test Title") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = subject,
                    onValueChange = { subject = it },
                    label = { Text("Subject (e.g. Science, Math, Tajweed)") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = className,
                        onValueChange = { className = it },
                        label = { Text("Class") },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = totalMarksText,
                        onValueChange = { totalMarksText = it },
                        label = { Text("Total Marks") },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text("Group:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = IslamicGreen)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    listOf("Tuition Boy", "Tuition Girl", "Hifz", "Playgroup").forEach { grp ->
                        FilterChip(
                            selected = groupType == grp,
                            onClick = { groupType = grp },
                            label = { Text(grp, fontSize = 11.sp) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("Cancel") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val dateStr = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
                            onCreate(
                                TestRecord(
                                    testId = "T-${System.currentTimeMillis()}",
                                    title = title,
                                    subject = subject,
                                    groupType = groupType,
                                    className = className,
                                    date = dateStr,
                                    totalMarks = totalMarksText.toIntOrNull() ?: 100,
                                    createdBy = currentUser.name
                                )
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = IslamicGreen)
                    ) {
                        Text("Create")
                    }
                }
            }
        }
    }
}

@Composable
fun EnterMarksDialog(
    test: TestRecord,
    students: List<Student>,
    existingMarks: List<TestMarks>,
    onDismiss: () -> Unit,
    onSave: (List<TestMarks>) -> Unit
) {
    val marksInputMap = remember(students, existingMarks) {
        mutableStateMapOf<String, String>().apply {
            students.forEach { st ->
                val existing = existingMarks.find { it.studentId == st.studentId }
                put(st.studentId, existing?.obtainedMarks?.toInt()?.toString() ?: "85")
            }
        }
    }

    val remarksMap = remember(students, existingMarks) {
        mutableStateMapOf<String, String>().apply {
            students.forEach { st ->
                val existing = existingMarks.find { it.studentId == st.studentId }
                put(st.studentId, existing?.remarks ?: "Good performance")
            }
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceWhite)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                Text(
                    text = "Enter Marks: ${test.title}",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = IslamicGreen
                )
                Text(
                    text = "Subject: ${test.subject}  |  Total: ${test.totalMarks}",
                    fontSize = 12.sp,
                    color = TextSecondaryGrey
                )

                IslamicGoldDivider(modifier = Modifier.padding(vertical = 8.dp))

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(students, key = { it.studentId }) { st ->
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = CreamBackground,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(
                                    text = "${st.name} (${st.studentId})",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = TextPrimaryCharcoal
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    OutlinedTextField(
                                        value = marksInputMap[st.studentId] ?: "",
                                        onValueChange = { marksInputMap[st.studentId] = it },
                                        label = { Text("Score") },
                                        modifier = Modifier.weight(1f),
                                        singleLine = true
                                    )
                                    OutlinedTextField(
                                        value = remarksMap[st.studentId] ?: "",
                                        onValueChange = { remarksMap[st.studentId] = it },
                                        label = { Text("Teacher Remarks") },
                                        modifier = Modifier.weight(2f),
                                        singleLine = true
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("Cancel") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val results = students.mapIndexed { idx, st ->
                                val score = marksInputMap[st.studentId]?.toDoubleOrNull() ?: 0.0
                                val pct = (score / test.totalMarks) * 100
                                val grade = when {
                                    pct >= 90 -> "A+"
                                    pct >= 80 -> "A"
                                    pct >= 70 -> "B"
                                    pct >= 60 -> "C"
                                    else -> "Needs Improvement"
                                }
                                TestMarks(
                                    id = "TM-${test.testId}-${st.studentId}",
                                    testId = test.testId,
                                    studentId = st.studentId,
                                    studentName = st.name,
                                    obtainedMarks = score,
                                    totalMarks = test.totalMarks.toDouble(),
                                    grade = grade,
                                    position = if (idx == 0) "1st" else if (idx == 1) "2nd" else if (idx == 2) "3rd" else "-",
                                    remarks = remarksMap[st.studentId] ?: ""
                                )
                            }
                            onSave(results)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = IslamicGreen)
                    ) {
                        Text("Save All Marks", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
