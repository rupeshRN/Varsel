package com.varsel.expensetracker.parser

import javax.inject.Inject

class MerchantExtractor @Inject constructor() {

    fun clean(text: String): String {

        var result = text

        // Remove common banking keywords
        val keywords = listOf(
            "UPI",
            "IMPS",
            "NEFT",
            "RTGS",
            "ACH",
            "ACHCR",
            "ACHDR",
            "INR"
        )

        keywords.forEach {
            result = result.replace(
                Regex("\\b$it\\b", RegexOption.IGNORE_CASE),
                " "
            )
        }

        // Remove IFSC / bank routing codes
        result = result.replace(
            Regex("\\b[A-Z]{4}0[A-Z0-9]{6,}\\b"),
            " "
        )

        // Remove UPI handles
        result = result.replace(
            Regex("[A-Za-z0-9._-]+@[A-Za-z]+"),
            " "
        )

        // Remove long numbers (UTR, Ref No etc.)
        result = result.replace(
            Regex("\\b\\d{6,}\\b"),
            " "
        )

        // Remove masked account numbers
        result = result.replace(
            Regex("X{3,}\\d*"),
            " "
        )

        // Replace separators
        result = result.replace("/", " ")
        result = result.replace("-", " ")

        // Collapse spaces
        result = result.replace(
            Regex("\\s+"),
            " "
        )

        return result.trim()
    }
}
