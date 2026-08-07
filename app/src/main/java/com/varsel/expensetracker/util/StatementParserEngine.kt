package com.varsel.expensetracker.util

import com.varsel.expensetracker.domain.model.TransactionType

/**
 * Data model representing a single transaction parsed from raw statement text or images.
 */
data class ParsedTransaction(
    val description: String,      // Text description of the transaction line item
    val amount: Double,           // Extracted numeric transaction amount
    val type: TransactionType,    // Classified transaction type (CREDIT or DEBIT)
    val referenceNumber: String?, // Optional reference ID if detected
    val timestamp: Long           // Epoch timestamp marking when parsing occurred
)

/**
 * Utility object responsible for parsing raw text strings extracted from bank/expense statements.
 */
object StatementParserEngine {

    /**
     * Parses raw text block line-by-line to identify transactions, amounts, and types.
     * @param rawText The raw text string extracted from the statement file.
     * @return A list of [ParsedTransaction] objects extracted from the text.
     */
    fun parseStatementText(rawText: String): List<ParsedTransaction> {
        // Initialize a mutable list to hold successfully parsed transaction candidates
        val transactions = mutableListOf<ParsedTransaction>()
        
        // Split the raw multi-line input string into individual lines
        val lines = rawText.lines()
        
        // Capture current epoch system time to use as the fallback timestamp
        val currentTime = System.currentTimeMillis()

        // Loop through each line in the text file
        for (line in lines) {
            // Skip execution for empty or blank lines
            if (line.isBlank()) continue
            
            // Convert line to uppercase to reliably check for keywords like CREDIT, CR, or '+'
            val upper = line.uppercase()
            
            // Determine transaction type based on keyword presence
            val type = if (upper.contains("CR") || upper.contains("CREDIT") || upper.contains("+")) {
                TransactionType.CREDIT
            } else {
                TransactionType.DEBIT
            }

            // Define regular expression matching standard decimal amounts (e.g., 15.50)
            val amountRegex = "([0-9]+\\.[0-9]{2})".toRegex()
            
            // Search the current line for a match against the amount pattern
            val match = amountRegex.find(line)
            
            // Proceed only if a valid monetary amount is found in the line
            if (match != null) {
                // Convert matched amount string to a Double value, defaulting to 0.0 if failed
                val amount = match.value.toDoubleOrNull() ?: 0.0
                
                // Construct a new ParsedTransaction item and add it to our list
                transactions.add(
                    ParsedTransaction(
                        description = line.trim(),
                        amount = amount,
                        type = type,
                        referenceNumber = null,
                        timestamp = currentTime
                    )
                )
            }
        }
        
        // Return the final list of compiled transaction items
        return transactions
    }
}
