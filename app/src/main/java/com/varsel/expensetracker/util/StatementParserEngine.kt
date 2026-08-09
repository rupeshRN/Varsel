package com.varsel.expensetracker.util

import com.varsel.expensetracker.parser.BankDetector
import com.varsel.expensetracker.parser.StatementImportResult
import com.varsel.expensetracker.parser.StatementSummaryExtractor
import com.varsel.expensetracker.parser.TextNormalizer
import com.varsel.expensetracker.parser.ReconciliationEngine
import javax.inject.Inject

class StatementParserEngine @Inject constructor(
    private val bankDetector: BankDetector,
    private val textNormalizer: TextNormalizer,
    private val statementSummaryExtractor: StatementSummaryExtractor,
    private val reconciliationEngine: ReconciliationEngine
) {

    fun parseStatement(
        rawText: String
    ): StatementImportResult {

        val normalizedText =
            textNormalizer.normalize(rawText)

        val summary =
            statementSummaryExtractor.extract(normalizedText)

        val parser =
            bankDetector.detect(normalizedText)

        val transactions =
            parser.parse(normalizedText)

        val reconciliation =
    reconciliationEngine.reconcile(
        summary,
        transactions
    )

return StatementImportResult(
    summary = summary,
    reconciliation = reconciliation,
    transactions = transactions
)
    }
}
