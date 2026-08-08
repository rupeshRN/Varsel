package com.varsel.expensetracker.util

import com.varsel.expensetracker.domain.model.TransactionType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

/**
 * Unit test class for validating bank statement parsing logic with reference numbers and tabular statements.
 */
class StatementParserEngineTest {

    private val statementParserEngine = StatementParserEngine()

    @Test
    fun testParseStatement() {
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

        assertFalse("Parsed transactions list should not be empty", parsedTransactions.isEmpty())
        assertEquals("Expected 2 parsed transactions", 2, parsedTransactions.size)

        val firstTransaction = parsedTransactions[0]
        assertEquals("Grocery Store Purchase", firstTransaction.description.trim())
        assertEquals(11000.00, firstTransaction.amount, 0.01)
        assertEquals(TransactionType.EXPENSE, firstTransaction.type)
        assertEquals("REF: 987654321", firstTransaction.referenceNumber?.trim())

        val secondTransaction = parsedTransactions[1]
        assertEquals("Salary Deposit", secondTransaction.description.trim())
        assertEquals(5000.00, secondTransaction.amount, 0.01)
        assertEquals(TransactionType.INCOME, secondTransaction.type)
        assertEquals("UTR: 123456ABC", secondTransaction.referenceNumber?.trim())
    }

    @Test
    fun testParseTabularBankStatement() {
        val tabularStatement = """
            Value Date Post Date Remitter Branch Description Cheque No DR CR Balance
            08/05/2023 08/05/2023 ATM SERVICE BRANCH WITHDRAWAL TRANSFER /IMPS COMMISSION CHARGES/312813269279/ TRANSFER TO 88907009396 5.90 14133.70CR
            08/05/2023 08/05/2023 ATM SERVICE BRANCH WITHDRAWAL TRANSFER UPI/349419035689/NA XXXXX/078905004535@ICIC00007 .ifsc.npciICIC0000789/Sandep Singh TRANSFER TO 97215009396 13000.00 1133.70CR
        """.trimIndent()

        val parsedTransactions = statementParserEngine.parseStatement(tabularStatement)

        assertFalse("Parsed tabular transactions list should not be empty", parsedTransactions.isEmpty())
        assertEquals("Expected 2 parsed transactions", 2, parsedTransactions.size)

        val tx1 = parsedTransactions[0]
        assertEquals(5.90, tx1.amount, 0.01)
        assertEquals(TransactionType.EXPENSE, tx1.type)
        assertEquals("312813269279", tx1.referenceNumber)

        val tx2 = parsedTransactions[1]
        assertEquals(13000.00, tx2.amount, 0.01)
        assertEquals(TransactionType.EXPENSE, tx2.type)
        assertEquals("349419035689", tx2.referenceNumber)
    }
}
