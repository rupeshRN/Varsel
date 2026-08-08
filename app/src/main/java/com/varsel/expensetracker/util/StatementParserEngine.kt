package com.varsel.expensetracker.util

import com.varsel.expensetracker.domain.model.Transaction
import com.varsel.expensetracker.parser.BankDetector
import javax.inject.Inject

class StatementParserEngine @Inject constructor(
    private val bankDetector: BankDetector
) {

    fun parseStatement(rawText: String): List<Transaction> {

        val parser = bankDetector.detect(rawText)

        return parser.parse(rawText)
    }
}
