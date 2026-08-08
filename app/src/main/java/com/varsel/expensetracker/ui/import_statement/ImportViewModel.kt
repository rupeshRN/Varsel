package com.varsel.expensetracker.ui.import_statement

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.varsel.expensetracker.domain.repository.TransactionRepository
import com.varsel.expensetracker.util.OcrManager
import com.varsel.expensetracker.util.PdfTextExtractor
import com.varsel.expensetracker.util.StatementParserEngine
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface ImportUiState {
    object Idle : ImportUiState
    object Loading : ImportUiState
    data class Success(val importedCount: Int) : ImportUiState
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

    /**
     * Handles statement file imports (PDFs or Images) by extracting raw text
     * and routing it through the hybrid template parsing engine.
     */
    fun importStatement(uri: Uri, mimeType: String?) {
        viewModelScope.launch {
            _uiState.value = ImportUiState.Loading
            try {
                val rawText = if (mimeType == "application/pdf" || uri.toString().endsWith(".pdf", ignoreCase = true)) {
                    pdfTextExtractor.extractTextFromPdf(context, uri)
                } else {
                    ocrManager.extractTextFromImage(context, uri)
                }

                if (rawText.isNullOrBlank()) {
                    _uiState.value = ImportUiState.Error("Could not extract text from the selected document.")
                    return@launch
                }

                // Parse the raw statement using the hybrid template routing engine
                val transactions = statementParserEngine.parseStatement(rawText)

                if (transactions.isEmpty()) {
                    _uiState.value = ImportUiState.Error("No valid transactions found using template parser.")
                    return@launch
                }

                // Persist parsed transactions into local database
                for (transaction in transactions) {
                    transactionRepository.insertTransaction(transaction)
                }

                _uiState.value = ImportUiState.Success(transactions.size)
            } catch (e: Exception) {
                _uiState.value = ImportUiState.Error(e.localizedMessage ?: "An unexpected error occurred during import.")
            }
        }
    }

    fun resetState() {
        _uiState.value = ImportUiState.Idle
    }
}
