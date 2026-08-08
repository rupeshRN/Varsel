package com.varsel.expensetracker.parser

import javax.inject.Inject

class TransactionBlockBuilder @Inject constructor() {

    private val dateRegex =
        Regex("^\\d{1,2}\\s+[A-Za-z]{3}\\s+\\d{4}")

    fun build(normalizedText: String): List<TransactionBlock> {

        val lines = normalizedText
            .lines()
            .map { it.trim() }
            .filter { it.isNotBlank() }

        val transactionLines = mutableListOf<String>()

        var accountActivityFound = false

        for (line in lines) {

            if (!accountActivityFound) {

                if (line.uppercase().contains("ACCOUNT ACTIVITY")) {
                    accountActivityFound = true
                }

                continue
            }

            transactionLines.add(line)
        }

        val blocks = mutableListOf<TransactionBlock>()

        var current = mutableListOf<String>()

        for (line in transactionLines) {

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
