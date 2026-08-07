package com.varsel.expensetracker.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.pdmodel.encryption.InvalidPasswordException
import com.tom_roush.pdfbox.text.PDFTextStripper
import java.io.InputStream

/**
 * Exception thrown specifically when a bank statement PDF requires a password to open.
 */
class PdfPasswordRequiredException(message: String = "PDF is password protected") : Exception(message)

/**
 * Exception thrown when the user-provided password fails to decrypt the PDF statement.
 */
class InvalidPdfPasswordException(message: String = "Incorrect password provided") : Exception(message)

/**
 * Offline PDF Utility responsible for:
 *  1. Detecting password-protected bank statements without forcing a prompt for unencrypted files.
 *  2. Decrypting PDFs using user-supplied passwords.
 *  3. Extracting embedded text directly or rendering high-DPI Bitmaps for ML Kit OCR.
 */
object PdfTextExtractor {

    private var isPdfBoxInitialized = false

    /**
     * Initializes PDFBox Android resource loader on demand.
     */
    fun init(context: Context) {
        if (!isPdfBoxInitialized) {
            PDFBoxResourceLoader.init(context)
            isPdfBoxInitialized = true
        }
    }

    /**
     * Attempts to extract raw text directly from a PDF Uri.
     *
     * Flow:
     * - Tries loading the PDF silently without a password.
     * - If successful & not encrypted, returns extracted text immediately.
     * - If encrypted and no password was passed, throws [PdfPasswordRequiredException].
     * - If an invalid password was passed, throws [InvalidPdfPasswordException].
     *
     * @param context Application context to open ContentResolver stream.
     * @param uri Uri pointing to the selected PDF document.
     * @param password Optional password entered by the user.
     * @return Extracted raw text string from all pages.
     */
    fun extractTextFromPdf(
        context: Context,
        uri: Uri,
        password: String? = null
    ): String {
        init(context)

        val inputStream: InputStream = context.contentResolver.openInputStream(uri)
            ?: throw IllegalArgumentException("Unable to open file stream for Uri: $uri")

        return try {
            // Attempt to load the document with or without password
            val document = if (password.isNullOrEmpty()) {
                PDDocument.load(inputStream)
            } else {
                PDDocument.load(inputStream, password)
            }

            // Verification check for documents that don't throw explicitly on load
            if (document.isEncrypted) {
                document.close()
                throw PdfPasswordRequiredException()
            }

            val stripper = PDFTextStripper()
            val text = stripper.getText(document)
            document.close()
            text

        } catch (e: InvalidPasswordException) {
            // Distinguish between missing initial password and wrong submitted password
            if (password.isNullOrEmpty()) {
                throw PdfPasswordRequiredException()
            } else {
                throw InvalidPdfPasswordException()
            }
        } catch (e: Exception) {
            // Safety catch for encrypted native security exceptions
            val msg = e.message?.lowercase() ?: ""
            if (msg.contains("encrypted") || msg.contains("password")) {
                if (password.isNullOrEmpty()) throw PdfPasswordRequiredException()
                else throw InvalidPdfPasswordException()
            } else {
                throw e
            }
        } finally {
            inputStream.close()
        }
    }

    /**
     * Fallback renderer: Converts PDF pages into high-DPI Bitmaps.
     * Used when a PDF contains scanned image pages rather than selectable digital text.
     *
     * @param context Application context.
     * @param uri Uri pointing to the selected PDF document.
     * @param dpi Target rendering quality (default 300 DPI for high OCR accuracy).
     * @return List of rendered page Bitmaps.
     */
    fun renderPdfToBitmaps(
        context: Context,
        uri: Uri,
        dpi: Int = 300
    ): List<Bitmap> {
        val bitmaps = mutableListOf<Bitmap>()
        val fileDescriptor = context.contentResolver.openFileDescriptor(uri, "r") ?: return emptyList()

        fileDescriptor.use { pfd ->
            val pdfRenderer = PdfRenderer(pfd)
            val pageCount = pdfRenderer.pageCount

            for (i in 0 until pageCount) {
                val page = pdfRenderer.openPage(i)
                // 72 DPI is base standard scale for Android PdfRenderer
                val scale = dpi / 72f
                val width = (page.width * scale).toInt()
                val height = (page.height * scale).toInt()

                val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                bitmaps.add(bitmap)
                page.close()
            }
            pdfRenderer.close()
        }
        return bitmaps
    }
}
