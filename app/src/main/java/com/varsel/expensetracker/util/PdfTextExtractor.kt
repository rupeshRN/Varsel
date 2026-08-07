package com.varsel.expensetracker.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject

class PdfTextExtractor @Inject constructor(
    @ApplicationContext private val context: Context,
    private val ocrManager: OcrManager
) {
    fun extractText(fileBytes: ByteArray): String {
        val stringBuilder = StringBuilder()
        var tempFile: File? = null
        var fileDescriptor: ParcelFileDescriptor? = null
        var pdfRenderer: PdfRenderer? = null

        try {
            // 1. Create a temporary file from the incoming PDF byte array
            tempFile = File.createTempFile("statement_temp", ".pdf", context.cacheDir)
            FileOutputStream(tempFile).use { fos ->
                fos.write(fileBytes)
            }

            // 2. Open the temporary file as a ParcelFileDescriptor
            fileDescriptor = ParcelFileDescriptor.open(tempFile, ParcelFileDescriptor.MODE_READ_ONLY)

            // 3. Initialize the PdfRenderer
            pdfRenderer = PdfRenderer(fileDescriptor)
            val pageCount = pdfRenderer.pageCount

            // 4. Render each page to a Bitmap and extract text via OcrManager
            for (i in 0 until pageCount) {
                val page = pdfRenderer.openPage(i)
                
                // Create a bitmap with appropriate scale for OCR readability
                val bitmap = Bitmap.createBitmap(
                    page.width * 2,
                    page.height * 2,
                    Bitmap.Config.ARGB_8888
                )
                page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_PRINT)
                
                // Extract text from the rendered page bitmap
                val pageText = ocrManager.extractTextFromBitmap(bitmap)
                stringBuilder.append(pageText).append("\n")

                page.close()
                bitmap.recycle()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            // Clean up resources safely
            pdfRenderer?.close()
            fileDescriptor?.close()
            tempFile?.delete()
        }

        return stringBuilder.toString()

    }
}
