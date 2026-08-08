package com.varsel.expensetracker.util

import com.varsel.expensetracker.domain.model.Transaction
import com.varsel.expensetracker.parser.BankDetector
import com.varsel.expensetracker.parser.TextNormalizer
import javax.inject.Inject

class StatementParserEngine @Inject constructor(
    private val bankDetector: BankDetector,
    private val textNormalizer: TextNormalizer
) {

    fun parseStatement(rawText: String): List<Transaction> {

        val normalizedText = textNormalizer.normalize(rawText)

        throw IllegalArgumentException(
            buildString {

                appendLine("========== RAW TEXT ==========")
                appendLine(rawText)

                appendLine()
                appendLine("========== NORMALIZED ==========")
                appendLine(normalizedText)

                appendLine()
                appendLine("========== END ==========")

            }
        )

    }
}
