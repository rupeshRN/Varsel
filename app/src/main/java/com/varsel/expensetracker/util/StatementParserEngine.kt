package com.varsel.expensetracker.util

import com.varsel.expensetracker.domain.model.TransactionType

data class ParsedTransaction(
    val description: String,
    val amount: Double,
    val type: TransactionType,
    val referenceNumber: String?,
    val timestamp: Long
)

class StatementParserEngine {
    companion object {
        fun parseStatementText(rawText: String): List<ParsedTransaction> {
            val transactions = mutableListOf<ParsedTransaction>()
            val lines = rawText.lines()
            val currentTime = System.currentTimeMillis()

            for (line in lines) {
                if (line.isBlank()) continue
                val upper = line.uppercase()
                val type = if (upper.contains("CR") || upper.contains("CREDIT") || upper.contains("+")) {
                    TransactionType.CREDIT
                } else {
                    TransactionType.DEBIT
                }

                val amountRegex = "([0-9]+\\.[0-9]{2})".toRegex()
                val match = amountRegex.find(line)
                if (match != null) {
                    val amount = match.value.toDoubleOrNull() ?: 0.0
                    transactions.add(
                        ParsedTransaction(
                            description = line.trim(),
                            amount = amount,
                            type = type,
                            referenceNumber = null,
                            timestamp = currentTime
                        )
                    )
                }
            }
            return transactions
        }
    }
}
