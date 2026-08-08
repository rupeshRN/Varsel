package com.varsel.expensetracker.parser

import com.varsel.expensetracker.domain.model.Transaction
import javax.inject.Inject

class IndianBankParser @Inject constructor() : StatementParser {

    override fun canParse(rawText: String): Boolean {

        val text = rawText.uppercase()

        return text.contains("INDIAN BANK")
                || text.contains("ACCOUNT ACTIVITY")
                || text.contains("ACCOUNT DETAILS")
                || text.contains("ACCOUNT SUMMARY")
    }

    override fun parse(rawText: String): List<Transaction> {

    val lines = rawText
        .lines()
        .map { it.trim() }
        .filter { it.isNotBlank() }

    val dateRegex =
        Regex("^\\d{1,2}\\s+[A-Za-z]{3}\\s+\\d{4}")

    val transactionStartLines = mutableListOf<String>()

    for (line in lines) {

        if (dateRegex.containsMatchIn(line)) {
            transactionStartLines.add(line)
        }
    }

    throw IllegalArgumentException(

        buildString {

            appendLine("Transaction Start Lines Found")

            appendLine("----------------------------")

            transactionStartLines.forEach {
                appendLine(it)
            }
        }

    )
    }
}
