package com.varsel.expensetracker.util

import com.varsel.expensetracker.domain.model.TransactionType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Unit tests verifying offline bank statement text extraction logic.
 */
class StatementParserEngineTest {

    private lateinit var parserEngine: StatementParserEngine

    @Before
    fun setUp() {
        parserEngine = StatementParserEngine()
    }

    @Test
    fun `parseText extracts debit transaction correctly`() {
        // Sample raw bank statement line item
        val rawStatementText = """
            05/08/2026 UPI/SWIGGY BANGALORE/12345/DR 450.00
        """.trimIndent()

        val results = parserEngine.parseStatementText(rawStatementText)

        assertEquals(1, results.size)
        val transaction = results.first()

        assertEquals(450.00, transaction.amount, 0.001)
        assertEquals(TransactionType.EXPENSE, transaction.type)
        assertTrue(transaction.description.contains("SWIGGY BANGALORE", ignoreCase = true))
    }

    @Test
    fun `parseText extracts credit transaction correctly`() {
        // Sample credit salary line item
        val rawStatementText = """
            01-08-2026 ACH/ACME CORP SALARY CREDIT/998877/CR 75,000.00
        """.trimIndent()

        val results = parserEngine.parseStatementText(rawStatementText)

        assertEquals(1, results.size)
        val transaction = results.first()

        assertEquals(75000.00, transaction.amount, 0.001)
        assertEquals(TransactionType.INCOME, transaction.type)
        assertTrue(transaction.description.contains("SALARY CREDIT", ignoreCase = true))
    }

    @Test
    fun `parseText ignores non-transaction noise lines`() {
        // Bank header and footer metadata noise
        val rawStatementText = """
            ACCOUNT STATEMENT FOR PERIOD 01-AUG-2026 TO 07-AUG-2026
            Page 1 of 3
            Opening Balance: $10,000.00
            -------------------------------------------------------
            03/08/2026 GROCERY STORE PURCHASE - DR 120.50
            -------------------------------------------------------
            Total Expenses: $120.50
        """.trimIndent()

        val results = parserEngine.parseStatementText(rawStatementText)

        // Should extract only the valid financial line item
        assertEquals(1, results.size)
        assertEquals(120.50, results.first().amount, 0.001)
    }

    @Test
    fun `parseText handles messy whitespaces and formatting`() {
        val rawStatementText = "  12/08/2026    COFFEE SHOP   $  15.50   DR  "

        val results = parserEngine.parseStatementText(rawStatementText)

        assertEquals(1, results.size)
        assertEquals(15.50, results.first().amount, 0.001)
        assertEquals(TransactionType.EXPENSE, results.first().type)
    }

    @Test
    fun `parseText returns empty list when text contains no valid transactions`() {
        val rawStatementText = "This is a random text document with no dates or monetary amounts."

        val results = parserEngine.parseStatementText(rawStatementText)

        assertTrue(results.isEmpty())
    }
}
