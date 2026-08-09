package com.varsel.expensetracker.parser

import javax.inject.Inject

class DisplayDescriptionBuilder @Inject constructor() {

    fun build(
        fields: TransactionFields,
        fallback: String
    ): String {

        val merchant =
            fields.merchant?.trim()

        val purpose =
            fields.purpose?.trim()

        //----------------------------------------------------
        // Both merchant and purpose available
        //----------------------------------------------------

        if (!merchant.isNullOrBlank() &&
            !purpose.isNullOrBlank()
        ) {

            if (isGenericPurpose(purpose))
                return merchant

            if (looksLikePersonName(merchant))
                return purpose

            return "$merchant • $purpose"
        }

        //----------------------------------------------------
        // Merchant only
        //----------------------------------------------------

        if (!merchant.isNullOrBlank())
            return merchant

        //----------------------------------------------------
        // Purpose only
        //----------------------------------------------------

        if (!purpose.isNullOrBlank())
            return purpose

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
