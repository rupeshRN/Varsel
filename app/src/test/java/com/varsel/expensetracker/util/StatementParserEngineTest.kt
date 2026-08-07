package com.varsel.expensetracker.util

import com.varsel.expensetracker.domain.model.TransactionType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit test suite validating the behavior and accuracy of [StatementParserEngine].
 */
class StatementParserEngineTest {

    /**
     * Test case to verify that valid raw statement text lines correctly parse into expected transaction fields.
     */
    @Test
    fun testParseStatementText_validEntries() {
        // Define a multi-line string simulating raw extracted statement data
        val sampleText = """
            STARBUCKS COFFEE 15.50 DEBIT
            SALARY DEPOSIT 1500.00 CREDIT
        """.trimIndent()

        // Call the parser engine method with our sample text
        val results = StatementParserEngine.parseStatementText(sampleText)

        // Assert that exactly 2 transaction entries were successfully extracted
        assertEquals(2, results.size)
        
        // Retrieve and validate properties of the first parsed item (Debit)
        val first = results[0]
        assertEquals("STARBUCKS COFFEE 15.50 DEBIT", first.description)
        assertEquals(15.50, first.amount, 0.001)
        assertEquals(TransactionType.DEBIT, first.type)

        // Retrieve and validate properties of the second parsed item (Credit)
        val second = results[1]
        assertEquals("SALARY DEPOSIT 1500.00 CREDIT", second.description)
        assertEquals(1500.00, second.amount, 0.001)
        assertEquals(TransactionType.CREDIT, second.type)
    }

    /**
     * Test case to verify that empty input strings yield an empty transaction list without crashing.
     */
    @Test
    fun testParseStatementText_emptyInput() {
        // Pass an empty string to the parser engine
        val results = StatementParserEngine.parseStatementText("")
        
        // Assert that the resulting collection is empty
        assertTrue(results.isEmpty())
    }
}
