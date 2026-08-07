package com.varsel.expensetracker.util

import com.varsel.expensetracker.domain.model.Transaction
import com.varsel.expensetracker.domain.model.TransactionType
import java.text.SimpleDateFormat
import java.util.Locale
import javax.inject.Inject
import kotlin.math.abs

class StatementParserEngine @Inject constructor() {

    fun parseStatement(rawText: String): List<Transaction> {
        val transactions = mutableListOf<Transaction>()
        val lines = rawText.split("\n").map { it.trim() }.filter { it.isNotEmpty() }

        val dateRegex = Regex("\\d{2}-[A-Za-z]{3}-\\d{4}|\\d{2}/\\d{2}/\\d{4}")
        val dateFormat = SimpleDateFormat("dd-MMM-yyyy", Locale.ENGLISH)
        val amountRegex = Regex("[-+]?\\d{1,3}(?:,\\d{3})*(?:\\.\\d{2})|[-+]?\\d+\\.\\d{2}")

        var i = 0
        while (i < lines.size) {
            val line = lines[i]

            // Skip headers, footers, and summary markers
            if (line.startsWith("Opening Balance") || 
                line.startsWith("Statement Summary") || 
                line.startsWith("Total") ||
                line.startsWith("Transaction History") ||
                line.startsWith("Date") ||
                line.startsWith("Branch:") ||
                line.startsWith("Account")) {
                i++
                continue
            }

            val dateMatch = dateRegex.find(line)
            if (dateMatch != null) {
                val dateStr = dateMatch.value
                val timestamp = try {
                    dateFormat.parse(dateStr)?.time ?: System.currentTimeMillis()
                } catch (e: Exception) {
                    System.currentTimeMillis()
                }

                // Format 1: Single-line pipe-delimited or CSV format
                if (line.contains("|") && amountRegex.containsMatchIn(line)) {
                    val parts = line.split("|").map { it.trim() }
                    if (parts.size >= 5) {
                        val description = parts[1]
                        val refNumber = parts.getOrNull(2) ?: ""
                        val withdrawalStr = parts.getOrNull(3)?.replace(",", "") ?: ""
                        val depositStr = parts.getOrNull(4)?.replace(",", "") ?: ""

                        val withdrawal = withdrawalStr.toDoubleOrNull()
                        val deposit = depositStr.toDoubleOrNull()

                        if (withdrawal != null && withdrawal > 0.0) {
                            transactions.add(
                                Transaction(
                                    description = description.ifEmpty { "Transaction" },
                                    amount = withdrawal,
                                    type = TransactionType.EXPENSE,
                                    category = "Uncategorized",
                                    dateTimestamp = timestamp,
                                    referenceNumber = refNumber
                                )
                            )
                        } else if (deposit != null && deposit > 0.0) {
                            transactions.add(
                                Transaction(
                                    description = description.ifEmpty { "Transaction" },
                                    amount = deposit,
                                    type = TransactionType.INCOME,
                                    category = "Uncategorized",
                                    dateTimestamp = timestamp,
                                    referenceNumber = refNumber
                                )
                            )
                        }
                    }
                    i++
                } else {
                    // Format 2: Vertical multi-line block format (PDFBox table extraction)
                    val blockLines = mutableListOf<String>()
                    i++
                    while (i < lines.size && dateRegex.find(lines[i]) == null) {
                        blockLines.add(lines[i])
                        i++
                    }
                    parseVerticalBlock(timestamp, blockLines, transactions, amountRegex)
                }
            } else {
                i++
            }
        }

        // Fallback generic scanner if structured parsing yields nothing
        if (transactions.isEmpty()) {
            for (line in lines) {
                val match = amountRegex.find(line)
                if (match != null) {
                    val amountStr = match.value.replace(",", "")
                    val amount = amountStr.toDoubleOrNull() ?: continue
                    val description = line.replace(match.value, "").trim()
                    if (description.length < 2) continue
                    val type = if (amount < 0) TransactionType.EXPENSE else TransactionType.INCOME
                    transactions.add(
                        Transaction(
                            description = description,
                            amount = abs(amount),
                            type = type,
                            category = "Uncategorized",
                            dateTimestamp = System.currentTimeMillis(),
                            referenceNumber = ""
                        )
                    )
                }
            }
        }

        return transactions
    }

    private fun parseVerticalBlock(
        timestamp: Long,
        blockLines: List<String>,
        transactions: MutableList<Transaction>,
        amountRegex: Regex
    ) {
        val cleaned = blockLines.map { it.replace("|", "").trim() }.filter { it.isNotEmpty() }
        if (cleaned.isEmpty()) return

        var description = ""
        var reference = ""
        val amounts = mutableListOf<Double>()

        for (item in cleaned) {
            val amountMatch = amountRegex.find(item)
            if (amountMatch != null) {
                val amt = amountMatch.value.replace(",", "").toDoubleOrNull()
                if (amt != null) {
                    amounts.add(amt)
                }
            } else {
                if (description.isEmpty()) {
                    description = item
                } else if (reference.isEmpty() && item.all { it.isLetterOrDigit() }) {
                    reference = item
                } else {
                    description += " $item"
                }
            }
        }

        if (amounts.isNotEmpty()) {
            val txnAmount = amounts[0]
            val upperDesc = description.uppercase()
            val isIncome = upperDesc.contains("SALARY") || 
                           upperDesc.contains("INTEREST") || 
                           upperDesc.contains("CR") || 
                           upperDesc.contains("DEPOSIT")

            val type = if (isIncome) TransactionType.INCOME else TransactionType.EXPENSE

            transactions.add(
                Transaction(
                    description = description.ifEmpty { "Transaction" },
                    amount = abs(txnAmount),
                    type = type,
                    category = "Uncategorized",
                    dateTimestamp = timestamp,
                    referenceNumber = reference
                )
            )
            
        }
    }
}
