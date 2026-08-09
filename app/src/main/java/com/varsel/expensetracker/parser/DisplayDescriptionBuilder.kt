package com.varsel.expensetracker.parser

import javax.inject.Inject

class DisplayDescriptionBuilder @Inject constructor() {

    fun build(
    fields: TransactionFields,
    fallback: String
): String {

    val merchant =
        fields.merchant
            ?.trim()
            ?.takeIf { it.isNotBlank() }

    val purpose =
        fields.purpose
            ?.trim()
            ?.takeIf { it.isNotBlank() }

    //----------------------------------------------------
    // Prefer a meaningful purpose
    //----------------------------------------------------

    if (purpose != null &&
        !isGenericPurpose(purpose)
    ) {
        return purpose
    }

    //----------------------------------------------------
    // Otherwise use merchant
    //----------------------------------------------------

    if (merchant != null) {
        return merchant
    }

    //----------------------------------------------------
    // Otherwise use purpose (generic)
    //----------------------------------------------------

    if (purpose != null) {
        return purpose
    }

    //----------------------------------------------------
    // Fallback
    //----------------------------------------------------

    return fallback
    }

    //----------------------------------------------------

    private fun isGenericPurpose(
        purpose: String
    ): Boolean {

        val text =
            purpose.lowercase()

        return text == "upi" ||
                text == "transfer" ||
                text == "payment" ||
                text == "pay" ||
                text == "fund transfer" ||
                text.startsWith("pay to")
    }

    //----------------------------------------------------

    private fun looksLikePersonName(
        merchant: String
    ): Boolean {

        val words =
            merchant.split("\\s+".toRegex())

        if (words.isEmpty())
            return false

        if (words.size > 3)
            return false

        return words.all {

            it.length >= 2 &&
            it.all(Char::isLetter)
        }
    }
}
