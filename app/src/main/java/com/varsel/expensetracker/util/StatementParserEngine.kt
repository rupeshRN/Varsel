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
        DateTimeFormatter.ofPattern("d-MMM-yyyy", Locale.ENGLISH),
        DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.ENGLISH),
        DateTimeFormatter.ofPattern("d/M/yyyy", Locale.ENGLISH),
        DateTimeFormatter.ofPattern("dd-MM-yyyy", Locale.ENGLISH),
        DateTimeFormatter.ofPattern("d-M-yyyy", Locale.ENGLISH),
        DateTimeFormatter.ofPattern("dd-MMM-yy", Locale.ENGLISH),
        DateTimeFormatter.ofPattern("d-MMM-yy", Locale.ENGLISH)
    )

    fun parseStatement(rawText: String): List<Transaction> {
        val transactions = mutableListOf<Transaction>()
        val lines = rawText.split("\n").map { it.trim() }.filter { it.isNotEmpty() }

        val dateRegex = Regex("\\b\\d{1,2}[-/][A-Za-z]{3}[-/]\\d{2,4}\\b|\\b\\d{1,2}[-/]\\d{1,2}[-/]\\d{2,4}\\b")
        val amountRegex = Regex("[-+]?\\d{1,3}(?:[\\s,]*\\d{3})*(?:\\.\\d{2})?|[-+]?\\d+(?:\\.\\d{2})?")

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
                val remainder = line.substring(dateMatch.range.last + 1).trim()
                if (remainder.isNotEmpty()) {
                    blockLines.add(remainder)
                }

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
                var normalizedDateStr = dateStr
                if (dateStr.matches(Regex(".*-\\d{2}$")) || dateStr.matches(Regex(".*/\\d{2}$"))) {
                    val parts = dateStr.split(Regex("[-/]"))
                    if (parts.size == 3 && parts[2].length == 2) {
                        val prefix = if (parts[2].toInt() > 50) "19" else "20"
                        normalizedDateStr = dateStr.replace(Regex("${parts[2]}$"), "$prefix${parts[2]}")
                    }
                }
                val localDate = LocalDate.parse(normalizedDateStr, formatter)
                return localDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
            } catch (e: DateTimeParseException) {
                // Continue to next format
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
        var referenceNumber: String? = null

        // Regex to detect reference numbers or UTR codes (e.g., REF: 12345, UTRN987654)
        val refPattern = Regex("(?i)\\b(REF|UTR|TXN|ID)[:\\s]*([A-Za-z0-9]{6,})\\b")

        val descBuilder = StringBuilder()

        for (item in cleanedBlock) {
            // Check if line contains a reference number
            val refMatch = refPattern.find(item)
            if (refMatch != null && referenceNumber == null) {
                referenceNumber = refMatch.value
                val textWithoutRef = item.replace(refMatch.value, "").trim()
                if (textWithoutRef.isNotEmpty()) {
                    descBuilder.append(" ").append(textWithoutRef)
                }
                continue
            }

            val amountMatch = amountRegex.find(item)
            if (amountMatch != null && transactionAmount == null) {
                val rawAmountStr = amountMatch.value.replace(" ", "").replace(",", "")
                transactionAmount = rawAmountStr.toDoubleOrNull()
                
                val upperItem = item.uppercase()
                transactionType = if (upperItem.contains("(DR)") || upperItem.contains(" DR") || rawAmountStr.startsWith("-")) {
                    TransactionType.EXPENSE
                } else if (upperItem.contains("(CR)") || upperItem.contains(" CR") || upperItem.contains("DEPOSIT")) {
                    TransactionType.INCOME
                } else {
                    if (transactionAmount != null && transactionAmount < 0) TransactionType.EXPENSE else TransactionType.INCOME
                }
            } else {
                descBuilder.append(" ").append(item)
            }
        }

        val rawDesc = descBuilder.toString().trim().replace(Regex("\\s+"), " ")
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
            referenceNumber = referenceNumber
        )
    }
}
