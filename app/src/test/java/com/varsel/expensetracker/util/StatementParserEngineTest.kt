package com.varsel.expensetracker.util

import com.varsel.expensetracker.domain.model.TransactionType
import org.junit.Assert.assertEquals
import org.junit.Test

class StatementParserEngineTest {

    @Test
    fun testParseStatement() {
        // Sample text matching both vertical PDFBox table extractions and pipe formats
        val sampleText = """
            02-Jul-2026
             | UPI-ZOMATO-ZOM2394@oksbi
             | 6183920192
             | 450.00
             |  | 44,780.00
            05-Jul-2026
             | NEFT-SALARY-TECHMAHIND
             | NEFT98231A
             |  | 75,000.00
             | 119,780.00
        """.trimIndent()

        val engine = StatementParserEngine()
        val transactions = engine.parseStatement(sampleText)

        assertEquals(2, transactions.size)

        // Verify transaction 1
        assertEquals("UPI-ZOMATO-ZOM2394@oksbi", transactions[0].description)
        assertEquals(450.0, transactions[0].amount, 0.01)
        assertEquals(TransactionType.EXPENSE, transactions[0].type)
        assertEquals("6183920192", transactions[0].referenceNumber)

        // Verify transaction 2
        assertEquals("NEFT-SALARY-TECHMAHIND", transactions[1].description)
        assertEquals(75000.0, transactions[1].amount, 0.01)
        assertEquals(TransactionType.INCOME, transactions[1].type)
        assertEquals("NEFT98231A", transactions[1].referenceNumber)
}
}
