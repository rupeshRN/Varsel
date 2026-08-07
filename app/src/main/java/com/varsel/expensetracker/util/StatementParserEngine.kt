package com.varsel.expensetracker.util

import com.varsel.expensetracker.domain.model.Transaction
import com.varsel.expensetracker.domain.model.TransactionType
import javax.inject.Inject
import kotlin.math.abs

class StatementParserEngine @Inject constructor() {

    // Flexible regex supporting various date separators (/ or -) and making the balance column optional
    private val transactionRegex = Regex(
        pattern = """(\d{2}[/-]\d{2}[/-]\d{4})\s+(.+?)\s+([-+]?[0-9,]+\.\d{2})"""
    )

    fun parseStatement(extractedText: String): List<Transaction> {
        val transactions = mutableListOf<Transaction>()
        val lines = extractedText.lines()

        for (line in lines) {
            val trimmedLine = line.trim()
            val matchResult = transactionRegex.find(trimmedLine)

            if (matchResult != null) {
                val rawDescription = matchResult.groups[2]?.value?.trim() ?: "Unknown"
                val amountStr = matchResult.groups[3]?.value ?: "0.00"

                val cleanDescription = rawDescription.replace(Regex("\\s+"), " ")
                val parsedAmount = amountStr.replace(",", "").toDoubleOrNull() ?: 0.0
                
                val transactionType = if (parsedAmount < 0) TransactionType.EXPENSE else TransactionType.INCOME

                val transaction = Transaction(
                    description = cleanDescription,
                    amount = abs(parsedAmount),
                    type = transactionType,
                    category = "Uncategorized",
                    dateTimestamp = System.currentTimeMillis(),
                    referenceNumber = ""
                )

                transactions.add(transaction)
            }
        }

        return transactions
    }
}
