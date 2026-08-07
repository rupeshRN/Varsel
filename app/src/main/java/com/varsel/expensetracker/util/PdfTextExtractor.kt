package com.varsel.expensetracker.util

import android.content.Context
import android.net.Uri
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.text.PDFTextStripper
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject

class PdfTextExtractor @Inject constructor(
    @ApplicationContext private val context: Context
) {
    suspend fun extractText(uri: Uri): String {
        var tempFile: File? = null
        return try {
            // Create a temporary file in the cache directory to hold the PDF stream
            tempFile = File.createTempFile("statement_temp", ".pdf", context.cacheDir)
            
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                FileOutputStream(tempFile).use { fos ->
                    inputStream.copyTo(fos)
                }
            } ?: return ""

            // Load the PDF document using PDFBox
            val document = PDDocument.load(tempFile)
            
            // Extract the text layer directly without OCR
            val stripper = PDFTextStripper()
            val text = stripper.getText(document)
            
            // Close document to release resources
            document.close()

            text ?: ""
        } catch (e: Exception) {
            e.printStackTrace()
            ""
        } finally {
            // Always clean up the temporary file
            tempFile?.delete()
        }
    }
}
