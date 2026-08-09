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

            Text(
                text = "Total Balance",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(
                modifier = Modifier.height(AppDimensions.SmallSpacing)
            )

            Text(
                text = "₹%,.2f".format(summary.totalBalance),
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            /*
             * Future Enhancement
             *
             * If multiple bank accounts exist,
             * show an "Account wise balance" section here.
             */

            if (summary.accounts.isNotEmpty()) {

                Spacer(
                    modifier = Modifier.height(AppDimensions.LargeSpacing)
                )

                HorizontalDivider()

                Spacer(
                    modifier = Modifier.height(AppDimensions.MediumSpacing)
                )

                Text(
                    text = "Account Wise Balance",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(
                    modifier = Modifier.height(AppDimensions.SmallSpacing)
                )

                summary.accounts.forEach { account ->

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {

                        Text(
                            text = "${account.bankName} ${account.accountDisplayName}",
                            style = MaterialTheme.typography.bodyMedium
                        )

                        Text(
                            text = "₹%,.2f".format(account.balance),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Spacer(
                        modifier = Modifier.height(AppDimensions.SmallSpacing)
                    )
                }
            }

            Spacer(
                modifier = Modifier.height(AppDimensions.LargeSpacing)
            )

            HorizontalDivider()

            Spacer(
                modifier = Modifier.height(AppDimensions.LargeSpacing)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {

                MetricItem(
                    title = "Income",
                    amount = summary.totalIncome
                )

                MetricItem(
                    title = "Expense",
                    amount = summary.totalExpense
                )

                MetricItem(
                    title = "Savings",
                    amount = summary.savings
                )
            }
        }
    }
}

@Composable
private fun MetricItem(
    title: String,
    amount: Double
) {

    Column {

        Text(
            text = title,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(
            modifier = Modifier.height(AppDimensions.ExtraSmallSpacing)
        )

        Text(
            text = "₹%,.0f".format(amount),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
