package com.varsel.expensetracker.ui.import_statement

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.varsel.expensetracker.domain.model.Transaction
import com.varsel.expensetracker.domain.repository.TransactionRepository
import com.varsel.expensetracker.util.OcrManager
import com.varsel.expensetracker.util.PdfTextExtractor
import com.varsel.expensetracker.util.StatementParserEngine
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface ImportUiState {
    object Idle : ImportUiState
    object Loading : ImportUiState
    object Processing : ImportUiState
    data class ParsedTransactions(
        val parsedTransactions: List<Transaction>
    ) : ImportUiState
    data class PasswordRequired(
        val isInvalidPasswordError: Boolean = false,
        val pendingUri: Uri? = null,
        val pendingMimeType: String? = null
    ) : ImportUiState
    data class Saved(val count: Int) : ImportUiState
    data class Error(val message: String) : ImportUiState
}

@HiltViewModel
class ImportViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val statementParserEngine: StatementParserEngine,
    private val pdfTextExtractor: PdfTextExtractor,
    private val ocrManager: OcrManager,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow<ImportUiState>(ImportUiState.Idle)
    val uiState: StateFlow<ImportUiState> = _uiState.asStateFlow()

    fun processSelectedFile(uri: Uri, mimeType: String? = null) {
        viewModelScope.launch {
            _uiState.value = ImportUiState.Loading
            try {
                // Resolve mimeType automatically if not passed by the UI layer
                const val DEFAULT_PDF_MIME = "application/pdf"
                val resolvedMimeType = mimeType ?: context.contentResolver.getType(uri)

                val rawText = if (resolvedMimeType == DEFAULT_PDF_MIME || uri.toString().endsWith(".pdf", ignoreCase = true)) {
                    pdfTextExtractor.extractTextFromPdf(context, uri)
                } else {
                    ocrManager.extractTextFromImage(context, uri)
                }

                if (rawText.isNullOrBlank()) {
                    _uiState.value = ImportUiState.Error("Could not extract text from the selected document.")
                    return@launch
                }

                val transactions = statementParserEngine.parseStatement(rawText)

                if (transactions.isEmpty()) {
                    _uiState.value = ImportUiState.Error("No valid transactions found using template parser.")
                    return@launch
                }

                _uiState.value = ImportUiState.ParsedTransactions(parsedTransactions = transactions)
            } catch (e: Exception) {
                _uiState.value = ImportUiState.Error(e.localizedMessage ?: "An unexpected error occurred during processing.")
            }
        }
    }

    fun confirmAndSaveTransactions(transactions: List<Transaction>) {
        viewModelScope.launch {
            try {
                for (transaction in transactions) {
                    transactionRepository.insertTransaction(transaction)
                }
                _uiState.value = ImportUiState.Saved(count = transactions.size)
            } catch (e: Exception) {
                _uiState.value = ImportUiState.Error(e.localizedMessage ?: "Failed to save transactions.")
            }
        }
    }

    fun resetState() {
        _uiState.value = ImportUiState.Idle
    }
}
