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
 * Jetpack UI State Machine representing statement import lifecycle.
 */
sealed interface ImportUiState {
    object Idle : ImportUiState
    object Processing : ImportUiState
    data class PasswordRequired(
        val uri: Uri,
        val isInvalidPasswordError: Boolean = false
    ) : ImportUiState
    data class Success(
        val parsedTransactions: List<ParsedTransaction>,
        val autoCategorizedCount: Int
    ) : ImportUiState
    object Saved : ImportUiState
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
     * Triggered when user selects a bank statement file (PDF or Image).
     */
    fun processSelectedFile(uri: Uri, password: String? = null) {
        currentSelectedUri = uri
        _uiState.value = ImportUiState.Processing

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val mimeType = context.contentResolver.getType(uri) ?: ""
                
                val rawText = if (mimeType.contains("pdf", ignoreCase = true) || uri.toString().endsWith(".pdf", ignoreCase = true)) {
                    extractPdfTextWithFallback(uri, password)
                } else {
                    // INLINE FIX: Replaced deprecated MediaStore.Images.Media.getBitmap with safe loadBitmapFromUri
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

                // INLINE FIX: Correctly reference StatementParserEngine.parseStatementText companion / object function
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
     * Submits password for encrypted PDF statement.
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
                    // INLINE FIX: Updated call to 'categorizeTransaction' with new signature parameters
                    val categoryName = categorizerEngine.categorizeTransaction(
                        rawDescription = candidate.description,
                        categories = emptyList(),
                        customRules = emptyList(),
                        historicalTransactions = emptyList()
                    )

                    // INLINE FIX: Map candidate fields to Transaction constructor matching (category, dateTimestamp, referenceNumber)
                    Transaction(
                        amount = candidate.amount,
                        type = candidate.type,
                        description = candidate.description,
                        category = categoryName ?: "Uncategorized",
                        dateTimestamp = candidate.timestamp,
                        referenceNumber = candidate.referenceNumber
                    )
                }

                repository.insertTransactions(transactionsToInsert)
                _uiState.value = ImportUiState.Saved
            } catch (e: Exception) {
                _uiState.value = ImportUiState.Error("Failed to save transactions: ${e.localizedMessage}")
            }
        }
    }

    fun resetState() {
        currentSelectedUri = null
        _uiState.value = ImportUiState.Idle
    }

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

    // INLINE FIX: Helper method handling bitmap decoding without using deprecated APIs on Android P+
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
