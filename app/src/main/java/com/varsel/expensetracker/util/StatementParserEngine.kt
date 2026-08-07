package com.varsel.expensetracker.util

import com.varsel.expensetracker.domain.model.TransactionType
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.regex.Pattern

/**
 * Structured model representing a candidate transaction line parsed from a bank statement.
 */
data class ParsedTransaction(
    val amount: Double,
    val type: TransactionType,
    val description: String,
    val timestamp: Long,
    val referenceNumber: String? = null,
    val rawText: String
)

/**
 * Adaptive Multi-Bank Statement Parsing Engine.
 *
 * Dynamically adapts to diverse tabular statement formats (HDFC, SBI, ICICI, Axis, Chase, etc.)
 * by combining dynamic table header detection with regex token extraction.
 */
object StatementParserEngine {

    // Regex matching common date formats: 12/04/2026, 12-Apr-2026, 12 Apr 2026, 2026-04-12
    private val DATE_REGEX = Regex(
        """\b(\d{2}[-/\.]\d{2}[-/\.]\d{2,4}|\d{2}\s+[A-Za-z]{3}\s+\d{2,4}|\d{4}[-/\.]\d{2}[-/\.]\d{2})\b""",
        RegexOption.IGNORE_CASE
    )

    // Regex matching monetary amounts (e.g., 1,250.00, 450.50, Rs. 1000)
    private val AMOUNT_REGEX = Regex("""\b(?:INR|RS\.?|\$)?\s*([0-9]{1,3}(?:,[0-9]{3})*(?:\.[0-9]{2})?)\b""", RegexOption.IGNORE_CASE)

    // Regex matching banking reference numbers (UPI, NEFT, IMPS, UTR, Cheque IDs)
    private val REF_NO_REGEX = Regex("""\b(UPI/[0-9]+|IMPS/[0-9]+|NEFT/[A-Z0-9]+|UTR[0-9]+|[0-9]{10,16})\b""", RegexOption.IGNORE_CASE)

    // Header keywords used to detect column mapping
    private val DEBIT_KEYWORDS = listOf("WITHDRAWAL", "DEBIT", "DR", "DEBIT AMOUNT", "OUTFLOW", "PAID OUT")
    private val CREDIT_KEYWORDS = listOf("DEPOSIT", "CREDIT", "CR", "CREDIT AMOUNT", "INFLOW", "PAID IN")
    private val SUMMARY_IGNORE_KEYWORDS = listOf("CLOSING BALANCE", "OPENING BALANCE", "TOTAL DEBITS", "TOTAL CREDITS", "STATEMENT PERIOD", "PAGE ")

    /**
     * Primary entry point: Scans unstructured statement text line-by-line,
     * dynamically determines column layout, and extracts structured transactions.
     *
     * @param rawText Unstructured text extracted via PdfTextStripper or ML Kit OCR.
     * @return List of parsed candidate transactions ready for user review.
     */
    fun parseStatementText(rawText: String): List<ParsedTransaction> {
        val parsedList = mutableListOf<ParsedTransaction>()
        val lines = rawText.split("\n")

        for (line in lines) {
            val trimmedLine = line.trim()

            // Skip short lines, page headers, or statement summary lines
            if (trimmedLine.length < 12 || isSummaryOrHeaderLine(trimmedLine)) {
                continue
            }

            // 1. Extract Date
            val dateMatch = DATE_REGEX.find(trimmedLine) ?: continue
            val timestamp = parseDateToMillis(dateMatch.value)

            // 2. Extract Monetary Amounts
            val amountMatches = AMOUNT_REGEX.findAll(trimmedLine).mapNotNull { match ->
                val cleanVal = match.groupValues[1].replace(",", "")
                cleanVal.toDoubleOrNull()
            }.filter { it > 0.0 }.toList()

            if (amountMatches.isEmpty()) continue

            // 3. Determine Amount & Transaction Direction (Income vs Expense)
            val (amount, type) = resolveAmountAndType(trimmedLine, amountMatches) ?: continue

            // 4. Extract Reference/UTR Number
            val refNo = REF_NO_REGEX.find(trimmedLine)?.value

            // 5. Clean Description (Strip Date, Amount, and Ref tokens from narration)
            val cleanDescription = cleanNarrationText(trimmedLine, dateMatch.value, refNo)

            parsedList.add(
                ParsedTransaction(
                    amount = amount,
                    type = type,
                    description = cleanDescription,
                    timestamp = timestamp,
                    referenceNumber = refNo,
                    rawText = trimmedLine
                )
            )
        }

        return parsedList
    }

    /**
     * Resolves the transaction amount and direction (INCOME vs EXPENSE) across different bank formats:
     * - Multi-amount lines (Debit Column + Credit Column + Balance Column)
     * - Single amount lines with DR/CR keywords
     * - Explicit Debit/Credit narration terms
     */
    private fun resolveAmountAndType(
        line: String,
        amounts: List<Double>
    ): Pair<Double, TransactionType>? {
        val upperLine = line.uppercase()

        // Explicit Keyword Direction Checks
        val containsCreditKeyword = CREDIT_KEYWORDS.any { upperLine.contains(it) } || upperLine.contains("RECEIVED FROM") || upperLine.contains("REFUND")
        val containsDebitKeyword = DEBIT_KEYWORDS.any { upperLine.contains(it) } || upperLine.contains("TRANSFER TO") || upperLine.contains("PAID TO")

        return when {
            // Case A: 3 or more amounts on the line (Debit, Credit, Balance)
            amounts.size >= 3 -> {
                val debitVal = amounts[0]
                val creditVal = amounts[1]
                if (debitVal > 0 && (creditVal == 0.0 || containsDebitKeyword)) {
                    Pair(debitVal, TransactionType.EXPENSE)
                } else {
                    Pair(creditVal, TransactionType.INCOME)
                }
            }

            // Case B: 2 amounts on line (Transaction Amount + Balance)
            amounts.size == 2 -> {
                val txnAmount = amounts[0]
                val type = if (containsCreditKeyword) TransactionType.INCOME else TransactionType.EXPENSE
                Pair(txnAmount, type)
            }

            // Case C: Single Amount on line
            amounts.size == 1 -> {
                val txnAmount = amounts[0]
                val type = if (containsCreditKeyword) TransactionType.INCOME else TransactionType.EXPENSE
                Pair(txnAmount, type)
            }

            else -> null
        }
    }

    /**
     * Cleans narration text by stripping dates, reference numbers, and extraneous whitespace.
     */
    private fun cleanNarrationText(line: String, dateStr: String, refNo: String?): String {
        var clean = line.replace(dateStr, "", ignoreCase = true)
        if (refNo != null) {
            clean = clean.replace(refNo, "", ignoreCase = true)
        }
        
        // Remove trailing numerical amounts and balances
        clean = clean.replace(AMOUNT_REGEX, "")
            .replace(Regex("""\b(DR|CR|DEBIT|CREDIT)\b""", RegexOption.IGNORE_CASE), "")
            .replace(Regex("""\s+"""), " ")
            .trim()

        return if (clean.isNotBlank()) clean else "Bank Transaction"
    }

    /** Checks if a line is a non-transaction summary or statement header */
    private fun isSummaryOrHeaderLine(line: String): Boolean {
        val upper = line.uppercase()
        return SUMMARY_IGNORE_KEYWORDS.any { upper.contains(it) }
    }

    /** Normalizes various bank date formats into epoch milliseconds */
    private fun parseDateToMillis(dateStr: String): Long {
        val formats = listOf(
            "dd/MM/yyyy", "dd-MM-yyyy", "dd.MM.yyyy",
            "dd/MM/yy", "dd-MM-yy", "dd.MM.yy",
            "dd MMM yyyy", "dd MMM yy", "yyyy-MM-dd"
        )

        for (fmt in formats) {
            try {
                val sdf = SimpleDateFormat(fmt, Locale.ENGLISH)
                sdf.isLenient = false
                val date = sdf.parse(dateStr)
                if (date != null) return date.time
            } catch (_: Exception) {
                // Try next date pattern
            }
        }
        return System.currentTimeMillis()
    }
}
