package com.varsel.expensetracker.util

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class PdfTextExtractor @Inject constructor(
    @ApplicationContext private val context: Context
) {
    fun extractText(fileBytes: ByteArray): String {
        // PDF text extraction implementation
        return ""
    }

}
