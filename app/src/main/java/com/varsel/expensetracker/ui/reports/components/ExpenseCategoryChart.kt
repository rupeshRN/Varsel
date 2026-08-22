package com.varsel.expensetracker.ui.reports.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.varsel.expensetracker.ui.reports.ReportsExpenseCategory
import java.text.NumberFormat
import java.util.Locale
import kotlin.math.max

/**
 * Visual breakdown of expenses by category.
 *
 * Each category receives a proportional horizontal bar
 * based on its total amount.
 *
 * No external chart library is required.
 */
@Composable
fun ExpenseCategoryChart(
    categories: List<ReportsExpenseCategory>,
    modifier: Modifier = Modifier
) {
    if (categories.isEmpty()) {
        Text(
            text = "No expense data for this period.",
            modifier = modifier
                .fillMaxWidth()
                .padding(vertical = 20.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        return
    }

    val total =
        categories.sumOf {
            max(it.totalAmount, 0.0)
        }

    if (total <= 0.0) {
        Text(
            text = "No expense data for this period.",
            modifier = modifier
                .fillMaxWidth()
                .padding(vertical = 20.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        return
    }

    val currencyFormatter =
        NumberFormat.getCurrencyInstance(
            Locale("en", "IN")
        )

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {

        categories.forEach { category ->

            val amount =
                max(category.totalAmount, 0.0)

            val percentage =
                (amount / total)
                    .toFloat()
                    .coerceIn(0f, 1f)

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement =
                    Arrangement.spacedBy(6.dp)
            ) {

                Row(
                    modifier = Modifier.fillMaxWidth()
                ) {

                    Text(
                        text = category.category,
                        modifier =
                            Modifier.weight(1f),
                        style =
                            MaterialTheme.typography.bodyMedium,
                        fontWeight =
                            FontWeight.Medium
                    )

                    Spacer(
                        modifier =
                            Modifier.width(12.dp)
                    )

                    Text(
                        text =
                            currencyFormatter.format(
                                amount
                            ),
                        style =
                            MaterialTheme.typography.bodyMedium,
                        fontWeight =
                            FontWeight.SemiBold
                    )
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .background(
                            color =
                                MaterialTheme.colorScheme
                                    .surfaceVariant,
                            shape =
                                RoundedCornerShape(50)
                        )
                ) {

                    Box(
                        modifier = Modifier
                            .fillMaxWidth(
                                percentage
                            )
                            .height(8.dp)
                            .background(
                                color =
                                    MaterialTheme.colorScheme
                                        .primary,
                                shape =
                                    RoundedCornerShape(50)
                            )
                    )
                }
            }
        }
    }
}
