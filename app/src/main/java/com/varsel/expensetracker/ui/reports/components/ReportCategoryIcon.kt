package com.varsel.expensetracker.ui.reports.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ReportCategoryIcon(
    category: String,
    modifier: Modifier = Modifier
) {
    val icon =
        when {
            category.equals(
                "Food",
                ignoreCase = true
            ) -> "🍔"

            category.equals(
                "Travel",
                ignoreCase = true
            ) -> "✈️"

            category.equals(
                "Shopping",
                ignoreCase = true
            ) -> "🛍️"

            category.equals(
                "Fuel",
                ignoreCase = true
            ) -> "⛽"

            category.equals(
                "Groceries",
                ignoreCase = true
            ) -> "🛒"

            category.equals(
                "Bills",
                ignoreCase = true
            ) -> "🧾"

            category.equals(
                "Medical",
                ignoreCase = true
            ) -> "💊"

            category.equals(
                "Salary",
                ignoreCase = true
            ) -> "💼"

            category.equals(
                "Investment",
                ignoreCase = true
            ) -> "📈"

            category.equals(
                "Transfer",
                ignoreCase = true
            ) -> "↔️"

            category.equals(
                "Uncategorized",
                ignoreCase = true
            ) -> "•"

            else -> "₹"
        }

    Surface(
        modifier =
            modifier.size(36.dp),
        shape =
            CircleShape,
        color =
            MaterialTheme.colorScheme
                .surfaceVariant
    ) {

        Box(
            contentAlignment =
                Alignment.Center
        ) {

            Text(
                text = icon
            )
        }
    }
}
