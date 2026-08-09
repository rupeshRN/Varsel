package com.varsel.expensetracker.util

import com.varsel.expensetracker.parser.BankDetector
import com.varsel.expensetracker.parser.StatementImportResult
import com.varsel.expensetracker.parser.StatementSummaryExtractor
import com.varsel.expensetracker.parser.TextNormalizer
import javax.inject.Inject

class StatementParserEngine @Inject constructor(
    private val bankDetector: BankDetector,
    private val textNormalizer: TextNormalizer,
    private val statementSummaryExtractor: StatementSummaryExtractor
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

        return StatementImportResult(
            summary = summary,
            transactions = transactions
        )
    }
}
