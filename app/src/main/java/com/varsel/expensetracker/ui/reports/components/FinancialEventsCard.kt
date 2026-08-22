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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import java.text.NumberFormat
import java.util.Locale

/**
 * Financial Events section for the Reports screen.
 *
 * This component is presentation-only.
 * The actual Financial Event calculations remain in
 * the ReportsViewModel / repository layer.
 */
@Composable
fun FinancialEventsCard(
    financialEventCount: Int,
    financialEventAmount: Double,
    modifier: Modifier = Modifier
) {
    val currencyFormatter =
        NumberFormat.getCurrencyInstance(
            Locale("en", "IN")
        )

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement =
                Arrangement.spacedBy(16.dp)
        ) {

            Text(
                text = "Financial Events",
                style =
                    MaterialTheme.typography.titleLarge,
                fontWeight =
                    FontWeight.Bold
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment =
                    Alignment.CenterVertically
            ) {

                Column(
                    modifier =
                        Modifier.weight(1f)
                ) {

                    Text(
                        text = "Events",
                        style =
                            MaterialTheme.typography
                                .labelMedium,
                        color =
                            MaterialTheme.colorScheme
                                .onSurfaceVariant
                    )

                    Text(
                        text =
                            financialEventCount.toString(),
                        style =
                            MaterialTheme.typography
                                .titleMedium,
                        fontWeight =
                            FontWeight.SemiBold
                    )
                }

                Spacer(
                    modifier = Modifier.width(20.dp)
                )

                Column(
                    modifier =
                        Modifier.weight(1f)
                ) {

                    Text(
                        text = "Amount",
                        style =
                            MaterialTheme.typography
                                .labelMedium,
                        color =
                            MaterialTheme.colorScheme
                                .onSurfaceVariant
                    )

                    Text(
                        text =
                            currencyFormatter.format(
                                financialEventAmount
                            ),
                        style =
                            MaterialTheme.typography
                                .titleMedium,
                        fontWeight =
                            FontWeight.SemiBold
                    )
                }
            }
        }
    }
}
