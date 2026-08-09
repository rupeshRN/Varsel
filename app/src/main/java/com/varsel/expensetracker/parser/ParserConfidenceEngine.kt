package com.varsel.expensetracker.parser

import javax.inject.Inject

class ParserConfidenceEngine @Inject constructor() {

    fun evaluate(
        fields: TransactionFields
    ): ConfidenceReport {

        val result = mutableListOf<FieldConfidence>()

        result += FieldConfidence(
            "Merchant",
            fields.merchant,
            merchantScore(fields.merchant)
        )

        result += FieldConfidence(
            "Purpose",
            fields.purpose,
            purposeScore(fields.purpose)
        )

        result += FieldConfidence(
            "UPI",
            fields.upiId,
            if (fields.upiId != null) 100 else 0
        )

        result += FieldConfidence(
            "Reference",
            fields.reference,
            if (fields.reference != null) 100 else 0
        )

        result += FieldConfidence(
            "IFSC",
            fields.ifsc,
            if (fields.ifsc != null) 100 else 0
        )

        return ConfidenceReport(result)
    }

    private fun merchantScore(
        merchant: String?
    ): Int {

        if (merchant.isNullOrBlank())
            return 0

        if (merchant.length < 3)
            return 20

        if (merchant.contains("@"))
            return 10

        if (merchant.any { it.isDigit() })
            return 60

        return 95
    }

    private fun purposeScore(
        purpose: String?
    ): Int {

        if (purpose.isNullOrBlank())
            return 0

        if (purpose.length < 4)
            return 30

        return 100
    }
}
