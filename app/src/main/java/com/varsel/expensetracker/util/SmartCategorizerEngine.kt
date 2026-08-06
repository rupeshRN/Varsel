package com.varsel.expensetracker.util

import com.varsel.expensetracker.data.local.entity.CategoryEntity
import com.varsel.expensetracker.data.local.entity.CustomRuleEntity
import com.varsel.expensetracker.data.local.entity.TransactionEntity

/**
 * On-Device Smart Categorization Engine.
 * 
 * Operates 100% offline using a 3-Tier fallback hierarchy:
 *  - Tier 1: Custom User Memory Rules (Highest Priority - derived from past manual corrections)
 *  - Tier 2: Dynamic Database Keywords (Stored inside CategoryEntity in Room DB)
 *  - Tier 3: Local Word-Frequency Statistical Classifier (Analyzes past user transaction history)
 */
object SmartCategorizerEngine {

    /**
     * Categorizes a transaction description by evaluating it sequentially against Tier 1, Tier 2, and Tier 3.
     * 
     * @param rawDescription The transaction string extracted from OCR, PDF, or manual input.
     * @param categories List of category records fetched from Room database.
     * @param customRules List of explicit merchant rules created from past user edits.
     * @param historicalTransactions Historical transaction entries used for statistical word matching.
     * @return The matched category name string, or "Uncategorized" if no match criteria are met.
     */
    fun categorizeTransaction(
        rawDescription: String,
        categories: List<CategoryEntity>,
        customRules: List<CustomRuleEntity>,
        historicalTransactions: List<TransactionEntity>
    ): String {
        // Sanitize string to uppercase for case-insensitive matching
        val cleanDesc = rawDescription.uppercase().trim()

        // ----------------------------------------------------------------------------------
        // TIER 1: Explicit Custom Rules (User Overrides)
        // Checks if the user manually reclassified a merchant in the past (e.g., "JOES SHOP").
        // ----------------------------------------------------------------------------------
        for (rule in customRules) {
            if (cleanDesc.contains(rule.pattern.uppercase())) {
                return rule.categoryName
            }
        }

        // ----------------------------------------------------------------------------------
        // TIER 2: Dynamic Database Keywords
        // Scans the dynamic comma-separated keywords stored in SQLite for each category.
        // ----------------------------------------------------------------------------------
        for (category in categories) {
            if (category.keywords.isNotBlank()) {
                val keywordList = category.keywords.split(",").map { it.trim().uppercase() }
                if (keywordList.any { keyword -> keyword.isNotEmpty() && cleanDesc.contains(keyword) }) {
                    return category.name
                }
            }
        }

        // ----------------------------------------------------------------------------------
        // TIER 3: Local Offline Word-Probability Classifier
        // Analyzes word co-occurrence frequencies across historical user transactions.
        // ----------------------------------------------------------------------------------
        val predictedCategory = predictFromHistory(cleanDesc, historicalTransactions)
        if (predictedCategory != null) {
            return predictedCategory
        }

        // Fallback default when no rules or patterns match
        return "Uncategorized"
    }

    /**
     * Lightweight, pure-Kotlin statistical classifier.
     * Tokenizes the transaction description into distinct terms and evaluates how frequently
     * those terms have been associated with specific categories in past user data.
     */
    private fun predictFromHistory(
        cleanDescription: String,
        historicalTransactions: List<TransactionEntity>
    ): String? {
        if (historicalTransactions.isEmpty()) return null

        // Tokenize description into clean words (filter out small noise words under 3 chars)
        val words = cleanDescription.split("\\s+".toRegex()).filter { it.length > 2 }
        if (words.isEmpty()) return null

        // Map to aggregate candidate category scores
        val categoryScores = mutableMapOf<String, Double>()

        for (tx in historicalTransactions) {
            // Ignore uncategorized history to prevent feedback loops
            if (tx.categoryName == "Uncategorized") continue

            val txWords = tx.description.uppercase().split("\\s+".toRegex())
            var matchCount = 0

            // Count matching word tokens between current and historical description
            for (word in words) {
                if (txWords.contains(word)) {
                    matchCount++
                }
            }

            if (matchCount > 0) {
                val currentScore = categoryScores.getOrDefault(tx.categoryName, 0.0)
                // Assign weighted score bonus for multiple word matches
                categoryScores[tx.categoryName] = currentScore + (matchCount * 1.5)
            }
        }

        // Find category with highest cumulative match score
        val bestMatch = categoryScores.maxByOrNull { it.value }

        // Require a minimum confidence score threshold of 1.5 to return prediction
        return if (bestMatch != null && bestMatch.value >= 1.5) {
            bestMatch.key
        } else {
            null
  
        }
    }
}
