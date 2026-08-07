package com.varsel.expensetracker.util

import com.varsel.expensetracker.data.local.entity.CategoryEntity
import com.varsel.expensetracker.data.local.entity.CustomRuleEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class SmartCategorizerEngineTest {

    private lateinit var categorizerEngine: SmartCategorizerEngine

    @Before
    fun setUp() {
        categorizerEngine = SmartCategorizerEngine()
    }

    @Test
    fun categorizeTransaction_matchesExactCustomRulePattern() {
        val customRules = listOf(
            CustomRuleEntity(pattern = "STARBUCKS", categoryName = "Coffee & Snacks"),
            CustomRuleEntity(pattern = "UBER", categoryName = "Transportation")
        )

        val category = categorizerEngine.categorizeTransaction(
            rawDescription = "POS STARBUCKS COFFEE #1204",
            categories = emptyList(),
            customRules = customRules,
            historicalTransactions = emptyList()
        )

        assertEquals("Coffee & Snacks", category)
    }

    @Test
    fun categorizeTransaction_fallbackKeywordMatch_returnsDefaultCategory() {
        val categories = listOf(
            CategoryEntity(name = "Food & Dining", colorHex = "#FF5722", iconName = "ic_food")
        )

        val category = categorizerEngine.categorizeTransaction(
            rawDescription = "SWIGGY FOOD ORDER #99831",
            categories = categories,
            customRules = emptyList(),
            historicalTransactions = emptyList()
        )

        assertEquals("Food & Dining", category)
    }

    @Test
    fun categorizeTransaction_unmatchedNarration_returnsNull() {
        val category = categorizerEngine.categorizeTransaction(
            rawDescription = "TRANSFER REF 981273912",
            categories = emptyList(),
            customRules = emptyList(),
            historicalTransactions = emptyList()
        )

        assertNull(category)
    }
}
