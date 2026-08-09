package com.varsel.expensetracker.parser

import javax.inject.Inject

data class StatementSummary(

    val openingBalance: Double? = null,

    val totalCredits: Double? = null,

    val totalDebits: Double? = null,

    val endingBalance: Double? = null
)

class StatementSummaryExtractor @Inject constructor() {

    private val moneyRegex =
        Regex("INR\\s*([\\d,]+\\.\\d{2})", RegexOption.IGNORE_CASE)

    fun extract(rawText: String): StatementSummary {

        var opening: Double? = null
        var credits: Double? = null
        var debits: Double? = null
        var ending: Double? = null

        val lines = rawText.lines()

        for (line in lines) {

            val amount = moneyRegex.find(line)
                ?.groupValues
                ?.get(1)
                ?.replace(",", "")
                ?.toDoubleOrNull()
                ?: continue

            val upper = line.uppercase()

            when {

                upper.contains("OPENING BALANCE") ->
                    opening = amount

                upper.contains("TOTAL CREDIT") ||
                upper.contains("TOTAL CREDITS") ->
                    credits = amount

                upper.contains("TOTAL DEBIT") ||
                upper.contains("TOTAL DEBITS") ->
                    debits = amount

                upper.contains("ENDING BALANCE") ||
                upper.contains("CLOSING BALANCE") ->
                    ending = amount
            }
        }

        return StatementSummary(
            openingBalance = opening,
            totalCredits = credits,
            totalDebits = debits,
            endingBalance = ending
        )
    }
}
