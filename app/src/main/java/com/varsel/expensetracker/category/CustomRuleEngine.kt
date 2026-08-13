package com.varsel.expensetracker.category

import com.varsel.expensetracker.data.repository.CustomRuleRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CustomRuleEngine @Inject constructor(

    private val repository: CustomRuleRepository

) {

    //--------------------------------------------------
    // Returns learned category if available
    //--------------------------------------------------

    suspend fun findLearnedCategory(

        description: String

    ): String? {

        val normalized = normalize(description)

        val rule = repository.findRule(normalized)

        return rule?.categoryName
    }

    //--------------------------------------------------
    // Same normalization used for both lookup and save
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
