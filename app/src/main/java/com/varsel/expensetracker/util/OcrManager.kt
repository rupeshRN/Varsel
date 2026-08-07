package com.varsel.expensetracker.util

import android.graphics.Bitmap
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * On-Device Optical Character Recognition (OCR) Engine powered by Google ML Kit.
 * 
 * Extracts text from images and rendered PDF bitmaps completely locally, 
 * guaranteeing zero data transmission outside the user's device for complete privacy.
 */
object OcrManager {

    /** Lazy initialization of Google ML Kit Latin Text Recognizer */
    private val recognizer by lazy {
        TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    }

    /**
     * Asynchronously processes a single [Bitmap] image and extracts recognized text.
     * 
     * Converts ML Kit's asynchronous Task API into an idiomatic Kotlin suspend function 
     * using [suspendCancellableCoroutine].
     *
     * @param bitmap Image or rendered PDF page to analyze.
     * @return Extracted raw text string organized by blocks, lines, and elements.
     */
    suspend fun extractTextFromBitmap(bitmap: Bitmap): String = suspendCancellableCoroutine { continuation ->
        val inputImage = InputImage.fromBitmap(bitmap, 0)

        recognizer.process(inputImage)
            .addOnSuccessListener { visionText ->
                if (continuation.isActive) {
                    continuation.resume(visionText.text)
                }
            }
            .addOnFailureListener { exception ->
                if (continuation.isActive) {
                    continuation.resumeWithException(exception)
                }
            }
    }

    /**
     * Batch processes multiple rendered PDF page [Bitmap]s sequentially and concatenates results.
     *
     * @param bitmaps List of rendered page images.
     * @return Aggregated text string containing extracted text from all pages.
     */
    suspend fun extractTextFromBitmaps(bitmaps: List<Bitmap>): String {
        val stringBuilder = StringBuilder()
        for ((index, bitmap) in bitmaps.withIndex()) {
            val pageText = extractTextFromBitmap(bitmap)
            stringBuilder.append("\n--- Page ${index + 1} ---\n")
            stringBuilder.append(pageText)
        }
        return stringBuilder.toString()
  
    }
}
