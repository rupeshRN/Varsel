package com.varsel.expensetracker.ui.dashboard.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import com.varsel.expensetracker.ui.design.AppDimensions
import com.varsel.expensetracker.ui.design.AppShapes
import com.varsel.expensetracker.ui.model.BalanceSummaryUiModel
import kotlin.math.abs

@Composable
fun BalanceCard(
    summary: BalanceSummaryUiModel,
    modifier: Modifier = Modifier
) {

    Card(

        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = AppDimensions.ScreenPadding),

        shape = AppShapes.HeroCard,

        elevation = CardDefaults.cardElevation(
            defaultElevation = AppDimensions.CardElevation
        )

    ) {

        Column(
            modifier = Modifier.padding(AppDimensions.CardPadding)
        ) {

            //--------------------------------------------------
            // Total Balance
            //--------------------------------------------------

            Text(
                text = "Total Balance",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(
                modifier = Modifier.height(
                    AppDimensions.SmallSpacing
                )
            )

            Text(
                text = "₹%,.2f".format(
                    summary.totalBalance
                ),
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            //--------------------------------------------------
            // Account-wise balance
            //--------------------------------------------------

            if (summary.accounts.isNotEmpty()) {

                Spacer(
                    modifier = Modifier.height(
                        AppDimensions.LargeSpacing
                    )
                )

                HorizontalDivider()

                Spacer(
                    modifier = Modifier.height(
                        AppDimensions.MediumSpacing
                    )
                )

                Text(
                    text = "Account Wise Balance",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(
                    modifier = Modifier.height(
                        AppDimensions.SmallSpacing
                    )
                )

                summary.accounts.forEach { account ->

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement =
                            Arrangement.SpaceBetween
                    ) {

                        Text(
                            text =
                                "${account.bankName} " +
                                account.accountDisplayName,

                            style =
                                MaterialTheme.typography.bodyMedium
                        )

                        Text(
                            text =
                                "₹%,.2f".format(
                                    account.balance
                                ),

                            style =
                                MaterialTheme.typography.bodyMedium,

                            fontWeight =
                                FontWeight.SemiBold
                        )
                    }

                    Spacer(
                        modifier = Modifier.height(
                            AppDimensions.SmallSpacing
                        )
                    )
                }
            }

            //--------------------------------------------------
            // Current month income / expense comparison
            //--------------------------------------------------

            Spacer(
                modifier = Modifier.height(
                    AppDimensions.LargeSpacing
                )
            )

            HorizontalDivider()

            Spacer(
                modifier = Modifier.height(
                    AppDimensions.LargeSpacing
                )
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement =
                    Arrangement.SpaceBetween
            ) {

                MonthlyMetricItem(
                    title = "Income",
                    amount = summary.totalIncome,
                    previousAmount =
                        summary.previousMonthIncome,
                    changePercent =
                        summary.incomeChangePercent,
                    positiveColor =
                        MaterialTheme.colorScheme.primary
                )

                MonthlyMetricItem(
                    title = "Expense",
                    amount = summary.totalExpense,
                    previousAmount =
                        summary.previousMonthExpense,
                    changePercent =
                        summary.expenseChangePercent,
                    positiveColor =
                        MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
private fun MonthlyMetricItem(
    title: String,
    amount: Double,
    previousAmount: Double,
    changePercent: Double?,
    positiveColor: androidx.compose.ui.graphics.Color
) {

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                horizontal = AppDimensions.SmallSpacing
            )
    ) {

        Text(
            text = title,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(
            modifier = Modifier.height(
                AppDimensions.ExtraSmallSpacing
            )
        )

        Row(
            verticalAlignment =
                androidx.compose.ui.Alignment.CenterVertically
        ) {

            Text(
                text = "₹%,.2f".format(amount),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(
                modifier = Modifier.padding(
                    horizontal =
                        AppDimensions.ExtraSmallSpacing
                )
            )

            if (changePercent == null) {

                Text(
                    text = "New",
                    style =
                        MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    color =
                        MaterialTheme.colorScheme.onSurfaceVariant
                )

            } else {

                val isIncrease =
                    changePercent > 0.0

                val isDecrease =
                    changePercent < 0.0

                val arrow =
                    when {
                        isIncrease -> "↑"
                        isDecrease -> "↓"
                        else -> "→"
                    }

                val changeColor =
                    when {

                        title == "Income" &&
                            isIncrease ->
                            positiveColor

                        title == "Income" &&
                            isDecrease ->
                            MaterialTheme.colorScheme.error

                        title == "Expense" &&
                            isIncrease ->
                            MaterialTheme.colorScheme.error

                        title == "Expense" &&
                            isDecrease ->
                            positiveColor

                        else ->
                            MaterialTheme.colorScheme.onSurfaceVariant
                    }

                Text(
                    text =
                        "$arrow${abs(changePercent).toInt()}%",

                    style =
                        MaterialTheme.typography.titleMedium,

                    fontWeight =
                        FontWeight.Medium,

                    color = changeColor
                )
            }
        }

        Spacer(
            modifier = Modifier.height(
                AppDimensions.ExtraSmallSpacing
            )
        )

        Text(
            text =
                "Compared to ₹%,.2f last\nmonth"
                    .format(previousAmount),

            style =
                MaterialTheme.typography.bodyMedium,

            color =
                MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
