package com.varsel.expensetracker.category

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CustomRuleEngine @Inject constructor() {

    //--------------------------------------------------
    // In-memory learned knowledge
    //--------------------------------------------------

    private var cache:
        Map<String, KnowledgeRecord> = emptyMap()

    //--------------------------------------------------
    // Called once before parsing starts
    //--------------------------------------------------

    fun loadCache(

        rules: Map<String, KnowledgeRecord>

    ) {

        cache = rules

    }

    //--------------------------------------------------
    // Lookup learned knowledge
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
        // (Reference numbers, UTRs, IDs, etc.)
        //--------------------------------------------------
        .replace(
            Regex("\\b\\d{5,}\\b"),
            ""
        )

        //--------------------------------------------------
        // Collapse multiple spaces
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
