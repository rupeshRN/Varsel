package com.varsel.expensetracker

import android.app.Application
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader
import dagger.hilt.android.HiltAndroidApp

/**
 * Root Application class for ExpenseTracker.
 * @HiltAndroidApp triggers Hilt's code generation for dependency injection.
 */
@HiltAndroidApp
class ExpenseTrackerApp : Application() {

    override fun onCreate() {
        super.onCreate()
        
        // Initialize PdfBox for offline PDF statement text extraction
        PDFBoxResourceLoader.init(applicationContext)
    
    }
}
