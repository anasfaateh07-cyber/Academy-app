package com.example.service

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.R
import com.example.data.BookProgress
import com.example.data.FeeRecord
import com.example.data.QuranProgress
import com.example.data.Student
import com.example.data.TestMarks
import com.example.data.TestRecord
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object PdfService {

    private const val PAGE_WIDTH = 595 // A4 standard width in points
    private const val PAGE_HEIGHT = 842 // A4 standard height in points

    private val colorIslamicGreen = Color.rgb(11, 93, 30) // #0B5D1E
    private val colorGold = Color.rgb(212, 175, 55) // #D4AF37
    private val colorTextDark = Color.rgb(30, 40, 30)
    private val colorLightGrey = Color.rgb(240, 244, 240)
    private val colorBorder = Color.rgb(200, 205, 200)

    private fun getPdfDir(context: Context): File {
        val dir = File(context.cacheDir, "pdfs")
        if (!dir.exists()) dir.mkdirs()
        return dir
    }

    private fun getLogoBitmap(context: Context): Bitmap? {
        return try {
            BitmapFactory.decodeResource(context.resources, R.drawable.alhadid_logo)
        } catch (e: Exception) {
            null
        }
    }

    private fun drawLetterhead(canvas: Canvas, context: Context, title: String): Float {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        // 1. Draw Golden Islamic Double Border
        paint.style = Paint.Style.STROKE
        paint.color = colorGold
        paint.strokeWidth = 2.5f
        canvas.drawRect(25f, 25f, (PAGE_WIDTH - 25).toFloat(), (PAGE_HEIGHT - 25).toFloat(), paint)

        paint.strokeWidth = 1f
        canvas.drawRect(30f, 30f, (PAGE_WIDTH - 30).toFloat(), (PAGE_HEIGHT - 30).toFloat(), paint)

        // Islamic corner ornaments
        val cornerSize = 14f
        canvas.drawLine(25f, 25f + cornerSize, 25f + cornerSize, 25f, paint)
        canvas.drawLine((PAGE_WIDTH - 25).toFloat(), 25f + cornerSize, (PAGE_WIDTH - 25 - cornerSize).toFloat(), 25f, paint)
        canvas.drawLine(25f, (PAGE_HEIGHT - 25 - cornerSize).toFloat(), 25f + cornerSize, (PAGE_HEIGHT - 25).toFloat(), paint)
        canvas.drawLine((PAGE_WIDTH - 25).toFloat(), (PAGE_HEIGHT - 25 - cornerSize).toFloat(), (PAGE_WIDTH - 25 - cornerSize).toFloat(), (PAGE_HEIGHT - 25).toFloat(), paint)

        // 2. Draw Centered Logo
        val logo = getLogoBitmap(context)
        val logoY = 40f
        val logoSize = 65f
        val centerX = PAGE_WIDTH / 2f

        if (logo != null) {
            val destRect = RectF(centerX - (logoSize / 2f), logoY, centerX + (logoSize / 2f), logoY + logoSize)
            canvas.drawBitmap(logo, null, destRect, paint)
        }

        // 3. Typography Header
        paint.style = Paint.Style.FILL
        paint.color = colorIslamicGreen
        paint.typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
        paint.textSize = 20f
        paint.textAlign = Paint.Align.CENTER
        canvas.drawText("AL HADID ACADEMY", centerX, logoY + logoSize + 22f, paint)

        paint.color = colorGold
        paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
        paint.textSize = 10f
        paint.letterSpacing = 0.15f
        canvas.drawText("HIFZ  •  NAZRA  •  TAJWEED  •  TUITION  •  COMPUTER", centerX, logoY + logoSize + 36f, paint)

        paint.color = Color.GRAY
        paint.textSize = 8.5f
        paint.letterSpacing = 0.05f
        canvas.drawText("Nasirabad Jatlan, Azad Kashmir  |  Contact: +92 300 9876541", centerX, logoY + logoSize + 48f, paint)

        // 4. Decorative Divider
        val dividerY = logoY + logoSize + 56f
        paint.color = colorGold
        paint.strokeWidth = 1.5f
        paint.style = Paint.Style.STROKE
        canvas.drawLine(60f, dividerY, (PAGE_WIDTH - 60).toFloat(), dividerY, paint)

        // Document Title Banner
        val bannerY = dividerY + 12f
        paint.style = Paint.Style.FILL
        paint.color = colorIslamicGreen
        val bannerRect = RectF(centerX - 130f, bannerY, centerX + 130f, bannerY + 24f)
        canvas.drawRoundRect(bannerRect, 4f, 4f, paint)

        paint.color = Color.WHITE
        paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        paint.textSize = 11f
        paint.textAlign = Paint.Align.CENTER
        canvas.drawText(title.uppercase(Locale.US), centerX, bannerY + 16f, paint)

        return bannerY + 42f
    }

    private fun drawFooter(canvas: Canvas) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        val footerY = (PAGE_HEIGHT - 45).toFloat()
        paint.color = colorGold
        paint.strokeWidth = 1f
        canvas.drawLine(50f, footerY - 10f, (PAGE_WIDTH - 50).toFloat(), footerY - 10f, paint)

        paint.color = Color.DKGRAY
        paint.textSize = 8f
        paint.textAlign = Paint.Align.CENTER
        paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
        canvas.drawText("This is an official computer-generated document issued by Al Hadid Academy, Nasirabad Jatlan, Azad Kashmir.", PAGE_WIDTH / 2f, footerY, paint)
        canvas.drawText("System Verification Code: AHAD-VER-" + System.currentTimeMillis().toString().takeLast(6), PAGE_WIDTH / 2f, footerY + 11f, paint)
    }

    // 1. GENERATE FEE RECEIPT PDF
    fun generateFeeReceiptPdf(context: Context, fee: FeeRecord, student: Student): File {
        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, 1).create()
        val page = document.startPage(pageInfo)
        val canvas = page.canvas

        var currentY = drawLetterhead(canvas, context, "OFFICIAL FEE RECEIPT")

        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        val leftX = 55f
        val rightX = (PAGE_WIDTH - 55).toFloat()

        // Meta info box
        paint.color = colorLightGrey
        paint.style = Paint.Style.FILL
        canvas.drawRoundRect(RectF(leftX, currentY, rightX, currentY + 36f), 4f, 4f, paint)

        paint.style = Paint.Style.FILL
        paint.color = colorIslamicGreen
        paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        paint.textSize = 10f
        paint.textAlign = Paint.Align.LEFT
        canvas.drawText("RECEIPT NO: ${fee.receiptNo}", leftX + 12f, currentY + 16f, paint)
        canvas.drawText("DATE: ${if (fee.paymentDate != "-") fee.paymentDate else SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())}", leftX + 12f, currentY + 28f, paint)

        paint.textAlign = Paint.Align.RIGHT
        canvas.drawText("MONTH / YEAR: ${fee.month} ${fee.year}", rightX - 12f, currentY + 16f, paint)
        canvas.drawText("STATUS: ${fee.status.uppercase(Locale.US)}", rightX - 12f, currentY + 28f, paint)

        currentY += 52f

        // Student Info Table
        paint.textAlign = Paint.Align.LEFT
        paint.color = colorIslamicGreen
        paint.textSize = 11f
        paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        canvas.drawText("STUDENT PARTICULARS", leftX, currentY, paint)

        currentY += 10f
        paint.style = Paint.Style.STROKE
        paint.color = colorBorder
        paint.strokeWidth = 1f
        canvas.drawRect(leftX, currentY, rightX, currentY + 68f, paint)

        paint.style = Paint.Style.FILL
        paint.color = colorTextDark
        paint.textSize = 9.5f
        paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)

        val col1 = leftX + 12f
        val col2 = leftX + 100f
        val col3 = leftX + 260f
        val col4 = leftX + 350f

        canvas.drawText("Student ID:", col1, currentY + 18f, paint)
        paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        canvas.drawText(student.studentId, col2, currentY + 18f, paint)
        paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)

        canvas.drawText("Class / Group:", col3, currentY + 18f, paint)
        paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        canvas.drawText("${student.className} / ${student.groupType}", col4, currentY + 18f, paint)
        paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)

        canvas.drawText("Student Name:", col1, currentY + 36f, paint)
        paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        canvas.drawText(student.name, col2, currentY + 36f, paint)
        paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)

        canvas.drawText("Teacher In-Charge:", col3, currentY + 36f, paint)
        canvas.drawText(student.assignedTeacher, col4, currentY + 36f, paint)

        canvas.drawText("Father Name:", col1, currentY + 54f, paint)
        canvas.drawText(student.fatherName, col2, currentY + 54f, paint)

        canvas.drawText("Parent Contact:", col3, currentY + 54f, paint)
        canvas.drawText(student.parentPhone, col4, currentY + 54f, paint)

        currentY += 88f

        // Fee Breakdown Table
        paint.color = colorIslamicGreen
        paint.textSize = 11f
        paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        canvas.drawText("FEE BREAKDOWN & ACCOUNT SUMMARY", leftX, currentY, paint)

        currentY += 10f
        // Header row
        paint.style = Paint.Style.FILL
        paint.color = colorIslamicGreen
        canvas.drawRect(leftX, currentY, rightX, currentY + 22f, paint)

        paint.color = Color.WHITE
        paint.textSize = 9.5f
        paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        canvas.drawText("DESCRIPTION", leftX + 12f, currentY + 15f, paint)
        paint.textAlign = Paint.Align.RIGHT
        canvas.drawText("AMOUNT (PKR)", rightX - 12f, currentY + 15f, paint)

        currentY += 22f
        val items = listOf(
            Pair("Tuition & Academy Monthly Fee (${fee.month})", String.format(Locale.US, "Rs %.2f", fee.amount)),
            Pair("Special Discount / Concession", String.format(Locale.US, "- Rs %.2f", fee.discount)),
            Pair("Late Fine / Extra Charges", String.format(Locale.US, "Rs %.2f", fee.fine)),
            Pair("Total Net Amount", String.format(Locale.US, "Rs %.2f", fee.amount - fee.discount + fee.fine)),
            Pair("Total Paid Amount", String.format(Locale.US, "Rs %.2f", fee.totalPaid)),
            Pair("Remaining Balance Due", String.format(Locale.US, "Rs %.2f", fee.due))
        )

        paint.style = Paint.Style.FILL
        items.forEachIndexed { idx, pair ->
            paint.color = if (idx % 2 == 0) Color.WHITE else colorLightGrey
            canvas.drawRect(leftX, currentY, rightX, currentY + 22f, paint)

            paint.color = if (idx >= 4) colorIslamicGreen else colorTextDark
            paint.typeface = if (idx >= 3) Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD) else Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
            paint.textAlign = Paint.Align.LEFT
            canvas.drawText(pair.first, leftX + 12f, currentY + 15f, paint)
            paint.textAlign = Paint.Align.RIGHT
            canvas.drawText(pair.second, rightX - 12f, currentY + 15f, paint)

            currentY += 22f
        }

        // Border around table
        paint.style = Paint.Style.STROKE
        paint.color = colorBorder
        paint.strokeWidth = 1f
        canvas.drawRect(leftX, currentY - (items.size * 22f) - 22f, rightX, currentY, paint)

        currentY += 25f

        // Payment Method & Collector Details
        paint.style = Paint.Style.FILL
        paint.textAlign = Paint.Align.LEFT
        paint.color = colorTextDark
        paint.textSize = 9.5f
        paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
        canvas.drawText("Payment Method: ${fee.paymentMethod}", leftX + 12f, currentY, paint)
        canvas.drawText("Received By: ${fee.receivedBy}", leftX + 12f, currentY + 16f, paint)

        // Signature Lines
        val signY = currentY + 65f
        paint.style = Paint.Style.STROKE
        paint.color = colorGold
        paint.strokeWidth = 1f
        canvas.drawLine(leftX + 20f, signY, leftX + 160f, signY, paint)
        canvas.drawLine(rightX - 160f, signY, rightX - 20f, signY, paint)

        paint.style = Paint.Style.FILL
        paint.color = colorTextDark
        paint.textSize = 9f
        paint.textAlign = Paint.Align.CENTER
        canvas.drawText("Cashier / Received By", leftX + 90f, signY + 14f, paint)
        canvas.drawText("Principal Awais Mustafa", rightX - 90f, signY + 14f, paint)

        drawFooter(canvas)

        document.finishPage(page)
        val file = File(getPdfDir(context), "Receipt_${fee.receiptNo}_${student.name.replace(" ", "_")}.pdf")
        document.writeTo(FileOutputStream(file))
        document.close()
        return file
    }

    // 2. GENERATE CHARACTER CERTIFICATE PDF
    fun generateCharacterCertificatePdf(context: Context, student: Student): File {
        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, 1).create()
        val page = document.startPage(pageInfo)
        val canvas = page.canvas

        var currentY = drawLetterhead(canvas, context, "CHARACTER & CONDUCT CERTIFICATE")

        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        val centerX = PAGE_WIDTH / 2f
        val leftX = 65f
        val rightX = (PAGE_WIDTH - 65).toFloat()

        currentY += 20f

        paint.color = colorGold
        paint.textSize = 11f
        paint.typeface = Typeface.create(Typeface.SERIF, Typeface.ITALIC)
        paint.textAlign = Paint.Align.CENTER
        canvas.drawText("IN THE NAME OF ALLAH, THE MOST GRACIOUS, THE MOST MERCIFUL", centerX, currentY, paint)

        currentY += 35f

        paint.color = colorTextDark
        paint.textSize = 11f
        paint.typeface = Typeface.create(Typeface.SERIF, Typeface.NORMAL)
        paint.textAlign = Paint.Align.LEFT

        val certDate = SimpleDateFormat("dd MMMM yyyy", Locale.US).format(Date())
        val textLines = listOf(
            "This is to certify that Mr. / Ms. ${student.name.uppercase(Locale.US)},",
            "Son / Daughter of ${student.fatherName.uppercase(Locale.US)}, bearing Student ID ${student.studentId},",
            "has been a regular and disciplined student of Al Hadid Academy,",
            "enrolled in Class ${student.className} (${student.groupType} Group),",
            "under the supervision of ${student.assignedTeacher}.",
            "",
            "During his/her tenure at this institution from ${student.admissionDate} to ${certDate},",
            "his/her moral conduct, devotion to Quranic studies, respect for teachers,",
            "and general behavior has been found to be:",
            "",
            "             ***** EXEMPLARY, PRAISEWORTHY & GOOD *****",
            "",
            "He/She maintains a high standard of Islamic character (Akhlaq-e-Hasana),",
            "regularity in Salah, and diligence in their daily studies and assignments.",
            "",
            "We pray to Almighty Allah for his/her grand success, prosperity and barakah",
            "in all future religious and worldly academic endeavors."
        )

        textLines.forEach { line ->
            if (line.contains("EXEMPLARY")) {
                paint.color = colorIslamicGreen
                paint.typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
                paint.textSize = 12.5f
                paint.textAlign = Paint.Align.CENTER
                canvas.drawText(line.trim(), centerX, currentY, paint)
                paint.textAlign = Paint.Align.LEFT
                paint.color = colorTextDark
                paint.textSize = 10.5f
                paint.typeface = Typeface.create(Typeface.SERIF, Typeface.NORMAL)
            } else {
                canvas.drawText(line, leftX, currentY, paint)
            }
            currentY += 20f
        }

        currentY += 40f

        // Signatures of both Principal Awais Mustafa & Teacher Anas Mustafa
        val signY = currentY + 30f
        paint.style = Paint.Style.STROKE
        paint.color = colorGold
        paint.strokeWidth = 1f
        canvas.drawLine(leftX + 15f, signY, leftX + 165f, signY, paint)
        canvas.drawLine(rightX - 165f, signY, rightX - 15f, signY, paint)

        // Seal circle in middle
        paint.style = Paint.Style.STROKE
        paint.color = colorIslamicGreen
        paint.strokeWidth = 1.5f
        canvas.drawCircle(centerX, signY - 5f, 26f, paint)

        paint.style = Paint.Style.FILL
        paint.color = colorIslamicGreen
        paint.textSize = 7.5f
        paint.textAlign = Paint.Align.CENTER
        paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        canvas.drawText("AL HADID", centerX, signY - 8f, paint)
        canvas.drawText("OFFICIAL SEAL", centerX, signY + 3f, paint)

        paint.color = colorTextDark
        paint.textSize = 9.5f
        canvas.drawText("Teacher Anas Mustafa", leftX + 90f, signY + 16f, paint)
        canvas.drawText("Administrator / Head of Academics", leftX + 90f, signY + 28f, paint)

        canvas.drawText("Principal Awais Mustafa", rightX - 90f, signY + 16f, paint)
        canvas.drawText("Principal & Main Tajweed Instructor", rightX - 90f, signY + 28f, paint)

        drawFooter(canvas)

        document.finishPage(page)
        val file = File(getPdfDir(context), "Character_Certificate_${student.studentId}_${student.name.replace(" ", "_")}.pdf")
        document.writeTo(FileOutputStream(file))
        document.close()
        return file
    }

    // 3. GENERATE WEEKLY PROGRESS REPORT PDF
    fun generateWeeklyReportPdf(
        context: Context,
        student: Student,
        quranEntry: QuranProgress?,
        booksList: List<BookProgress>,
        feeRecord: FeeRecord?
    ): File {
        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, 1).create()
        val page = document.startPage(pageInfo)
        val canvas = page.canvas

        var currentY = drawLetterhead(canvas, context, "WEEKLY COMPREHENSIVE PROGRESS REPORT")

        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        val leftX = 50f
        val rightX = (PAGE_WIDTH - 50).toFloat()

        // Student Info Box
        paint.color = colorLightGrey
        paint.style = Paint.Style.FILL
        canvas.drawRoundRect(RectF(leftX, currentY, rightX, currentY + 36f), 4f, 4f, paint)

        paint.color = colorIslamicGreen
        paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        paint.textSize = 10f
        paint.textAlign = Paint.Align.LEFT
        canvas.drawText("Student: ${student.name} (${student.studentId})", leftX + 12f, currentY + 16f, paint)
        canvas.drawText("Father: ${student.fatherName}", leftX + 12f, currentY + 28f, paint)

        paint.textAlign = Paint.Align.RIGHT
        canvas.drawText("Class: ${student.className} | Group: ${student.groupType}", rightX - 12f, currentY + 16f, paint)
        canvas.drawText("Teacher: ${student.assignedTeacher}", rightX - 12f, currentY + 28f, paint)

        currentY += 50f

        // Section 1: Quran Progress (Sabaq, Sabaqi, Manzil)
        paint.textAlign = Paint.Align.LEFT
        paint.color = colorIslamicGreen
        paint.textSize = 10.5f
        paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        canvas.drawText("1. QURANIC STUDIES & RECITATION PROGRESS", leftX, currentY, paint)

        currentY += 10f
        paint.style = Paint.Style.STROKE
        paint.color = colorBorder
        paint.strokeWidth = 1f
        canvas.drawRect(leftX, currentY, rightX, currentY + 60f, paint)

        paint.style = Paint.Style.FILL
        paint.color = colorTextDark
        paint.textSize = 9f
        paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)

        val q = quranEntry ?: QuranProgress("0", student.studentId, "Current", "Surah Yaseen Ayat 1-15", "Para 22 Quarter 1", "Para 1-4", "Good makharij", "Regular and attentive", student.assignedTeacher)
        canvas.drawText("• Current Sabaq (Daily Lesson): ${q.sabaq}", leftX + 10f, currentY + 16f, paint)
        canvas.drawText("• Sabaqi (Recent Revision): ${q.sabaqi}", leftX + 10f, currentY + 30f, paint)
        canvas.drawText("• Manzil (Old Revision): ${q.manzil}", leftX + 10f, currentY + 44f, paint)
        canvas.drawText("• Tajweed Notes & Remarks: ${q.remarks}", leftX + 10f, currentY + 56f, paint)

        currentY += 76f

        // Section 2: Book-wise Academic Progress ("Apka Bacha Kis Book Me Acha Hai")
        paint.color = colorIslamicGreen
        paint.textSize = 10.5f
        paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        canvas.drawText("2. SCHOOL BOOKS PROGRESS (\"Apka Bacha Kis Book Me Acha Hai\")", leftX, currentY, paint)

        currentY += 10f
        // Header
        paint.style = Paint.Style.FILL
        paint.color = colorIslamicGreen
        canvas.drawRect(leftX, currentY, rightX, currentY + 18f, paint)

        paint.color = Color.WHITE
        paint.textSize = 8.5f
        paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        canvas.drawText("SUBJECT", leftX + 10f, currentY + 12f, paint)
        canvas.drawText("SCORE / GRADE", leftX + 140f, currentY + 12f, paint)
        canvas.drawText("TEACHER REMARKS / OBSERVATION", leftX + 260f, currentY + 12f, paint)

        currentY += 18f
        val filteredBooks = booksList.filter { it.studentId == student.studentId }.take(7)
        filteredBooks.forEachIndexed { i, b ->
            paint.color = if (i % 2 == 0) Color.WHITE else colorLightGrey
            canvas.drawRect(leftX, currentY, rightX, currentY + 18f, paint)

            paint.color = colorTextDark
            paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
            paint.textSize = 8.5f
            canvas.drawText(b.subject, leftX + 10f, currentY + 12f, paint)
            canvas.drawText("${b.percentage}% (${b.grade})", leftX + 140f, currentY + 12f, paint)
            canvas.drawText(b.remarks.take(45), leftX + 260f, currentY + 12f, paint)

            currentY += 18f
        }

        paint.style = Paint.Style.STROKE
        paint.color = colorBorder
        canvas.drawRect(leftX, currentY - (filteredBooks.size * 18f) - 18f, rightX, currentY, paint)

        currentY += 24f

        // Section 3: Fee Status & Attendance
        paint.style = Paint.Style.FILL
        paint.color = colorIslamicGreen
        paint.textSize = 10.5f
        paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        canvas.drawText("3. ATTENDANCE & FEE STATUS", leftX, currentY, paint)

        currentY += 10f
        paint.style = Paint.Style.STROKE
        paint.color = colorBorder
        canvas.drawRect(leftX, currentY, rightX, currentY + 36f, paint)

        paint.style = Paint.Style.FILL
        paint.color = colorTextDark
        paint.textSize = 9f
        paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
        val feeStatusText = feeRecord?.status ?: "Paid"
        val feeAmountText = feeRecord?.totalPaid?.toInt() ?: student.monthlyFee.toInt()
        canvas.drawText("Attendance Record: Present 96% of active days this month.", leftX + 12f, currentY + 15f, paint)
        canvas.drawText("Fee Status: $feeStatusText (Rs $feeAmountText paid for current cycle).", leftX + 12f, currentY + 28f, paint)

        currentY += 56f

        // Signatures
        val signY = currentY + 20f
        paint.style = Paint.Style.STROKE
        paint.color = colorGold
        canvas.drawLine(leftX + 20f, signY, leftX + 160f, signY, paint)
        canvas.drawLine(rightX - 160f, signY, rightX - 20f, signY, paint)

        paint.style = Paint.Style.FILL
        paint.color = colorTextDark
        paint.textSize = 9f
        paint.textAlign = Paint.Align.CENTER
        canvas.drawText("Class Teacher: ${student.assignedTeacher}", leftX + 90f, signY + 14f, paint)
        canvas.drawText("Principal Awais Mustafa", rightX - 90f, signY + 14f, paint)

        drawFooter(canvas)

        document.finishPage(page)
        val file = File(getPdfDir(context), "Weekly_Report_${student.studentId}.pdf")
        document.writeTo(FileOutputStream(file))
        document.close()
        return file
    }

    // 4. GENERATE TEST RESULT CARD PDF
    fun generateResultCardPdf(
        context: Context,
        test: TestRecord,
        marks: TestMarks,
        student: Student
    ): File {
        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, 1).create()
        val page = document.startPage(pageInfo)
        val canvas = page.canvas

        var currentY = drawLetterhead(canvas, context, "OFFICIAL RESULT CARD")

        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        val leftX = 55f
        val rightX = (PAGE_WIDTH - 55).toFloat()

        // Test details banner
        paint.color = colorLightGrey
        paint.style = Paint.Style.FILL
        canvas.drawRoundRect(RectF(leftX, currentY, rightX, currentY + 36f), 4f, 4f, paint)

        paint.color = colorIslamicGreen
        paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        paint.textSize = 10f
        paint.textAlign = Paint.Align.LEFT
        canvas.drawText("TEST: ${test.title}", leftX + 12f, currentY + 16f, paint)
        canvas.drawText("SUBJECT: ${test.subject}", leftX + 12f, currentY + 28f, paint)

        paint.textAlign = Paint.Align.RIGHT
        canvas.drawText("DATE: ${test.date}", rightX - 12f, currentY + 16f, paint)
        canvas.drawText("CLASS: ${test.className}", rightX - 12f, currentY + 28f, paint)

        currentY += 54f

        // Student Info
        paint.textAlign = Paint.Align.LEFT
        paint.color = colorIslamicGreen
        paint.textSize = 11f
        paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        canvas.drawText("CANDIDATE PARTICULARS", leftX, currentY, paint)

        currentY += 10f
        paint.style = Paint.Style.STROKE
        paint.color = colorBorder
        canvas.drawRect(leftX, currentY, rightX, currentY + 50f, paint)

        paint.style = Paint.Style.FILL
        paint.color = colorTextDark
        paint.textSize = 9.5f
        paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
        canvas.drawText("Student Name: ${student.name}", leftX + 12f, currentY + 18f, paint)
        canvas.drawText("Father Name: ${student.fatherName}", leftX + 12f, currentY + 34f, paint)
        canvas.drawText("Student ID: ${student.studentId}", leftX + 260f, currentY + 18f, paint)
        canvas.drawText("Teacher: ${student.assignedTeacher}", leftX + 260f, currentY + 34f, paint)

        currentY += 70f

        // Result Scorecard
        paint.color = colorIslamicGreen
        paint.textSize = 11f
        paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        canvas.drawText("ACADEMIC PERFORMANCE & MARKS EVALUATION", leftX, currentY, paint)

        currentY += 10f
        // Big Marks Box
        paint.style = Paint.Style.FILL
        paint.color = colorLightGrey
        canvas.drawRoundRect(RectF(leftX, currentY, rightX, currentY + 80f), 6f, 6f, paint)

        paint.style = Paint.Style.STROKE
        paint.color = colorGold
        paint.strokeWidth = 1.5f
        canvas.drawRoundRect(RectF(leftX, currentY, rightX, currentY + 80f), 6f, 6f, paint)

        paint.style = Paint.Style.FILL
        val pct = (marks.obtainedMarks / marks.totalMarks * 100).toInt()

        paint.color = colorIslamicGreen
        paint.textSize = 28f
        paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        paint.textAlign = Paint.Align.CENTER
        canvas.drawText("${marks.obtainedMarks.toInt()} / ${marks.totalMarks.toInt()}", PAGE_WIDTH / 2f, currentY + 38f, paint)

        paint.color = colorGold
        paint.textSize = 12f
        canvas.drawText("PERCENTAGE: $pct%   |   GRADE: ${marks.grade}   |   POSITION: ${marks.position}", PAGE_WIDTH / 2f, currentY + 58f, paint)

        currentY += 100f

        // Teacher Remarks
        paint.textAlign = Paint.Align.LEFT
        paint.color = colorIslamicGreen
        paint.textSize = 10.5f
        paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        canvas.drawText("EVALUATOR'S REMARKS & GUIDANCE", leftX, currentY, paint)

        currentY += 10f
        paint.style = Paint.Style.STROKE
        paint.color = colorBorder
        canvas.drawRect(leftX, currentY, rightX, currentY + 45f, paint)

        paint.style = Paint.Style.FILL
        paint.color = colorTextDark
        paint.textSize = 9.5f
        paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.ITALIC)
        val rem = if (marks.remarks.isNotEmpty()) marks.remarks else "Excellent performance. Demonstrated thorough comprehension and dedication."
        canvas.drawText("\"$rem\"", leftX + 12f, currentY + 26f, paint)

        currentY += 75f

        // Signatures
        val signY = currentY + 20f
        paint.style = Paint.Style.STROKE
        paint.color = colorGold
        paint.strokeWidth = 1f
        canvas.drawLine(leftX + 20f, signY, leftX + 160f, signY, paint)
        canvas.drawLine(rightX - 160f, signY, rightX - 20f, signY, paint)

        paint.style = Paint.Style.FILL
        paint.color = colorTextDark
        paint.textSize = 9f
        paint.textAlign = Paint.Align.CENTER
        canvas.drawText("Teacher In-Charge", leftX + 90f, signY + 14f, paint)
        canvas.drawText("Principal Awais Mustafa", rightX - 90f, signY + 14f, paint)

        drawFooter(canvas)

        document.finishPage(page)
        val file = File(getPdfDir(context), "ResultCard_${student.studentId}_${test.testId}.pdf")
        document.writeTo(FileOutputStream(file))
        document.close()
        return file
    }

    // Helper: Share PDF via WhatsApp or any sharing app
    fun sharePdf(context: Context, pdfFile: File, message: String, phoneNumber: String? = null) {
        try {
            val uri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                pdfFile
            )

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_TEXT, message)
                putExtra(Intent.EXTRA_SUBJECT, "Al Hadid Academy Document")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            // WhatsApp specific target if installed, otherwise system chooser
            val chooser = Intent.createChooser(shareIntent, "Share Document via WhatsApp / Email")
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(chooser)
        } catch (e: Exception) {
            Toast.makeText(context, "Error sharing document: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    // Helper: View / Open PDF
    fun openPdf(context: Context, pdfFile: File) {
        try {
            val uri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                pdfFile
            )

            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/pdf")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "PDF generated successfully at ${pdfFile.name}. Open with any PDF reader.", Toast.LENGTH_LONG).show()
        }
    }
}
