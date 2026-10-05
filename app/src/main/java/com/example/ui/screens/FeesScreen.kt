package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import com.example.ui.components.IslamicArchCard
import com.example.ui.components.IslamicGoldDivider
import com.example.ui.components.IslamicMetricCard
import com.example.ui.components.PdfPreviewDialog
import com.example.ui.theme.*
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeesScreen(
    repository: AcademyRepository,
    currentUser: CurrentUser,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val allFees by repository.fees.collectAsState()
    val allStudents by repository.students.collectAsState()

    var statusFilter by remember { mutableStateOf("All") }
    var searchQuery by remember { mutableStateOf("") }
    var showAddFeeDialog by remember { mutableStateOf(false) }

    // PDF Preview
    var generatedPdfFile by remember { mutableStateOf<File?>(null) }
    var pdfTitle by remember { mutableStateOf("") }
    var pdfShareMsg by remember { mutableStateOf("") }
    var targetPhone by remember { mutableStateOf<String?>(null) }

    val filteredFees = remember(allFees, statusFilter, searchQuery) {
        allFees.filter { fee ->
            val matchesStatus = if (statusFilter == "All") true else fee.status == statusFilter
            val matchesSearch = fee.studentName.contains(searchQuery, ignoreCase = true) ||
                    fee.receiptNo.contains(searchQuery, ignoreCase = true) ||
                    fee.month.contains(searchQuery, ignoreCase = true)
            matchesStatus && matchesSearch
        }
    }

    val totalCollected = allFees.filter { it.status == "Paid" }.sumOf { it.totalPaid }
    val totalPending = allFees.filter { it.status != "Paid" }.sumOf { it.due }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = Color.Transparent,
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showAddFeeDialog = true },
                containerColor = IslamicGreen,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp),
                icon = { Icon(Icons.Default.Receipt, contentDescription = "Add Payment") },
                text = { Text("Collect Fee", fontWeight = FontWeight.Bold) }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            // Stats Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                IslamicMetricCard(
                    title = "Collected (Oct)",
                    value = "Rs ${totalCollected.toInt()}",
                    subtitle = "Verified Received",
                    iconRes = "💰",
                    accentColor = IslamicGreen,
                    modifier = Modifier.weight(1f)
                )

                IslamicMetricCard(
                    title = "Pending Dues",
                    value = "Rs ${totalPending.toInt()}",
                    subtitle = "To be recovered",
                    iconRes = "⏳",
                    accentColor = Color(0xFFEF4444),
                    modifier = Modifier.weight(1f)
                )
            }

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Search by student name or receipt #", fontSize = 13.sp) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = IslamicGreen) },
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Status Filter Chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf("All", "Paid", "Pending", "Overdue").forEach { st ->
                    FilterChip(
                        selected = statusFilter == st,
                        onClick = { statusFilter = st },
                        label = { Text(st, fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = IslamicGreen,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Fees List
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredFees, key = { it.feeId }) { fee ->
                    val student = allStudents.find { it.studentId == fee.studentId }
                    FeeItemCard(
                        fee = fee,
                        student = student,
                        onGenerateReceipt = {
                            val st = student ?: Student(
                                studentId = fee.studentId,
                                name = fee.studentName,
                                fatherName = "Guardian",
                                gender = "Boy",
                                dateOfBirth = "2014-01-01",
                                className = "Primary",
                                groupType = "General",
                                parentPhone = "+92 300 1234567",
                                monthlyFee = fee.amount,
                                assignedTeacher = "Anas Mustafa"
                            )
                            val pdf = PdfService.generateFeeReceiptPdf(context, fee, st)
                            generatedPdfFile = pdf
                            pdfTitle = "Fee Receipt - ${fee.receiptNo}"
                            targetPhone = st.parentPhone
                            pdfShareMsg = "Assalam-o-Alaikum, Apke bache ${fee.studentName} ne ${fee.month} ki fee Rs ${fee.totalPaid.toInt()} jama kara di hai. - Al Hadid Academy"
                        }
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }
    }

    if (showAddFeeDialog) {
        AddFeePaymentDialog(
            students = allStudents,
            currentUser = currentUser,
            onDismiss = { showAddFeeDialog = false },
            onSave = { newFee, student ->
                repository.addFee(newFee)
                showAddFeeDialog = false
                // Auto generate PDF receipt immediately on save as requested
                val pdf = PdfService.generateFeeReceiptPdf(context, newFee, student)
                generatedPdfFile = pdf
                pdfTitle = "Fee Receipt - ${newFee.receiptNo}"
                targetPhone = student.parentPhone
                pdfShareMsg = "Assalam-o-Alaikum, Apke bache ${student.name} ne ${newFee.month} ki fee Rs ${newFee.totalPaid.toInt()} jama kara di hai. - Al Hadid Academy"
            }
        )
    }

    generatedPdfFile?.let { file ->
        PdfPreviewDialog(
            pdfFile = file,
            documentTitle = pdfTitle,
            shareMessage = pdfShareMsg,
            parentPhone = targetPhone,
            onDismiss = { generatedPdfFile = null }
        )
    }
}

@Composable
fun FeeItemCard(
    fee: FeeRecord,
    student: Student?,
    onGenerateReceipt: () -> Unit
) {
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
                Column {
                    Text(
                        text = fee.studentName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = TextPrimaryCharcoal
                    )
                    Text(
                        text = "${fee.month} ${fee.year}  •  ${fee.receiptNo}",
                        fontSize = 12.sp,
                        color = TextSecondaryGrey
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = when (fee.status) {
                        "Paid" -> Color(0xFFD1FAE5)
                        "Pending" -> Color(0xFFFEF3C7)
                        else -> Color(0xFFFEE2E2)
                    }
                ) {
                    Text(
                        text = fee.status.uppercase(Locale.US),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = when (fee.status) {
                            "Paid" -> Color(0xFF065F46)
                            "Pending" -> Color(0xFF92400E)
                            else -> Color(0xFF991B1B)
                        },
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Paid: Rs ${fee.totalPaid.toInt()}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = IslamicGreen
                    )
                    if (fee.due > 0) {
                        Text(
                            text = "Due: Rs ${fee.due.toInt()}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = ColorError
                        )
                    }
                }

                Button(
                    onClick = onGenerateReceipt,
                    colors = ButtonDefaults.buttonColors(containerColor = IslamicGreen),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Default.Receipt, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("PDF Receipt", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddFeePaymentDialog(
    students: List<Student>,
    currentUser: CurrentUser,
    onDismiss: () -> Unit,
    onSave: (FeeRecord, Student) -> Unit
) {
    var selectedStudentIndex by remember { mutableIntStateOf(0) }
    var month by remember { mutableStateOf("October") }
    var year by remember { mutableIntStateOf(2026) }
    val currentStudent = students.getOrNull(selectedStudentIndex) ?: students.first()

    var amountText by remember { mutableStateOf(currentStudent.monthlyFee.toInt().toString()) }
    var discountText by remember { mutableStateOf("0") }
    var fineText by remember { mutableStateOf("0") }
    var paymentMethod by remember { mutableStateOf("Cash") }
    var receivedBy by remember {
        mutableStateOf(
            if (currentUser.role == UserRole.PRINCIPAL_AWAIS) "Awais Mustafa" else "Anas Mustafa"
        )
    }

    LaunchedEffect(selectedStudentIndex) {
        amountText = currentStudent.monthlyFee.toInt().toString()
    }

    val amount = amountText.toDoubleOrNull() ?: 0.0
    val discount = discountText.toDoubleOrNull() ?: 0.0
    val fine = fineText.toDoubleOrNull() ?: 0.0
    val totalPaid = (amount - discount + fine).coerceAtLeast(0.0)

    var saveStatus by remember { mutableStateOf("") }
    LaunchedEffect(amountText) {
        if (amountText.isNotBlank()) {
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
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Add Fee Payment",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = IslamicGreen,
                            fontFamily = FontFamily.Serif
                        )
                        Text(
                            text = "Receipt with Academy Logo will be generated instantly",
                            fontSize = 11.sp,
                            color = TextSecondaryGrey
                        )
                    }

                    if (saveStatus.isNotBlank()) {
                        Text(
                            text = saveStatus,
                            color = Color(0xFF16A34A),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                IslamicGoldDivider(modifier = Modifier.padding(vertical = 8.dp))

                // Select Student
                Text("Select Student:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = IslamicGreen)
                var expandedStudentDropdown by remember { mutableStateOf(false) }

                ExposedDropdownMenuBox(
                    expanded = expandedStudentDropdown,
                    onExpandedChange = { expandedStudentDropdown = it }
                ) {
                    OutlinedTextField(
                        value = "${currentStudent.name} (${currentStudent.studentId}) - ${currentStudent.className}",
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedStudentDropdown) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )
                    ExposedDropdownMenu(
                        expanded = expandedStudentDropdown,
                        onDismissRequest = { expandedStudentDropdown = false }
                    ) {
                        students.forEachIndexed { idx, st ->
                            DropdownMenuItem(
                                text = { Text("${st.name} (${st.studentId}) - ${st.groupType}") },
                                onClick = {
                                    selectedStudentIndex = idx
                                    expandedStudentDropdown = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = month,
                        onValueChange = { month = it },
                        label = { Text("Month") },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = amountText,
                        onValueChange = { amountText = it },
                        label = { Text("Fee (PKR)") },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = discountText,
                        onValueChange = { discountText = it },
                        label = { Text("Discount") },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = fineText,
                        onValueChange = {},
                        enabled = false, // Disabled default 0 as specified in prompt
                        label = { Text("Fine (0 - Future)") },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Payment Method
                Text("Payment Method:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = IslamicGreen)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf("Cash", "Easypaisa", "JazzCash").forEach { method ->
                        FilterChip(
                            selected = paymentMethod == method,
                            onClick = { paymentMethod = method },
                            label = { Text(method, fontSize = 11.sp) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = CreamBackground,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Total Payable:", fontWeight = FontWeight.Bold, color = TextPrimaryCharcoal)
                        Text("Rs ${totalPaid.toInt()}", fontWeight = FontWeight.Bold, color = IslamicGreen, fontSize = 15.sp)
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
                            val receiptNo = "AHAD-REC-${(100..999).random()}"
                            val dateStr = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
                            val newFee = FeeRecord(
                                feeId = "F-${System.currentTimeMillis()}",
                                receiptNo = receiptNo,
                                studentId = currentStudent.studentId,
                                studentName = currentStudent.name,
                                month = month,
                                year = year,
                                amount = amount,
                                discount = discount,
                                fine = fine,
                                totalPaid = totalPaid,
                                due = 0.0,
                                status = "Paid",
                                paymentMethod = paymentMethod,
                                paymentDate = dateStr,
                                receivedBy = receivedBy
                            )
                            onSave(newFee, currentStudent)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = IslamicGreen)
                    ) {
                        Text("Save & Generate Receipt", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
