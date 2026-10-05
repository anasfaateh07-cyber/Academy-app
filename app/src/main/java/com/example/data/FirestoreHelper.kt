package com.example.data

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object FirestoreHelper {
    private const val TAG = "FirestoreHelper"

    private val firestoreInstance: FirebaseFirestore? by lazy {
        try {
            FirebaseFirestore.getInstance()
        } catch (e: Exception) {
            Log.w(TAG, "FirebaseFirestore instance not ready or unprovisioned: ${e.message}")
            null
        }
    }

    /**
     * If user selects Role=Teacher (or any role), save role in Firestore: users/{uid} {role: "teacher"}
     */
    fun saveUserRole(uid: String, role: String, email: String = "", name: String = "") {
        if (uid.isBlank()) return
        val roleKey = role.lowercase().trim()
        val data = hashMapOf(
            "role" to roleKey,
            "email" to email,
            "name" to name,
            "updatedAt" to System.currentTimeMillis()
        )
        try {
            firestoreInstance?.collection("users")
                ?.document(uid)
                ?.set(data, SetOptions.merge())
                ?.addOnSuccessListener {
                    Log.d(TAG, "Saved user role to users/$uid: $roleKey")
                }
                ?.addOnFailureListener { e ->
                    Log.w(TAG, "Failed saving user role: ${e.message}")
                }
        } catch (e: Exception) {
            Log.w(TAG, "saveUserRole exception: ${e.message}")
        }
    }

    /**
     * Feature 1: Delete from Firestore students/{id}
     */
    fun deleteStudentFromFirestore(studentId: String) {
        try {
            firestoreInstance?.collection("students")
                ?.document(studentId)
                ?.delete()
                ?.addOnSuccessListener {
                    Log.d(TAG, "Deleted student from students/$studentId")
                }
                ?.addOnFailureListener { e ->
                    Log.w(TAG, "Failed deleting student: ${e.message}")
                }
        } catch (e: Exception) {
            Log.w(TAG, "deleteStudentFromFirestore exception: ${e.message}")
        }
    }

    /**
     * Feature 1: Save or restore student in Firestore students/{id}
     */
    fun saveStudentToFirestore(student: Student) {
        val data = hashMapOf(
            "studentId" to student.studentId,
            "name" to student.name,
            "fatherName" to student.fatherName,
            "className" to student.className,
            "groupType" to student.groupType,
            "parentPhone" to student.parentPhone,
            "monthlyFee" to student.monthlyFee,
            "assignedTeacher" to student.assignedTeacher,
            "timestamp" to System.currentTimeMillis()
        )
        try {
            firestoreInstance?.collection("students")
                ?.document(student.studentId)
                ?.set(data, SetOptions.merge())
        } catch (e: Exception) {
            Log.w(TAG, "saveStudentToFirestore exception: ${e.message}")
        }
    }

    /**
     * Feature 3: Sabaq Recorded Karna (Quran Progress)
     * Firestore structure:
     * quran_progress/{studentId}/{date} = {
     *   sabaq: "Para 2 Ruku 5",
     *   sabaqStatus: "Yaad",
     *   manzil: "Para 1",
     *   audioUrl: "",
     *   teacherId,
     *   timestamp
     * }
     */
    fun saveQuranProgress(
        studentId: String,
        date: String,
        sabaq: String,
        sabaqStatus: String,
        manzil: String,
        audioUrl: String,
        teacherId: String
    ) {
        val cleanDate = date.ifBlank { "2026-10-05" }
        val data = hashMapOf(
            "sabaq" to sabaq,
            "sabaqStatus" to sabaqStatus,
            "manzil" to manzil,
            "audioUrl" to audioUrl,
            "teacherId" to teacherId,
            "studentId" to studentId,
            "date" to cleanDate,
            "timestamp" to System.currentTimeMillis()
        )
        try {
            firestoreInstance
                ?.collection("quran_progress")
                ?.document(studentId)
                ?.collection("history")
                ?.document(cleanDate)
                ?.set(data, SetOptions.merge())
                ?.addOnSuccessListener {
                    Log.d(TAG, "Saved Quran progress to quran_progress/$studentId/$cleanDate")
                }

            // Also keep top-level document for direct path queries
            firestoreInstance
                ?.collection("quran_progress")
                ?.document("${studentId}_$cleanDate")
                ?.set(data, SetOptions.merge())
        } catch (e: Exception) {
            Log.w(TAG, "saveQuranProgress exception: ${e.message}")
        }
    }

    /**
     * Feature 2: Save Fee Record with discount and concession
     */
    fun saveFeeRecord(record: FeeRecord) {
        val data = hashMapOf(
            "feeId" to record.feeId,
            "receiptNo" to record.receiptNo,
            "studentId" to record.studentId,
            "studentName" to record.studentName,
            "month" to record.month,
            "amount" to record.amount,
            "discount" to record.discount,
            "concessionType" to record.concessionType,
            "discountReason" to record.discountReason,
            "totalPaid" to record.totalPaid,
            "due" to record.due,
            "status" to record.status,
            "paymentDate" to record.paymentDate,
            "timestamp" to System.currentTimeMillis()
        )
        try {
            firestoreInstance?.collection("fees")
                ?.document(record.feeId)
                ?.set(data, SetOptions.merge())
        } catch (e: Exception) {
            Log.w(TAG, "saveFeeRecord exception: ${e.message}")
        }
    }

    /**
     * Store in Firestore: attendance/{date}/{classId}/{studentId} = {status, time, markedBy: teacherId}
     */
    fun saveAttendance(
        date: String,
        classId: String,
        studentId: String,
        status: String,
        markedBy: String
    ) {
        val cleanClassId = classId.ifBlank { "General" }.replace("/", "_")
        val cleanDate = date.ifBlank { "2026-10-05" }
        val timeNow = SimpleDateFormat("hh:mm a", Locale.US).format(Date())

        val recordData = hashMapOf(
            "status" to status,
            "time" to timeNow,
            "markedBy" to markedBy,
            "studentId" to studentId,
            "classId" to cleanClassId,
            "date" to cleanDate,
            "timestamp" to System.currentTimeMillis()
        )

        try {
            firestoreInstance
                ?.collection("attendance")
                ?.document(cleanDate)
                ?.collection(cleanClassId)
                ?.document(studentId)
                ?.set(recordData, SetOptions.merge())
                ?.addOnSuccessListener {
                    Log.d(TAG, "Saved attendance to attendance/$cleanDate/$cleanClassId/$studentId")
                }
                ?.addOnFailureListener { e ->
                    Log.w(TAG, "Failed saving attendance: ${e.message}")
                }
        } catch (e: Exception) {
            Log.w(TAG, "saveAttendance exception: ${e.message}")
        }
    }

    /**
     * Auto-save debounced field to Firestore
     */
    fun saveFieldDebounced(
        collection: String,
        documentId: String,
        fieldName: String,
        value: Any
    ) {
        try {
            firestoreInstance?.collection(collection)
                ?.document(documentId)
                ?.set(mapOf(fieldName to value, "lastSaved" to System.currentTimeMillis()), SetOptions.merge())
        } catch (e: Exception) {
            Log.w(TAG, "saveFieldDebounced notice: ${e.message}")
        }
    }
}
