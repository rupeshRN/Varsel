package com.varsel.expensetracker.parser

import com.varsel.expensetracker.domain.model.TransactionType
import javax.inject.Inject

class AmountInterpreter @Inject constructor() {

    private val amountRegex =
        Regex("INR\\s*([\\d,]+\\.\\d{2})")

    fun parse(firstLine: String): ParsedAmount? {

        val matches = amountRegex.findAll(firstLine).toList()

        if (matches.size < 2)
            return null

        val firstAmount =
            matches[0]
                .groupValues[1]
                .replace(",", "")
                .toDoubleOrNull()
                ?: return null

        val balance =
            matches[1]
                .groupValues[1]
                .replace(",", "")
                .toDoubleOrNull()
                ?: return null

        //----------------------------------------------------
        // Determine whether first amount is Debit or Credit
        //----------------------------------------------------

        val beforeFirstAmount =
            firstLine.substring(
                0,
                matches[0].range.first
            ).trimEnd()

        val betweenAmounts =
            firstLine.substring(
                matches[0].range.last + 1,
                matches[1].range.first
            ).trim()

        return when {

            //------------------------------------------------
            // Credit
            //
            // Example:
            // SBIN0014160/SURESH - INR 1774.00 INR 3298.59
            //------------------------------------------------

            beforeFirstAmount.endsWith("-") -> {

                ParsedAmount(
                    amount = firstAmount,
                    balance = balance,
                    type = TransactionType.INCOME
                )
            }

            //------------------------------------------------
            // Debit
            //
            // Example:
            // YESBOPTMUPI INR 70.00 - INR 4070.10
            //------------------------------------------------

            betweenAmounts == "-" -> {

                ParsedAmount(
                    amount = firstAmount,
                    balance = balance,
                    type = TransactionType.EXPENSE
                )
            }

            //------------------------------------------------
            // Fallback
            //------------------------------------------------

            else -> {

                ParsedAmount(
                    amount = firstAmount,
                    balance = balance,
                    type = TransactionType.EXPENSE
                )
            }
        }
    }
}
