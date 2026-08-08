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
        DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.ENGLISH),
        DateTimeFormatter.ofPattern("d/M/yyyy", Locale.ENGLISH),
        DateTimeFormatter.ofPattern("dd/MM/yy", Locale.ENGLISH),
        DateTimeFormatter.ofPattern("d/M/yy", Locale.ENGLISH),
        DateTimeFormatter.ofPattern("dd-MMM-yyyy", Locale.ENGLISH),
        DateTimeFormatter.ofPattern("d-MMM-yyyy", Locale.ENGLISH),
        DateTimeFormatter.ofPattern("dd-MM-yyyy", Locale.ENGLISH)
    )

    fun parseStatement(rawText: String): List<Transaction> {
        val transactions = mutableListOf<Transaction>()
        val normalizedText = rawText.replace("\r\n", "\n").replace("\r", "\n")

        val dateRegex = Regex("\\b\\d{1,2}[-/]\\d{1,2}(?:[-/]\\d{2,4})?\\b|\\b\\d{1,2}[-/][A-Za-z]{3}[-/]\\d{2,4}\\b")
        val dateMatches = dateRegex.findAll(normalizedText).toList()

        if (dateMatches.isEmpty()) return emptyList()

        for (i in dateMatches.indices) {
            val match = dateMatches[i]
            val startIndex = match.range.first
            val endIndex = if (i + 1 < dateMatches.size) dateMatches[i + 1].range.first else normalizedText.length
            val chunk = normalizedText.substring(startIndex, endIndex).replace("\n", " ").trim()

            val dateStr = match.value
            val timestamp = parseDateSafely(dateStr)

            val txn = parseChunkIntoTransaction(timestamp, chunk)
            if (txn != null) {
                transactions.add(txn)
            }
        }

        return transactions.filterNot { it.type == TransactionType.INCOME && it.amount < 0 }
    }

    private fun parseDateSafely(dateStr: String): Long {
        var formatStr = dateStr
        val hasYear = Regex("\\d{4}").containsMatchIn(formatStr) || Regex("-\\d{2}$").containsMatchIn(formatStr) || Regex("/\\d{2}$").containsMatchIn(formatStr)
        if (!hasYear) {
            formatStr = "$formatStr/2023"
        }
        for (formatter in dateFormats) {
            try {
                val localDate = LocalDate.parse(formatStr, formatter)
                return localDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
            } catch (e: DateTimeParseException) {
                // Try next format
            }
        }
        return System.currentTimeMillis()
    }

    private fun parseChunkIntoTransaction(timestamp: Long, chunk: String): Transaction? {
        val cleanedChunk = chunk.replace(Regex("\\s+"), " ").trim()
        
        val upper = cleanedChunk.uppercase()
        if (upper.contains("VALUE DATE") || upper.contains("STATEMENT SUMMARY") || upper.contains("OPENING BALANCE")) {
            return null
        }

        val refPattern = Regex("(?i)\\b(REF|UTR|TXN|ID)[:\\s]*([A-Za-z0-9]+)\\b")
        val implicitRefPattern = Regex("\\b\\d{10,16}\\b")

        var referenceNumber: String? = null
        val refMatch = refPattern.find(cleanedChunk)
        if (refMatch != null) {
            referenceNumber = refMatch.value.trim()
        } else {
            val implicitMatches = implicitRefPattern.findAll(cleanedChunk).toList()
            for (m in implicitMatches) {
                val candidate = m.value
                if (!cleanedChunk.contains(".$candidate") && candidate.length in 10..15) {
                    referenceNumber = candidate
                    break
                }
            }
        }

        val amountRegex = Regex("\\b\\d{1,3}(?:,\\d{3})*\\.\\d{2}\\b|\\b\\d+\\.\\d{2}\\b")
        val amounts = amountRegex.findAll(cleanedChunk).map { it.value.replace(",", "").toDouble() }.toList()

        if (amounts.isEmpty()) return null

        val transactionAmount: Double = if (amounts.size >= 2 && cleanedChunk.contains("CR")) {
            amounts[0]
        } else {
            amounts.last()
        }

        val isDebit = upper.contains("DR") || upper.contains("DEBIT") || upper.contains("WITHDRAWAL") || upper.contains("IMPS") || upper.contains("UPI")
        val transactionType = if (isDebit) TransactionType.EXPENSE else TransactionType.INCOME

        var description = cleanedChunk
            .replace(Regex("^\\d{1,2}[-/]\\d{1,2}(?:[-/]\\d{2,4})?\\b"), "")
            .replace("ATM SERVICE BRANCH", "")
            .replace(amountRegex, "")
            .replace(Regex("\\b\\d+\\.\\d{2}CR\\b"), "")
            .replace("CR", "")
            .replace("DR", "")
            .replace("|", "")
            .replace(Regex("\\s+"), " ")
            .trim()

        if (description.isEmpty() || description.length < 3) {
            description = "Bank Transaction"
        }

        return Transaction(
            amount = kotlin.math.abs(transactionAmount),
            type = transactionType,
            description = description,
            category = "Uncategorized",
            dateTimestamp = timestamp,
            referenceNumber = referenceNumber
        )
    }
}
