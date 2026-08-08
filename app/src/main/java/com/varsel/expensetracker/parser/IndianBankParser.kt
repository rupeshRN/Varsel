package com.varsel.expensetracker.parser

import com.varsel.expensetracker.domain.model.Transaction
import com.varsel.expensetracker.domain.model.TransactionType
import java.text.SimpleDateFormat
import java.util.Locale
import javax.inject.Inject

class IndianBankParser @Inject constructor(
    private val transactionBlockBuilder: TransactionBlockBuilder,
    private val indianBlockParser: IndianBlockParser
) : StatementParser {

    override fun canParse(rawText: String): Boolean {

        val text = rawText.uppercase()

        return text.contains("INDIAN BANK") ||
                text.contains("ACCOUNT ACTIVITY") ||
                text.contains("ACCOUNT DETAILS") ||
                text.contains("ACCOUNT SUMMARY")
    }

    override fun parse(rawText: String): List<Transaction> {

        val blocks = transactionBlockBuilder.build(rawText)

        val output = mutableListOf<Transaction>()

        val formatter =
            SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH)

        for (block in blocks) {

            try {

                val parsed = indianBlockParser.parse(block.lines.joinToString("\n"))

                val date =
                    formatter.parse(parsed.date)

                output.add(
                    Transaction(
                        amount = parsed.debit ?: parsed.credit ?: 0.0,
                        type = if (parsed.debit != null)
                            TransactionType.EXPENSE
                        else
                            TransactionType.INCOME,
                        description = parsed.description,
                        category = "Uncategorized",
                        dateTimestamp = date?.time ?: 0L,
                        referenceNumber = null
                    )
                )

            } catch (_: Exception) {
                // Ignore malformed transaction blocks
            }

        }

        return output
    }
}
