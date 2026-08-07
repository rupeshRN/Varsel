package com.varsel.expensetracker.util

import com.varsel.expensetracker.domain.model.Transaction
import com.varsel.expensetracker.domain.model.TransactionType
import javax.inject.Inject

class SmartCategorizerEngine @Inject constructor() {

    data class SmartDetails(val displayName: String, val category: String)

    fun getSmartDetails(description: String, type: TransactionType): SmartDetails {
        val upperDesc = description.uppercase()

        // 1. Check for Income
        if (upperDesc.contains("SALARY") || upperDesc.contains("WAGE")) {
            return SmartDetails("Salary", "Income")
        }
        if (upperDesc.contains("INTEREST") || upperDesc.contains("DIVIDEND")) {
            return SmartDetails("Bank Interest", "Income")
        }

        // 2. Check for Food & Dining
        if (upperDesc.contains("ZOMATO") || upperDesc.contains("SWIGGY") || upperDesc.contains("UBER EATS")) {
            return SmartDetails("Food Delivery", "Food & Drink")
        }
        if (upperDesc.contains("RESTAURANT") || upperDesc.contains("CAFÉ") || upperDesc.contains("CAFE")) {
            return SmartDetails("Dining Out", "Food & Drink")
        }

        // 3. Check for Groceries
        if (upperDesc.contains("GROCERY") || upperDesc.contains("SUPERMARKET")) {
            return SmartDetails("Groceries", "Groceries")
        }

        // 4. Check for Transport
        if (upperDesc.contains("UBER") || upperDesc.contains("OLA")) {
            return SmartDetails("Rideshare", "Transport")
        }
        if (upperDesc.contains("FUEL") || upperDesc.contains("GAS STATION")) {
            return SmartDetails("Fuel", "Transport")
        }

        // 5. Check for Utilities / Subscriptions
        if (upperDesc.contains("NETFLIX") || upperDesc.contains("SPOTIFY") || upperDesc.contains("AMAZON PRIME")) {
            return SmartDetails("Subscription", "Subscriptions")
        }
        if (upperDesc.contains("ELECTRICITY") || upperDesc.contains("WATER") || upperDesc.contains("INTERNET BILL")) {
            return SmartDetails("Utility Bill", "Utilities")
        }

        // 6. Check for Transfers
        if (upperDesc.contains("TRANSFER TO") || upperDesc.contains("NEFT") || upperDesc.contains("IMPS")) {
             val recipient = extractRecipientFromTransfer(description)
             return SmartDetails(recipient, "Transfers")
        }

        // 7. Fallback for Cash withdrawals
        if (upperDesc.contains("ATM CASH") || upperDesc.contains("WDL")) {
            return SmartDetails("Cash Withdrawal", "Cash")
        }
        
        // Default for everything else
        val defaultName = description.split("-").firstOrNull()?.trim()?.capitalizeWords() ?: "Unknown Transaction"
        return SmartDetails(defaultName, "Uncategorized")
    }

    private fun extractRecipientFromTransfer(fullDescription: String): String {
        // Example: "NEFT-MOHIT KUMAR-RENT" -> Returns "Mohit Kumar"
        // Example: "UPI-JANE DOE-FOOD" -> Returns "Jane Doe"
        val parts = fullDescription.split("-")
        return if (parts.size > 2) {
            parts[1].capitalizeWords()
        } else {
            "Bank Transfer"
        }
    }
    
    private fun String.capitalizeWords(): String = split(" ").joinToString(" ") { it.toLowerCase().capitalize() }
}
