package com.varsel.expensetracker.ui.reports.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.varsel.expensetracker.ui.reports.ReportsIncomeCategory
import java.text.NumberFormat
import java.util.Locale

/**
 * Income category breakdown.
 */
@Composable
fun IncomeCategoryList(
    categories: List<ReportsIncomeCategory>,
    modifier: Modifier = Modifier
) {
    if (categories.isEmpty()) {

        Text(
            text = "No income for this period.",
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

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        vertical = 10.dp
                    ),
                verticalAlignment =
                    Alignment.CenterVertically
            ) {

                Text(
                    text = category.category,
                    modifier =
                        Modifier.weight(1f),
                    style =
                        MaterialTheme.typography
                            .bodyLarge,
                    fontWeight =
                        FontWeight.Medium
                )

                Spacer(
                    modifier =
                        Modifier.width(16.dp)
                )

                Text(
                    text =
                        currencyFormatter.format(
                            category.totalAmount
                        ),
                    style =
                        MaterialTheme.typography
                            .bodyLarge,
                    fontWeight =
                        FontWeight.SemiBold
                )
            }
        }
    }
}
