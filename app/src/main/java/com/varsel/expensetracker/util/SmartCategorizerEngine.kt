package com.varsel.expensetracker.util

import com.varsel.expensetracker.data.local.entity.CategoryEntity
import com.varsel.expensetracker.data.local.entity.CustomRuleEntity
import com.varsel.expensetracker.data.local.entity.TransactionEntity
import javax.inject.Inject
import javax.inject.Singleton

/**
 * On-Device Smart Categorization Engine.
 * 
 * Operates 100% offline using a 3-Tier fallback hierarchy:
 *  - Tier 1: Custom User Memory Rules (Highest Priority - derived from past manual corrections)
 *  - Tier 2: Dynamic Database Keywords (Stored inside CategoryEntity in Room DB)
 *  - Tier 3: Local Word-Frequency Statistical Classifier (Analyzes past user transaction history)
 */
@Singleton
class SmartCategorizerEngine @Inject constructor() {

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
        val cleanDesc = rawDescription.uppercase().trim()
        if (cleanDesc.isBlank()) return "Uncategorized"

        // ----------------------------------------------------------------------------------
        // TIER 1: Explicit Custom Rules (User Overrides)
        // Checks if the user manually reclassified a merchant in the past (e.g., "JOES SHOP").
        // ----------------------------------------------------------------------------------
        for (rule in customRules) {
            val pattern = rule.pattern.uppercase().trim()
            if (pattern.isNotBlank() && cleanDesc.contains(pattern)) {
                return rule.categoryName
            }
        }

        // ----------------------------------------------------------------------------------
        // TIER 2: Dynamic Database Keywords with Word-Boundary Match
        // Prevents partial word false positives (e.g., 'CAR' matching 'STARBUCKS').
        // ----------------------------------------------------------------------------------
        for (category in categories) {
            if (category.keywords.isNotBlank()) {
                val keywordList = category.keywords.split(",")
                    .map { it.trim().uppercase() }
                    .filter { it.isNotBlank() }

                for (keyword in keywordList) {
                    // Use word boundary regex to avoid partial word matching
                    val wordBoundaryRegex = Regex("""\b${Regex.escape(keyword)}\b""", RegexOption.IGNORE_CASE)
                    if (wordBoundaryRegex.containsMatchIn(cleanDesc)) {
                        return category.name
                    }
                }
            }
        }

        // ----------------------------------------------------------------------------------
        // TIER 3: Local Offline Word-Probability Classifier
        // Tokenizes narration using spaces & delimiters (/ - * . #) to match historical words.
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
     * Tokenizes transaction descriptions into distinct terms across common bank delimiters
     * and evaluates historical category co-occurrence frequencies.
     */
    private fun predictFromHistory(
        cleanDescription: String,
        historicalTransactions: List<TransactionEntity>
    ): String? {
        if (historicalTransactions.isEmpty()) return null

        // Split on whitespace and common banking delimiters: /, -, *, #, ., _, commas
        val delimiterRegex = Regex("""[\s/\\-*#,_.]+""")
        val words = cleanDescription.split(delimiterRegex).filter { it.length > 2 }
        if (words.isEmpty()) return null

        val categoryScores = mutableMapOf<String, Double>()

        for (tx in historicalTransactions) {
            // Ignore uncategorized history to prevent feedback loops
            if (tx.categoryName.equals("Uncategorized", ignoreCase = true)) continue

            val txWords = tx.description.uppercase().split(delimiterRegex).toSet()
            var matchCount = 0

            // Count matching word tokens between current and historical description
            for (word in words) {
                if (txWords.contains(word)) {
                    matchCount++
                }
            }

            if (matchCount > 0) {
                val currentScore = categoryScores.getOrDefault(tx.categoryName, 0.0)
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
