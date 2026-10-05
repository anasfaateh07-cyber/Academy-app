package com.example.data

enum class UserRole(val displayName: String) {
    PRINCIPAL_AWAIS("Principal Awais Mustafa (Full Admin)"),
    TEACHER_ANAS("Teacher Anas Mustafa (Full Admin)"),
    TEACHER_ISRA("Teacher Isra (Limited Teacher)"),
    PARENT("Parent Portal")
}

data class CurrentUser(
    val role: UserRole,
    val name: String,
    val email: String,
    val phone: String,
    val isFullAdmin: Boolean,
    val allowedGroups: List<String>
)

data class Student(
    val studentId: String, // e.g. AHAD-001
    val name: String,
    val fatherName: String,
    val motherName: String = "",
    val gender: String, // "Boy" or "Girl"
    val dateOfBirth: String,
    val className: String, // "Playgroup", "1", "2", "3", "4", "5", "6", "7", "8"
    val groupType: String, // "Hifz", "Nazra", "Tajweed", "Tuition Boy", "Tuition Girl", "Playgroup", "Computer"
    val parentPhone: String, // WhatsApp phone
    val motherPhone: String = "",
    val address: String = "Nasirabad Jatlan, Azad Kashmir",
    val admissionDate: String = "2024-03-15",
    val monthlyFee: Double = 1500.0,
    val assignedTeacher: String, // "Awais Mustafa", "Anas Mustafa", "Isra"
    val previousSchool: String = "Government Primary School",
    val medicalInfo: String = "None",
    val stationaryNeeded: String? = null
)

data class Teacher(
    val teacherId: String,
    val name: String,
    val email: String,
    val phone: String,
    val assignedGroups: List<String>,
    val role: String, // "Full Admin" or "Teacher"
    val specialization: String
)

data class FeeRecord(
    val feeId: String,
    val receiptNo: String, // e.g. AHAD-REC-001
    val studentId: String,
    val studentName: String,
    val month: String, // e.g. "October"
    val year: Int = 2026,
    val amount: Double,
    val discount: Double = 0.0,
    val fine: Double = 0.0,
    val totalPaid: Double,
    val due: Double = 0.0,
    val status: String, // "Paid", "Pending", "Overdue", "FREE"
    val paymentMethod: String = "Cash", // "Cash", "Easypaisa", "JazzCash"
    val paymentDate: String,
    val receivedBy: String, // "Anas Mustafa" or "Awais Mustafa"
    val concessionType: String = "No Discount", // "No Discount", "50% Free", "100% Free - Yateem / Mustahiq"
    val discountReason: String = "" // "Yateem / Hafiz / Staff Child"
)

data class AttendanceRecord(
    val id: String,
    val date: String, // YYYY-MM-DD
    val studentId: String,
    val studentName: String,
    val className: String,
    val groupType: String,
    val status: String, // "Present", "Absent"
    val markedBy: String
)

data class QuranProgress(
    val id: String,
    val studentId: String,
    val date: String,
    val sabaq: String, // e.g. Surah Yaseen Ayat 1-10
    val sabaqi: String, // e.g. Para 22 Quarter 1
    val manzil: String, // e.g. Para 1-4
    val tajweedMistakes: String, // e.g. Madd & Ghunnah
    val remarks: String,
    val teacherName: String,
    val hasAudio: Boolean = false,
    val audioDurationSec: Int = 0,
    val sabaqStatus: String = "Yaad", // "Yaad", "Kacha", "Pakka"
    val audioPath: String = ""
)

data class BookProgress(
    val id: String,
    val studentId: String,
    val subject: String, // "Math", "English", "Urdu", "Science", "Islamiat", "Computer", "Nazra Qaida", "Tajweed"
    val grade: String, // "A+", "A", "B", "C", "Needs Improvement"
    val percentage: Int, // 0 - 100
    val remarks: String,
    val updatedDate: String,
    val updatedBy: String
)

data class TestRecord(
    val testId: String,
    val title: String, // e.g. "Monthly Test October 2026"
    val subject: String,
    val groupType: String,
    val className: String,
    val date: String,
    val totalMarks: Int = 100,
    val createdBy: String
)

data class TestMarks(
    val id: String,
    val testId: String,
    val studentId: String,
    val studentName: String,
    val obtainedMarks: Double,
    val totalMarks: Double = 100.0,
    val grade: String,
    val position: String = "-",
    val remarks: String = ""
)

data class Announcement(
    val id: String,
    val title: String,
    val message: String,
    val type: String, // "Important", "Holiday", "Fee Reminder", "Absent Alert", "Stationary Required", "PTM"
    val targetGroup: String, // "All", "Hifz", "Nazra", "Tajweed", "Tuition Boy", "Tuition Girl", "Playgroup"
    val createdBy: String,
    val createdAt: String,
    val priority: String = "High" // "High", "Normal"
)

data class AppNotification(
    val id: String,
    val title: String,
    val body: String,
    val type: String,
    val timestamp: String,
    val targetStudentId: String? = null,
    val isRead: Boolean = false
)
