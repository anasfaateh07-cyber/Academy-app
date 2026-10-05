package com.example

import android.app.Application
import android.util.Log
import com.google.firebase.FirebaseApp

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        try {
            if (FirebaseApp.getApps(this).isEmpty()) {
                FirebaseApp.initializeApp(this)
                Log.d("MyApplication", "FirebaseApp initialized successfully")
            }
        } catch (e: Exception) {
            Log.w("MyApplication", "FirebaseApp init check: ${e.message}")
        }
    }
}
