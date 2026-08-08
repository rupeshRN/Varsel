package com.varsel.expensetracker.util

import android.content.Context
import android.net.Uri
import android.os.ParcelFileDescriptor
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfReader
import com.itextpdf.kernel.pdf.canvas.parser.PdfTextExtractor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.InputStream
import javax.inject.Inject

/**
 * Utility responsible for extracting text content from digital PDF bank statements
 * entirely on-device and offline.
 */
class PdfTextExtractor @Inject constructor() {

    /**
     * Extracts raw string text from a given PDF document Uri.
     * 
     * @param context Application or activity context used to open the content resolver stream.
     * @param uri The content Uri pointing to the selected PDF statement.
     * @return Extracted raw text string, or null if extraction fails.
     */
    suspend fun extractTextFromPdf(context: Context, uri: Uri): String? {
        return withContext(Dispatchers.IO) {
            var inputStream: InputStream? = null
            try {
                inputStream = context.contentResolver.openInputStream(uri) ?: return@withContext null
                val pdfReader = PdfReader(inputStream)
                val pdfDoc = PdfDocument(pdfReader)
                val parsedText = StringBuilder()

                for (i in 1..pdfDoc.numberOfPages) {
                    val page = pdfDoc.getPage(i)
                    val text = PdfTextExtractor.getTextFromPage(page)
                    parsedText.append(text).append("\n")
                }

                pdfDoc.close()
                val result = parsedText.toString()
                if (result.isBlank()) null else result
            } catch (e: Exception) {
                null
            } finally {
                try {
                    inputStream?.close()
                } catch (ignored: Exception) {
                }
            }
        }
  }
}
