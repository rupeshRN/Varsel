package com.varsel.expensetracker.util

import com.varsel.expensetracker.domain.model.TransactionType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Test

/**
 * Unit test class for validating bank statement parsing logic with reference numbers.
 */
class StatementParserEngineTest {

    private val statementParserEngine = StatementParserEngine()

    @Test
    fun testParseStatement() {
        // Sample statement separating description, reference numbers, and amounts
        val sampleStatement = """
            07/08/2026
            Grocery Store Purchase
            REF: 987654321
            -11000.00
            
            06/08/2026
            Salary Deposit
            UTR: 123456ABC
            +5000.00
        """.trimIndent()

        val parsedTransactions = statementParserEngine.parseStatement(sampleStatement)

        // Ensure parser successfully extracted transactions
        assertFalse("Parsed transactions list should not be empty", parsedTransactions.isEmpty())
        assertEquals("Expected 2 parsed transactions", 2, parsedTransactions.size)

        // Validate the first transaction description, amount, and extracted reference number
        val firstTransaction = parsedTransactions[0]
        assertEquals("Grocery Store Purchase", firstTransaction.description)
        assertEquals(11000.00, firstTransaction.amount, 0.01)
        assertEquals(TransactionType.EXPENSE, firstTransaction.type)
        assertEquals("REF: 987654321", firstTransaction.referenceNumber)

        // Validate the second transaction
        val secondTransaction = parsedTransactions[1]
        assertEquals("Salary Deposit", secondTransaction.description)
        assertEquals(5000.00, secondTransaction.amount, 0.01)
        assertEquals(TransactionType.INCOME, secondTransaction.type)
        assertEquals("UTR: 123456ABC", secondTransaction.referenceNumber)
    }
}
