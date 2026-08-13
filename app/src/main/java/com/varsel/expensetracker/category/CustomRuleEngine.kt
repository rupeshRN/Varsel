package com.varsel.expensetracker.category

import com.varsel.expensetracker.data.repository.CustomRuleRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CustomRuleEngine @Inject constructor() {

    private var cache: Map<String, String> = emptyMap()

    //--------------------------------------------------
    // Called once before parsing starts
    //--------------------------------------------------

    fun loadCache(

        rules: Map<String, String>

    ) {

        cache = rules

    }

    //--------------------------------------------------
    // Lookup
    //--------------------------------------------------

    fun findLearnedCategory(

        description: String

    ): String? {

        return cache[normalize(description)]

    }

    //--------------------------------------------------

    private fun normalize(

        description: String

    ): String {

        return description
            .lowercase()
            .trim()
            .replace(Regex("\\s+"), " ")

    }

}
