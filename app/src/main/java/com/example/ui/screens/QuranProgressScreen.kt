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
import com.example.service.AudioService
import com.example.ui.components.IslamicArchCard
import com.example.ui.components.IslamicGoldDivider
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun QuranProgressScreen(
    repository: AcademyRepository,
    currentUser: CurrentUser,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val allQuranProgress by repository.quranProgress.collectAsState()
    val allStudents by repository.students.collectAsState()

    val quranStudents = remember(allStudents) {
        allStudents.filter { it.groupType in listOf("Hifz", "Nazra", "Tajweed") }
    }

    var showAddDialog by remember { mutableStateOf(false) }
    var playingId by remember { mutableStateOf<String?>(null) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = Color.Transparent,
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = IslamicGreen,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp),
                icon = { Icon(Icons.Default.Add, contentDescription = "Add Sabaq") },
                text = { Text("Log Daily Sabaq", fontWeight = FontWeight.Bold) }
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
                text = "Hifz, Nazra & Tajweed Quran Tracker",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = IslamicGreen,
                fontFamily = FontFamily.Serif
            )
            Text(
                text = "Track daily lessons (Sabaq), revision (Sabaqi), Manzil and recitation audio",
                fontSize = 12.sp,
                color = TextSecondaryGrey
            )

            Spacer(modifier = Modifier.height(12.dp))

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(allQuranProgress, key = { it.id }) { qp ->
                    val student = allStudents.find { it.studentId == qp.studentId }
                    val isPlaying = playingId == qp.id

                    IslamicArchCard {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = student?.name ?: qp.studentId,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimaryCharcoal
                                )
                                Text(
                                    text = "Group: ${student?.groupType ?: "Hifz"}  •  ${qp.date}",
                                    fontSize = 11.sp,
                                    color = IslamicGoldDark
                                )
                            }

                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = IslamicGreenContainer
                            ) {
                                Text(
                                    text = "Instructor: ${qp.teacherName}",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = IslamicGreen,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = CreamBackground,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text("📖 Sabaq (New): ${qp.sabaq}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = IslamicGreen)
                                Text("🔄 Sabaqi (Recent): ${qp.sabaqi}", fontSize = 11.sp, color = TextPrimaryCharcoal)
                                Text("📚 Manzil (Revision): ${qp.manzil}", fontSize = 11.sp, color = TextPrimaryCharcoal)
                                if (qp.tajweedMistakes.isNotEmpty()) {
                                    Text("⚠️ Tajweed Guidance: ${qp.tajweedMistakes}", fontSize = 11.sp, color = ColorError)
                                }
                                Text("💬 Remarks: ${qp.remarks}", fontSize = 11.sp, color = TextSecondaryGrey)
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (qp.hasAudio) {
                                Button(
                                    onClick = {
                                        if (isPlaying) {
                                            AudioService.stopPlaying()
                                            playingId = null
                                        } else {
                                            playingId = qp.id
                                            AudioService.playAudio(context) {
                                                playingId = null
                                            }
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = if (isPlaying) ColorError else IslamicGreen),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Icon(
                                        imageVector = if (isPlaying) Icons.Default.Stop else Icons.Default.PlayArrow,
                                        contentDescription = null,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(if (isPlaying) "Playing (${qp.audioDurationSec}s)" else "Play Recitation Audio", fontSize = 11.sp)
                                }
                            } else {
                                Text("No audio recorded", fontSize = 11.sp, color = TextSecondaryGrey)
                            }

                            OutlinedButton(
                                onClick = {
                                    Toast.makeText(context, "Sabaq audio recorded for ${student?.name ?: "student"}", Toast.LENGTH_SHORT).show()
                                },
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Icon(Icons.Default.Mic, contentDescription = null, tint = IslamicGoldDark, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Record Voice", fontSize = 11.sp, color = IslamicGoldDark)
                            }
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }
    }

    if (showAddDialog) {
        AddQuranProgressDialog(
            students = quranStudents,
            currentUser = currentUser,
            onDismiss = { showAddDialog = false },
            onAdd = { newEntry ->
                repository.addQuranProgress(newEntry)
                showAddDialog = false
                Toast.makeText(context, "Sabaq entry logged successfully!", Toast.LENGTH_SHORT).show()
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddQuranProgressDialog(
    students: List<Student>,
    currentUser: CurrentUser,
    onDismiss: () -> Unit,
    onAdd: (QuranProgress) -> Unit
) {
    var selectedStudentIndex by remember { mutableIntStateOf(0) }
    val currentStudent = students.getOrNull(selectedStudentIndex) ?: students.first()

    var sabaq by remember { mutableStateOf("Surah ") }
    var sabaqi by remember { mutableStateOf("Para ") }
    var manzil by remember { mutableStateOf("Para ") }
    var tajweedMistakes by remember { mutableStateOf("Madd, Ghunnah Makharij") }
    var remarks by remember { mutableStateOf("MashaAllah regular and attentive") }
    var hasAudioRecorded by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceWhite)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "Log Daily Quran Sabaq",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = IslamicGreen,
                    fontFamily = FontFamily.Serif
                )

                IslamicGoldDivider(modifier = Modifier.padding(vertical = 8.dp))

                Text("Select Hifz / Nazra Student:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = IslamicGreen)

                var expandedStudent by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = expandedStudent,
                    onExpandedChange = { expandedStudent = it }
                ) {
                    OutlinedTextField(
                        value = "${currentStudent.name} (${currentStudent.groupType})",
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedStudent) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = expandedStudent,
                        onDismissRequest = { expandedStudent = false }
                    ) {
                        students.forEachIndexed { idx, st ->
                            DropdownMenuItem(
                                text = { Text("${st.name} - ${st.groupType} (${st.studentId})") },
                                onClick = {
                                    selectedStudentIndex = idx
                                    expandedStudent = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = sabaq,
                    onValueChange = { sabaq = it },
                    label = { Text("Daily Sabaq (e.g. Surah Yaseen 1-15)") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    OutlinedTextField(
                        value = sabaqi,
                        onValueChange = { sabaqi = it },
                        label = { Text("Sabaqi") },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = manzil,
                        onValueChange = { manzil = it },
                        label = { Text("Manzil") },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                OutlinedTextField(
                    value = tajweedMistakes,
                    onValueChange = { tajweedMistakes = it },
                    label = { Text("Tajweed Guidance") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(6.dp))

                OutlinedTextField(
                    value = remarks,
                    onValueChange = { remarks = it },
                    label = { Text("Teacher Remarks") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Record Sabaq Button
                Button(
                    onClick = { hasAudioRecorded = true },
                    colors = ButtonDefaults.buttonColors(containerColor = if (hasAudioRecorded) ColorSuccess else IslamicGoldDark),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.Mic, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(if (hasAudioRecorded) "Audio Recorded (30 sec)" else "Record Sabaq Recitation Audio", fontSize = 12.sp)
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) { Text("Cancel") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val dateStr = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
                            onAdd(
                                QuranProgress(
                                    id = "QP-${System.currentTimeMillis()}",
                                    studentId = currentStudent.studentId,
                                    date = dateStr,
                                    sabaq = sabaq,
                                    sabaqi = sabaqi,
                                    manzil = manzil,
                                    tajweedMistakes = tajweedMistakes,
                                    remarks = remarks,
                                    teacherName = currentUser.name,
                                    hasAudio = hasAudioRecorded,
                                    audioDurationSec = if (hasAudioRecorded) 30 else 0
                                )
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = IslamicGreen)
                    ) {
                        Text("Save Sabaq")
                    }
                }
            }
        }
    }
}
