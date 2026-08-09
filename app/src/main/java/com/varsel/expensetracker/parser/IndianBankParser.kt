package com.varsel.expensetracker.parser

import com.varsel.expensetracker.domain.model.Transaction
import com.varsel.expensetracker.domain.model.TransactionType
import java.text.SimpleDateFormat
import java.util.Locale
import javax.inject.Inject

class IndianBankParser @Inject constructor(
    private val blockBuilder: TransactionBlockBuilder,
    private val descriptionCleaner: DescriptionCleaner,
    private val slashTokenizer: SlashTokenizer,
    private val fieldInterpreter: FieldInterpreter,
    private val descriptionBuilder: DescriptionBuilder
) : StatementParser {

    override fun canParse(rawText: String): Boolean {

        val text = rawText.uppercase()

        return text.contains("ACCOUNT ACTIVITY") ||
                text.contains("ACCOUNT SUMMARY") ||
                text.contains("ACCOUNT DETAILS")
    }

    override fun parse(rawText: String): List<Transaction> {

        val blocks = blockBuilder.build(rawText)

        val transactions = mutableListOf<Transaction>()

        val dateRegex =
            Regex("^\\d{1,2}\\s*[A-Za-z]{3}\\s+\\d{4}")

        val amountRegex =
            Regex("INR\\s*([\\d,]+\\.\\d{2})")

        val dateFormatter =
            SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH)

        for (block in blocks) {

            if (block.lines.isEmpty()) continue

            val firstLine = block.lines.first()

            val dateMatch = dateRegex.find(firstLine) ?: continue

            val date = try {
                dateFormatter.parse(dateMatch.value)
            } catch (e: Exception) {
                null
            } ?: continue

            val allText = block.lines.joinToString(" ")

            val amounts = amountRegex.findAll(allText).toList()

            // First amount = transaction amount
            // Second amount = running balance
            if (amounts.size < 2) continue

            val amount = amounts[0]
                .groupValues[1]
                .replace(",", "")
                .toDoubleOrNull() ?: continue

            var rawDescription = allText

            rawDescription = rawDescription.replace(dateMatch.value, "")
            rawDescription = rawDescription.replace(amounts[0].value, "")
            rawDescription = rawDescription.replace(amounts[1].value, "")
            rawDescription = rawDescription.trim()

            // Remove IFSC codes, UPI IDs, account numbers, etc.
            val cleanedDescription =
                descriptionCleaner.clean(rawDescription)

            // Break into logical fields
            val tokens =
                slashTokenizer.tokenize(cleanedDescription)

            // Interpret merchant / purpose
            val fields =
                fieldInterpreter.interpret(tokens)

            // Build final user-facing description
            val description =
                descriptionBuilder.build(
                    listOfNotNull(
                        fields.purpose,
                        fields.merchant
                    )
                )

            val upper = description.uppercase()

            val type =
                if (
                    upper.contains("ACHCR") ||
                    upper.contains(" CREDIT") ||
                    upper.contains("CR ") ||
                    upper.contains("SALARY") ||
                    upper.contains("NEFTCR") ||
                    upper.contains("IMPSCR")
                ) {
                    TransactionType.INCOME
                } else {
                    TransactionType.EXPENSE
                }

            transactions.add(
                Transaction(
                    amount = amount,
                    type = type,
                    description = description,
                    category = "Uncategorized",
                    dateTimestamp = date.time,
                    referenceNumber = null
                )
            )
        }

        return transactions
    }
}
