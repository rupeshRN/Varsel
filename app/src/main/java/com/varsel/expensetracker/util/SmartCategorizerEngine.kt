package com.varsel.expensetracker.util

import com.varsel.expensetracker.data.local.dao.CustomRuleDao
import com.varsel.expensetracker.domain.model.TransactionType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import javax.inject.Inject

class SmartCategorizerEngine @Inject constructor(
    private val customRuleDao: CustomRuleDao? = null
) {

    data class SmartDetails(val displayName: String, val category: String)

    suspend fun getSmartDetails(description: String, type: TransactionType): SmartDetails = withContext(Dispatchers.IO) {
        val upperDesc = description.uppercase()

        try {
            if (customRuleDao != null) {
                val rules = try {
                    customRuleDao.getAllRules().first()
                } catch (e: Exception) {
                    @Suppress("UNCHECKED_CAST")
                    customRuleDao.getAllRules() as? List<com.varsel.expensetracker.data.local.entity.CustomRuleEntity> ?: emptyList()
                }

                for (rule in rules) {
                    val keyword = rule.keyword.uppercase()
                    val regex = Regex("\\b${Regex.escape(keyword)}\\b")
                    if (regex.containsMatchIn(upperDesc)) {
                        return@withContext SmartDetails(rule.displayName, rule.categoryName)
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        if (upperDesc.contains("ZOMATO") || upperDesc.contains("SWIGGY") || upperDesc.contains("UBER EATS")) {
            return@withContext SmartDetails("Food Delivery", "Food & Drink")
        }
        if (upperDesc.contains("SALARY") || upperDesc.contains("WAGE")) {
            return@withContext SmartDetails("Salary", "Income")
        }
        if (upperDesc.contains("NETFLIX") || upperDesc.contains("SPOTIFY") || upperDesc.contains("AMAZON PRIME")) {
            return@withContext SmartDetails("Subscription", "Subscriptions")
        }
        if (upperDesc.contains("ATM CASH") || upperDesc.contains("WDL")) {
            return@withContext SmartDetails("Cash Withdrawal", "Cash")
        }
        if (upperDesc.contains("ELECTRICITY") || upperDesc.contains("WATER") || upperDesc.contains("INTERNET")) {
            return@withContext SmartDetails("Utility Bill", "Utilities")
        }

        val defaultName = description.split("-").firstOrNull()?.trim()?.capitalizeWords() ?: "Transaction"
        return@withContext SmartDetails(defaultName, "Uncategorized")
    }

    private fun String.capitalizeWords(): String = 
        split(" ").joinToString(" ") { it.lowercase().replaceFirstChar { char -> char.uppercase() } }
}
