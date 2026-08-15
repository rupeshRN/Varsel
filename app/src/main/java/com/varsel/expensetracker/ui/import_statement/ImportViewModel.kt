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
import com.varsel.expensetracker.developer.ParserDiagnostics
import com.varsel.expensetracker.developer.ParserDiagnosticsManager
import com.varsel.expensetracker.developer.DeveloperRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn

sealed interface ImportUiState {
    object Idle : ImportUiState
    object Loading : ImportUiState
    object Processing : ImportUiState

data class ParsedTransactions(

    val summary: ImportSummary,

    val parsedTransactions: List<SelectableTransaction>

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
    private val developerRepository: DeveloperRepository,
@ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow<ImportUiState>(ImportUiState.Idle)
    val uiState: StateFlow<ImportUiState> = _uiState.asStateFlow()

// Developer diagnostics
    private val _diagnostics =
    MutableStateFlow(ParserDiagnostics())

    val diagnostics: StateFlow<ParserDiagnostics> =
    _diagnostics.asStateFlow()

    val parserDiagnosticsEnabled =
    developerRepository
        .parserDiagnosticsEnabled
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false
        )

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

    // Update developer diagnostics
    _diagnostics.value =
    ParserDiagnosticsManager.latest

if (result.transactions.isEmpty()) {
    _uiState.value =
        ImportUiState.Error("No transactions found.")
    return@launch
}

//--------------------------------------------------
// Build UI summary
//--------------------------------------------------

val credits =

    result.transactions.count {

        it.type == com.varsel.expensetracker.domain.model.TransactionType.INCOME

    }

val debits =

    result.transactions.count {

        it.type == com.varsel.expensetracker.domain.model.TransactionType.EXPENSE

    }

val summary =

    ImportSummary(

        //--------------------------------------------------
        // Temporary values
        // (will become dynamic later)
        //--------------------------------------------------

        bankName = "Indian Bank",

        statementPeriod =
    formatStatementPeriod(
        result.summary.statementStartDate,
        result.summary.statementEndDate
    ),

        transactionsDetected =
            ParserDiagnosticsManager.latest.blocksBuilt,

        transactionsParsed =
            result.transactions.size,

        credits = credits,

        debits = debits,

        duplicates = 0,

        learnedMatches = 0,

        needsReview = 0,

        reconciliationPassed =
            result.reconciliation.isBalanced

    )

_uiState.value =

    ImportUiState.ParsedTransactions(

        summary = summary,

        parsedTransactions =

            result.transactions.map {

                SelectableTransaction(it)

            }

    )

            } catch (e: Exception) {

                _uiState.value = ImportUiState.Error(
    e.message ?: e.stackTraceToString())
            }
        }
    }

    fun confirmAndSaveTransactions(
    transactions: List<SelectableTransaction>
) {
        viewModelScope.launch {

            try {

val selectedTransactions =
    transactions.filter { it.selected }

if (selectedTransactions.isEmpty()) {

    _uiState.value =
        ImportUiState.Error(
            "Please select at least one transaction."
        )

    return@launch
}

selectedTransactions.forEach {

    transactionRepository.insertTransaction(
        it.transaction
    )
}

             _uiState.value =
    ImportUiState.Saved(
        selectedTransactions.size
    )

            } catch (e: Exception) {

                _uiState.value = ImportUiState.Error(
                    e.localizedMessage ?: "Failed to save transactions."
                )
            }
        }
    }

    private fun formatStatementPeriod(
    startDate: Long?,
    endDate: Long?
): String {

    if (startDate == null || endDate == null) {
        return "Statement Period Unavailable"
    }

    val formatter =
        java.text.SimpleDateFormat(
            "dd MMM ''yy",
            java.util.Locale.ENGLISH
        )

    return "${formatter.format(startDate)} – ${formatter.format(endDate)}"
}

    fun resetState() {
        _uiState.value = ImportUiState.Idle
    }
}
