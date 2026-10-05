package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.*
import com.example.service.AudioService
import com.example.ui.components.IslamicArchCard
import com.example.ui.components.IslamicGoldDivider
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuranProgressScreen(
    repository: AcademyRepository,
    currentUser: CurrentUser,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val allQuranProgress by repository.quranProgress.collectAsState()
    val allStudents by repository.students.collectAsState()

    val todayDate = remember { SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date()) }

    // Top: Select Class: Hifz Group / Nazra
    var selectedGroup by remember { mutableStateOf("Hifz") }
    val groupOptions = listOf("Hifz", "Nazra", "Tajweed")

    // Filter students by selected group
    val studentsInGroup = remember(allStudents, selectedGroup) {
        allStudents.filter { it.groupType == selectedGroup }
    }

    var searchQuery by remember { mutableStateOf("") }
    val displayedStudents = remember(studentsInGroup, searchQuery) {
        studentsInGroup.filter {
            searchQuery.isBlank() || it.name.contains(searchQuery, ignoreCase = true) || it.studentId.contains(searchQuery, ignoreCase = true)
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = Color.Transparent
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Quran Sabaq & Hifz Tracker",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = IslamicGreen,
                        fontFamily = FontFamily.Serif
                    )
                    Text(
                        text = "Real-time Sabaq evaluation, audio recording & auto-save",
                        fontSize = 11.5.sp,
                        color = TextSecondaryGrey
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFFDCFCE7)
                ) {
                    Text(
                        text = "Auto-Save ✓",
                        color = Color(0xFF15803D),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Class Selection: Hifz Group / Nazra
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                groupOptions.forEach { grp ->
                    val isSelected = selectedGroup == grp
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedGroup = grp },
                        label = {
                            Text(
                                text = if (grp == "Hifz") "Hifz Group" else grp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 12.5.sp
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = IslamicGreen,
                            selectedLabelColor = Color.White,
                            containerColor = SurfaceWhite
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = isSelected,
                            borderColor = if (isSelected) IslamicGreen else BorderGold
                        ),
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Search filter
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search Hifz student name...", fontSize = 12.sp) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = IslamicGreen, modifier = Modifier.size(18.dp)) },
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = IslamicGreen,
                    unfocusedBorderColor = BorderGold.copy(alpha = 0.5f)
                ),
                modifier = Modifier.fillMaxWidth().height(48.dp),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            // List of Hifz students
            if (displayedStudents.isEmpty()) {
                Box(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No students found in $selectedGroup.", color = TextSecondaryGrey, fontSize = 13.sp)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(displayedStudents, key = { it.studentId }) { student ->
                        val studentHistory = allQuranProgress.filter { it.studentId == student.studentId }
                        val todayRecord = studentHistory.find { it.date == todayDate }

                        StudentSabaqRowCard(
                            student = student,
                            todayRecord = todayRecord,
                            history = studentHistory,
                            teacherName = currentUser.name.ifBlank { "Teacher Anas Mustafa" },
                            onSaveRecord = { sabaq, status, manzil, sabaqi, audioPath ->
                                // Save in repository
                                val entry = QuranProgress(
                                    id = "QP-${System.currentTimeMillis()}-${student.studentId}",
                                    studentId = student.studentId,
                                    date = todayDate,
                                    sabaq = sabaq,
                                    sabaqi = sabaqi,
                                    manzil = manzil,
                                    tajweedMistakes = "Good Makharij",
                                    remarks = "Status: $status",
                                    teacherName = currentUser.name.ifBlank { "Teacher Anas Mustafa" },
                                    hasAudio = audioPath.isNotBlank(),
                                    audioDurationSec = 0,
                                    sabaqStatus = status,
                                    audioPath = audioPath
                                )
                                repository.addQuranProgress(entry)

                                // Store in Firestore:
                                // quran_progress/{studentId}/{date} = { sabaq, sabaqStatus, manzil, audioUrl, teacherId, timestamp }
                                FirestoreHelper.saveQuranProgress(
                                    studentId = student.studentId,
                                    date = todayDate,
                                    sabaq = sabaq,
                                    sabaqStatus = status,
                                    manzil = manzil,
                                    audioUrl = audioPath,
                                    teacherId = currentUser.email.ifBlank { "teacher_anas" }
                                )
                            }
                        )
                    }
                }
            }
        }
    }
}

/**
 * Feature 3: Individual Student Sabaq Row with Auto-Save and Audio Recording
 */
@Composable
fun StudentSabaqRowCard(
    student: Student,
    todayRecord: QuranProgress?,
    history: List<QuranProgress>,
    teacherName: String,
    onSaveRecord: (sabaq: String, status: String, manzil: String, sabaqi: String, audioPath: String) -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var todaySabaq by remember(todayRecord) { mutableStateOf(todayRecord?.sabaq ?: "Para 2, Ruku 5") }
    var sabaqStatus by remember(todayRecord) { mutableStateOf(todayRecord?.sabaqStatus ?: "Yaad") }
    var manzil by remember(todayRecord) { mutableStateOf(todayRecord?.manzil ?: "Para 1") }
    var sabaqi by remember(todayRecord) { mutableStateOf(todayRecord?.sabaqi ?: "Para 2 Quarter 1") }
    var audioPath by remember(todayRecord) { mutableStateOf(todayRecord?.audioPath ?: "") }

    var saveStatusText by remember { mutableStateOf("Sabaq Saved ✓") }
    var isRecordingAudio by remember { mutableStateOf(false) }
    var isPlayingAudio by remember { mutableStateOf(false) }
    var showHistory by remember { mutableStateOf(false) }

    fun triggerAutoSave() {
        val timeNow = SimpleDateFormat("hh:mm a", Locale.US).format(Date())
        saveStatusText = "Saving..."
        coroutineScope.launch {
            delay(400)
            onSaveRecord(todaySabaq, sabaqStatus, manzil, sabaqi, audioPath)
            saveStatusText = "Sabaq Saved ✓ $timeNow"
        }
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, BorderGold.copy(alpha = 0.5f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Student Info Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = CircleShape,
                        color = IslamicGreen.copy(alpha = 0.12f),
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
                            fontSize = 15.sp,
                            color = TextPrimaryCharcoal
                        )
                        Text(
                            text = "S/o ${student.fatherName} • ID: ${student.studentId}",
                            fontSize = 11.5.sp,
                            color = TextSecondaryGrey
                        )
                    }
                }

                // Auto-Save Status Text: "Sabaq Saved ✓ 2:30 PM"
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Color(0xFFDCFCE7)
                ) {
                    Text(
                        text = saveStatusText,
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF15803D),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Today's Sabaq TextField
            Text(
                text = "Today's Sabaq (Sabak):",
                fontSize = 11.5.sp,
                fontWeight = FontWeight.Bold,
                color = IslamicGreen
            )
            OutlinedTextField(
                value = todaySabaq,
                onValueChange = {
                    todaySabaq = it
                    triggerAutoSave()
                },
                placeholder = { Text("e.g. Para 2, Ruku 5", fontSize = 12.sp) },
                singleLine = true,
                shape = RoundedCornerShape(10.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = IslamicGreen,
                    unfocusedBorderColor = BorderGold.copy(alpha = 0.6f)
                ),
                modifier = Modifier.fillMaxWidth().height(50.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Sabaq Status: [Yaad] [Kacha] [Pakka] - 3 colored buttons
            Text(
                text = "Sabaq Status:",
                fontSize = 11.5.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimaryCharcoal
            )
            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Button 1: Yaad (Green)
                val isYaad = sabaqStatus == "Yaad"
                Button(
                    onClick = {
                        sabaqStatus = "Yaad"
                        triggerAutoSave()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isYaad) Color(0xFF16A34A) else Color(0xFFDCFCE7),
                        contentColor = if (isYaad) Color.White else Color(0xFF16A34A)
                    ),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                    modifier = Modifier.weight(1f).height(38.dp)
                ) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Yaad", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                // Button 2: Kacha (Amber/Orange)
                val isKacha = sabaqStatus == "Kacha"
                Button(
                    onClick = {
                        sabaqStatus = "Kacha"
                        triggerAutoSave()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isKacha) Color(0xFFEA580C) else Color(0xFFFFEDD5),
                        contentColor = if (isKacha) Color.White else Color(0xFFEA580C)
                    ),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                    modifier = Modifier.weight(1f).height(38.dp)
                ) {
                    Icon(Icons.Default.Warning, contentDescription = null, modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Kacha", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                // Button 3: Pakka (Blue/Indigo)
                val isPakka = sabaqStatus == "Pakka"
                Button(
                    onClick = {
                        sabaqStatus = "Pakka"
                        triggerAutoSave()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isPakka) Color(0xFF2563EB) else Color(0xFFDBEAFE),
                        contentColor = if (isPakka) Color.White else Color(0xFF2563EB)
                    ),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                    modifier = Modifier.weight(1f).height(38.dp)
                ) {
                    Icon(Icons.Default.Verified, contentDescription = null, modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Pakka", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Manzil & Revision Fields
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Manzil
                Column(modifier = Modifier.weight(1f)) {
                    Text("Manzil:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextPrimaryCharcoal)
                    OutlinedTextField(
                        value = manzil,
                        onValueChange = {
                            manzil = it
                            triggerAutoSave()
                        },
                        placeholder = { Text("e.g. Para 1", fontSize = 11.5.sp) },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = IslamicGreen,
                            unfocusedBorderColor = BorderGold.copy(alpha = 0.5f)
                        ),
                        modifier = Modifier.fillMaxWidth().height(48.dp)
                    )
                }

                // Revision / Sabaqi
                Column(modifier = Modifier.weight(1f)) {
                    Text("Revision (Sabaqi):", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextPrimaryCharcoal)
                    OutlinedTextField(
                        value = sabaqi,
                        onValueChange = {
                            sabaqi = it
                            triggerAutoSave()
                        },
                        placeholder = { Text("Quarter 1", fontSize = 11.5.sp) },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = IslamicGreen,
                            unfocusedBorderColor = BorderGold.copy(alpha = 0.5f)
                        ),
                        modifier = Modifier.fillMaxWidth().height(48.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Audio Record Button & Playback Controls
            // Audio Record Button: Mic icon -> Press to record teacher listening to student (store audio file path in Firestore)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (isRecordingAudio) {
                    Button(
                        onClick = {
                            val savedFile = AudioService.stopRecording()
                            isRecordingAudio = false
                            if (savedFile != null) {
                                audioPath = savedFile.absolutePath
                                triggerAutoSave()
                                Toast.makeText(context, "Audio recorded & saved to Quran record!", Toast.LENGTH_SHORT).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.height(38.dp)
                    ) {
                        Icon(Icons.Default.Stop, contentDescription = "Stop", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Recording... Stop", fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                    }
                } else {
                    OutlinedButton(
                        onClick = {
                            val file = AudioService.startRecording(context, student.studentId)
                            isRecordingAudio = true
                            Toast.makeText(context, "Recording teacher recitation review...", Toast.LENGTH_SHORT).show()
                        },
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, IslamicGreen),
                        modifier = Modifier.height(38.dp)
                    ) {
                        Icon(Icons.Default.Mic, contentDescription = "Record", tint = IslamicGreen, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Record Recitation", fontSize = 11.5.sp, color = IslamicGreen, fontWeight = FontWeight.Bold)
                    }
                }

                // If audio recorded or exists, show Play button
                if (audioPath.isNotBlank()) {
                    IconButton(
                        onClick = {
                            isPlayingAudio = true
                            val file = File(audioPath)
                            AudioService.playAudioFile(context, file) {
                                isPlayingAudio = false
                            }
                        },
                        modifier = Modifier.size(38.dp)
                    ) {
                        Icon(
                            imageVector = if (isPlayingAudio) Icons.Default.VolumeUp else Icons.Default.PlayArrow,
                            contentDescription = "Play Audio",
                            tint = IslamicGoldDark
                        )
                    }
                }

                // Show History Toggle
                TextButton(
                    onClick = { showHistory = !showHistory },
                    contentPadding = PaddingValues(horizontal = 8.dp)
                ) {
                    Icon(
                        if (showHistory) Icons.Default.ExpandLess else Icons.Default.History,
                        contentDescription = null,
                        tint = IslamicGoldDark,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        if (showHistory) "Hide History" else "7-Day History (${history.size})",
                        fontSize = 11.sp,
                        color = IslamicGoldDark,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            // History Section: Last 7 days sabaq list below each student
            AnimatedVisibility(visible = showHistory) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp)
                        .background(CreamBackground, RoundedCornerShape(10.dp))
                        .padding(10.dp)
                ) {
                    Text(
                        text = "Last 7 Days Sabaq History:",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = IslamicGreen
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    if (history.isEmpty()) {
                        Text("No prior records yet.", fontSize = 11.sp, color = TextSecondaryGrey)
                    } else {
                        history.take(7).forEach { item ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "${item.date}: ${item.sabaq}",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = TextPrimaryCharcoal
                                    )
                                    Text(
                                        text = "Manzil: ${item.manzil} • ${item.sabaqi}",
                                        fontSize = 10.5.sp,
                                        color = TextSecondaryGrey
                                    )
                                }

                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = when (item.sabaqStatus) {
                                        "Yaad" -> Color(0xFFDCFCE7)
                                        "Kacha" -> Color(0xFFFFEDD5)
                                        else -> Color(0xFFDBEAFE)
                                    }
                                ) {
                                    Text(
                                        text = item.sabaqStatus.uppercase(),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = when (item.sabaqStatus) {
                                            "Yaad" -> Color(0xFF16A34A)
                                            "Kacha" -> Color(0xFFEA580C)
                                            else -> Color(0xFF2563EB)
                                        },
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            HorizontalDivider(color = BorderGold.copy(alpha = 0.3f))
                        }
                    }
                }
            }
        }
    }
}

/**
 * Direct alias for QuranScreen so any route can call it
 */
@Composable
fun QuranScreen(
    repository: AcademyRepository,
    currentUser: CurrentUser,
    modifier: Modifier = Modifier
) {
    QuranProgressScreen(repository = repository, currentUser = currentUser, modifier = modifier)
}
