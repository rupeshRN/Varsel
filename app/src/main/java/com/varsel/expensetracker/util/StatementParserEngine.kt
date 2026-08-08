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

        // Normalize extracted PDF text
        val normalizedText = textNormalizer.normalize(rawText)

        // Detect the appropriate parser
        val parser = bankDetector.detect(normalizedText)

        // Parse transactions
        return parser.parse(normalizedText)
    }
}
