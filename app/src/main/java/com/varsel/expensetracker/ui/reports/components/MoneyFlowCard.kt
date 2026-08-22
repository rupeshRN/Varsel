package com.varsel.expensetracker.ui.reports.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.varsel.expensetracker.ui.reports.ReportsFlow

/**
 * Container for the Money Flow section.
 *
 * This component deliberately does not draw the chart yet.
 * The chart and category list will be separate components.
 */
@Composable
fun MoneyFlowCard(
    selectedFlow: ReportsFlow,
    onFlowSelected: (ReportsFlow) -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
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
                text = "Money Flow",
                style =
                    MaterialTheme.typography.titleLarge,
                fontWeight =
                    FontWeight.Bold
            )

            FlowSelector(
                selectedFlow = selectedFlow,
                onFlowSelected = onFlowSelected
            )

            content()
        }
    }
}

/**
 * Expenses / Income selector.
 */
@Composable
private fun FlowSelector(
    selectedFlow: ReportsFlow,
    onFlowSelected: (ReportsFlow) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth()
    ) {

        FlowTab(
            label = "Expenses",
            selected =
                selectedFlow ==
                    ReportsFlow.EXPENSES,
            onClick = {
                onFlowSelected(
                    ReportsFlow.EXPENSES
                )
            },
            modifier =
                Modifier.weight(1f)
        )

        Spacer(
            modifier = Modifier.width(8.dp)
        )

        FlowTab(
            label = "Income",
            selected =
                selectedFlow ==
                    ReportsFlow.INCOME,
            onClick = {
                onFlowSelected(
                    ReportsFlow.INCOME
                )
            },
            modifier =
                Modifier.weight(1f)
        )
    }
}

/**
 * Individual Money Flow tab.
 */
@Composable
private fun FlowTab(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color =
            if (selected) {
                MaterialTheme.colorScheme
                    .primaryContainer
            } else {
                MaterialTheme.colorScheme
                    .surfaceVariant
            },
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    vertical = 11.dp
                ),
            horizontalArrangement =
                Arrangement.Center
        ) {
            Text(
                text = label,
                style =
                    MaterialTheme.typography
                        .labelLarge,
                fontWeight =
                    if (selected) {
                        FontWeight.Bold
                    } else {
                        FontWeight.Normal
                    },
                color =
                    if (selected) {
                        MaterialTheme.colorScheme
                            .onPrimaryContainer
                    } else {
                        MaterialTheme.colorScheme
                            .onSurfaceVariant
                    }
            )
        }
    }
}
