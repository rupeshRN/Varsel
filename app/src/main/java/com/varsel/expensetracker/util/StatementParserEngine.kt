package com.varsel.expensetracker.util

import com.varsel.expensetracker.domain.model.Transaction
import com.varsel.expensetracker.parser.BankDetector
import com.varsel.expensetracker.parser.TextNormalizer
import com.varsel.expensetracker.parser.TransactionBlockBuilder
import javax.inject.Inject

class StatementParserEngine @Inject constructor(
    private val bankDetector: BankDetector,
    private val textNormalizer: TextNormalizer,
    private val transactionBlockBuilder: TransactionBlockBuilder
) {

    fun parseStatement(rawText: String): List<Transaction> {

        val normalizedText = textNormalizer.normalize(rawText)

        val blocks = transactionBlockBuilder.build(normalizedText)

        throw IllegalArgumentException(

            buildString {

                appendLine("TOTAL BLOCKS : ${blocks.size}")
                appendLine()

                blocks.forEachIndexed { index, block ->

                    appendLine("==============================")
                    appendLine("BLOCK ${index + 1}")
                    appendLine("==============================")

                    block.lines.forEach {
                        appendLine(it)
                    }

                    appendLine()
                }

            }

        )

        // Will be enabled later
        // val parser = bankDetector.detect(normalizedText)
        // return parser.parse(normalizedText)
    }
}
