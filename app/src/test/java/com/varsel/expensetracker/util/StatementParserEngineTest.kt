package com.varsel.expensetracker.util

import com.varsel.expensetracker.domain.model.TransactionType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

/**
 * Unit test class for validating bank statement parsing logic within [StatementParserEngine].
 */
class StatementParserEngineTest {

    private val statementParserEngine = StatementParserEngine()

    @Test
    fun testParseStatement() {
        // Sample bank statement structured into vertical blocks matching parser expectations
        val sampleStatement = """
            07/08/2026
            Grocery Store Purchase
            -11000.00
            
            06/08/2026
            Salary Deposit
            +5000.00
        """.trimIndent()

        val parsedTransactions = statementParserEngine.parseStatement(sampleStatement)

        // Ensure the parser successfully extracted transactions
        assertFalse("Parsed transactions list should not be empty", parsedTransactions.isEmpty())

        // Validate the parsed values for the first transaction
        val firstTransaction = parsedTransactions[0]
        
        assertEquals("Grocery Store Purchase", firstTransaction.description)
        assertEquals(11000.00, firstTransaction.amount, 0.01)
        assertEquals(TransactionType.EXPENSE, firstTransaction.type)
    }
}
