package com.varsel.expensetracker.category

import javax.inject.Inject
import javax.inject.Singleton

/**
 * Central learning engine responsible for applying user-learned knowledge
 * during statement import.
 *
 * Responsibilities:
 *
 * • Maintains an in-memory cache of all learned rules.
 * • Performs fast lookups without querying Room for every transaction.
 * • Normalizes incoming descriptions before matching.
 * • Returns the learned display description and category when a match exists.
 *
 * Learning Flow
 * ----------------------------------------------------
 *
 * Room Database
 *        │
 *        ▼
 * CustomRuleRepository.loadRuleCache()
 *        │
 *        ▼
 * loadCache()
 *        │
 *        ▼
 * findKnowledge()
 *        │
 *        ├── Exact Match
 *        ├── Longest Contains Match
 *        └── No Match
 *        │
 *        ▼
 * CategoryRuleEngine
 *        │
 *        ▼
 * Import Preview
 *
 * This class intentionally contains only lookup logic.
 *
 * It never:
 * • writes to Room
 * • performs parsing
 * • decides categories itself
 * • performs UI updates
 */
@Singleton
class CustomRuleEngine @Inject constructor() {

    //--------------------------------------------------
    // In-memory learned knowledge
    //
    // Key   = normalized merchant pattern
    // Value = learned description + category
    //--------------------------------------------------

    private var cache:
        Map<String, KnowledgeRecord> = emptyMap()

    //--------------------------------------------------
    // Loads all learned rules once before statement
    // parsing begins.
    //
    // Every lookup afterwards is performed entirely
    // in memory for maximum performance.
    //--------------------------------------------------

    fun loadCache(

        rules: Map<String, KnowledgeRecord>

    ) {

        cache = rules

    }

    //--------------------------------------------------
    // Searches for previously learned knowledge.
    //
    // Matching strategy:
    //
    // 1. Exact normalized match
    // 2. Longest "contains" match
    // 3. No match
    //
    // The longest match prevents generic merchants
    // from overriding more specific learned rules.
    //--------------------------------------------------

    fun findKnowledge(

        description: String

    ): KnowledgeRecord? {

        val normalized =

            normalize(description)

        //--------------------------------------------------
        // Exact match
        //--------------------------------------------------

        cache[normalized]?.let {

            return it

        }

        //--------------------------------------------------
        // Longest contains match
        //--------------------------------------------------

        return cache

            .entries

            .filter {

                normalized.contains(it.key)

            }

            .maxByOrNull {

                it.key.length

            }

            ?.value

    }

    //--------------------------------------------------
    // Converts parser output into a stable lookup key.
    //
    // Parser normalization and Learning normalization
    // intentionally serve different purposes:
    //
    // Parser:
    //     Improve extraction quality.
    //
    // Learning:
    //     Remove noisy tokens so similar descriptions
    //     can match the same learned rule.
    //--------------------------------------------------

    private fun normalize(

        description: String

    ): String {

        return description

            //--------------------------------------------------
            // Ignore case
            //--------------------------------------------------
            .lowercase()

            //--------------------------------------------------
            // Replace punctuation with spaces
            //--------------------------------------------------
            .replace(
                Regex("[^a-z0-9 ]"),
                " "
            )

            //--------------------------------------------------
            // Remove long numeric tokens
            //
            // Examples:
            // • UTR numbers
            // • Reference IDs
            // • Transaction IDs
            //--------------------------------------------------
            .replace(
                Regex("\\b\\d{5,}\\b"),
                ""
            )

            //--------------------------------------------------
            // Collapse repeated spaces
            //--------------------------------------------------
            .replace(
                Regex("\\s+"),
                " "
            )

            //--------------------------------------------------
            // Final cleanup
            //--------------------------------------------------
            .trim()

    }

}
