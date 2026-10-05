package com.example

import com.example.data.AcademyRepository
import com.example.data.UserRole
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class ExampleUnitTest {

    private lateinit var repository: AcademyRepository

    @Before
    fun setUp() {
        repository = AcademyRepository()
    }

    @Test
    fun testSeededStudentsCategories() {
        val students = repository.students.value
        assertEquals("Should have 14 seeded students", 14, students.size)

        val hifzCount = students.count { it.groupType == "Hifz" }
        val nazraCount = students.count { it.groupType == "Nazra" }
        val tajweedCount = students.count { it.groupType == "Tajweed" }
        val tuitionBoysCount = students.count { it.groupType == "Tuition Boy" }
        val tuitionGirlsCount = students.count { it.groupType == "Tuition Girl" }
        val playgroupCount = students.count { it.groupType == "Playgroup" }

        assertEquals("Should have 3 Hifz students", 3, hifzCount)
        assertEquals("Should have 2 Nazra students", 2, nazraCount)
        assertEquals("Should have 2 Tajweed students", 2, tajweedCount)
        assertEquals("Should have 3 Boys Tuition students", 3, tuitionBoysCount)
        assertEquals("Should have 2 Girls Tuition students", 2, tuitionGirlsCount)
        assertEquals("Should have 2 Playgroup students", 2, playgroupCount)
    }

    @Test
    fun testDualAdminLoginAwaisAndAnas() {
        // Principal Awais Mustafa - Full Admin
        assertTrue(repository.login("awais@alhadid.com", "Alhadid@123"))
        var user = repository.currentUser.value
        assertNotNull(user)
        assertEquals(UserRole.PRINCIPAL_AWAIS, user?.role)
        assertTrue(user?.isFullAdmin == true)

        // Teacher Anas Mustafa - Full Admin
        assertTrue(repository.login("anas@alhadid.com", "Alhadid@123"))
        user = repository.currentUser.value
        assertNotNull(user)
        assertEquals(UserRole.TEACHER_ANAS, user?.role)
        assertTrue(user?.isFullAdmin == true)
    }

    @Test
    fun testTeacherIsraLimitedPermissions() {
        assertTrue(repository.login("isra@alhadid.com", "Isra@123"))
        val user = repository.currentUser.value
        assertNotNull(user)
        assertEquals(UserRole.TEACHER_ISRA, user?.role)
        assertFalse(user?.isFullAdmin == true)
        assertTrue(user?.allowedGroups?.contains("Tuition Girl") == true)
        assertTrue(user?.allowedGroups?.contains("Playgroup") == true)
        assertFalse(user?.allowedGroups?.contains("Tajweed") == true)
    }

    @Test
    fun testParentLoginAndDataPrivacy() {
        val parentPhone = "+92 300 1234567"
        assertTrue(repository.login(parentPhone, "123456"))
        val user = repository.currentUser.value
        assertNotNull(user)
        assertEquals(UserRole.PARENT, user?.role)

        val myChildren = repository.getStudentsForParent(parentPhone)
        assertFalse("Parent should see their own child", myChildren.isEmpty())
        assertEquals("Muhammad Bilal", myChildren.first().name)
    }

    @Test
    fun testBookWiseProgressSeeded() {
        val bookRecords = repository.bookProgress.value
        assertFalse(bookRecords.isEmpty())
        val subjects = listOf("Math", "English", "Urdu", "Science", "Islamiat", "Computer", "Nazra Qaida", "Tajweed")
        val bilalBooks = bookRecords.filter { it.studentId == "AHAD-001" }
        assertEquals(8, bilalBooks.size)
        subjects.forEach { sub ->
            assertTrue(bilalBooks.any { it.subject == sub })
        }
    }
}
