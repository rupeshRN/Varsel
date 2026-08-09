package com.varsel.expensetracker.parser

import javax.inject.Inject

class TransactionBlockBuilder @Inject constructor() {

    private val dateRegex =
        Regex("^\\d{1,2}\\s+[A-Za-z]{3}\\s+\\d{4}")

        private val footerKeywords = listOf(

    "ENDING BALANCE",

    "TOTAL CREDITS",
    "TOTAL DEBITS",

    "OPENING BALANCE",

    "ACCOUNT SUMMARY",

    "ACCOUNT DETAILS",

    "CUSTOMER'S ADDRESS",

    "IFSC",

    "ACCOUNT HOLDER",

    "ACCOUNT NUMBER"
)

    fun build(normalizedText: String): List<TransactionBlock> {

        val lines = normalizedText
            .lines()
            .map { it.trim() }
            .filter { it.isNotBlank() }

        val transactionLines = mutableListOf<String>()

        var accountActivityFound = false

        for (line in lines) {

    val upper = line.uppercase()

    if (!accountActivityFound) {

        if (upper.contains("ACCOUNT ACTIVITY")) {
            accountActivityFound = true
        }

        continue
    }

    // Skip table header
    if (upper.contains("DATE TRANSACTION DETAILS")) {
        continue
    }

    // Stop when footer starts
    if (footerKeywords.any { upper.contains(it) }) {
        break
    }

    transactionLines.add(line)
        }

        val blocks = mutableListOf<TransactionBlock>()

        var current = mutableListOf<String>()

        for (line in transactionLines) {

    // Ignore table header
    if (line.uppercase().startsWith("DATE TRANSACTION")) {
        continue
    }

    if (dateRegex.containsMatchIn(line)) {

        if (current.isNotEmpty()) {

            blocks.add(
                TransactionBlock(current.toList())
            )

            current.clear()
        }
    }

    current.add(line)
        }

        if (current.isNotEmpty()) {

            blocks.add(
                TransactionBlock(current.toList())
            )
        }

        return blocks
    }
}
