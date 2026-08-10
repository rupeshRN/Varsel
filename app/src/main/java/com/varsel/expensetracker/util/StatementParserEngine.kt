package com.varsel.expensetracker.util

import com.varsel.expensetracker.parser.BankDetector
import com.varsel.expensetracker.parser.ReconciliationEngine
import com.varsel.expensetracker.parser.StatementImportResult
import com.varsel.expensetracker.parser.StatementSummaryExtractor
import com.varsel.expensetracker.parser.TextNormalizer
import javax.inject.Inject
import android.util.Log

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
            
 //below dateregex and detecteddates and Log.d is for debug purpose only
        val dateRegex =
            Regex("\\d{1,2}\\s+[A-Za-z]{3}\\s+\\d{4}")

        val detectedDates =
            dateRegex.findAll(normalizedText).count()

            Log.d(
    "StatementParser",
    "Dates detected = $detectedDates"
)

        val summary =
            statementSummaryExtractor.extract(normalizedText)

        val parser =
            bankDetector.detect(normalizedText)

        val transactions =
            parser.parse(normalizedText)

    //below Log.d is for debug purpose only
        Log.d(
    "StatementParser",
    "Transactions parsed = ${transactions.size}"
)

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
