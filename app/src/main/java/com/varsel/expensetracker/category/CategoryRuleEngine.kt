package com.varsel.expensetracker.category

import javax.inject.Inject

class CategoryRuleEngine @Inject constructor() {

    private val rules = listOf

        // Food
        KeywordRule("tea", Category.FOOD, 95),
        KeywordRule("coffee", Category.FOOD, 95),
        KeywordRule("breakfast", Category.FOOD, 95),
        KeywordRule("lunch", Category.FOOD, 95),
        KeywordRule("dinner", Category.FOOD, 95),
        KeywordRule("restaurant", Category.FOOD, 90),

        // Groceries
        KeywordRule("grocery", Category.GROCERIES, 95),
        KeywordRule("vegetable", Category.GROCERIES, 90),
        KeywordRule("milk", Category.GROCERIES, 90),

        // Travel
        KeywordRule("train", Category.TRAVEL, 98),
        KeywordRule("ticket", Category.TRAVEL, 90),
        KeywordRule("metro", Category.TRAVEL, 95),
        KeywordRule("bus", Category.TRAVEL, 95),
        KeywordRule("cab", Category.TRAVEL, 95),

        // Fuel
        KeywordRule("petrol", Category.FUEL, 100),
        KeywordRule("diesel", Category.FUEL, 100),
        KeywordRule("fuel", Category.FUEL, 100),

        // Mobile
        KeywordRule("recharge", Category.MOBILE, 100),

        // Bills
        KeywordRule("electricity", Category.BILLS, 95),
        KeywordRule("water", Category.BILLS, 95),
        KeywordRule("gas", Category.BILLS, 95),

        // Healthcare
        KeywordRule("medical", Category.HEALTHCARE, 95),
        KeywordRule("hospital", Category.HEALTHCARE, 100),
        KeywordRule("pharmacy", Category.HEALTHCARE, 100)
    )

    fun categorize(
        description: String
    ): CategoryResult {

val words =
    description
        .lowercase()
        .replace(Regex("[^a-z0-9 ]"), " ")
        .split(Regex("\\s+"))
        .filter { it.isNotBlank() }

val match =
    rules
        .filter { rule ->

            words.any { word ->
                word == rule.keyword
            }

        }
        .maxByOrNull { it.confidence }

        return if (match != null) {

            CategoryResult(
                category = match.category,
                confidence = match.confidence
            )

        } else {

            CategoryResult(
                category = Category.UNCATEGORIZED,
                confidence = 0
            )
        }
    }
}
