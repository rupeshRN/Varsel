package com.varsel.expensetracker.util

import com.varsel.expensetracker.domain.model.Transaction
import com.varsel.expensetracker.domain.model.TransactionType
import java.util.Date
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.regex.Pattern
import kotlin.math.abs

class StatementParserEngine {

    fun parseStatement(rawText: String): List<Transaction> {
        // Try primary tabular parser first
        val tabularTransactions = parseTabularFormat(rawText)
        if (tabularTransactions.isNotEmpty()) {
            return tabularTransactions
        }

        // Fallback to robust heuristic OCR / block parser
        val fallbackTransactions = parseFallbackFormat(rawText)
        if (fallbackTransactions.isNotEmpty()) {
            return fallbackTransactions
        }

        throw IllegalArgumentException("No valid transactions found using template parser.")
    }

    private fun parseTabularFormat(rawText: String): List<Transaction> {
        val transactions = mutableListOf<Transaction>()
        val lines = rawText.lines().map { it.trim() }.filter { it.isNotEmpty() }
        
        val datePattern = Pattern.compile("(\\d{2}[/-]\\d{2}(?:[/-]\\d{2,4})?)")
        val amountPattern = Pattern.compile("([\\d,]+\\.\\d{2})(?:\\s*(CR|DR))?", Pattern.CASE_INSENSITIVE)

        var currentDate: Date? = null
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val altDateFormat = SimpleDateFormat("dd/MM", Locale.getDefault())

        for (line in lines) {
            val dateMatcher = datePattern.matcher(line)
            if (dateMatcher.find()) {
                val dateStr = dateMatcher.group(1)
                currentDate = try {
                    dateFormat.parse(dateStr) ?: altDateFormat.parse(dateStr)
                } catch (e: Exception) {
                    null
                }
            }

            val amountMatcher = amountPattern.matcher(line)
            val amounts = mutableListOf<Pair<Double, String?>>()
            while (amountMatcher.find()) {
                val amtStr = amountMatcher.group(1)?.replace(",", "")
                val typeStr = amountMatcher.group(2)
                amtStr?.toDoubleOrNull()?.let { amt ->
                    amounts.add(Pair(amt, typeStr))
                }
            }

            if (amounts.isNotEmpty() && currentDate != null) {
                val isDebit = line.contains("DR", ignoreCase = true) || 
                              line.contains("WITHDRAWAL", ignoreCase = true) ||
                              line.contains("IMPS", ignoreCase = true) ||
                              line.contains("UPI", ignoreCase = true)
                
                val transactionAmount = amounts[0].first
                val type = if (isDebit) TransactionType.EXPENSE else TransactionType.INCOME

                var description = line
                dateMatcher.reset()
                if (dateMatcher.find()) {
                    description = description.replace(dateMatcher.group(1) ?: "", "")
                }
                for (amt in amounts) {
                    description = description.replace(amt.first.toString(), "")
                    amt.second?.let { description = description.replace(it, "", ignoreCase = true) }
                }
                description = description.replace(Regex("[/\\\\|]+"), " ").trim()
                if (description.isEmpty()) {
                    description = "Bank Transaction"
                }

                val referenceNumber = extractReferenceNumber(line)
                val timestamp = currentDate.time

                transactions.add(
                    Transaction(
                        amount = abs(transactionAmount),
                        type = type,
                        description = description,
                        category = "Uncategorized",
                        dateTimestamp = timestamp,
                        referenceNumber = referenceNumber
                    )
                )
            }
        }
        return transactions
    }

    private fun parseFallbackFormat(rawText: String): List<Transaction> {
        val transactions = mutableListOf<Transaction>()
        val lines = rawText.lines().map { it.trim() }.filter { it.isNotEmpty() }
        
        val datePattern = Pattern.compile("(\\d{2}[/-]\\d{2}(?:[/-]\\d{2,4})?)")
        val amountPattern = Pattern.compile("(\\d{1,3}(?:,\\d{3})*\\.\\d{2})(?:\\s*(CR|DR))?", Pattern.CASE_INSENSITIVE)

        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val altDateFormat = SimpleDateFormat("dd/MM", Locale.getDefault())

        var pendingDate: Date? = null
        var descriptionBuilder = StringBuilder()
        var pendingAmount: Double? = null
        var isDebit = true

        for (line in lines) {
            val dateMatcher = datePattern.matcher(line)
            val amountMatcher = amountPattern.matcher(line)

            if (dateMatcher.find()) {
                val dateStr = dateMatcher.group(1)
                pendingDate = try {
                    dateFormat.parse(dateStr) ?: altDateFormat.parse(dateStr)
                } catch (e: Exception) {
                    null
                }
            }

            if (amountMatcher.find()) {
                val amtStr = amountMatcher.group(1)?.replace(",", "")
                pendingAmount = amtStr?.toDoubleOrNull()
                val typeStr = amountMatcher.group(2)
                if (typeStr != null && typeStr.equals("CR", ignoreCase = true)) {
                    isDebit = false
                } else if (line.contains("DR", ignoreCase = true) || line.contains("WITHDRAWAL", ignoreCase = true)) {
                    isDebit = true
                }
            }

            if (!dateMatcher.find() && !amountMatcher.find()) {
                if (descriptionBuilder.isNotEmpty()) descriptionBuilder.append(" ")
                descriptionBuilder.append(line)
            }

            if (pendingDate != null && pendingAmount != null) {
                val desc = if (descriptionBuilder.isNotEmpty()) descriptionBuilder.toString() else "Bank Transaction"
                val refNo = extractReferenceNumber(desc)
                val timestamp = pendingDate.time

                transactions.add(
                    Transaction(
                        amount = abs(pendingAmount),
                        type = if (isDebit) TransactionType.EXPENSE else TransactionType.INCOME,
                        description = desc.replace(Regex("[/\\\\|]+"), " ").trim(),
                        category = "Uncategorized",
                        dateTimestamp = timestamp,
                        referenceNumber = refNo
                    )
                )

                pendingAmount = null
                descriptionBuilder = StringBuilder()
            }
        }

        return transactions
    }

    private fun extractReferenceNumber(text: String): String? {
        val refPattern = Pattern.compile("(?:IMPS|UPI|REF|UTR|TXN)[/:\\s]*([A-Za-z0-9]{8,})", Pattern.CASE_INSENSITIVE)
        val matcher = refPattern.matcher(text)
        return if (matcher.find()) matcher.group(1) else null
    }
}
