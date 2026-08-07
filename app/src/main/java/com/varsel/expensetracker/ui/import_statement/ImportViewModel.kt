package com.varsel.expensetracker.ui.import_statement

import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.varsel.expensetracker.domain.model.Transaction
import com.varsel.expensetracker.domain.repository.TransactionRepository
import com.varsel.expensetracker.util.InvalidPdfPasswordException
import com.varsel.expensetracker.util.OcrManager
import com.varsel.expensetracker.util.ParsedTransaction
import com.varsel.expensetracker.util.PdfPasswordRequiredException
import com.varsel.expensetracker.util.PdfTextExtractor
import com.varsel.expensetracker.util.SmartCategorizerEngine
import com.varsel.expensetracker.util.StatementParserEngine
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Modern Jetpack UI State Machine representing statement import lifecycle.
 */
sealed interface ImportUiState {
    /** Initial idle state before user selects a bank statement */
    object Idle : ImportUiState

    /** Active background parsing / OCR extraction state */
    object Processing : ImportUiState

    /** Triggered when the selected PDF statement is password protected */
    data class PasswordRequired(
        val uri: Uri,
        val isInvalidPasswordError: Boolean = false
    ) : ImportUiState

    /** Statement successfully parsed; holds candidate transactions for user preview */
    data class Success(
        val parsedTransactions: List<ParsedTransaction>,
        val autoCategorizedCount: Int
    ) : ImportUiState

    /** Saved successfully to Room database */
    object Saved : ImportUiState

    /** Error state holding human-readable message */
    data class Error(val message: String) : ImportUiState
}

@HiltViewModel
class ImportViewModel @Inject constructor(
    private val repository: TransactionRepository,
    private val categorizerEngine: SmartCategorizerEngine,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow<ImportUiState>(ImportUiState.Idle)
    val uiState: StateFlow<ImportUiState> = _uiState.asStateFlow()

    private var currentSelectedUri: Uri? = null

    /**
     * Triggered when user picks a bank statement file (PDF or Image) from system document picker.
     */
    fun processSelectedFile(uri: Uri, password: String? = null) {
        currentSelectedUri = uri
        _uiState.value = ImportUiState.Processing

        viewModelScope.launch(Dispatchers.IO) {
            try {
                // Determine file type via ContentResolver
                val mimeType = context.contentResolver.getType(uri) ?: ""
                
                val rawText = if (mimeType.contains("pdf", ignoreCase = true) || uri.toString().endsWith(".pdf", ignoreCase = true)) {
                    // Attempt native PDF embedded text extraction
                    extractPdfTextWithFallback(uri, password)
                } else {
                    // HIGHLIGHT: Updated deprecated MediaStore.Images.Media.getBitmap to safe ImageDecoder/InputStream handling
                    val bitmap = loadBitmapFromUri(uri)
                    if (bitmap != null) {
                        OcrManager.extractTextFromBitmap(bitmap)
                    } else {
                        ""
                    }
                }

                if (rawText.isBlank()) {
                    _uiState.value = ImportUiState.Error("Could not extract readable text from statement.")
                    return@launch
                }

                // Parse unstructured text into candidate transaction lines
                val parsedCandidates = StatementParserEngine.parseStatementText(rawText)

                if (parsedCandidates.isEmpty()) {
                    _uiState.value = ImportUiState.Error("No valid transactions detected in this statement format.")
                    return@launch
                }

                _uiState.value = ImportUiState.Success(
                    parsedTransactions = parsedCandidates,
                    autoCategorizedCount = parsedCandidates.count { it.description.isNotBlank() }
                )

            } catch (e: PdfPasswordRequiredException) {
                _uiState.value = ImportUiState.PasswordRequired(uri = uri, isInvalidPasswordError = false)
            } catch (e: InvalidPdfPasswordException) {
                _uiState.value = ImportUiState.PasswordRequired(uri = uri, isInvalidPasswordError = true)
            } catch (e: Exception) {
                _uiState.value = ImportUiState.Error("Failed to parse statement: ${e.localizedMessage ?: "Unknown error"}")
            }
        }
    }

    /**
     * Submits user-entered password for an encrypted PDF statement.
     */
    fun submitPdfPassword(password: String) {
        val uri = currentSelectedUri ?: return
        processSelectedFile(uri, password)
    }

    /**
     * Confirms and batch-saves selected candidate transactions into the database.
     */
    fun confirmAndSaveTransactions(candidates: List<ParsedTransaction>) {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.value = ImportUiState.Processing
            try {
                val transactionsToInsert = candidates.map { candidate ->
                    // Run Smart Categorizer Engine to assign Category based on narration
                    val category = categorizerEngine.categorizeTransaction(
                        narration = candidate.description,
                        amount = candidate.amount
                    )

                    // HIGHLIGHT: Updated parameter mapping ('category', 'date', and 'note') to match domain Transaction schema
                    Transaction(
                        amount = candidate.amount,
                        type = candidate.type,
                        description = candidate.description,
                        category = category,
                        date = candidate.timestamp,
                        note = candidate.referenceNumber,
                        isAutoParsed = true
                    )
                }

                repository.insertTransactions(transactionsToInsert)
                _uiState.value = ImportUiState.Saved
            } catch (e: Exception) {
                _uiState.value = ImportUiState.Error("Failed to save transactions: ${e.localizedMessage}")
            }
        }
    }

    /**
     * Resets UI state machine back to Idle.
     */
    fun resetState() {
        currentSelectedUri = null
        _uiState.value = ImportUiState.Idle
    }

    /**
     * Helper to extract text from PDF; falls back to OCR bitmap rendering if embedded text is empty.
     */
    private suspend fun extractPdfTextWithFallback(uri: Uri, password: String?): String {
        return try {
            val text = PdfTextExtractor.extractTextFromPdf(context, uri, password)
            if (text.isNotBlank()) {
                text
            } else {
                val bitmaps = PdfTextExtractor.renderPdfToBitmaps(context, uri)
                OcrManager.extractTextFromBitmaps(bitmaps)
            }
        } catch (e: Exception) {
            throw e
        }
    }

    // HIGHLIGHT: Added non-deprecated helper for decoding image URIs across Android API versions
    private fun loadBitmapFromUri(uri: Uri): Bitmap? {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val source = ImageDecoder.createSource(context.contentResolver, uri)
                ImageDecoder.decodeBitmap(source)
            } else {
                @Suppress("DEPRECATION")
                MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
            }
        } catch (e: Exception) {
            null
        }
    }
}
