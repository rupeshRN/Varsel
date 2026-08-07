package com.varsel.expensetracker.util

import com.varsel.expensetracker.domain.model.TransactionType
import org.junit.Assert.assertEquals
import org.junit.Test

class StatementParserEngineTest {

    private val statementParserEngine = StatementParserEngine()

    @Test
    fun testParseStatement() {
        val sampleText = """
            15/04/2026 Supermarket -45.50 1200.00
            16/04/2026 Employer 2500.00 3700.00
        """.trimIndent()

        val transactions = statementParserEngine.parseStatement(sampleText)

        assertEquals(2, transactions.size)

        val first = transactions[0]
        assertEquals("Supermarket", first.description)
        assertEquals(45.50, first.amount, 0.01)
        assertEquals(TransactionType.EXPENSE, first.type)

        val second = transactions[1]
        assertEquals("Employer", second.description)
        assertEquals(2500.00, second.amount, 0.01)
        assertEquals(TransactionType.INCOME, second.type)
    }
}
