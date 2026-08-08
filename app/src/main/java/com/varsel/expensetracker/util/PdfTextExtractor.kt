package com.varsel.expensetracker.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.ParcelFileDescriptor
import com.google.android.gms.tasks.Tasks
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class PdfTextExtractor @Inject constructor() {

    private val textRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    suspend fun extractTextFromPdf(context: Context, uri: Uri): String? {
        return withContext(Dispatchers.IO) {
            var fileDescriptor: ParcelFileDescriptor? = null
            var pdfRenderer: PdfRenderer? = null
            try {
                fileDescriptor = context.contentResolver.openFileDescriptor(uri, "r") ?: return@withContext null
                pdfRenderer = PdfRenderer(fileDescriptor)
                val fullTextBuilder = StringBuilder()

                for (i in 0 until pdfRenderer.pageCount) {
                    val page = pdfRenderer.openPage(i)
                    val width = page.width * 2
                    val height = page.height * 2
                    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                    page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_PRINT)
                    page.close()

                    val image = InputImage.fromBitmap(bitmap, 0)
                    val task = textRecognizer.process(image)
                    val result = Tasks.await(task)
                    if (result.text.isNotBlank()) {
                        fullTextBuilder.append(result.text).append("\n")
                    }
                }

                val finalResult = fullTextBuilder.toString()
                if (finalResult.isBlank()) null else finalResult
            } catch (e: Exception) {
                null
            } finally {
                try {
                    pdfRenderer?.close()
                    fileDescriptor?.close()
                } catch (ignored: Exception) {
                }
            }
        }
    }
}
