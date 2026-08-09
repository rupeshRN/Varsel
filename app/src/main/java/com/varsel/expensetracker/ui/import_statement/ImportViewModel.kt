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

    fun processSelectedFile(
        uri: Uri,
        mimeType: String? = null
    ) {
        viewModelScope.launch {

            _uiState.value = ImportUiState.Loading

            try {

                val resolvedMimeType =
                    mimeType ?: context.contentResolver.getType(uri)

                val rawText =
                    if (resolvedMimeType == "application/pdf" ||
                        uri.toString().endsWith(".pdf", true)
                    ) {
                        pdfTextExtractor.extractTextFromPdf(context, uri)
                    } else {
                        ocrManager.extractTextFromImage(context, uri)
                    }

                if (rawText.isNullOrBlank()) {
                    _uiState.value = ImportUiState.Error(
                        "Could not extract any text from the selected document."
                    )
                    return@launch
                }

                // ======================================================

               // _uiState.value = ImportUiState.Error(rawText)
                // return@launch
val result =
    statementParserEngine.parseStatement(rawText)

if (result.transactions.isEmpty()) {
    _uiState.value =
        ImportUiState.Error("No transactions found.")
    return@launch
}

_uiState.value =
    ImportUiState.ParsedTransactions(
        result.transactions
    )

            } catch (e: Exception) {

                _uiState.value = ImportUiState.Error(
    e.message ?: e.stackTraceToString())
            }
        }
    }

    fun confirmAndSaveTransactions(
        transactions: List<Transaction>
    ) {
        viewModelScope.launch {

            try {

                transactions.forEach {
                    transactionRepository.insertTransaction(it)
                }

                _uiState.value =
                    ImportUiState.Saved(transactions.size)

            } catch (e: Exception) {

                _uiState.value = ImportUiState.Error(
                    e.localizedMessage ?: "Failed to save transactions."
                )
            }
        }
    }

    fun resetState() {
        _uiState.value = ImportUiState.Idle
    }
}
