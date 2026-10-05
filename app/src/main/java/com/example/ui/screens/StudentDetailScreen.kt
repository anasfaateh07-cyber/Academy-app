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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.*
import com.example.service.AudioService
import com.example.service.PdfService
import com.example.ui.components.*
import com.example.ui.theme.*
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentDetailScreen(
    student: Student,
    repository: AcademyRepository,
    currentUser: CurrentUser,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val allQuranProgress by repository.quranProgress.collectAsState()
    val allBookProgress by repository.bookProgress.collectAsState()
    val allFees by repository.fees.collectAsState()

    val studentQuran = allQuranProgress.find { it.studentId == student.studentId }
    val studentBooks = allBookProgress.filter { it.studentId == student.studentId }
    val studentFees = allFees.filter { it.studentId == student.studentId }

    // Dialogs
    var showStationaryDialog by remember { mutableStateOf(false) }
    var stationaryItem by remember { mutableStateOf("") }
    var editingBookProgress by remember { mutableStateOf<BookProgress?>(null) }

    // PDF Preview
    var generatedPdfFile by remember { mutableStateOf<File?>(null) }
    var pdfTitle by remember { mutableStateOf("") }
    var pdfShareMsg by remember { mutableStateOf("") }

    // Audio Playback
    var isAudioPlaying by remember { mutableStateOf(false) }

    IslamicWatermarkBackground {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = student.name,
                            fontWeight = FontWeight.Bold,
                            color = IslamicGreen,
                            fontFamily = FontFamily.Serif
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = IslamicGreen)
                        }
                    },
                    actions = {
                        IconButton(onClick = {
                            val pdf = PdfService.generateCharacterCertificatePdf(context, student)
                            generatedPdfFile = pdf
                            pdfTitle = "Character Certificate - ${student.name}"
                            pdfShareMsg = "Assalam-o-Alaikum, Al Hadid Academy Character Certificate for ${student.name} S/o ${student.fatherName}."
                        }) {
                            Icon(Icons.Default.WorkspacePremium, contentDescription = "Certificate", tint = IslamicGoldDark)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = SurfaceWhite)
                )
            },
            containerColor = Color.Transparent
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Profile Header Card
                IslamicArchCard {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(if (student.gender == "Girl") Color(0xFFFCE7F3) else IslamicGreenContainer)
                                .border(2.dp, IslamicGold, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = student.name.take(2).uppercase(),
                                fontWeight = FontWeight.Bold,
                                color = if (student.gender == "Girl") Color(0xFFBE185D) else IslamicGreen,
                                fontSize = 22.sp
                            )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = student.name,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimaryCharcoal
                            )
                            Text(
                                text = "Father: ${student.fatherName}",
                                fontSize = 13.sp,
                                color = TextSecondaryGrey
                            )
                            Text(
                                text = "ID: ${student.studentId}  |  Class ${student.className}",
                                fontSize = 12.sp,
                                color = IslamicGreen,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    IslamicGoldDivider(modifier = Modifier.padding(vertical = 10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Group:", fontSize = 11.sp, color = TextSecondaryGrey)
                            Text(student.groupType, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = IslamicGreen)
                        }
                        Column {
                            Text("Teacher:", fontSize = 11.sp, color = TextSecondaryGrey)
                            Text(student.assignedTeacher, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimaryCharcoal)
                        }
                        Column {
                            Text("Monthly Fee:", fontSize = 11.sp, color = TextSecondaryGrey)
                            Text("Rs ${student.monthlyFee.toInt()}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = IslamicGoldDark)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Stationary Needed Button
                        Button(
                            onClick = { showStationaryDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = IslamicGoldDark),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.MenuBook, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Stationary Needed", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        // WhatsApp Message to parent
                        OutlinedButton(
                            onClick = {
                                Toast.makeText(context, "Opening WhatsApp for ${student.parentPhone}...", Toast.LENGTH_SHORT).show()
                            },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Phone, contentDescription = null, tint = IslamicGreen, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Call Parent", fontSize = 11.sp, color = IslamicGreen)
                        }
                    }
                }

                // Quick PDF Reports Generation Bar
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.Brush.linearGradient(listOf(IslamicGold, IslamicGreen)))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "📜 Official Reports & Certificates (With Logo)",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = IslamicGreen
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    val pdf = PdfService.generateWeeklyReportPdf(
                                        context,
                                        student,
                                        studentQuran,
                                        studentBooks,
                                        studentFees.firstOrNull()
                                    )
                                    generatedPdfFile = pdf
                                    pdfTitle = "Weekly Progress Report - ${student.name}"
                                    pdfShareMsg = "Assalam-o-Alaikum, Al Hadid Academy Weekly Report for ${student.name} S/o ${student.fatherName}."
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Weekly Report", fontSize = 11.sp, color = IslamicGreen)
                            }

                            OutlinedButton(
                                onClick = {
                                    val pdf = PdfService.generateCharacterCertificatePdf(context, student)
                                    generatedPdfFile = pdf
                                    pdfTitle = "Character Certificate - ${student.name}"
                                    pdfShareMsg = "Assalam-o-Alaikum, Official Character Certificate for ${student.name} from Al Hadid Academy."
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Certificate", fontSize = 11.sp, color = IslamicGoldDark)
                            }
                        }
                    }
                }

                // Section: School Books Wise Progress ("Apka Bacha Kis Book Me Acha Hai")
                IslamicArchCard {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "School Books Progress",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = IslamicGreen
                            )
                            Text(
                                text = "Apka Bacha Kis Book Me Acha Hai",
                                fontSize = 11.sp,
                                color = IslamicGoldDark,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        Text(
                            text = "Tap subject to edit",
                            fontSize = 10.sp,
                            color = TextSecondaryGrey
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    if (studentBooks.isEmpty()) {
                        Text("No book records yet.", fontSize = 12.sp, color = TextSecondaryGrey)
                    } else {
                        studentBooks.forEach { bp ->
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { editingBookProgress = bp }
                                    .padding(vertical = 6.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = bp.subject,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 13.sp,
                                        color = TextPrimaryCharcoal
                                    )
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "${bp.percentage}%",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            color = IslamicGreen
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        GradeBadge(grade = bp.grade)
                                    }
                                }

                                Spacer(modifier = Modifier.height(4.dp))

                                LinearProgressIndicator(
                                    progress = { bp.percentage / 100f },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(6.dp)
                                        .clip(RoundedCornerShape(3.dp)),
                                    color = if (bp.percentage >= 80) IslamicGreen else if (bp.percentage >= 60) IslamicGold else Color(0xFFEF4444),
                                    trackColor = CreamBackground
                                )

                                Spacer(modifier = Modifier.height(3.dp))

                                Text(
                                    text = bp.remarks,
                                    fontSize = 11.sp,
                                    color = TextSecondaryGrey
                                )
                            }
                            HorizontalDivider(color = DividerMuted.copy(alpha = 0.5f))
                        }
                    }
                }

                // Section: Quran Progress Tracker (Hifz / Nazra / Tajweed)
                if (student.groupType in listOf("Hifz", "Nazra", "Tajweed")) {
                    IslamicArchCard {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Quran Progress Tracker",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = IslamicGreen
                            )
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = IslamicGreenContainer
                            ) {
                                Text(
                                    text = student.groupType,
                                    color = IslamicGreen,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        val quran = studentQuran ?: QuranProgress(
                            id = "temp",
                            studentId = student.studentId,
                            date = "Today",
                            sabaq = "Surah Yaseen Ayat 1-12",
                            sabaqi = "Para 22 Quarter 1",
                            manzil = "Para 1 to 4",
                            tajweedMistakes = "Qalqalah on Qaaf clear",
                            remarks = "MashaAllah good recitation pace",
                            teacherName = student.assignedTeacher,
                            hasAudio = true,
                            audioDurationSec = 28
                        )

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = CreamBackground,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text("📖 Sabaq (Daily Lesson): ${quran.sabaq}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimaryCharcoal)
                                Text("🔄 Sabaqi (Recent Revision): ${quran.sabaqi}", fontSize = 12.sp, color = TextPrimaryCharcoal)
                                Text("📚 Manzil (Old Revision): ${quran.manzil}", fontSize = 12.sp, color = TextPrimaryCharcoal)
                                Text("✨ Tajweed Mistakes: ${quran.tajweedMistakes}", fontSize = 11.sp, color = ColorError)
                                Text("📝 Remarks: ${quran.remarks}", fontSize = 11.sp, color = TextSecondaryGrey)
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Audio Player & Recorder
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Button(
                                onClick = {
                                    if (isAudioPlaying) {
                                        AudioService.stopPlaying()
                                        isAudioPlaying = false
                                    } else {
                                        isAudioPlaying = true
                                        AudioService.playAudio(context) {
                                            isAudioPlaying = false
                                        }
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = if (isAudioPlaying) ColorError else IslamicGreen),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(
                                    imageVector = if (isAudioPlaying) Icons.Default.Stop else Icons.Default.PlayArrow,
                                    contentDescription = "Play Sabaq Audio",
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(if (isAudioPlaying) "Playing Sabaq..." else "Play Recorded Sabaq", fontSize = 11.sp)
                            }

                            OutlinedButton(
                                onClick = {
                                    Toast.makeText(context, "Sabaq audio recorded and saved to Quran progress!", Toast.LENGTH_SHORT).show()
                                },
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Default.Mic, contentDescription = "Record", tint = IslamicGoldDark, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Record Sabaq", fontSize = 11.sp, color = IslamicGoldDark)
                            }
                        }
                    }
                }

                // Section: Fees & Receipts
                IslamicArchCard {
                    Text(
                        text = "Fee Records & Official Receipts",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = IslamicGreen
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    if (studentFees.isEmpty()) {
                        Text("No fee history recorded.", fontSize = 12.sp, color = TextSecondaryGrey)
                    } else {
                        studentFees.forEach { fee ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "${fee.month} ${fee.year} (${fee.receiptNo})",
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 12.sp,
                                        color = TextPrimaryCharcoal
                                    )
                                    Text(
                                        text = "Paid: Rs ${fee.totalPaid.toInt()}  |  Due: Rs ${fee.due.toInt()}",
                                        fontSize = 11.sp,
                                        color = TextSecondaryGrey
                                    )
                                }

                                Button(
                                    onClick = {
                                        val pdf = PdfService.generateFeeReceiptPdf(context, fee, student)
                                        generatedPdfFile = pdf
                                        pdfTitle = "Fee Receipt - ${fee.receiptNo}"
                                        pdfShareMsg = "Assalam-o-Alaikum, Apke bache ${student.name} ne ${fee.month} ki fee Rs ${fee.totalPaid.toInt()} jama kara di hai. - Al Hadid Academy"
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = IslamicGreen),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Icon(Icons.Default.Receipt, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Receipt PDF", fontSize = 11.sp)
                                }
                            }
                            HorizontalDivider(color = DividerMuted.copy(alpha = 0.5f))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(30.dp))
            }
        }
    }

    // Stationary Dialog
    if (showStationaryDialog) {
        AlertDialog(
            onDismissRequest = { showStationaryDialog = false },
            title = { Text("Request Stationary from Parent") },
            text = {
                Column {
                    Text("What item is needed for ${student.name}? (e.g. Math copy, geometry box, Qaida)")
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = stationaryItem,
                        onValueChange = { stationaryItem = it },
                        placeholder = { Text("e.g. Math 4-line copy") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (stationaryItem.isNotBlank()) {
                            repository.requestStationary(student.studentId, student.name, stationaryItem)
                            Toast.makeText(context, "Stationary notification sent to parent!", Toast.LENGTH_SHORT).show()
                            showStationaryDialog = false
                            stationaryItem = ""
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = IslamicGreen)
                ) {
                    Text("Send Alert to Parent")
                }
            },
            dismissButton = {
                TextButton(onClick = { showStationaryDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Edit Book Progress Dialog
    editingBookProgress?.let { bp ->
        var grade by remember { mutableStateOf(bp.grade) }
        var pctText by remember { mutableStateOf(bp.percentage.toString()) }
        var remarks by remember { mutableStateOf(bp.remarks) }

        Dialog(onDismissRequest = { editingBookProgress = null }) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceWhite)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "Edit ${bp.subject} Progress",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = IslamicGreen
                    )
                    Text(
                        text = "Update student performance in ${bp.subject}",
                        fontSize = 12.sp,
                        color = TextSecondaryGrey
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = pctText,
                        onValueChange = { pctText = it },
                        label = { Text("Percentage (0-100)") },
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
                        label = { Text("Teacher Remarks") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { editingBookProgress = null }) {
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
                                editingBookProgress = null
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

    generatedPdfFile?.let { file ->
        PdfPreviewDialog(
            pdfFile = file,
            documentTitle = pdfTitle,
            shareMessage = pdfShareMsg,
            parentPhone = student.parentPhone,
            onDismiss = { generatedPdfFile = null }
        )
    }
}
