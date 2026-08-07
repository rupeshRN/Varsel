package com.varsel.expensetracker.ui.import_statement

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.varsel.expensetracker.domain.model.Transaction
import com.varsel.expensetracker.domain.repository.TransactionRepository
import com.varsel.expensetracker.util.PdfTextExtractor
import com.varsel.expensetracker.util.StatementParserEngine
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ImportViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val pdfTextExtractor: PdfTextExtractor,
    private val statementParserEngine: StatementParserEngine
) : ViewModel() {

    private val _uiState = MutableStateFlow<ImportUiState>(ImportUiState.Idle)
    val uiState: StateFlow<ImportUiState> = _uiState.asStateFlow()

    fun processSelectedFile(uri: Uri) {
        viewModelScope.launch {
            _uiState.value = ImportUiState.Processing
            try {
                val extractedText = pdfTextExtractor.extractText(uri)
                val transactions = statementParserEngine.parseStatement(extractedText)
                _uiState.value = ImportUiState.ParsedTransactions(transactions)
            } catch (e: Exception) {
                _uiState.value = ImportUiState.Error(e.message ?: "Unknown error occurred")
            }
        }
    }

    fun confirmAndSaveTransactions(transactions: List<Transaction>) {
        viewModelScope.launch {
            try {
                for (transaction in transactions) {
                    transactionRepository.insertTransaction(transaction)
                }
                _uiState.value = ImportUiState.Saved
            } catch (e: Exception) {
                _uiState.value = ImportUiState.Error(e.message ?: "Failed to save transactions")
            }
        }
    }

    fun resetState() {
        _uiState.value = ImportUiState.Idle
    }

    fun submitPdfPassword(password: String) {
        // Handle password submission if required
    }
}

sealed class ImportUiState {
    object Idle : ImportUiState()
    object Loading : ImportUiState()
    object Processing : ImportUiState()
    data class ParsedTransactions(val parsedTransactions: List<Transaction>) : ImportUiState()
    object Saved : ImportUiState()
    data class PasswordRequired(val isInvalidPasswordError: Boolean = false) : ImportUiState()
    data class Error(val message: String) : ImportUiState()
}
