package com.varsel.expensetracker.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class StatementParserEngineTest {

    private lateinit var statementParserEngine: StatementParserEngine

    @Before
    fun setUp() {
        statementParserEngine = StatementParserEngine()
    }

    @Test
    fun parseStatementText_validBankStatementLines_returnsParsedTransactions() {
        val rawText = """
            01/04/2026 UBER TRIP SAN FRANCISCO CA - 24.50 DR REF: UB123456
            02/04/2026 SALARY CREDIT COMPANY INC + 3500.00 CR REF: SAL78901
        """.trimIndent()

        val transactions = statementParserEngine.parseStatementText(rawText)

        assertEquals(2, transactions.size)

        val debitTransaction = transactions[0]
        assertEquals("UBER TRIP SAN FRANCISCO CA", debitTransaction.description)
        assertEquals(24.50, debitTransaction.amount, 0.001)
        assertEquals("DEBIT", debitTransaction.type)
        assertEquals("UB123456", debitTransaction.referenceNumber)

        val creditTransaction = transactions[1]
        assertEquals("SALARY CREDIT COMPANY INC", creditTransaction.description)
        assertEquals(3500.00, creditTransaction.amount, 0.001)
        assertEquals("CREDIT", creditTransaction.type)
        assertEquals("SAL78901", creditTransaction.referenceNumber)
    }

    @Test
    fun parseStatementText_emptyOrUnrecognizableText_returnsEmptyList() {
        val invalidText = "THIS IS NOT A VALID BANK STATEMENT SUMMARY PAGE"
        val result = statementParserEngine.parseStatementText(invalidText)

        assertTrue(result.isEmpty())
    }
}
