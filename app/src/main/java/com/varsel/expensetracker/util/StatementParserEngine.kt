package com.varsel.expensetracker.util // Defines the package where this parsing utility class resides

import com.varsel.expensetracker.data.local.entity.TransactionEntity // Imports the database entity model used to store transaction records

class StatementParserEngine { // Declares the core utility class responsible for turning raw text files into structured transaction objects

    // Defines a strict regular expression pattern to isolate date, description text, amount, and balance columns independently
    private val transactionRegex = Regex(
        pattern = """^(\d{2}/\d{2}/\d{4})\s+(.+?)\s+([-+]?[0-9,]+\.\d{2})\s+([0-9,]+\.\d{2})$"""
    )

    /**
     * Parses raw multi-line text extracted from a statement PDF and converts it into structured transactions.
     */
    fun parseStatement(extractedText: String): List<TransactionEntity> { // Public function entry point accepting raw text and returning a list of entities
        val transactions = mutableListOf<TransactionEntity>() // Initializes an empty mutable list to collect successfully parsed transaction items
        val lines = extractedText.lines() // Splits the raw string document into a collection of individual text lines

        for (line in lines) { // Begins a loop to iterate through every line found in the extracted document text
            val trimmedLine = line.trim() // Strips any leading or trailing whitespace from the current line
            val matchResult = transactionRegex.find(trimmedLine) // Attempts to evaluate the line against our defined regex pattern

            if (matchResult != null) { // Conditional check that executes only if the line successfully matches a transaction row format
                val dateStr = matchResult.groups[1]?.value ?: continue // Extracts Group 1 (Date value) or skips the line if it is missing
                val rawDescription = matchResult.groups[2]?.value?.trim() ?: "Unknown" // Extracts Group 2 (Merchant/Description text) non-greedily, avoiding number spillover
                val amountStr = matchResult.groups[3]?.value ?: "0.00" // Extracts Group 3 (Transaction amount string)
                val balanceStr = matchResult.groups[4]?.value ?: "0.00" // Extracts Group 4 (Running balance string)

                val cleanDescription = rawDescription.replace(Regex("\\s+"), " ") // Cleans up inner whitespace gaps inside the description text

                val parsedAmount = amountStr.replace(",", "").toDoubleOrNull() ?: 0.0 // Strips commas and safely parses the amount string into a Double value
                val parsedBalance = balanceStr.replace(",", "").toDoubleOrNull() ?: 0.0 // Strips commas and safely parses the balance string into a Double value

                val transactionEntity = TransactionEntity( // Instantiates a new TransactionEntity database record using the parsed data fields
                    description = cleanDescription, // Assigns the isolated text description
                    amount = parsedAmount // Assigns the parsed numerical transaction amount
                )

                transactions.add(transactionEntity) // Appends the constructed transaction record into our results list
            } // Closes the match validation conditional block
        } // Closes the line iteration loop

        return transactions // Returns the final list of cleanly parsed transaction entities back to the caller
    } // Closes the parseStatement function block
} // Closes the StatementParserEngine 
class block
