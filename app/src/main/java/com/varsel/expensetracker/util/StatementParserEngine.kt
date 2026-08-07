package com.varsel.expensetracker.util

import com.varsel.expensetracker.domain.model.Transaction
import com.varsel.expensetracker.domain.model.TransactionType
import javax.inject.Inject
import kotlin.math.abs

class StatementParserEngine @Inject constructor() {

    fun parseStatement(rawText: String): List<Transaction> {
        val transactions = mutableListOf<Transaction>()
        val lines = rawText.split("\n")

        // Regex pattern to look for currency amounts (e.g., 45.00, 1,234.56, -50.20)
        val amountRegex = Regex("[-+]?\\d{1,3}(?:,\\d{3})*(?:\\.\\d{2})|[-+]?\\d+\\.\\d{2}")

        for (line in lines) {
            val trimmedLine = line.trim()
            if (trimmedLine.isEmpty()) continue

            val match = amountRegex.find(trimmedLine)
            if (match != null) {
                val amountStr = match.value.replace(",", "")
                val amount = amountStr.toDoubleOrNull() ?: continue

                // Extract description by removing the amount part from the line
                val description = trimmedLine.replace(match.value, "").trim()
                if (description.length < 2) continue

                val type = if (amount < 0) TransactionType.EXPENSE else TransactionType.INCOME

                transactions.add(
                    Transaction(
                        description = description,
                        amount = abs(amount),
                        type = type,
                        category = "Uncategorized",
                        dateTimestamp = System.currentTimeMillis(),
                        referenceNumber = ""
                    )
                )
            }
        }
        return transactions
    }
}
