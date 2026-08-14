package com.varsel.expensetracker.category

/**
 * Represents everything Varsel has learned about a merchant.
 *
 * One normalized merchant pattern maps to one knowledge record.
 */
data class KnowledgeRecord(

    /**
     * Description shown to the user.
     * Example:
     * "Netflix"
     */
    val displayDescription: String,

    /**
     * Learned category.
     * Example:
     * Category.ENTERTAINMENT
     */
    val categoryName: String

)
