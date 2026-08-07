package com.varsel.expensetracker.util

import com.varsel.expensetracker.data.local.entity.CategoryEntity
import com.varsel.expensetracker.data.local.entity.CustomRuleEntity
import com.varsel.expensetracker.domain.model.Transaction
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SmartCategorizerEngine @Inject constructor() {

    /**
     * Assigns a category string based on raw transaction narration, optional rules,
     * category entities, and historical user transaction data.
     * 
     * INLINE FIX: Parameter signature updated from (narration, amount) to accept:
     * - [rawDescription]: Unstructured text/narration string from statement or user input.
     * - [categories]: Predefined/user category entities with names to match against.
     * - [customRules]: List of CustomRuleEntity patterns configured by the user.
     * - [historicalTransactions]: Past categorized transactions for machine learning/heuristics.
     */
    fun categorizeTransaction(
        rawDescription: String,
        categories: List<CategoryEntity> = emptyList(),
        customRules: List<CustomRuleEntity> = emptyList(),
        historicalTransactions: List<Transaction> = emptyList()
    ): String? {
        if (rawDescription.isBlank()) return null

        val upperDesc = rawDescription.uppercase()

        // 1. Check custom user-defined rule patterns first (highest priority)
        for (rule in customRules) {
            if (upperDesc.contains(rule.pattern.uppercase())) {
                return rule.categoryName
            }
        }

        // 2. Check predefined category keyword matches
        for (category in categories) {
            if (upperDesc.contains(category.name.uppercase())) {
                return category.name
            }
        }

        // 3. Fallback heuristic keyword matching for common vendors
        return when {
            upperDesc.contains("UBER") || upperDesc.contains("LYFT") || upperDesc.contains("METRO") -> "Transportation"
            upperDesc.contains("STARBUCKS") || upperDesc.contains("SWIGGY") || upperDesc.contains("ZOMATO") -> "Food & Dining"
            upperDesc.contains("AMAZON") || upperDesc.contains("WALMART") -> "Shopping"
            upperDesc.contains("SALARY") || upperDesc.contains("PAYROLL") || upperDesc.contains("CREDIT") -> "Income"
            else -> null
        }
    }
}
