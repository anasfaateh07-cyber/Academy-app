package com.example.ui.screens

import android.widget.Toast
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.R
import com.example.data.AcademyRepository
import com.example.data.CurrentUser
import com.example.data.Student
import com.example.service.PdfService
import com.example.ui.components.GradeBadge
import com.example.ui.components.IslamicArchCard
import com.example.ui.components.IslamicGoldDivider
import com.example.ui.components.PdfPreviewDialog
import com.example.ui.theme.*
import java.io.File
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentsScreen(
    repository: AcademyRepository,
    currentUser: CurrentUser,
    onStudentClick: (Student) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val allStudents by repository.students.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var selectedGroupFilter by remember { mutableStateOf("All") }
    var showAddDialog by remember { mutableStateOf(false) }

    // PDF Preview state
    var generatedPdfFile by remember { mutableStateOf<File?>(null) }
    var pdfTitle by remember { mutableStateOf("") }
    var pdfShareMsg by remember { mutableStateOf("") }

    // Filter students based on role permissions and search
    val visibleStudents = remember(allStudents, searchQuery, selectedGroupFilter, currentUser) {
        allStudents.filter { st ->
            val matchesRole = if (currentUser.isFullAdmin) {
                true
            } else {
                currentUser.allowedGroups.contains(st.groupType)
            }
            val matchesGroup = if (selectedGroupFilter == "All") true else st.groupType == selectedGroupFilter
            val matchesSearch = st.name.contains(searchQuery, ignoreCase = true) ||
                    st.studentId.contains(searchQuery, ignoreCase = true) ||
                    st.fatherName.contains(searchQuery, ignoreCase = true) ||
                    st.className.contains(searchQuery, ignoreCase = true)

            matchesRole && matchesGroup && matchesSearch
        }
    }

    val availableGroups = if (currentUser.isFullAdmin) {
        listOf("All", "Hifz", "Nazra", "Tajweed", "Tuition Boy", "Tuition Girl", "Playgroup")
    } else {
        listOf("All") + currentUser.allowedGroups
    }

    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = Color.Transparent,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = IslamicGreen,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp),
                icon = { Icon(Icons.Default.PersonAdd, contentDescription = "Add Student") },
                text = { Text("Add Student", fontWeight = FontWeight.Bold) }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                placeholder = { Text("Search by name, ID (e.g. AHAD-001), father...", fontSize = 13.sp) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", tint = IslamicGreen) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear")
                        }
                    }
                },
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = IslamicGreen,
                    unfocusedBorderColor = BorderGold,
                    focusedContainerColor = SurfaceWhite,
                    unfocusedContainerColor = SurfaceWhite
                ),
                singleLine = true
            )

            // Group Filter Chips
            ScrollableTabRow(
                selectedTabIndex = availableGroups.indexOf(selectedGroupFilter).coerceAtLeast(0),
                edgePadding = 0.dp,
                containerColor = Color.Transparent,
                divider = {},
                indicator = {}
            ) {
                availableGroups.forEach { group ->
                    val isSelected = selectedGroupFilter == group
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedGroupFilter = group },
                        label = {
                            Text(
                                text = group,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = IslamicGreen,
                            selectedLabelColor = Color.White,
                            containerColor = SurfaceWhite,
                            labelColor = TextPrimaryCharcoal
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = isSelected,
                            borderColor = if (isSelected) IslamicGreen else BorderGold
                        ),
                        modifier = Modifier.padding(end = 6.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Student Count Summary
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Enrolled Students (${visibleStudents.size})",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = IslamicGreen
                )
                if (!currentUser.isFullAdmin) {
                    Text(
                        text = "Teacher Isra Section",
                        fontSize = 11.sp,
                        color = IslamicGoldDark,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Student List
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(visibleStudents, key = { it.studentId }) { student ->
                    StudentItemCard(
                        student = student,
                        isFullAdmin = currentUser.isFullAdmin,
                        onViewDetails = { onStudentClick(student) },
                        onGenerateCertificate = {
                            val pdf = PdfService.generateCharacterCertificatePdf(context, student)
                            generatedPdfFile = pdf
                            pdfTitle = "Character Certificate - ${student.name}"
                            pdfShareMsg = "Assalam-o-Alaikum, Al Hadid Academy Character Certificate for ${student.name} S/o ${student.fatherName}."
                        },
                        onDelete = {
                            val deletedStudent = student
                            repository.deleteStudent(student.studentId)
                            FirestoreHelper.deleteStudentFromFirestore(student.studentId)
                            Toast.makeText(context, "Student ${student.name} deleted", Toast.LENGTH_SHORT).show()

                            coroutineScope.launch {
                                val result = snackbarHostState.showSnackbar(
                                    message = "Student ${deletedStudent.name} nikal diya gaya. (Auto Saved ✓)",
                                    actionLabel = "Undo",
                                    duration = SnackbarDuration.Long
                                )
                                if (result == SnackbarResult.ActionPerformed) {
                                    repository.addStudent(deletedStudent)
                                    FirestoreHelper.saveStudentToFirestore(deletedStudent)
                                    Toast.makeText(context, "${deletedStudent.name} restored successfully! Auto Saved ✓", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }
    }

    if (showAddDialog) {
        AddStudentDialog(
            currentUser = currentUser,
            onDismiss = { showAddDialog = false },
            onAdd = { newStudent ->
                repository.addStudent(newStudent)
                showAddDialog = false
                Toast.makeText(context, "Student ${newStudent.name} added successfully!", Toast.LENGTH_SHORT).show()
            }
        )
    }

    generatedPdfFile?.let { file ->
        PdfPreviewDialog(
            pdfFile = file,
            documentTitle = pdfTitle,
            shareMessage = pdfShareMsg,
            onDismiss = { generatedPdfFile = null }
        )
    }
}

@Composable
fun StudentItemCard(
    student: Student,
    isFullAdmin: Boolean,
    onViewDetails: () -> Unit,
    onGenerateCertificate: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onViewDetails() },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.Brush.horizontalGradient(listOf(BorderGold.copy(alpha = 0.5f), Color.Transparent)))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Avatar circle with Islamic initial or icon
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(if (student.gender == "Girl") Color(0xFFFCE7F3) else IslamicGreenContainer)
                        .border(1.5.dp, IslamicGold, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = student.name.take(2).uppercase(),
                        fontWeight = FontWeight.Bold,
                        color = if (student.gender == "Girl") Color(0xFFBE185D) else IslamicGreen,
                        fontSize = 16.sp
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = student.name,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = TextPrimaryCharcoal
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = CreamBackground,
                            border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.Brush.linearGradient(listOf(BorderGold, BorderGold)))
                        ) {
                            Text(
                                text = student.studentId,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = IslamicGreen,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Text(
                        text = "S/o ${student.fatherName}  •  Class ${student.className}",
                        fontSize = 12.sp,
                        color = TextSecondaryGrey
                    )

                    Text(
                        text = "Group: ${student.groupType} | Teacher: ${student.assignedTeacher}",
                        fontSize = 11.sp,
                        color = IslamicGoldDark,
                        fontWeight = FontWeight.Medium
                    )
                }

                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = "View Details",
                    tint = TextSecondaryGrey
                )
            }

            if (student.stationaryNeeded != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFFFFFBEB),
                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.Brush.linearGradient(listOf(Color(0xFFF59E0B), Color(0xFFF59E0B))))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.MenuBook, contentDescription = null, tint = Color(0xFFD97706), modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Stationary Required: ${student.stationaryNeeded}",
                            fontSize = 11.sp,
                            color = Color(0xFFB45309),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Action row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Monthly: Rs ${student.monthlyFee.toInt()}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = IslamicGreen
                )

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    // Certificate button
                    IconButton(
                        onClick = onGenerateCertificate,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.WorkspacePremium,
                            contentDescription = "Character Certificate PDF",
                            tint = IslamicGoldDark,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    if (isFullAdmin) {
                        var showConfirmDelete by remember { mutableStateOf(false) }
                        IconButton(
                            onClick = { showConfirmDelete = true },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.DeleteOutline,
                                contentDescription = "Delete Student",
                                tint = ColorError,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        if (showConfirmDelete) {
                            AlertDialog(
                                onDismissRequest = { showConfirmDelete = false },
                                title = { Text("Delete ${student.name}?") },
                                text = { Text("This will remove this student and all associated records permanently.") },
                                confirmButton = {
                                    TextButton(onClick = {
                                        showConfirmDelete = false
                                        onDelete()
                                    }) {
                                        Text("Delete", color = ColorError, fontWeight = FontWeight.Bold)
                                    }
                                },
                                dismissButton = {
                                    TextButton(onClick = { showConfirmDelete = false }) {
                                        Text("Cancel")
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddStudentDialog(
    currentUser: CurrentUser,
    onDismiss: () -> Unit,
    onAdd: (Student) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var fatherName by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf(if (currentUser.isFullAdmin) "Boy" else "Girl") }
    var className by remember { mutableStateOf("3") }
    var groupType by remember { mutableStateOf(if (currentUser.isFullAdmin) "Hifz" else "Tuition Girl") }
    var parentPhone by remember { mutableStateOf("+92 ") }
    var monthlyFee by remember { mutableStateOf("1500") }
    var address by remember { mutableStateOf("Nasirabad Jatlan, Azad Kashmir") }
    var assignedTeacher by remember {
        mutableStateOf(
            if (currentUser.role == com.example.data.UserRole.TEACHER_ISRA) "Isra"
            else if (currentUser.role == com.example.data.UserRole.PRINCIPAL_AWAIS) "Awais Mustafa"
            else "Anas Mustafa"
        )
    }

    val allowedGroups = if (currentUser.isFullAdmin) {
        listOf("Hifz", "Nazra", "Tajweed", "Tuition Boy", "Tuition Girl", "Playgroup", "Computer")
    } else {
        listOf("Tuition Girl", "Playgroup")
    }

    var saveStatus by remember { mutableStateOf("") }
    LaunchedEffect(name) {
        if (name.isNotBlank()) {
            saveStatus = "Saving..."
            delay(1500)
            saveStatus = "Auto Saved ✓ ${System.currentTimeMillis()}"
        } else {
            saveStatus = ""
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceWhite)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Text(
                    text = "Add New Student",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = IslamicGreen,
                    fontFamily = FontFamily.Serif
                )
                Text(
                    text = "Enter particulars for Al Hadid Academy enrollment",
                    fontSize = 12.sp,
                    color = TextSecondaryGrey
                )

                IslamicGoldDivider(modifier = Modifier.padding(vertical = 8.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Student Name *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                if (saveStatus.isNotBlank()) {
                    Text(
                        text = saveStatus,
                        color = Color(0xFF16A34A),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(start = 4.dp, top = 2.dp)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = fatherName,
                    onValueChange = { fatherName = it },
                    label = { Text("Father Name *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = className,
                        onValueChange = { className = it },
                        label = { Text("Class (e.g. 5)") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = monthlyFee,
                        onValueChange = { monthlyFee = it },
                        label = { Text("Fee (PKR)") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = parentPhone,
                    onValueChange = { parentPhone = it },
                    label = { Text("Parent WhatsApp Phone *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Group selector
                Text(text = "Select Academy Group:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = IslamicGreen)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    allowedGroups.take(3).forEach { grp ->
                        FilterChip(
                            selected = groupType == grp,
                            onClick = { groupType = grp },
                            label = { Text(grp, fontSize = 11.sp) }
                        )
                    }
                }
                if (allowedGroups.size > 3) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        allowedGroups.drop(3).take(3).forEach { grp ->
                            FilterChip(
                                selected = groupType == grp,
                                onClick = { groupType = grp },
                                label = { Text(grp, fontSize = 11.sp) }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel", color = TextSecondaryGrey)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (name.isNotBlank() && fatherName.isNotBlank()) {
                                val generatedId = "AHAD-${(100..999).random()}"
                                onAdd(
                                    Student(
                                        studentId = generatedId,
                                        name = name.trim(),
                                        fatherName = fatherName.trim(),
                                        gender = gender,
                                        dateOfBirth = "2015-01-01",
                                        className = className.trim(),
                                        groupType = groupType,
                                        parentPhone = parentPhone.trim(),
                                        monthlyFee = monthlyFee.toDoubleOrNull() ?: 1500.0,
                                        address = address,
                                        assignedTeacher = assignedTeacher
                                    )
                                )
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = IslamicGreen)
                    ) {
                        Text("Save Student", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
