package com.varsel.expensetracker.ui.transaction.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.varsel.expensetracker.ui.design.AppDimensions

@Composable
fun MonthlySummaryCard(

    monthTitle: String,

    income: Double,

    expense: Double,

    modifier: Modifier = Modifier

) {

    val balance = income - expense

    Card(

        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = AppDimensions.ScreenPadding),

        shape = RoundedCornerShape(24.dp),

        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp
        )

    ) {

        Column(
            modifier = Modifier.padding(24.dp)
        ) {

            Text(
                text = monthTitle,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {

                SummaryMetric(
                    title = "Income",
                    amount = income
                )

                SummaryMetric(
                    title = "Expense",
                    amount = expense
                )

            }

            Spacer(modifier = Modifier.height(20.dp))

            HorizontalDivider()

            Spacer(modifier = Modifier.height(20.dp))

            Text(

                text = "Net Balance",

                style = MaterialTheme.typography.labelLarge,

                color = MaterialTheme.colorScheme.onSurfaceVariant

            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(

                text = "₹%,.2f".format(balance),

                style = MaterialTheme.typography.headlineMedium,

                fontWeight = FontWeight.Bold,

                color = if (balance >= 0)
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.error

            )
        }
    }
}

@Composable
private fun SummaryMetric(

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
            modifier = Modifier.height(4.dp)
        )

        Text(

            text = "₹%,.2f".format(amount),

            style = MaterialTheme.typography.titleMedium,

            fontWeight = FontWeight.SemiBold

        )
    }
}
