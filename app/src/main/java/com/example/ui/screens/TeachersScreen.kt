package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Person
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
import com.example.data.AcademyRepository
import com.example.data.CurrentUser
import com.example.data.Teacher
import com.example.ui.components.IslamicArchCard
import com.example.ui.components.IslamicGoldDivider
import com.example.ui.theme.*

@Composable
fun TeachersScreen(
    repository: AcademyRepository,
    currentUser: CurrentUser,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val teachers by repository.teachers.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = Color.Transparent,
        floatingActionButton = {
            if (currentUser.isFullAdmin) {
                ExtendedFloatingActionButton(
                    onClick = { showAddDialog = true },
                    containerColor = IslamicGreen,
                    contentColor = Color.White,
                    shape = RoundedCornerShape(16.dp),
                    icon = { Icon(Icons.Default.Add, contentDescription = "Add Teacher") },
                    text = { Text("Add New Teacher", fontWeight = FontWeight.Bold) }
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
                text = "Faculty & Instructors Directory",
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = IslamicGreen,
                fontFamily = FontFamily.Serif
            )
            Text(
                text = "Al Hadid Academy Teaching Staff and In-Charges",
                fontSize = 12.sp,
                color = TextSecondaryGrey
            )

            Spacer(modifier = Modifier.height(12.dp))

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(teachers, key = { it.teacherId }) { t ->
                    IslamicArchCard {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = t.name,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimaryCharcoal
                                )
                                Text(
                                    text = "${t.role}  •  ${t.specialization}",
                                    fontSize = 11.5.sp,
                                    color = IslamicGoldDark,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }

                            if (currentUser.isFullAdmin && t.teacherId != "T-01" && t.teacherId != "T-02") {
                                IconButton(onClick = {
                                    repository.deleteTeacher(t.teacherId)
                                    Toast.makeText(context, "${t.name} removed", Toast.LENGTH_SHORT).show()
                                }) {
                                    Icon(Icons.Default.DeleteOutline, contentDescription = "Delete", tint = ColorError)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "Email: ${t.email}  |  Phone: ${t.phone}",
                            fontSize = 11.sp,
                            color = TextSecondaryGrey
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "Assigned Groups: ${t.assignedGroups.joinToString(", ")}",
                            fontSize = 11.5.sp,
                            color = IslamicGreen,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }
    }

    if (showAddDialog) {
        AddTeacherDialog(
            onDismiss = { showAddDialog = false },
            onAdd = { newTeacher ->
                repository.addTeacher(newTeacher)
                showAddDialog = false
                Toast.makeText(context, "Teacher ${newTeacher.name} added successfully!", Toast.LENGTH_SHORT).show()
            }
        )
    }
}

@Composable
fun AddTeacherDialog(
    onDismiss: () -> Unit,
    onAdd: (Teacher) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("+92 ") }
    var specialization by remember { mutableStateOf("Quran & Islamic Studies") }
    var groups by remember { mutableStateOf("Nazra, Tajweed") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceWhite)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "Add New Faculty Member",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = IslamicGreen
                )

                IslamicGoldDivider(modifier = Modifier.padding(vertical = 8.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Teacher Full Name *") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email Address") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Phone Number") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = specialization,
                    onValueChange = { specialization = it },
                    label = { Text("Specialization / Subject") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = groups,
                    onValueChange = { groups = it },
                    label = { Text("Assigned Groups (comma separated)") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("Cancel") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (name.isNotBlank()) {
                                onAdd(
                                    Teacher(
                                        teacherId = "T-${System.currentTimeMillis()}",
                                        name = name.trim(),
                                        email = email.trim(),
                                        phone = phone.trim(),
                                        assignedGroups = groups.split(",").map { it.trim() },
                                        role = "Teacher",
                                        specialization = specialization.trim()
                                    )
                                )
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = IslamicGreen)
                    ) {
                        Text("Save Teacher")
                    }
                }
            }
        }
    }
}
