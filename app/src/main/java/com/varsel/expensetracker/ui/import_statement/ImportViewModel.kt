package com.varsel.expensetracker.ui.import_statement

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.varsel.expensetracker.data.local.entity.TransactionEntity
import com.varsel.expensetracker.domain.repository.TransactionRepository
import com.varsel.expensetracker.util.OcrManager
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
    private val statementParserEngine: StatementParserEngine,
    private val ocrManager: OcrManager
) : ViewModel() {

    private val _uiState = MutableStateFlow<ImportUiState>(ImportUiState.Idle)
    val uiState: StateFlow<ImportUiState> = _uiState.asStateFlow()

    fun parseAndSaveStatement(fileBytes: ByteArray) {
        viewModelScope.launch {
            _uiState.value = ImportUiState.Loading
            try {
                val extractedText = pdfTextExtractor.extractText(fileBytes)
                val transactions = statementParserEngine.parseStatement(extractedText)

                for (transaction in transactions) {
                    transactionRepository.insertTransaction(transaction)
                }

                _uiState.value = ImportUiState.Success(transactions)
            } catch (e: Exception) {
                _uiState.value = ImportUiState.Error(e.message ?: "Unknown error occurred")
            }
        }
    }
}

sealed class ImportUiState {
    object Idle : ImportUiState()
    object Loading : ImportUiState()
    data class Success(val transactions: List<TransactionEntity>) : ImportUiState()
    data class Error(val message: String) : ImportUiSt
    ate()
}
