package com.varsel.expensetracker.parser

import com.varsel.expensetracker.domain.model.Transaction
import java.text.SimpleDateFormat
import java.util.Locale
import javax.inject.Inject

class IndianBankParser @Inject constructor(
    private val blockBuilder: TransactionBlockBuilder,
    private val merchantExtractor: MerchantExtractor,
    private val descriptionCleaner: DescriptionCleaner,
    private val slashTokenizer: SlashTokenizer,
    private val fieldInterpreter: FieldInterpreter,
    private val amountInterpreter: AmountInterpreter,
    private val parserConfidenceEngine: ParserConfidenceEngine
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

        val dateFormatter =
            SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH)

        for (block in blocks) {

            if (block.lines.isEmpty())
                continue

            val firstLine = block.lines.first()

            val dateMatch =
                dateRegex.find(firstLine) ?: continue

            val date = try {
                dateFormatter.parse(dateMatch.value)
            } catch (e: Exception) {
                null
            } ?: continue

            //--------------------------------------------------
            // Amount + Type
            //--------------------------------------------------

            val parsedAmount =
                amountInterpreter.parse(firstLine)
                    ?: continue

            //--------------------------------------------------
            // Description
            //--------------------------------------------------

            val allText =
                block.lines.joinToString(" ")

            var rawDescription = allText

            rawDescription =
                rawDescription.replace(dateMatch.value, "")

            rawDescription =
                rawDescription.replaceFirst(
                    Regex("INR\\s*[\\d,]+\\.\\d{2}"),
                    ""
                )

            rawDescription =
                rawDescription.replaceFirst(
                    Regex("INR\\s*[\\d,]+\\.\\d{2}"),
                    ""
                )

            rawDescription = rawDescription.trim()

            val tokens =
                slashTokenizer.tokenize(rawDescription)

            val fields =
                fieldInterpreter.interpret(tokens)

            val confidence =
    parserConfidenceEngine.evaluate(fields)

            val description =
                when {

                    !fields.purpose.isNullOrBlank() ->
                        fields.purpose!!

                    !fields.merchant.isNullOrBlank() ->
                        fields.merchant!!

                    else ->
                        descriptionCleaner.clean(rawDescription)
                }

            //--------------------------------------------------
            // Transaction
            //--------------------------------------------------

            transactions.add(
                Transaction(
                    amount = parsedAmount.amount,
                    type = parsedAmount.type,
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
