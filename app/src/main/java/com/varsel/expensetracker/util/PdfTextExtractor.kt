package com.varsel.expensetracker.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.ParcelFileDescriptor
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject

class PdfTextExtractor @Inject constructor(
    @ApplicationContext private val context: Context,
    private val ocrManager: OcrManager
) {
    suspend fun extractText(uri: Uri): String {
        val stringBuilder = StringBuilder()
        var tempFile: File? = null
        var fileDescriptor: ParcelFileDescriptor? = null
        var pdfRenderer: PdfRenderer? = null

        try {
            tempFile = File.createTempFile("statement_temp", ".pdf", context.cacheDir)
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                FileOutputStream(tempFile).use { fos ->
                    inputStream.copyTo(fos)
                }
            } ?: return ""

            fileDescriptor = ParcelFileDescriptor.open(tempFile, ParcelFileDescriptor.MODE_READ_ONLY)
            pdfRenderer = PdfRenderer(fileDescriptor)
            val pageCount = pdfRenderer.pageCount

            for (i in 0 until pageCount) {
                val page = pdfRenderer.openPage(i)
                val bitmap = Bitmap.createBitmap(
                    page.width * 2,
                    page.height * 2,
                    Bitmap.Config.ARGB_8888
                )
                page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_PRINT)
                
                val pageText = ocrManager.extractTextFromBitmap(bitmap)
                stringBuilder.append(pageText).append("\n")

                page.close()
                bitmap.recycle()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            pdfRenderer?.close()
            fileDescriptor?.close()
            tempFile?.delete()
        }

        return stringBuilder.toString()

    }
}
