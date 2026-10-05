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
import com.example.data.*
import com.example.service.PdfService
import com.example.ui.components.IslamicArchCard
import com.example.ui.components.IslamicGoldDivider
import com.example.ui.components.PdfPreviewDialog
import com.example.ui.theme.*
import java.io.File

@Composable
fun ReportsScreen(
    repository: AcademyRepository,
    currentUser: CurrentUser,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val allStudents by repository.students.collectAsState()
    val allQuran by repository.quranProgress.collectAsState()
    val allBooks by repository.bookProgress.collectAsState()
    val allFees by repository.fees.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    val filteredStudents = remember(allStudents, searchQuery) {
        allStudents.filter {
            it.name.contains(searchQuery, ignoreCase = true) ||
            it.studentId.contains(searchQuery, ignoreCase = true) ||
            it.groupType.contains(searchQuery, ignoreCase = true)
        }
    }

    // PDF Preview State
    var generatedPdfFile by remember { mutableStateOf<File?>(null) }
    var pdfTitle by remember { mutableStateOf("") }
    var pdfShareMsg by remember { mutableStateOf("") }
    var parentPhone by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Reports & Official Certificates",
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
            color = IslamicGreen,
            fontFamily = FontFamily.Serif
        )
        Text(
            text = "Generate Character Certificates, Weekly & Monthly Reports with Logo & Border",
            fontSize = 12.sp,
            color = TextSecondaryGrey
        )

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Filter student for report generation...", fontSize = 13.sp) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = IslamicGreen) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(filteredStudents, key = { it.studentId }) { st ->
                val quran = allQuran.find { it.studentId == st.studentId }
                val fee = allFees.find { it.studentId == st.studentId }

                IslamicArchCard {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = st.name,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = TextPrimaryCharcoal
                            )
                            Text(
                                text = "ID: ${st.studentId}  •  Class ${st.className} (${st.groupType})",
                                fontSize = 11.5.sp,
                                color = TextSecondaryGrey
                            )
                        }

                        Surface(shape = RoundedCornerShape(6.dp), color = IslamicGreenContainer) {
                            Text(
                                text = st.assignedTeacher,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = IslamicGreen,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    IslamicGoldDivider(modifier = Modifier.padding(vertical = 8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Character Certificate
                        Button(
                            onClick = {
                                val pdf = PdfService.generateCharacterCertificatePdf(context, st)
                                generatedPdfFile = pdf
                                pdfTitle = "Character Certificate - ${st.name}"
                                parentPhone = st.parentPhone
                                pdfShareMsg = "Assalam-o-Alaikum, Official Character Certificate for ${st.name} from Al Hadid Academy."
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = IslamicGoldDark),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.Default.WorkspacePremium, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Certificate", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        // Weekly Report
                        Button(
                            onClick = {
                                val pdf = PdfService.generateWeeklyReportPdf(context, st, quran, allBooks, fee)
                                generatedPdfFile = pdf
                                pdfTitle = "Weekly Progress Report - ${st.name}"
                                parentPhone = st.parentPhone
                                pdfShareMsg = "Assalam-o-Alaikum, Al Hadid Academy Weekly Report for ${st.name} S/o ${st.fatherName}."
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = IslamicGreen),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.Default.Assessment, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Weekly PDF", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(40.dp))
            }
        }
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
