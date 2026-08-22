package com.varsel.expensetracker.ui.reports.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.varsel.expensetracker.ui.reports.ReportsExpenseCategory
import java.text.NumberFormat
import java.util.Locale

/**
 * Expense category breakdown.
 *
 * Uses the already-filtered expense categories from
 * ReportsUiState.
 */
@Composable
fun ExpenseCategoryList(
    categories: List<ReportsExpenseCategory>,
    modifier: Modifier = Modifier
) {
    if (categories.isEmpty()) {
        EmptyCategoryMessage(
            message = "No expenses for this period.",
            modifier = modifier
        )
        return
    }

    val currencyFormatter =
        NumberFormat.getCurrencyInstance(
            Locale("en", "IN")
        )

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement =
            Arrangement.spacedBy(4.dp)
    ) {

        categories.forEach { category ->

            ExpenseCategoryRow(
                category = category,
                currencyFormatter = currencyFormatter
            )
        }
    }
}

@Composable
private fun ExpenseCategoryRow(
    category: ReportsExpenseCategory,
    currencyFormatter: NumberFormat
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                vertical = 10.dp
            ),
        verticalAlignment =
            Alignment.CenterVertically
    ) {

        Column(
            modifier = Modifier.weight(1f)
        ) {

            Text(
                text = category.category,
                style =
                    MaterialTheme.typography.bodyLarge,
                fontWeight =
                    FontWeight.Medium
            )

            /*
             * Only show the financial-event detail when
             * there is actually a Financial Event amount.
             */
            if (
                category.financialEventAmount != 0.0
            ) {

                Text(
                    text =
                        "Financial Events included",
                    style =
                        MaterialTheme.typography
                            .labelSmall,
                    color =
                        MaterialTheme.colorScheme
                            .onSurfaceVariant
                )
            }
        }

        Spacer(
            modifier = Modifier.width(16.dp)
        )

        Text(
            text =
                currencyFormatter.format(
                    category.totalAmount
                ),
            style =
                MaterialTheme.typography.bodyLarge,
            fontWeight =
                FontWeight.SemiBold
        )
    }
}

@Composable
private fun EmptyCategoryMessage(
    message: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = message,
        modifier = modifier
            .fillMaxWidth()
            .padding(
                vertical = 20.dp
            ),
        style =
            MaterialTheme.typography.bodyMedium,
        color =
            MaterialTheme.colorScheme
                .onSurfaceVariant
    )
}
