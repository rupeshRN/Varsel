package com.varsel.expensetracker.util

import com.varsel.expensetracker.parser.BankDetector
import com.varsel.expensetracker.parser.ReconciliationEngine
import com.varsel.expensetracker.parser.StatementImportResult
import com.varsel.expensetracker.parser.StatementSummaryExtractor
import com.varsel.expensetracker.parser.TextNormalizer
import javax.inject.Inject
import com.varsel.expensetracker.developer.ParserDiagnosticsManager

class StatementParserEngine @Inject constructor(
    private val bankDetector: BankDetector,
    private val textNormalizer: TextNormalizer,
    private val statementSummaryExtractor: StatementSummaryExtractor,
    private val reconciliationEngine: ReconciliationEngine
) {

    fun parseStatement(
        rawText: String
    ): StatementImportResult {

        ParserDiagnosticsManager.reset() // To Populate Diagnostics

        val normalizedText =
            textNormalizer.normalize(rawText)

            // val rawLines is to Populate Diagnostics
        val rawLines =
    rawText
        .lines()
        .count { it.isNotBlank() }

val normalizedLines =
    normalizedText
        .lines()
        .count { it.isNotBlank() }

ParserDiagnosticsManager.latest =
    ParserDiagnosticsManager.latest.copy(

        rawLines = rawLines,

        normalizedLines = normalizedLines

    )
            
 //below dateregex and detecteddates and ParserDiagnosticsManager is for debug purpose only
        val dateRegex =
            Regex("\\d{1,2}\\s+[A-Za-z]{3}\\s+\\d{4}")

        val detectedDates =
            dateRegex.findAll(normalizedText).count()

      ParserDiagnosticsManager.latest =
    ParserDiagnosticsManager.latest.copy(

        datesDetected = detectedDates

    )

        val summary =
            statementSummaryExtractor.extract(normalizedText)

        val parser =
            bankDetector.detect(normalizedText)

        val transactions =
            parser.parse(normalizedText)

            //below parser diagnostic is for debug
           ParserDiagnosticsManager.latest =
    ParserDiagnosticsManager.latest.copy(

        transactionsParsed = transactions.size,

        lastParsedDate =
            transactions
                .maxByOrNull { it.dateTimestamp }
                ?.let {

                    java.text.SimpleDateFormat(
                        "dd MMM yyyy",
                        java.util.Locale.ENGLISH
                    ).format(
                        java.util.Date(it.dateTimestamp)
                    )

                } ?: "—"

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
