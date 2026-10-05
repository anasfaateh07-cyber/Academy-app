package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.example.data.AcademyRepository
import com.example.data.CurrentUser
import com.example.data.UserRole
import com.example.service.NotificationService
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme

enum class AppScreen {
    SPLASH,
    AUTH,
    WELCOME,
    ADMIN_DASHBOARD,
    PARENT_DASHBOARD
}

class MainActivity : ComponentActivity() {

    private val repository = AcademyRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        NotificationService.initNotificationChannel(this)

        setContent {
            MyApplicationTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    var currentScreen by remember { mutableStateOf(AppScreen.SPLASH) }
                    val currentUser by repository.currentUser.collectAsState()

                    Crossfade(targetState = currentScreen, label = "screen_transition") { screen ->
                        when (screen) {
                            AppScreen.SPLASH -> {
                                SplashScreen(
                                    onFinished = {
                                        // Task 2: Mandatory Login/SignUp - no bypass directly to Home
                                        currentScreen = AppScreen.AUTH
                                    }
                                )
                            }
                            AppScreen.AUTH -> {
                                AuthScreen(
                                    repository = repository,
                                    onAuthSuccess = {
                                        // Task 4: After login, before Home dashboard, show WelcomeScreen
                                        currentScreen = AppScreen.WELCOME
                                    }
                                )
                            }
                            AppScreen.WELCOME -> {
                                WelcomeScreen(
                                    onContinue = {
                                        val user = repository.currentUser.value
                                        currentScreen = if (user?.role == UserRole.PARENT) {
                                            AppScreen.PARENT_DASHBOARD
                                        } else {
                                            AppScreen.ADMIN_DASHBOARD
                                        }
                                    }
                                )
                            }
                            AppScreen.ADMIN_DASHBOARD -> {
                                val user = currentUser ?: CurrentUser(
                                    role = UserRole.PRINCIPAL_AWAIS,
                                    name = "Principal Awais Mustafa",
                                    email = "awais@alhadid.com",
                                    phone = "+92 300 9876541",
                                    isFullAdmin = true,
                                    allowedGroups = listOf("All")
                                )
                                AdminDashboardScreen(
                                    repository = repository,
                                    currentUser = user,
                                    onLogout = {
                                        repository.logout()
                                        currentScreen = AppScreen.AUTH
                                    }
                                )
                            }
                            AppScreen.PARENT_DASHBOARD -> {
                                val user = currentUser ?: CurrentUser(
                                    role = UserRole.PARENT,
                                    name = "Parent Portal",
                                    email = "",
                                    phone = "+92 300 1234567",
                                    isFullAdmin = false,
                                    allowedGroups = emptyList()
                                )
                                ParentDashboardScreen(
                                    repository = repository,
                                    currentUser = user,
                                    onLogout = {
                                        repository.logout()
                                        currentScreen = AppScreen.AUTH
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
