package com.varsel.expensetracker.parser

import javax.inject.Inject

class FieldInterpreter @Inject constructor() {

    private val ifscRegex =
        Regex("^[A-Z]{4}0[A-Z0-9]{6}$")

    private val maskedAccountRegex =
        Regex("^X{3,}\\d*$", RegexOption.IGNORE_CASE)

    private val upiRegex =
        Regex(".+@.+", RegexOption.IGNORE_CASE)

    private val referenceRegex =
        Regex("^\\d{10,18}$")

    fun interpret(
        fields: List<String>
    ): TransactionFields {

        var ifsc: String? = null
        var account: String? = null
        var upiId: String? = null
        var reference: String? = null
        var channel: String? = null

        val remaining = mutableListOf<String>()

        for (field in fields) {

            when {

                ifsc == null &&
                        ifscRegex.matches(field) -> {
                    ifsc = field
                }

                account == null &&
                        maskedAccountRegex.matches(field) -> {
                    account = field
                }

                upiId == null &&
                        upiRegex.matches(field) -> {
                    upiId = field
                }

                reference == null &&
                        referenceRegex.matches(field) -> {
                    reference = field
                }

                field.equals("UPI", true) ||
                        field.equals("IMPS", true) ||
                        field.equals("NEFT", true) ||
                        field.equals("RTGS", true) -> {
                    channel = field.uppercase()
                }

                else -> remaining.add(field)
            }
        }

        val merchant =
            remaining.firstOrNull()

        val purpose =
            if (remaining.size >= 2)
                remaining.last()
            else
                null

        return TransactionFields(
            ifsc = ifsc,
            account = account,
            upiId = upiId,
            reference = reference,
            channel = channel,
            merchant = merchant,
            purpose = purpose,
            unknown = remaining
        )
    }
}
