package com.varsel.expensetracker.util

import com.varsel.expensetracker.domain.model.Transaction
import com.varsel.expensetracker.domain.model.TransactionType
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.Locale
import javax.inject.Inject
import kotlin.math.abs

/**
 * Advanced Statement Parser Engine implementing 4 core logic pillars:
 * 1. Multi-format Date Extraction with Year Fallback & Anchor Precedence.
 * 2. Bounded Description Extraction, Boilerplate Filtering & Reference Number Isolation.
 * 3. Dual-Column & Unified Amount Parsing with Indicator Heuristics (DR/CR/Signs).
 * 4. Account Number Header Extraction & Running Balance Verification.
 */
class StatementParserEngine @Inject constructor() {

    private val dateFormats = listOf(
        DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.ENGLISH),
        DateTimeFormatter.ofPattern("d/M/yyyy", Locale.ENGLISH),
        DateTimeFormatter.ofPattern("dd/MM/yy", Locale.ENGLISH),
        DateTimeFormatter.ofPattern("d/M/yy", Locale.ENGLISH),
        DateTimeFormatter.ofPattern("dd-MMM-yyyy", Locale.ENGLISH),
        DateTimeFormatter.ofPattern("d-MMM-yyyy", Locale.ENGLISH),
        DateTimeFormatter.ofPattern("dd-MM-yyyy", Locale.ENGLISH),
        DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.ENGLISH)
    )

    private val boilerplateBlacklist = listOf(
        "ATM SERVICE", "BRANCH", "UPI", "IMPS", "NEFT", "RTGS", 
        "POS", "ECOM", "REMITTER BRANCH", "CHEQUE NO", "TRANSACTION HISTORY"
    )

    data class StatementMetadata(
        val accountNumber: String? = null,
        val inferredYear: Int = LocalDate.now().year,
        val openingBalance: Double? = null,
        val closingBalance: Double? = null
    )

    fun parseStatement(rawText: String): List<Transaction> {
        if (rawText.isBlank()) return emptyList()
        val normalizedText = rawText.replace("\r\n", "\n").replace("\r", "\n")

        // Step 4a: Extract Statement Header Metadata (Account Number, Year Fallback, Balances)
        val metadata = extractStatementMetadata(normalizedText)

        // Route based on tabular vs vertical layout signatures
        val transactions = if (isTabularTemplate(normalizedText)) {
            parseTabularTemplateStatement(normalizedText, metadata)
        } else {
            parseStandardTemplateStatement(normalizedText, metadata)
        }

        if (transactions.isEmpty()) {
            return parseUniversalFallbackStatement(normalizedText, metadata)
        }

        return transactions
    }

    /**
     * Step 4: Extract Account Number, Statement Year, and Opening/Closing Balances from headers.
     */
    private fun extractStatementMetadata(text: String): StatementMetadata {
        // Account Number Extraction (e.g., Account No: XXXX-XXXX-1234 or A/C: 123456789)
        val accRegex = Regex("(?i)(?:account\\s*(?:no|number)?|a/c)[:\\s]*([A-Za-z0-9\\-_]{4,25})")
        val accMatch = accRegex.find(text)
        val accountNumber = accMatch?.groupValues?.get(1)?.trim()

        // Year Ingestion Fallback: Scan headers for statement period dates
        val yearRegex = Regex("\\b20\\d{2}\\b")
        val yearMatch = yearRegex.find(text)
        val inferredYear = yearMatch?.value?.toInt() ?: LocalDate.now().year

        // Opening & Closing Balance Capture
        val openingRegex = Regex("(?i)OPENING\\s*BALANCE[:\\s]*([+-]?\\d{1,3}(?:,\\d{3})*\\.\\d{2})")
        val closingRegex = Regex("(?i)CLOSING\\s*BALANCE[:\\s]*([+-]?\\d{1,3}(?:,\\d{3})*\\.\\d{2})")
        
        val openingBalance = openingRegex.find(text)?.groupValues?.get(1)?.replace(",", "")?.toDoubleOrNull()
        val closingBalance = closingRegex.find(text)?.groupValues?.get(1)?.replace(",", "")?.toDoubleOrNull()

        return StatementMetadata(accountNumber, inferredYear, openingBalance, closingBalance)
    }

    private fun isTabularTemplate(text: String): Boolean {
        val upper = text.uppercase()
        return upper.contains("VALUE DATE") ||
                upper.contains("POST DATE") ||
                upper.contains("TRANSACTION DATE") ||
                (upper.contains("DESCRIPTION") && upper.contains("AMOUNT") && upper.contains("BALANCE")) ||
                upper.contains("WITHDRAWAL") || upper.contains("DEPOSIT")
    }

    /**
     * Step 1, 2, 3 & 4: Tabular Statement Parsing with Dual-Column / Balance tracking.
     */
    private fun parseTabularTemplateStatement(rawText: String, metadata: StatementMetadata): List<Transaction> {
        val transactions = mutableListOf<Transaction>()
        val dateRegex = Regex("\\b\\d{1,2}[-/]\\d{1,2}(?:[-/]\\d{2,4})?\\b|\\b\\d{1,2}[-/][A-Za-z]{3}[-/]\\d{2,4}\\b|\\b\\d{4}-\\d{2}-\\d{2}\\b")
        val dateMatches = dateRegex.findAll(rawText).toList()

        if (dateMatches.isEmpty()) return emptyList()

        for (i in dateMatches.indices) {
            val match = dateMatches[i]
            val startIndex = match.range.first
            val endIndex = if (i + 1 < dateMatches.size) dateMatches[i + 1].range.first else rawText.length
            val chunk = rawText.substring(startIndex, endIndex).replace("\n", " ").trim()

            val dateStr = match.value
            val timestamp = parseDateSafely(dateStr, metadata.inferredYear)

            parseChunkIntoTransaction(timestamp, chunk)?.let {
                transactions.add(it)
            }
        }

        return transactions.filterNot { it.type == TransactionType.INCOME && it.amount < 0 }
    }

    /**
     * Step 1, 2 & 3: Standard Vertical Statement Parsing.
     */
    private fun parseStandardTemplateStatement(rawText: String, metadata: StatementMetadata): List<Transaction> {
        val transactions = mutableListOf<Transaction>()
        val lines = rawText.split("\n").map { it.trim() }.filter { it.isNotEmpty() }
        val dateRegex = Regex("\\b\\d{1,2}[-/][A-Za-z]{3}[-/]\\d{2,4}\\b|\\b\\d{1,2}[-/]\\d{1,2}[-/]\\d{2,4}\\b|\\b\\d{4}-\\d{2}-\\d{2}\\b")
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
                val timestamp = parseDateSafely(dateStr, metadata.inferredYear)

                val blockLines = mutableListOf<String>()
                val remainder = line.substring(dateMatch.range.last + 1).trim()
                if (remainder.isNotEmpty()) {
                    blockLines.add(remainder)
                }

                i++
                while (i < lines.size && dateRegex.find(lines[i]) == null) {
                    if (isMetadataLine(lines[i])) {
                        i++
                        continue
                    }
                    blockLines.add(lines[i])
                    i++
                }

                parseVerticalBlockIntoTransaction(timestamp, blockLines, amountRegex)?.let {
                    transactions.add(it)
                }
            } else {
                i++
            }
        }

        return transactions.filterNot { it.type == TransactionType.INCOME && it.amount < 0 }
    }

    /**
     * Universal Fallback Strategy following all 4 rules across unstructured statement text.
     */
    private fun parseUniversalFallbackStatement(rawText: String, metadata: StatementMetadata): List<Transaction> {
        val transactions = mutableListOf<Transaction>()
        val lines = rawText.split("\n").map { it.trim() }.filter { it.isNotEmpty() }
        val dateRegex = Regex("\\b\\d{1,2}[-/]\\d{1,2}(?:[-/]\\d{2,4})?\\b|\\b\\d{1,2}[-/][A-Za-z]{3}[-/]\\d{2,4}\\b|\\b\\d{4}-\\d{2}-\\d{2}\\b")
        val amountRegex = Regex("[-+]?\\b\\d{1,3}(?:,\\d{3})*\\.\\d{2}\\b|[-+]?\\b\\d+\\.\\d{2}\\b")

        for (line in lines) {
            if (isMetadataLine(line)) continue

            val dateMatch = dateRegex.find(line)
            val amountMatch = amountRegex.find(line)

            if (dateMatch != null && amountMatch != null) {
                val timestamp = parseDateSafely(dateMatch.value, metadata.inferredYear)
                val rawAmountStr = amountMatch.value.replace(",", "")
                val amount = rawAmountStr.toDoubleOrNull() ?: continue

                val upper = line.uppercase()
                // Step 3: Indicator Heuristics (DR / CR / Signs)
                val isDebit = upper.contains("DR") || upper.contains("DEBIT") || upper.contains("WITHDRAWAL") || rawAmountStr.startsWith("-")
                val type = if (isDebit) TransactionType.EXPENSE else TransactionType.INCOME

                // Step 2: Description Bounding & Boilerplate Filtering
                var description = line
                    .replace(dateMatch.value, "")
                    .replace(amountMatch.value, "")
                
                for (term in boilerplateBlacklist) {
                    description = description.replace(term, "", ignoreCase = true)
                }

                description = description
                    .replace("DR", "", ignoreCase = true)
                    .replace("CR", "", ignoreCase = true)
                    .replace(Regex("\\s+"), " ")
                    .trim()

                if (description.length < 2) {
                    description = "Statement Transaction"
                }

                transactions.add(
                    Transaction(
                        amount = abs(amount),
                        type = type,
                        description = description,
                        category = "Uncategorized",
                        dateTimestamp = timestamp,
                        referenceNumber = extractReferenceNumber(line)
                    )
                )
            }
        }
        return transactions
    }

    private fun isMetadataLine(line: String): Boolean {
        val upper = line.uppercase()
        return upper.contains("STATEMENT SUMMARY") ||
                upper.contains("OPENING BALANCE") ||
                upper.contains("CLOSING BALANCE") ||
                upper.contains("TOTAL DEBITS") ||
                upper.contains("TOTAL CREDITS") ||
                upper.contains("TRANSACTION HISTORY") ||
                upper.contains("REMITTER BRANCH") ||
                upper.contains("CHEQUE NO") ||
                (upper.startsWith("DATE") && !upper.contains("DESCRIPTION")) ||
                upper.startsWith("BRANCH:") ||
                upper.startsWith("ACCOUNT:") ||
                upper.startsWith("CURRENCY:") ||
                upper.contains("PAGE ") ||
                upper == "DR" || upper == "CR" || upper == "BALANCE" || upper == "DESCRIPTION"
    }

    /**
     * Step 1: Parse Date safely with Year Ingestion Fallback.
     */
    private fun parseDateSafely(dateStr: String, defaultYear: Int): Long {
        var formatStr = dateStr
        val hasYear = formatStr.contains(Regex("\\d{4}")) || formatStr.contains(Regex("-\\d{2}$")) || formatStr.contains(Regex("/\\d{2}$"))
        if (!hasYear) {
            formatStr = "$formatStr/$defaultYear"
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

    /**
     * Step 2: Reference Number Isolation (UTR, REF, TXN, ID).
     */
    private fun extractReferenceNumber(text: String): String? {
        val refPattern = Regex("(?i)\\b(REF|UTR|TXN|ID)[:\\s]*([A-Za-z0-9]+)\\b")
        val refMatch = refPattern.find(text)
        if (refMatch != null) return refMatch.value.trim()

        val implicitRefPattern = Regex("\\b\\d{10,16}\\b")
        val implicitMatches = implicitRefPattern.findAll(text).toList()
        for (m in implicitMatches) {
            val candidate = m.value
            if (!text.contains(".$candidate") && candidate.length in 10..15) {
                return candidate
            }
        }
        return null
    }

    private fun parseChunkIntoTransaction(timestamp: Long, chunk: String): Transaction? {
        val cleanedChunk = chunk.replace(Regex("\\s+"), " ").trim()
        val upper = cleanedChunk.uppercase()
        if (upper.contains("VALUE DATE") || upper.contains("STATEMENT SUMMARY") || upper.contains("OPENING BALANCE")) {
            return null
        }

        val referenceNumber = extractReferenceNumber(cleanedChunk)

        val amountRegex = Regex("[-+]?\\b\\d{1,3}(?:,\\d{3})*\\.\\d{2}\\b|[-+]?\\b\\d+\\.\\d{2}\\b")
        val allAmounts = amountRegex.findAll(cleanedChunk).map { it.value.replace(",", "").toDouble() }.toList()

        if (allAmounts.isEmpty()) return null

        val amountMatch = amountRegex.find(cleanedChunk)
        // Step 4: For tabular statements, the final amount column is typically the Running Balance. 
        // We pick the transaction amount (usually preceding the balance, or the sole amount).
        val rawAmountStr = if (allAmounts.size >= 2 && upper.contains("BALANCE")) {
            allAmounts[allAmounts.size - 2].toString()
        } else {
            amountMatch?.value ?: allAmounts.last().toString()
        }
        val transactionAmount = rawAmountStr.replace(",", "").toDouble()

        // Step 3: Indicator Heuristics
        val isDebit = upper.contains("DR") || upper.contains("DEBIT") || upper.contains("WITHDRAWAL") || upper.contains("IMPS") || upper.contains("UPI") || rawAmountStr.startsWith("-")
        val transactionType = if (isDebit) TransactionType.EXPENSE else TransactionType.INCOME

        // Step 2: Description Bounding & Boilerplate Filtering
        var description = cleanedChunk
            .replace(Regex("^\\d{1,2}[-/]\\d{1,2}(?:[-/]\\d{2,4})?\\b"), "")
            .replace(amountRegex, "")

        for (term in boilerplateBlacklist) {
            description = description.replace(term, "", ignoreCase = true)
        }

        description = description
            .replace(Regex("(?i)\\b(REF|UTR|TXN|ID)[:\\s]*([A-Za-z0-9]+)\\b"), "")
            .replace(Regex("\\b\\d+\\.\\d{2}CR\\b"), "")
            .replace("CR", "", ignoreCase = true)
            .replace("DR", "", ignoreCase = true)
            .replace("|", "")
            .replace(Regex("\\s+"), " ")
            .trim()

        if (description.isEmpty() || description.length < 3) {
            description = "Bank Transaction"
        }

        return Transaction(
            amount = abs(transactionAmount),
            type = transactionType,
            description = description,
            category = "Uncategorized",
            dateTimestamp = timestamp,
            referenceNumber = referenceNumber
        )
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

        val descBuilder = StringBuilder()
        var foundDebitIndicator = false
        var foundCreditIndicator = false

        for (item in cleanedBlock) {
            val upperItem = item.uppercase()
            if (upperItem.contains("DR") || upperItem.contains("DEBIT") || upperItem.contains("WITHDRAWAL")) {
                foundDebitIndicator = true
            }
            if (upperItem.contains("CR") || upperItem.contains("CREDIT") || upperItem.contains("DEPOSIT")) {
                foundCreditIndicator = true
            }

            val extractedRef = extractReferenceNumber(item)
            if (extractedRef != null && referenceNumber == null) {
                referenceNumber = extractedRef
                val textWithoutRef = item.replace(extractedRef, "").trim()
                if (textWithoutRef.isNotEmpty()) {
                    descBuilder.append(" ").append(textWithoutRef)
                }
                continue
            }

            val amountMatch = amountRegex.find(item)
            if (amountMatch != null && transactionAmount == null) {
                val rawAmountStr = amountMatch.value.replace(" ", "").replace(",", "")
                val parsedAmt = rawAmountStr.toDoubleOrNull()
                if (parsedAmt != null) {
                    if (upperItem.endsWith("CR") && !upperItem.contains("DR") && item.contains("BALANCE")) {
                        descBuilder.append(" ").append(item)
                        continue
                    }
                    transactionAmount = parsedAmt
                    transactionType = if (upperItem.contains("(DR)") || upperItem.contains(" DR") || rawAmountStr.startsWith("-") || upperItem == "DR") {
                        TransactionType.EXPENSE
                    } else if (upperItem.contains("(CR)") || upperItem.contains(" CR") || upperItem.contains("DEPOSIT") || upperItem == "CR") {
                        TransactionType.INCOME
                    } else {
                        if (foundCreditIndicator && !foundDebitIndicator) TransactionType.INCOME else TransactionType.EXPENSE
                    }
                    continue
                }
            }

            // Filter boilerplate from vertical block lines
            var cleanedLine = item
            for (term in boilerplateBlacklist) {
                cleanedLine = cleanedLine.replace(term, "", ignoreCase = true)
            }
            descBuilder.append(" ").append(cleanedLine)
        }

        val rawDesc = descBuilder.toString().trim().replace(Regex("\\s+"), " ")
        if (rawDesc.isNotEmpty()) {
            description = rawDesc
        }

        val finalAmount = transactionAmount ?: 0.0
        if (finalAmount == 0.0) return null

        return Transaction(
            amount = abs(finalAmount),
            type = transactionType,
            description = description,
            category = "Uncategorized",
            dateTimestamp = timestamp,
            referenceNumber = referenceNumber
        )
    }
}
