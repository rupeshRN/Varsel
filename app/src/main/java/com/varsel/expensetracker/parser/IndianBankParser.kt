package com.varsel.expensetracker.parser

import com.varsel.expensetracker.domain.model.Transaction
import javax.inject.Inject

class IndianBankParser @Inject constructor(
    private val blockBuilder: TransactionBlockBuilder
) : StatementParser {

    override fun canParse(rawText: String): Boolean {

        val text = rawText.uppercase()

        return text.contains("ACCOUNT ACTIVITY") ||
                text.contains("ACCOUNT SUMMARY") ||
                text.contains("ACCOUNT DETAILS")
    }

    override fun parse(rawText: String): List<Transaction> {

        val blocks = blockBuilder.build(rawText)

        throw IllegalArgumentException(

            buildString {

                appendLine("TOTAL BLOCKS = ${blocks.size}")
                appendLine()

                blocks.forEachIndexed { index, block ->

                    appendLine("========== BLOCK ${index + 1} ==========")

                    block.lines.forEach {
                        appendLine(it)
                    }

                    appendLine()
                }
            }

        )
    }
}
