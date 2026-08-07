package com.varsel.expensetracker.util

import com.varsel.expensetracker.data.local.entity.CustomRuleEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for merchant categorization and custom rule override priority.
 */
class SmartCategorizerEngineTest {

    private lateinit var categorizerEngine: SmartCategorizerEngine

    @Before
    fun setUp() {
        categorizerEngine = SmartCategorizerEngine()
    }

    @Test
    fun `categorize matching default keyword returns expected category ID`() {
        val description = "POS Purchase at ZOMATO Bangalore"
        
        // Assuming default rule maps 'ZOMATO' to Category ID 1 (Food & Dining)
        val categoryId = categorizerEngine.categorize(
            description = description,
            customRules = emptyList()
        )

        assertEquals(1L, categoryId)
    }

    @Test
    fun `categorize matching is case-insensitive`() {
        val lowerCaseDescription = "swiggy food order"
        val upperCaseDescription = "SWIGGY FOOD ORDER"

        val lowerResult = categorizerEngine.categorize(lowerCaseDescription, emptyList())
        val upperResult = categorizerEngine.categorize(upperCaseDescription, emptyList())

        assertEquals(lowerResult, upperResult)
    }

    @Test
    fun `custom rules override default keyword rules`() {
        val description = "UBER RIDE SAN FRANCISCO"

        // Default categorizer maps UBER -> Transport (e.g. ID 3)
        // Custom rule maps UBER -> Business Expenses (e.g. ID 10)
        val customRules = listOf(
            CustomRuleEntity(id = 1, merchantPattern = "UBER", categoryId = 10L)
        )

        val categoryId = categorizerEngine.categorize(
            description = description,
            customRules = customRules
        )

        // Custom rule must take priority over default rules
        assertEquals(10L, categoryId)
    }

    @Test
    fun `categorize returns null for unknown merchant with no matching rules`() {
        val description = "UNKNOWN LOCAL SHOP 9982"

        val categoryId = categorizerEngine.categorize(
            description = description,
            customRules = emptyList()
        )

        assertNull(categoryId)
    }
}
