package com.varsel.expensetracker.util

import com.varsel.expensetracker.domain.model.TransactionType
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class SmartCategorizerEngineTest {

    private val smartCategorizerEngine = SmartCategorizerEngine(customRuleDao = null)

    @Test
    fun testGetSmartDetails_foodDelivery() = runTest {
        val result = smartCategorizerEngine.getSmartDetails(
            description = "Zomato Order #1234",
            type = TransactionType.EXPENSE
        )
        assertEquals("Food Delivery", result.displayName)
        assertEquals("Food & Drink", result.category)
    }

    @Test
    fun testGetSmartDetails_salary() = runTest {
        val result = smartCategorizerEngine.getSmartDetails(
            description = "Monthly Salary Credit",
            type = TransactionType.INCOME
        )
        assertEquals("Salary", result.displayName)
        assertEquals("Income", result.category)
    }

    @Test
    fun testGetSmartDetails_subscription() = runTest {
        val result = smartCategorizerEngine.getSmartDetails(
            description = "Netflix Monthly Subscription",
            type = TransactionType.EXPENSE
        )
        assertEquals("Subscription", result.displayName)
        assertEquals("Subscriptions", result.category)
    }

    @Test
    fun testGetSmartDetails_utility() = runTest {
        val result = smartCategorizerEngine.getSmartDetails(
            description = "Electricity Bill Payment",
            type = TransactionType.EXPENSE
        )
        assertEquals("Utility Bill", result.displayName)
        assertEquals("Utilities", result.category)
    }

    @Test
    fun testGetSmartDetails_fallback() = runTest {
        val result = smartCategorizerEngine.getSmartDetails(
            description = "Random Local Store",
            type = TransactionType.EXPENSE
        )
        assertEquals("Random Local Store", result.displayName)
        assertEquals("Uncategorized", result.category)
    }
}
