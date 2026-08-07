package com.varsel.expensetracker.util

import com.varsel.expensetracker.domain.model.Transaction
import com.varsel.expensetracker.domain.model.TransactionType
import javax.inject.Inject

class StatementParserEngine @Inject constructor(
    private val smartCategorizer: SmartCategorizerEngine
) {

    fun parseStatement(rawText: String): List<Transaction> {
        val transactions = mutableListOf<Transaction>()
        val lines = rawText.split("\n").map { it.trim() }.filter { it.isNotEmpty() }

        // Match standard date formats
        val dateRegex = Regex("\\d{2}-[A-Za-z]{3}-\\d{4}|\\d{2}/\\d{2}/\\d{4}")
        // Improved amount regex to strictly match currency formats
        val amountRegex = Regex("[-+]?\\d{1,3}(?:,\\d{3})*(?:\\.\\d{2})|[-+]?\\d+\\.\\d{2}")

            // Fallback based on expected amounts from screenshot
                    if (transactionAmount != null && transactionAmount < 0) TransactionType.EXPENSE else TransactionType.INCOME
package com.varsel.expensetracker.util

import com.varsel.expensetracker.domain.model.Transaction
import com.varsel.expensetracker.domain.model.TransactionType
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.Locale
import javax.inject.Inject

class StatementParserEngine @Inject constructor() {

    private val dateFormats = listOf(
        DateTimeFormatter.ofPattern("dd-MMM-yyyy", Locale.ENGLISH),
        DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.ENGLISH),
        DateTimeFormatter.ofPattern("dd-MM-yyyy", Locale.ENGLISH)
    )

    fun parseStatement(rawText: String): List<Transaction> {
        val transactions = mutableListOf<Transaction>()
        val lines = rawText.split("\n").map { it.trim() }.filter { it.isNotEmpty() }

        val dateRegex = Regex("\\d{2}-[A-Za-z]{3}-\\d{4}|\\d{2}/\\d{2}/\\d{4}|\\d{2}-\\d{2}-\\d{4}")
        val amountRegex = Regex("[-+]?\\d{1,3}(?:,\\d{3})*(?:\\.\\d{2})|[-+]?\\d+\\.\\d{2}")

        var i = 0
        while (i < lines.size) {
            val line = lines[i]

            if (isMetadataLine(line)) {
                i++
                continue
            }

            val dateMatch = dateRegex.find(line)
            if (dateMatch != null) {
                val dateStr = dateMatch.value
                val timestamp = parseDateSafely(dateStr)

                val blockLines = mutableListOf<String>()
                i++
                while (i < lines.size && dateRegex.find(lines[i]) == null) {
                    blockLines.add(lines[i])
                    i++
                }

                val txn = parseVerticalBlockIntoTransaction(timestamp, blockLines, amountRegex)
                if (txn != null) {
                    transactions.add(txn)
                }
            } else {
                i++
            }
        }

        return transactions.filterNot { it.type == TransactionType.INCOME && it.amount < 0 }
    }

    private fun isMetadataLine(line: String): Boolean {
        val upper = line.uppercase()
        return upper.contains("STATEMENT SUMMARY") ||
                upper.contains("OPENING BALANCE") ||
                upper.contains("CLOSING BALANCE") ||
                upper.contains("TOTAL DEBITS") ||
                upper.contains("TOTAL CREDITS") ||
                upper.contains("TRANSACTION HISTORY") ||
                (upper.startsWith("DATE") && !upper.contains("DESCRIPTION")) ||
                upper.startsWith("BRANCH:") ||
                upper.startsWith("ACCOUNT:") ||
                upper.startsWith("CURRENCY:") ||
                upper.contains("PAGE ")
    }

    private fun parseDateSafely(dateStr: String): Long {
        for (formatter in dateFormats) {
            try {
                val localDate = LocalDate.parse(dateStr, formatter)
                return localDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
            } catch (e: DateTimeParseException) {
                // Try next format
            }
        }
        return System.currentTimeMillis()
    }

    private fun parseVerticalBlockIntoTransaction(
        timestamp: Long,
        blockLines: List<String>,
        amountRegex: Regex
    ): Transaction? {
        val cleanedBlock = blockLines.map { it.replace("|", "").trim() }.filter { it.isNotEmpty() }
        
        var description = "Transaction"
        var transactionAmount: Double? = null
        var transactionType = TransactionType.EXPENSE

        val descBuilder = StringBuilder()

        for (item in cleanedBlock) {
            val amountMatch = amountRegex.find(item)
            if (amountMatch != null && transactionAmount == null) {
                val amountStr = amountMatch.value.replace(",", "")
                transactionAmount = amountStr.toDoubleOrNull()
                
                val upperItem = item.uppercase()
                transactionType = if (upperItem.contains("(DR)") || amountStr.startsWith("-")) {
                    TransactionType.EXPENSE
                } else if (upperItem.contains("(CR)") || upperItem.contains("DEPOSIT")) {
                    TransactionType.INCOME
                } else {
                    if (transactionAmount != null && transactionAmount < 0) TransactionType.EXPENSE else TransactionType.INCOME
                }
            } else {
                descBuilder.append(" ").append(item)
            }
        }

        val rawDesc = descBuilder.toString().trim()
        if (rawDesc.isNotEmpty()) {
            description = rawDesc
        }

        val finalAmount = transactionAmount ?: 0.0
        if (finalAmount == 0.0) return null

        return Transaction(
            amount = kotlin.math.abs(finalAmount),
            type = transactionType,
            description = description,
            category = "Uncategorized",
            dateTimestamp = timestamp,
            referenceNumber = null
        )
    }
}
