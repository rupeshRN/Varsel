import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val PreviewBackground = Color(0xFFF7F7F5)
private val PrimaryText = Color(0xFF171717)
private val SecondaryText = Color(0xFF6B6B68)
private val IncomeColor = Color(0xFF2E7D5B)
private val ExpenseColor = Color(0xFFC45151)
private val CardSurface = Color.White

@Composable
fun ReportsPreview() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFE7E7E3)),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .width(390.dp)
                .fillMaxHeight()
                .background(PreviewBackground)
                .verticalScroll(rememberScrollState())
                .padding(
                    horizontal = 20.dp,
                    vertical = 24.dp
                ),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {

            ReportsHeader()

            FinancialSummaryCard()

            SpendingOverview()

            SectionTitle(
                title = "Top spending"
            )

            SpendingList()

            SectionTitle(
                title = "Income"
            )

            IncomeCard()

            SectionTitle(
                title = "Financial events"
            )

            FinancialEventsCard()

            Spacer(
                modifier = Modifier.height(12.dp)
            )
        }
    }
}

@Composable
private fun ReportsHeader() {
    Column(
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {

        Text(
            text = "Reports",
            style = MaterialTheme.typography.headlineMedium,
            color = PrimaryText
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {

            Text(
                text = "August 2026",
                style = MaterialTheme.typography.bodyLarge,
                color = SecondaryText
            )

            Row {
                TextButton(
                    onClick = {}
                ) {
                    Text("‹")
                }

                TextButton(
                    onClick = {}
                ) {
                    Text("›")
                }
            }
        }
    }
}

@Composable
private fun FinancialSummaryCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = CardSurface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 1.dp
        )
    ) {

        Column(
            modifier = Modifier.padding(22.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {

            Text(
                text = "NET CASH FLOW",
                style = MaterialTheme.typography.labelMedium,
                color = SecondaryText
            )

            Text(
                text = "₹12,450",
                style = MaterialTheme.typography.headlineLarge,
                color = PrimaryText
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {

                SummaryAmount(
                    label = "Income",
                    amount = "₹35,000",
                    valueColor = IncomeColor,
                    modifier = Modifier.weight(1f)
                )

                SummaryAmount(
                    label = "Expense",
                    amount = "₹22,550",
                    valueColor = ExpenseColor,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun SummaryAmount(
    label: String,
    amount: String,
    valueColor: Color,
    modifier: Modifier = Modifier
) {

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {

        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = SecondaryText
        )

        Text(
            text = amount,
            style = MaterialTheme.typography.titleMedium,
            color = valueColor
        )
    }
}

@Composable
private fun SpendingOverview() {

    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        SectionTitle(
            title = "Spending"
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(
                containerColor = CardSurface
            )
        ) {

            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {

                Text(
                    text = "Where your money went",
                    style = MaterialTheme.typography.titleMedium,
                    color = PrimaryText
                )

                SpendingBar(
                    label = "Food",
                    amount = "₹6,200",
                    fraction = 0.82f
                )

                SpendingBar(
                    label = "Shopping",
                    amount = "₹4,100",
                    fraction = 0.58f
                )

                SpendingBar(
                    label = "Transport",
                    amount = "₹2,800",
                    fraction = 0.40f
                )

                SpendingBar(
                    label = "Bills",
                    amount = "₹2,100",
                    fraction = 0.30f
                )
            }
        }
    }
}

@Composable
private fun SpendingBar(
    label: String,
    amount: String,
    fraction: Float
) {

    Column(
        verticalArrangement = Arrangement.spacedBy(7.dp)
    ) {

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {

            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = PrimaryText
            )

            Text(
                text = amount,
                style = MaterialTheme.typography.bodyMedium,
                color = SecondaryText
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .background(
                    Color(0xFFE8E8E4),
                    RoundedCornerShape(50)
                )
        ) {

            Box(
                modifier = Modifier
                    .fillMaxWidth(fraction)
                    .fillMaxHeight()
                    .background(
                        ExpenseColor,
                        RoundedCornerShape(50)
                    )
            )
        }
    }
}

@Composable
private fun SectionTitle(
    title: String
) {

    Text(
        text = title,
        style = MaterialTheme.typography.titleLarge,
        color = PrimaryText
    )
}

@Composable
private fun SpendingList() {

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = CardSurface
        )
    ) {

        Column(
            modifier = Modifier.padding(
                horizontal = 20.dp
            )
        ) {

            SpendingRow(
                label = "Food",
                amount = "₹6,200"
            )

            SpendingRow(
                label = "Shopping",
                amount = "₹4,100"
            )

            SpendingRow(
                label = "Transport",
                amount = "₹2,800"
            )

            SpendingRow(
                label = "Bills",
                amount = "₹2,100"
            )

            SpendingRow(
                label = "Other",
                amount = "₹1,350"
            )
        }
    }
}

@Composable
private fun SpendingRow(
    label: String,
    amount: String
) {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {

        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = PrimaryText
        )

        Text(
            text = amount,
            style = MaterialTheme.typography.bodyLarge,
            color = PrimaryText
        )
    }
}

@Composable
private fun IncomeCard() {

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = CardSurface
        )
    ) {

        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            IncomeRow(
                label = "Salary",
                amount = "₹30,000"
            )

            IncomeRow(
                label = "Other income",
                amount = "₹5,000"
            )
        }
    }
}

@Composable
private fun IncomeRow(
    label: String,
    amount: String
) {

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {

        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = PrimaryText
        )

        Text(
            text = amount,
            style = MaterialTheme.typography.bodyLarge,
            color = IncomeColor
        )
    }
}

@Composable
private fun FinancialEventsCard() {

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = CardSurface
        )
    ) {

        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            Text(
                text = "3 financial events",
                style = MaterialTheme.typography.titleMedium,
                color = PrimaryText
            )

            Text(
                text = "₹4,250 reimbursed",
                style = MaterialTheme.typography.bodyLarge,
                color = IncomeColor
            )

            Text(
                text = "₹8,600 effective expense",
                style = MaterialTheme.typography.bodyLarge,
                color = SecondaryText
            )
        }
    }
}
