package com.varsel.expensetracker

import android.app.Application
import android.content.Intent
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class ExpenseTrackerApp : Application() {
    override fun onCreate() {
        super.onCreate()
        
        // Intercepts any fatal crash and displays it visually instead of crashing to black screen
        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            try {
                val intent = Intent(applicationContext, CrashActivity::class.java).apply {
                    putExtra("STACK_TRACE", throwable.stackTraceToString())
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                }
                applicationContext.startActivity(intent)
                System.exit(2)
            } catch (e: Exception) {
                defaultHandler?.uncaughtException(thread, throwable)
            }
        }
    }
}
