package com.example.data

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AcademyRepository {

    private val _currentUser = MutableStateFlow<CurrentUser?>(null)
    val currentUser: StateFlow<CurrentUser?> = _currentUser.asStateFlow()

    private val _students = MutableStateFlow<List<Student>>(emptyList())
    val students: StateFlow<List<Student>> = _students.asStateFlow()

    private val _teachers = MutableStateFlow<List<Teacher>>(emptyList())
    val teachers: StateFlow<List<Teacher>> = _teachers.asStateFlow()

    private val _fees = MutableStateFlow<List<FeeRecord>>(emptyList())
    val fees: StateFlow<List<FeeRecord>> = _fees.asStateFlow()

    private val _attendance = MutableStateFlow<List<AttendanceRecord>>(emptyList())
    val attendance: StateFlow<List<AttendanceRecord>> = _attendance.asStateFlow()

    private val _quranProgress = MutableStateFlow<List<QuranProgress>>(emptyList())
    val quranProgress: StateFlow<List<QuranProgress>> = _quranProgress.asStateFlow()

    private val _bookProgress = MutableStateFlow<List<BookProgress>>(emptyList())
    val bookProgress: StateFlow<List<BookProgress>> = _bookProgress.asStateFlow()

    private val _tests = MutableStateFlow<List<TestRecord>>(emptyList())
    val tests: StateFlow<List<TestRecord>> = _tests.asStateFlow()

    private val _testMarks = MutableStateFlow<List<TestMarks>>(emptyList())
    val testMarks: StateFlow<List<TestMarks>> = _testMarks.asStateFlow()

    private val _announcements = MutableStateFlow<List<Announcement>>(emptyList())
    val announcements: StateFlow<List<Announcement>> = _announcements.asStateFlow()

    private val _notifications = MutableStateFlow<List<AppNotification>>(emptyList())
    val notifications: StateFlow<List<AppNotification>> = _notifications.asStateFlow()

    init {
        seedInitialData()
    }

    private fun seedInitialData() {
        val seededTeachers = listOf(
            Teacher(
                teacherId = "T-01",
                name = "Principal Awais Mustafa",
                email = "awais@alhadid.com",
                phone = "+92 300 9876541",
                assignedGroups = listOf("Tajweed", "Executive Administration"),
                role = "Full Admin",
                specialization = "Qirat, Tajweed Ul Quran, Management"
            ),
            Teacher(
                teacherId = "T-02",
                name = "Teacher Anas Mustafa",
                email = "anas@alhadid.com",
                phone = "+92 300 9876542",
                assignedGroups = listOf("Hifz", "Nazra", "Tuition Boy", "Computer Course", "Tajweed"),
                role = "Full Admin",
                specialization = "Hifz Ul Quran, Mathematics, Computer Sciences"
            ),
            Teacher(
                teacherId = "T-03",
                name = "Teacher Isra",
                email = "isra@alhadid.com",
                phone = "+92 300 9876543",
                assignedGroups = listOf("Tuition Girl", "Playgroup"),
                role = "Teacher",
                specialization = "Early Childhood Education, Science, English"
            )
        )
        _teachers.value = seededTeachers

        // 14 Seeded Students matching prompt categories
        val seededStudents = listOf(
            // 3 Hifz Boys
            Student(
                studentId = "AHAD-001",
                name = "Muhammad Bilal",
                fatherName = "Tariq Khan",
                gender = "Boy",
                dateOfBirth = "2013-05-12",
                className = "6",
                groupType = "Hifz",
                parentPhone = "+92 300 1234567",
                address = "Mohallah Kalan, Nasirabad Jatlan, Azad Kashmir",
                monthlyFee = 2000.0,
                assignedTeacher = "Anas Mustafa"
            ),
            Student(
                studentId = "AHAD-002",
                name = "Abdullah Zaid",
                fatherName = "Rashid Mehmood",
                gender = "Boy",
                dateOfBirth = "2014-08-20",
                className = "5",
                groupType = "Hifz",
                parentPhone = "+92 301 2345678",
                address = "Main Bazar, Nasirabad Jatlan, Azad Kashmir",
                monthlyFee = 2000.0,
                assignedTeacher = "Anas Mustafa"
            ),
            Student(
                studentId = "AHAD-003",
                name = "Hamza Farooq",
                fatherName = "Farooq Ahmad",
                gender = "Boy",
                dateOfBirth = "2012-11-04",
                className = "7",
                groupType = "Hifz",
                parentPhone = "+92 302 3456789",
                address = "Near Masjid Bilal, Nasirabad Jatlan, Azad Kashmir",
                monthlyFee = 2000.0,
                assignedTeacher = "Anas Mustafa"
            ),
            // 2 Nazra
            Student(
                studentId = "AHAD-004",
                name = "Usman Ali",
                fatherName = "Muhammad Ali",
                gender = "Boy",
                dateOfBirth = "2015-02-14",
                className = "4",
                groupType = "Nazra",
                parentPhone = "+92 303 4567890",
                address = "Railway Road, Nasirabad Jatlan, Azad Kashmir",
                monthlyFee = 1500.0,
                assignedTeacher = "Anas Mustafa"
            ),
            Student(
                studentId = "AHAD-005",
                name = "Huzaifa Noor",
                fatherName = "Noor Muhammad",
                gender = "Boy",
                dateOfBirth = "2016-09-18",
                className = "3",
                groupType = "Nazra",
                parentPhone = "+92 304 5678901",
                address = "Canal View, Nasirabad Jatlan, Azad Kashmir",
                monthlyFee = 1500.0,
                assignedTeacher = "Anas Mustafa"
            ),
            // 2 Tajweed
            Student(
                studentId = "AHAD-006",
                name = "Zeeshan Khan",
                fatherName = "Shaukat Khan",
                gender = "Boy",
                dateOfBirth = "2011-07-25",
                className = "8",
                groupType = "Tajweed",
                parentPhone = "+92 305 6789012",
                address = "Chowk Yadgar, Nasirabad Jatlan, Azad Kashmir",
                monthlyFee = 1800.0,
                assignedTeacher = "Awais Mustafa"
            ),
            Student(
                studentId = "AHAD-007",
                name = "Talha Rehman",
                fatherName = "Abdul Rehman",
                gender = "Boy",
                dateOfBirth = "2012-04-10",
                className = "7",
                groupType = "Tajweed",
                parentPhone = "+92 306 7890123",
                address = "Old Village, Nasirabad Jatlan, Azad Kashmir",
                monthlyFee = 1800.0,
                assignedTeacher = "Awais Mustafa"
            ),
            // 3 Boys Tuition (Classes 3, 7, 8)
            Student(
                studentId = "AHAD-008",
                name = "Saad Babar",
                fatherName = "Babar Azam",
                gender = "Boy",
                dateOfBirth = "2016-01-30",
                className = "3",
                groupType = "Tuition Boy",
                parentPhone = "+92 307 8901234",
                address = "Gulberg Colony, Nasirabad Jatlan, Azad Kashmir",
                monthlyFee = 1200.0,
                assignedTeacher = "Anas Mustafa"
            ),
            Student(
                studentId = "AHAD-009",
                name = "Rayyan Shah",
                fatherName = "Shah Faisal",
                gender = "Boy",
                dateOfBirth = "2012-06-15",
                className = "7",
                groupType = "Tuition Boy",
                parentPhone = "+92 308 9012345",
                address = "Model Town, Nasirabad Jatlan, Azad Kashmir",
                monthlyFee = 1500.0,
                assignedTeacher = "Anas Mustafa"
            ),
            Student(
                studentId = "AHAD-010",
                name = "Danyal Malik",
                fatherName = "Malik Asif",
                gender = "Boy",
                dateOfBirth = "2011-10-08",
                className = "8",
                groupType = "Tuition Boy",
                parentPhone = "+92 309 0123456",
                address = "School Road, Nasirabad Jatlan, Azad Kashmir",
                monthlyFee = 1500.0,
                assignedTeacher = "Anas Mustafa"
            ),
            // 2 Girls Tuition (Classes 5, 6)
            Student(
                studentId = "AHAD-011",
                name = "Fatima Zahra",
                fatherName = "Zahid Hussain",
                gender = "Girl",
                dateOfBirth = "2014-03-22",
                className = "5",
                groupType = "Tuition Girl",
                parentPhone = "+92 310 1122334",
                address = "Green Town, Nasirabad Jatlan, Azad Kashmir",
                monthlyFee = 1400.0,
                assignedTeacher = "Isra"
            ),
            Student(
                studentId = "AHAD-012",
                name = "Ayesha Bibi",
                fatherName = "Akhtar Zaman",
                gender = "Girl",
                dateOfBirth = "2013-12-05",
                className = "6",
                groupType = "Tuition Girl",
                parentPhone = "+92 311 2233445",
                address = "Post Office Road, Nasirabad Jatlan, Azad Kashmir",
                monthlyFee = 1400.0,
                assignedTeacher = "Isra"
            ),
            // 2 Playgroup (1 Boy, 1 Girl)
            Student(
                studentId = "AHAD-013",
                name = "Zainab Gul",
                fatherName = "Gul Nawaz",
                gender = "Girl",
                dateOfBirth = "2021-04-18",
                className = "Playgroup",
                groupType = "Playgroup",
                parentPhone = "+92 312 3344556",
                address = "Garden Town, Nasirabad Jatlan, Azad Kashmir",
                monthlyFee = 1000.0,
                assignedTeacher = "Isra"
            ),
            Student(
                studentId = "AHAD-014",
                name = "Ibrahim Jan",
                fatherName = "Jan Sher",
                gender = "Boy",
                dateOfBirth = "2021-08-11",
                className = "Playgroup",
                groupType = "Playgroup",
                parentPhone = "+92 313 4455667",
                address = "New City, Nasirabad Jatlan, Azad Kashmir",
                monthlyFee = 1000.0,
                assignedTeacher = "Isra"
            )
        )
        _students.value = seededStudents

        // Seeded Fees
        val seededFees = mutableListOf<FeeRecord>()
        seededStudents.forEachIndexed { index, st ->
            val receiptNumber = "AHAD-REC-${String.format(Locale.US, "%03d", index + 1)}"
            val status = if (index % 3 == 0) "Paid" else if (index % 3 == 1) "Pending" else "Overdue"
            val paid = if (status == "Paid") st.monthlyFee else 0.0
            val due = if (status == "Paid") 0.0 else st.monthlyFee
            seededFees.add(
                FeeRecord(
                    feeId = "F-$index",
                    receiptNo = receiptNumber,
                    studentId = st.studentId,
                    studentName = st.name,
                    month = "October",
                    year = 2026,
                    amount = st.monthlyFee,
                    discount = 0.0,
                    fine = 0.0,
                    totalPaid = paid,
                    due = due,
                    status = status,
                    paymentMethod = if (index % 2 == 0) "Cash" else "Easypaisa",
                    paymentDate = if (status == "Paid") "2026-10-03" else "-",
                    receivedBy = if (index % 2 == 0) "Anas Mustafa" else "Awais Mustafa"
                )
            )
        }
        _fees.value = seededFees

        // Seeded Attendance
        val seededAttendance = mutableListOf<AttendanceRecord>()
        seededStudents.forEachIndexed { i, st ->
            val status = if (i == 4 || i == 9) "Absent" else "Present"
            seededAttendance.add(
                AttendanceRecord(
                    id = "ATT-$i",
                    date = "2026-10-05",
                    studentId = st.studentId,
                    studentName = st.name,
                    className = st.className,
                    groupType = st.groupType,
                    status = status,
                    markedBy = if (st.assignedTeacher == "Isra") "Teacher Isra" else "Teacher Anas Mustafa"
                )
            )
        }
        _attendance.value = seededAttendance

        // Seeded Quran Progress (for Hifz, Nazra, Tajweed)
        val seededQuran = listOf(
            QuranProgress(
                id = "QP-1",
                studentId = "AHAD-001",
                date = "2026-10-05",
                sabaq = "Surah Yaseen Ayat 1-18",
                sabaqi = "Para 22 Quarter 2",
                manzil = "Para 1 to 4",
                tajweedMistakes = "Qalqalah on Qaaf clear, keep breathing in check",
                remarks = "MashaAllah good recitation and memory pace",
                teacherName = "Anas Mustafa",
                hasAudio = true,
                audioDurationSec = 28
            ),
            QuranProgress(
                id = "QP-2",
                studentId = "AHAD-002",
                date = "2026-10-05",
                sabaq = "Surah Al-Mulk Ayat 1-14",
                sabaqi = "Para 28 First Ruku",
                manzil = "Para 5 to 7",
                tajweedMistakes = "Ghunnah in Noon Mushaddad",
                remarks = "Very focused, keep revising sabaqi daily",
                teacherName = "Anas Mustafa",
                hasAudio = true,
                audioDurationSec = 30
            ),
            QuranProgress(
                id = "QP-3",
                studentId = "AHAD-006",
                date = "2026-10-05",
                sabaq = "Surah Al-Baqarah Ayat 255-257",
                sabaqi = "Para 2 Rukuh 3",
                manzil = "Para 1 Full",
                tajweedMistakes = "Madd Munfasil duration correct",
                remarks = "Excellent Tajweed pronunciation under Qari Awais",
                teacherName = "Awais Mustafa",
                hasAudio = true,
                audioDurationSec = 32
            )
        )
        _quranProgress.value = seededQuran

        // Seeded Book Progress (all subjects)
        val seededBookProgress = mutableListOf<BookProgress>()
        seededStudents.forEach { st ->
            val subjects = listOf("Math", "English", "Urdu", "Science", "Islamiat", "Computer", "Nazra Qaida", "Tajweed")
            subjects.forEachIndexed { subIndex, sub ->
                val (pct, grade, rem) = when (sub) {
                    "Math" -> Triple(90, "A+", "Math me bohat acha hai, quick calculations")
                    "English" -> Triple(75, "B", "English vocabulary me thori mehnat ki zaroorat hai")
                    "Urdu" -> Triple(88, "A", "Urdu writing aur reading bohat behtareen hai")
                    "Science" -> Triple(84, "A", "Science concepts clear hain")
                    "Islamiat" -> Triple(95, "A+", "Duaen aur kalma achi tarah yaad hain")
                    "Computer" -> Triple(80, "A", "Keyboard typing aur basics achi samajh hain")
                    "Nazra Qaida" -> Triple(92, "A+", "Harf ki pehchan aur Makharij perfect hain")
                    else -> Triple(85, "A", "Tajweed qawaid follow kar raha hai")
                }
                seededBookProgress.add(
                    BookProgress(
                        id = "BP-${st.studentId}-$subIndex",
                        studentId = st.studentId,
                        subject = sub,
                        grade = grade,
                        percentage = pct,
                        remarks = rem,
                        updatedDate = "2026-10-04",
                        updatedBy = st.assignedTeacher
                    )
                )
            }
        }
        _bookProgress.value = seededBookProgress

        // Seeded Tests
        val seededTests = listOf(
            TestRecord(
                testId = "T-101",
                title = "Monthly Academic Assessment (October)",
                subject = "Mathematics & Science",
                groupType = "Tuition Boy",
                className = "7, 8",
                date = "2026-10-02",
                totalMarks = 100,
                createdBy = "Teacher Anas Mustafa"
            ),
            TestRecord(
                testId = "T-102",
                title = "Hifz Tajweed & Retention Evaluation",
                subject = "Quran & Tajweed",
                groupType = "Hifz",
                className = "All Hifz",
                date = "2026-10-03",
                totalMarks = 100,
                createdBy = "Principal Awais Mustafa"
            ),
            TestRecord(
                testId = "T-103",
                title = "Girls Tuition English & Science Exam",
                subject = "English / Science",
                groupType = "Tuition Girl",
                className = "5, 6",
                date = "2026-10-01",
                totalMarks = 100,
                createdBy = "Teacher Isra"
            )
        )
        _tests.value = seededTests

        // Seeded Test Marks
        val seededMarks = listOf(
            TestMarks("TM-1", "T-101", "AHAD-009", "Rayyan Shah", 92.0, 100.0, "A+", "1st", "Outstanding in Algebra"),
            TestMarks("TM-2", "T-101", "AHAD-010", "Danyal Malik", 86.0, 100.0, "A", "2nd", "Very good work"),
            TestMarks("TM-3", "T-102", "AHAD-001", "Muhammad Bilal", 96.0, 100.0, "A+", "1st", "Flawless retention of Surah Yaseen"),
            TestMarks("TM-4", "T-102", "AHAD-002", "Abdullah Zaid", 90.0, 100.0, "A+", "2nd", "Excellent Tajweed Makharij"),
            TestMarks("TM-5", "T-103", "AHAD-011", "Fatima Zahra", 94.0, 100.0, "A+", "1st", "Brilliant grammar and handwriting")
        )
        _testMarks.value = seededMarks

        // Seeded Announcements
        val seededAnnouncements = listOf(
            Announcement(
                id = "ANN-1",
                title = "Quarterly Parent-Teacher Meeting (PTM)",
                message = "Respected Parents, PTM will be held this Saturday at 10:00 AM. Please arrive on time to discuss your child's Quran & academic report.",
                type = "PTM",
                targetGroup = "All",
                createdBy = "Principal Awais Mustafa",
                createdAt = "2026-10-04 09:30",
                priority = "High"
            ),
            Announcement(
                id = "ANN-2",
                title = "Tajweed Ul Quran Special Seminar",
                message = "All Tajweed & Hifz students will participate in the special Makharij workshop conducted by Qari Awais Mustafa this Friday.",
                type = "Important",
                targetGroup = "Tajweed",
                createdBy = "Principal Awais Mustafa",
                createdAt = "2026-10-03 14:00",
                priority = "High"
            ),
            Announcement(
                id = "ANN-3",
                title = "Stationary & Course Copies Required",
                message = "Students in Class 3, 7 and 8 must bring their 4-line English copies and geometry box tomorrow.",
                type = "Stationary Required",
                targetGroup = "Tuition Boy",
                createdBy = "Teacher Anas Mustafa",
                createdAt = "2026-10-04 11:20",
                priority = "Normal"
            )
        )
        _announcements.value = seededAnnouncements

        // Seeded Notifications
        val seededNotifs = listOf(
            AppNotification(
                id = "N-1",
                title = "Al Hadid Academy - Important",
                body = "Quarterly Parent-Teacher Meeting (PTM) announced for Saturday 10:00 AM.",
                type = "Announcement",
                timestamp = "Today 09:30 AM"
            ),
            AppNotification(
                id = "N-2",
                title = "Fee Receipt Generated",
                body = "Muhammad Bilal October Fee Rs 2000 has been marked Paid. Receipt # AHAD-REC-001 available.",
                type = "Fee",
                timestamp = "Yesterday",
                targetStudentId = "AHAD-001"
            ),
            AppNotification(
                id = "N-3",
                title = "Stationary Reminder",
                body = "Apke bache ko kal Math copy aur Geometry box chahiye - Al Hadid Academy",
                type = "Stationary",
                timestamp = "Today 11:20 AM",
                targetStudentId = "AHAD-001"
            )
        )
        _notifications.value = seededNotifs
    }

    // Role / User Authentication
    fun login(emailOrPhone: String, passOrOtp: String): Boolean {
        val trimmed = emailOrPhone.trim().lowercase(Locale.US)
        when {
            // Principal Awais Mustafa - Full Admin
            trimmed == "awais@alhadid.com" || trimmed == "awais" -> {
                _currentUser.value = CurrentUser(
                    role = UserRole.PRINCIPAL_AWAIS,
                    name = "Principal Awais Mustafa",
                    email = "awais@alhadid.com",
                    phone = "+92 300 9876541",
                    isFullAdmin = true,
                    allowedGroups = listOf("All", "Tajweed", "Hifz", "Nazra", "Tuition Boy", "Tuition Girl", "Playgroup", "Computer")
                )
                return true
            }
            // Teacher Anas Mustafa - Full Admin
            trimmed == "anas@alhadid.com" || trimmed == "anas" -> {
                _currentUser.value = CurrentUser(
                    role = UserRole.TEACHER_ANAS,
                    name = "Teacher Anas Mustafa",
                    email = "anas@alhadid.com",
                    phone = "+92 300 9876542",
                    isFullAdmin = true,
                    allowedGroups = listOf("All", "Hifz", "Nazra", "Tuition Boy", "Computer Course", "Tajweed", "Tuition Girl", "Playgroup")
                )
                return true
            }
            // Teacher Isra - Limited Teacher
            trimmed == "isra@alhadid.com" || trimmed == "isra" -> {
                _currentUser.value = CurrentUser(
                    role = UserRole.TEACHER_ISRA,
                    name = "Teacher Isra",
                    email = "isra@alhadid.com",
                    phone = "+92 300 9876543",
                    isFullAdmin = false,
                    allowedGroups = listOf("Tuition Girl", "Playgroup")
                )
                return true
            }
            // Parent OTP Login
            trimmed.contains("300") || trimmed.contains("+92") || trimmed.contains("parent") || trimmed.length >= 7 -> {
                _currentUser.value = CurrentUser(
                    role = UserRole.PARENT,
                    name = "Parent Portal",
                    email = "",
                    phone = if (emailOrPhone.contains("+92")) emailOrPhone.trim() else "+92 300 1234567",
                    isFullAdmin = false,
                    allowedGroups = emptyList()
                )
                return true
            }
        }
        return false
    }

    fun loginWithGoogle(displayName: String? = null, email: String? = null): Boolean {
        val name = displayName?.takeIf { it.isNotBlank() } ?: "Teacher Anas Mustafa"
        val userEmail = email?.takeIf { it.isNotBlank() } ?: "anas@alhadid.com"
        _currentUser.value = CurrentUser(
            role = UserRole.TEACHER_ANAS,
            name = name,
            email = userEmail,
            phone = "+92 300 9876542",
            isFullAdmin = true,
            allowedGroups = listOf("All", "Hifz", "Nazra", "Tuition Boy", "Computer Course", "Tajweed", "Tuition Girl", "Playgroup")
        )
        return true
    }

    fun logout() {
        _currentUser.value = null
    }

    // Student CRUD
    fun addStudent(student: Student): Boolean {
        val list = _students.value.toMutableList()
        list.add(0, student)
        _students.value = list

        // Create default book progress for new student
        val subjects = listOf("Math", "English", "Urdu", "Science", "Islamiat", "Computer", "Nazra Qaida", "Tajweed")
        val newBookProgress = _bookProgress.value.toMutableList()
        val dateStr = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
        subjects.forEachIndexed { idx, sub ->
            newBookProgress.add(
                BookProgress(
                    id = "BP-${student.studentId}-$idx",
                    studentId = student.studentId,
                    subject = sub,
                    grade = "A",
                    percentage = 85,
                    remarks = "Newly enrolled. Performance being evaluated.",
                    updatedDate = dateStr,
                    updatedBy = student.assignedTeacher
                )
            )
        }
        _bookProgress.value = newBookProgress
        return true
    }

    fun updateStudent(student: Student) {
        val list = _students.value.map { if (it.studentId == student.studentId) student else it }
        _students.value = list
    }

    fun deleteStudent(studentId: String) {
        _students.value = _students.value.filter { it.studentId != studentId }
        _fees.value = _fees.value.filter { it.studentId != studentId }
        _attendance.value = _attendance.value.filter { it.studentId != studentId }
        _quranProgress.value = _quranProgress.value.filter { it.studentId != studentId }
        _bookProgress.value = _bookProgress.value.filter { it.studentId != studentId }
        _testMarks.value = _testMarks.value.filter { it.studentId != studentId }
    }

    // Teacher CRUD
    fun addTeacher(teacher: Teacher) {
        val list = _teachers.value.toMutableList()
        list.add(teacher)
        _teachers.value = list
    }

    fun deleteTeacher(teacherId: String) {
        _teachers.value = _teachers.value.filter { it.teacherId != teacherId }
    }

    // Fee CRUD
    fun addFee(record: FeeRecord) {
        val list = _fees.value.toMutableList()
        list.add(0, record)
        _fees.value = list

        // Also add notification for parent
        addNotification(
            AppNotification(
                id = "N-${System.currentTimeMillis()}",
                title = "Fee Payment Recorded",
                body = "Assalam-o-Alaikum, Apke bache ${record.studentName} ne ${record.month} ki fee Rs ${record.totalPaid.toInt()} jama kara di hai. - Al Hadid Academy",
                type = "Fee",
                timestamp = "Just now",
                targetStudentId = record.studentId
            )
        )
    }

    fun updateFee(record: FeeRecord) {
        val list = _fees.value.map { if (it.feeId == record.feeId) record else it }
        _fees.value = list
    }

    // Attendance
    fun markAttendance(records: List<AttendanceRecord>) {
        val current = _attendance.value.toMutableList()
        records.forEach { rec ->
            val index = current.indexOfFirst { it.date == rec.date && it.studentId == rec.studentId }
            if (index >= 0) {
                current[index] = rec
            } else {
                current.add(0, rec)
            }
            // Auto absent notification
            if (rec.status == "Absent") {
                addNotification(
                    AppNotification(
                        id = "N-ABS-${System.currentTimeMillis()}-${rec.studentId}",
                        title = "Absent Alert - Al Hadid Academy",
                        body = "Apka bacha ${rec.studentName} aj (${rec.date}) ko academy nahi aya. Please contact administration.",
                        type = "Absent Alert",
                        timestamp = "Today",
                        targetStudentId = rec.studentId
                    )
                )
            }
        }
        _attendance.value = current
    }

    // Quran Progress
    fun addQuranProgress(entry: QuranProgress) {
        val list = _quranProgress.value.toMutableList()
        list.add(0, entry)
        _quranProgress.value = list
    }

    // Book Progress
    fun updateBookProgress(entry: BookProgress) {
        val list = _bookProgress.value.toMutableList()
        val idx = list.indexOfFirst { it.studentId == entry.studentId && it.subject == entry.subject }
        if (idx >= 0) {
            list[idx] = entry
        } else {
            list.add(0, entry)
        }
        _bookProgress.value = list
    }

    // Tests & Marks
    fun addTest(test: TestRecord) {
        val list = _tests.value.toMutableList()
        list.add(0, test)
        _tests.value = list
    }

    fun saveTestMarks(marksList: List<TestMarks>) {
        val current = _testMarks.value.toMutableList()
        marksList.forEach { m ->
            val idx = current.indexOfFirst { it.testId == m.testId && it.studentId == m.studentId }
            if (idx >= 0) {
                current[idx] = m
            } else {
                current.add(0, m)
            }
        }
        _testMarks.value = current
    }

    // Announcements & Stationary
    fun postAnnouncement(announcement: Announcement) {
        val list = _announcements.value.toMutableList()
        list.add(0, announcement)
        _announcements.value = list

        addNotification(
            AppNotification(
                id = "N-${System.currentTimeMillis()}",
                title = "Al Hadid Academy - ${announcement.title}",
                body = announcement.message,
                type = announcement.type,
                timestamp = "Just now"
            )
        )
    }

    fun requestStationary(studentId: String, studentName: String, itemName: String) {
        val list = _students.value.map {
            if (it.studentId == studentId) it.copy(stationaryNeeded = itemName) else it
        }
        _students.value = list

        addNotification(
            AppNotification(
                id = "N-STAT-${System.currentTimeMillis()}",
                title = "Stationary Alert - Al Hadid Academy",
                body = "Apke bache $studentName ko kal $itemName chahiye - Al Hadid Academy",
                type = "Stationary Required",
                timestamp = "Just now",
                targetStudentId = studentId
            )
        )
    }

    private fun addNotification(notification: AppNotification) {
        val list = _notifications.value.toMutableList()
        list.add(0, notification)
        _notifications.value = list
    }

    fun markNotificationsRead() {
        val list = _notifications.value.map { it.copy(isRead = true) }
        _notifications.value = list
    }

    // Helper queries
    fun getStudentById(id: String): Student? = _students.value.find { it.studentId == id }

    fun getStudentsForParent(phone: String): List<Student> {
        val cleanPhone = phone.replace(" ", "").replace("-", "")
        val matched = _students.value.filter {
            it.parentPhone.replace(" ", "").replace("-", "") == cleanPhone
        }
        return if (matched.isNotEmpty()) matched else listOfNotNull(_students.value.firstOrNull())
    }
}
