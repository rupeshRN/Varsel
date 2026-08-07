package com.varsel.expensetracker.util

import com.varsel.expensetracker.data.local.dao.CustomRuleDao
import com.varsel.expensetracker.domain.model.TransactionType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class SmartCategorizerEngine @Inject constructor(
    private val customRuleDao: CustomRuleDao? = null
) {

    data class SmartDetails(val displayName: String, val category: String)

    suspend fun getSmartDetails(description: String, type: TransactionType): SmartDetails = withContext(Dispatchers.IO) {
        val upperDesc = description.uppercase()

        // 1. Check user-defined custom rules from database safely without blocking threads
        try {
            if (customRuleDao != null) {
                val rules = customRuleDao.getAllRules()
                for (rule in rules) {
                    val keyword = rule.keyword.uppercase()
                    // Use word boundary check to avoid false substring collisions (e.g. "CAR" matching "RESCAR")
                    val regex = Regex("\\b${Regex.escape(keyword)}\\b")
                    if (regex.containsMatchIn(upperDesc)) {
                        return@withContext SmartDetails(rule.displayName, rule.categoryName)
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // 2. Built-in Smart Rules
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
        if (upperDesc.contains("TRANSFER TO") || upperDesc.contains("NEFT") || upperDesc.contains("IMPS")) {
            val recipient = extractRecipientFromTransfer(description)
            return@withContext SmartDetails(recipient, "Transfers")
        }

        // 3. Ultimate Fallback
        val defaultName = description.split("-").firstOrNull()?.trim()?.capitalizeWords() ?: "Transaction"
        return@withContext SmartDetails(defaultName, "Uncategorized")
    }

    private fun extractRecipientFromTransfer(fullDescription: String): String {
        val parts = fullDescription.split("-")
        return if (parts.size > 2) {
            parts[1].capitalizeWords()
        } else {
            "Bank Transfer"
        }
    }

    private fun String.capitalizeWords(): String = 
        split(" ").joinToString(" ") { it.lowercase().replaceFirstChar { char -> char.uppercase() } }
}
