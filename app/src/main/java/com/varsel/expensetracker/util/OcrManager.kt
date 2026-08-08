package com.varsel.expensetracker.util

import android.content.Context
import android.net.Uri
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

/**
 * Manager responsible for performing on-device, offline text recognition (OCR)
 * on image-based bank statements and receipts using Google ML Kit.
 */
class OcrManager @Inject constructor() {

    // Initialize the ML Kit text recognizer with default offline Latin options
    private val textRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    /**
     * Extracts raw text from an image Uri entirely offline using local ML Kit models.
     * 
     * @param context Application or activity context required to resolve content URIs.
     * @param uri The image file location (e.g., gallery photo or captured document snapshot).
     * @return Extracted raw string text, or null if recognition fails or yields blank text.
     */
    suspend fun extractTextFromImage(context: Context, uri: Uri): String? {
        return try {
            val image = InputImage.fromFilePath(context, uri)
            val result = textRecognizer.process(image).await()
            
            val recognizedText = result.text
            if (recognizedText.isBlank()) {
                null
            } else {
                recognizedText
            }
        } catch (e: Exception) {
            // Return null safely on failure so the ImportViewModel can handle the error state
            null
        }
   }
}
