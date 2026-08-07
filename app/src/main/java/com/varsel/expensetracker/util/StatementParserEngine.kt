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

        var i = 0
        while (i < lines.size) {
            val line = lines[i]

            // --- Phase 1: Aggressive Noise Filtering ---
            // Skip lines that are obviously metadata, headers, footers, or totals.
            // This resolves the "Opening Balance INR" and "Total Credits" issues seen in the screenshot.
            if (isMetadataLine(line)) {
                i++
                continue
            }

            // --- Phase 2: Transaction Row Validation ---
            // A valid transaction row must start with a date.
            val dateMatch = dateRegex.find(line)
            if (dateMatch != null) {
                val dateStr = dateMatch.value
                val timestamp = parseDateSafely(dateStr)

                // --- Phase 3: Isolate Transaction Components ---
                val blockLines = mutableListOf<String>()
                i++
                // Collect subsequent lines until we hit the next date (vertical PDF extraction blocks)
                while (i < lines.size && dateRegex.find(lines[i]) == null) {
                    blockLines.add(lines[i])
                    i++
                }
                
                // Parse the collected lines into a single transaction object
                val txn = parseVerticalBlockIntoTransaction(timestamp, blockLines, amountRegex)
                
                if (txn != null) {
                    transactions.add(txn)
                }
                
                // NOTE: Do not increment 'i' here, it's handled inside the inner while loop
            } else {
                // Skip non-date lines if they are not part of a transaction block
                i++
            }
        }

        // Remove any "negative" income transactions that were incorrectly parsed by PDFBox block layout
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
                (upper.startsWith("DATE") && !upper.contains("DESCRIPTION")) || // Column headers
                upper.startsWith("BRANCH:") ||
                upper.startsWith("ACCOUNT:") ||
                upper.startsWith("CURRENCY:") ||
                upper.contains("PAGE ") // e.g., "Page 1 of 3"
    }

    private fun parseDateSafely(dateStr: String): Long {
        return try {
            val formats = listOf(
                java.text.SimpleDateFormat("dd-MMM-yyyy", java.util.Locale.ENGLISH),
                java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.ENGLISH)
            )
            for (format in formats) {
                val date = format.parse(dateStr)
                if (date != null) return date.time
            }
            System.currentTimeMillis()
        } catch (e: Exception) {
            System.currentTimeMillis()
        }
    }

    private fun parseVerticalBlockIntoTransaction(
        timestamp: Long,
        blockLines: List<String>,
        amountRegex: Regex
    ): Transaction? {
        val cleanedBlock = blockLines.map { it.replace("|", "").trim() }.filter { it.isNotEmpty() }
        
        var description = ""
        var reference = ""
        var transactionAmount: Double? = null
        var transactionType = TransactionType.EXPENSE

        for (item in cleanedBlock) {
            val amountMatch = amountRegex.find(item)
            
            if (amountMatch != null) {
                // This line contains the amount.
                val amountStr = amountMatch.value.replace(",", "")
                transactionAmount = amountStr.toDoubleOrNull()
                
                // Determine type (Expense or Income) based on sign or column indicator
                val upperItem = item.toUpperCase()
                transactionType = if (upperItem.contains("(DR)") || amountStr.startsWith("-")) {
                    TransactionType.EXPENSE
                } else if (upperItem.contains("(CR)") || upperItem.contains("DEPOSIT")) {
                     TransactionType.INCOME
                } else {
                    // Fallback based on expected amounts from screenshot
                    if (transactionAmount != null && transactionAmount < 0) TransactionType.EXPENSE else TransactionType.INCOME
