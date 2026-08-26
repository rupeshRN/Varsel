package com.varsel.expensetracker.ui.reports.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
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
import com.varsel.expensetracker.ui.design.AppColors
import com.varsel.expensetracker.ui.reports.ReportsFinancialEvent
import java.text.NumberFormat
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun FinancialEventsCard(
    financialEvents: List<ReportsFinancialEvent>,
    onFinancialEventClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val formatter =
        NumberFormat.getCurrencyInstance(
            Locale("en", "IN")
        )

    val totalEffectiveCost =
        financialEvents.sumOf {
            it.effectiveCost
        }

    Surface(
        modifier =
            modifier.fillMaxWidth(),

        shape =
            RoundedCornerShape(
                20.dp
            ),

        color =
            MaterialTheme.colorScheme
                .surface
    ) {

        Column(
            modifier =
                Modifier.padding(
                    20.dp
                ),

            verticalArrangement =
                Arrangement.spacedBy(
                    16.dp
                )
        ) {

            Text(
                text =
                    "Financial Events",

                style =
                    MaterialTheme.typography
                        .titleLarge,

                fontWeight =
                    FontWeight.Bold
            )

            Row(
                modifier =
                    Modifier.fillMaxWidth()
            ) {

                Column(
                    modifier =
                        Modifier.weight(1f)
                ) {

                    Text(
                        text =
                            "Events",

                        style =
                            MaterialTheme.typography
                                .labelMedium,

                        color =
                            MaterialTheme.colorScheme
                                .onSurfaceVariant
                    )

                    Text(
                        text =
                            financialEvents.size
                                .toString(),

                        style =
                            MaterialTheme.typography
                                .titleMedium,

                        fontWeight =
                            FontWeight.SemiBold
                    )
                }

                Column(
                    modifier =
                        Modifier.weight(1f)
                ) {

                    Text(
                        text =
                            "Effective Cost",

                        style =
                            MaterialTheme.typography
                                .labelMedium,

                        color =
                            MaterialTheme.colorScheme
                                .onSurfaceVariant
                    )

                    Text(
                        text =
                            formatter.format(
                                totalEffectiveCost
                            ),

                        style =
                            MaterialTheme.typography
                                .titleMedium,

                        fontWeight =
                            FontWeight.SemiBold,

                        color =
                            AppColors.Expense
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

                    color =
                        MaterialTheme.colorScheme
                            .onSurfaceVariant
                )

            } else {

                Column(
                    modifier =
                        Modifier.fillMaxWidth(),

                    verticalArrangement =
                        Arrangement.spacedBy(
                            4.dp
                        )
                ) {

                    financialEvents.forEach { event ->

                        FinancialEventRow(
                            event =
                                event,

                            formatter =
                                formatter,

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
    formatter: NumberFormat,
    onClick: () -> Unit
) {

    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .clickable(
                    onClick = onClick
                )
                .padding(
                    vertical = 10.dp
                ),

        verticalArrangement =
            Arrangement.spacedBy(
                6.dp
            )
    ) {

        Row(
            modifier =
                Modifier.fillMaxWidth(),

            verticalAlignment =
                Alignment.CenterVertically
        ) {

            Column(
                modifier =
                    Modifier.weight(1f)
            ) {

                Text(
                    text =
                        event.groupName,

                    style =
                        MaterialTheme.typography
                            .bodyLarge,

                    fontWeight =
                        FontWeight.Medium
                )

                if (
                    event.category.isNotBlank()
                ) {

                    Text(
                        text =
                            event.category,

                        style =
                            MaterialTheme.typography
                                .labelMedium,

                        color =
                            MaterialTheme.colorScheme
                                .onSurfaceVariant
                    )
                }

                /*
                 * Show a small indicator only when the
                 * Financial Event has linked transactions
                 * in more than one month.
                 */
                if (
                    event.coveredMonths.size > 1
                ) {

                    Text(
                        text =
                            formatEventPeriod(
                                event.coveredMonths
                            ),

                        style =
                            MaterialTheme.typography
                                .labelSmall,

                        color =
                            MaterialTheme.colorScheme
                                .onSurfaceVariant,

                        fontWeight =
                            FontWeight.Medium
                    )
                }
            }

            Spacer(
                modifier =
                    Modifier.width(
                        8.dp
                    )
            )

            Text(
                text =
                    formatter.format(
                        event.effectiveCost
                    ),

                style =
                    MaterialTheme.typography
                        .bodyLarge,

                fontWeight =
                    FontWeight.Bold,

                color =
                    AppColors.Expense
            )

            Spacer(
                modifier =
                    Modifier.width(
                        6.dp
                    )
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

        Row(
            horizontalArrangement =
                Arrangement.spacedBy(
                    16.dp
                )
        ) {

            Text(
                text =
                    "${formatter.format(
                        event.expenseAmount
                    )} expense",

                style =
                    MaterialTheme.typography
                        .labelMedium,

                color =
                    AppColors.Expense,

                fontWeight =
                    FontWeight.Medium
            )

            if (
                event.reimbursedAmount > 0.0
            ) {

                Text(
                    text =
                        "${formatter.format(
                            event.reimbursedAmount
                        )} reimbursed",

                    style =
                        MaterialTheme.typography
                            .labelMedium,

                    color =
                        AppColors.Income,

                    fontWeight =
                        FontWeight.Medium
                )
            }
        }

        HorizontalDivider(
            modifier =
                Modifier.padding(
                    top = 6.dp
                )
        )
    }
}

/**
 * Creates a concise user-facing description of the months
 * covered by a Financial Event.
 *
 * Examples:
 *
 * Jun 2026 + Jul 2026
 *     -> Spans Jun–Jul 2026
 *
 * Jun 2026 + Jul 2026 + Aug 2026
 *     -> Spans Jun–Aug 2026
 *
 * Dec 2025 + Jan 2026
 *     -> Spans Dec 2025–Jan 2026
 */
private fun formatEventPeriod(
    months: List<YearMonth>
): String {

    if (months.size < 2) {
        return ""
    }

    val sortedMonths =
        months.distinct().sorted()

    val first =
        sortedMonths.first()

    val last =
        sortedMonths.last()

    val monthFormatter =
        DateTimeFormatter.ofPattern(
            "MMM"
        )

    val monthYearFormatter =
        DateTimeFormatter.ofPattern(
            "MMM yyyy"
        )

    return if (
        first.year == last.year
    ) {

        "Spans ${
            first.format(
                monthFormatter
            )
        }–${
            last.format(
                monthFormatter
            )
        } ${last.year}"

    } else {

        "Spans ${
            first.format(
                monthYearFormatter
            )
        }–${
            last.format(
                monthYearFormatter
            )
        }"
    }
}
