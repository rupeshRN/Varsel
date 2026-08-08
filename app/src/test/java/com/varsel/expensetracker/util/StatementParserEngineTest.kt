package com.varsel.expensetracker.util

import com.varsel.expensetracker.domain.model.TransactionType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class StatementParserEngineTest {

    private lateinit var parserEngine: StatementParserEngine

    @Before
    fun setUp() {
        parserEngine = StatementParserEngine()
    }

    @Test
    fun testParseTabularTemplateStatement() {
        val rawStatement = """
            STATEMENT SUMMARY
            VALUE DATE POST DATE DESCRIPTION REF NO AMOUNT
            07/08/2026 Grocery Store Purchase REF: 987654321 -11000.00
        """.trimIndent()

        val transactions = parserEngine.parseStatement(rawStatement)

        assertEquals(1, transactions.size)
        val txn = transactions[0]
        assertEquals("Grocery Store Purchase", txn.description)
        assertEquals(11000.0, txn.amount, 0.01)
        assertEquals(TransactionType.EXPENSE, txn.type)
        assertEquals("REF: 987654321", txn.referenceNumber)
    }

    @Test
    fun testParseStandardTemplateStatement() {
        val rawStatement = """
            Account Statement
            08/Aug/2026
            Online Subscription Payment
            UTR: UTR123456789
            -500.00 DR
        """.trimIndent()

        val transactions = parserEngine.parseStatement(rawStatement)

        assertEquals(1, transactions.size)
        val txn = transactions[0]
        assertTrue(txn.description.contains("Online Subscription Payment"))
        assertEquals(500.0, txn.amount, 0.01)
        assertEquals(TransactionType.EXPENSE, txn.type)
    }

    @Test
    fun testParseStatementWithMetadataSkipping() {
        val rawStatement = """
            OPENING BALANCE: 50,000.00 CR
            01/01/2026
            Coffee Shop
            5.90 DR
            CLOSING BALANCE: 49,944.10 CR
        """.trimIndent()

        val transactions = parserEngine.parseStatement(rawStatement)

        assertEquals(1, transactions.size)
        assertEquals("Coffee Shop", transactions[0].description)
        assertEquals(5.90, transactions[0].amount, 0.01)
        assertEquals(TransactionType.EXPENSE, transactions[0].type)
    }
}
