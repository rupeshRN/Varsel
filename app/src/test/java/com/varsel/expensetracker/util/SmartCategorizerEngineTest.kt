package com.varsel.expensetracker.util

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
        // Arrange: Rule maps pattern string to a category name
        val customRules = listOf(
            CustomRuleEntity(pattern = "STARBUCKS", categoryName = "Coffee & Snacks"),
            CustomRuleEntity(pattern = "UBER", categoryName = "Transportation")
        )

        // Act
        val category = categorizerEngine.categorizeTransaction(
            narration = "POS STARBUCKS COFFEE #1204",
            amount = 14.50,
            customRules = customRules
        )

        // Assert
        assertEquals("Coffee & Snacks", category)
    }

    @Test
    fun categorizeTransaction_fallbackKeywordMatch_returnsDefaultCategory() {
        // Arrange
        val customRules = emptyList<CustomRuleEntity>()

        // Act
        val category = categorizerEngine.categorizeTransaction(
            narration = "SWIGGY FOOD ORDER #99831",
            amount = 250.0,
            customRules = customRules
        )

        // Assert
        assertEquals("Food & Dining", category)
    }

    @Test
    fun categorizeTransaction_unmatchedNarration_returnsUncategorized() {
        // Arrange
        val customRules = emptyList<CustomRuleEntity>()

        // Act
        val category = categorizerEngine.categorizeTransaction(
            narration = "TRANSFER REF 981273912",
            amount = 500.0,
            customRules = customRules
        )

        // Assert
        assertNull(category)
    }
}
