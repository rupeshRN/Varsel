package com.varsel.expensetracker.ui.reports.components

import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.varsel.expensetracker.ui.reports.ReportsFinancialEvent
import java.text.NumberFormat
import java.util.Locale

/**
 * Financial Events section for the Reports screen.
 *
 * Presentation-only component.
 *
 * Navigation is delegated to the parent screen so this
 * component does not know anything about NavController
 * or navigation routes.
 */
@Composable
fun FinancialEventsCard(
    financialEvents: List<ReportsFinancialEvent>,
    onFinancialEventClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val currencyFormatter =
        NumberFormat.getCurrencyInstance(
            Locale("en", "IN")
        )

    val totalEffectiveCost =
        financialEvents.sumOf {
            it.effectiveCost
        }

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
                            MaterialTheme.typography.labelMedium,
                        color =
                            MaterialTheme.colorScheme
                                .onSurfaceVariant
                    )

                    Text(
                        text =
                            financialEvents.size.toString(),
                        style =
                            MaterialTheme.typography.titleMedium,
                        fontWeight =
                            FontWeight.SemiBold
                    )
                }

                Spacer(
                    modifier =
                        Modifier.width(20.dp)
                )

                Column(
                    modifier =
                        Modifier.weight(1f)
                ) {

                    Text(
                        text = "Effective Cost",
                        style =
                            MaterialTheme.typography.labelMedium,
                        color =
                            MaterialTheme.colorScheme
                                .onSurfaceVariant
                    )

                    Text(
                        text =
                            currencyFormatter.format(
                                totalEffectiveCost
                            ),
                        style =
                            MaterialTheme.typography.titleMedium,
                        fontWeight =
                            FontWeight.SemiBold
                    )
                }
            }

            if (financialEvents.isEmpty()) {

                Text(
                    text =
                        "No Financial Events for this period.",
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(
                                vertical = 12.dp
                            ),
                    style =
                        MaterialTheme.typography.bodyMedium,
                    color =
                        MaterialTheme.colorScheme
                            .onSurfaceVariant
                )

            } else {

                LazyColumn(
                    modifier =
                        Modifier.fillMaxWidth(),
                    verticalArrangement =
                        Arrangement.spacedBy(4.dp)
                ) {

                    items(
                        items = financialEvents,
                        key = {
                            it.transactionLinkId
                        }
                    ) { event ->

                        FinancialEventRow(
                            event = event,
                            currencyFormatter =
                                currencyFormatter,
                            onClick = {
                                onFinancialEventClick(
                                    event.transactionLinkId
                                )
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FinancialEventRow(
    event: ReportsFinancialEvent,
    currencyFormatter: NumberFormat,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                onClick = onClick
            )
            .padding(
                vertical = 10.dp
            ),
        verticalArrangement =
            Arrangement.spacedBy(4.dp)
    ) {

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
                    text = event.groupName,
                    style =
                        MaterialTheme.typography.bodyLarge,
                    fontWeight =
                        FontWeight.Medium
                )

                if (event.category.isNotBlank()) {

                    Text(
                        text = event.category,
                        style =
                            MaterialTheme.typography.labelMedium,
                        color =
                            MaterialTheme.colorScheme
                                .onSurfaceVariant
                    )
                }
            }

            Spacer(
                modifier =
                    Modifier.width(12.dp)
            )

            Text(
                text =
                    currencyFormatter.format(
                        event.effectiveCost
                    ),
                style =
                    MaterialTheme.typography.bodyLarge,
                fontWeight =
                    FontWeight.SemiBold
            )

            Spacer(
                modifier =
                    Modifier.width(8.dp)
            )

            Icon(
                imageVector =
                    Icons.AutoMirrored.Filled
                        .ArrowForward,
                contentDescription =
                    "Open Financial Event",
                tint =
                    MaterialTheme.colorScheme
                        .onSurfaceVariant
            )
        }

        if (event.reimbursedAmount > 0.0) {

            Text(
                text =
                    "Reimbursed: ${
                        currencyFormatter.format(
                            event.reimbursedAmount
                        )
                    }",
                style =
                    MaterialTheme.typography.labelSmall,
                color =
                    MaterialTheme.colorScheme
                        .onSurfaceVariant
            )
        }

        HorizontalDivider(
            modifier =
                Modifier.padding(
                    top = 6.dp
                )
        )
    }
}
