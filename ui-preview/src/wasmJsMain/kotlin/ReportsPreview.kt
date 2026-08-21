import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.AssistChip
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt

private val VarselBackground = Color(0xFFF5F5F2)
private val VarselSurface = Color(0xFFFFFFFF)
private val VarselPrimary = Color(0xFF181A18)
private val VarselSecondary = Color(0xFF6B706B)
private val VarselMuted = Color(0xFFE8EAE5)

private val IncomeColor = Color(0xFF2F8061)
private val ExpenseColor = Color(0xFFD15A58)
private val ReimbursementColor = Color(0xFF5C73C5)

private data class ExpenseCategory(
    val name: String,
    val amount: Int,
    val normalAmount: Int,
    val financialEventAmount: Int,
    val color: Color
)

private data class IncomeCategory(
    val name: String,
    val amount: Int,
    val color: Color
)

private data class FinancialEventPreview(
    val title: String,
    val category: String,
    val originalExpense: Int,
    val reimbursement: Int
) {
    val finalExpense: Int
        get() = originalExpense - reimbursement
}

private val expenseCategories = listOf(
    ExpenseCategory(
        name = "Food",
        amount = 7000,
        normalAmount = 6000,
        financialEventAmount = 1000,
        color = Color(0xFFD36A5A)
    ),
    ExpenseCategory(
        name = "Transport",
        amount = 2500,
        normalAmount = 1500,
        financialEventAmount = 1000,
        color = Color(0xFF5D82B8)
    ),
    ExpenseCategory(
        name = "Shopping",
        amount = 4100,
        normalAmount = 4100,
        financialEventAmount = 0,
        color = Color(0xFF8A69A8)
    ),
    ExpenseCategory(
        name = "Bills",
        amount = 2100,
        normalAmount = 2100,
        financialEventAmount = 0,
        color = Color(0xFFC29A4A)
    ),
    ExpenseCategory(
        name = "Other",
        amount = 2600,
        normalAmount = 2600,
        financialEventAmount = 0,
        color = Color(0xFF6D8D78)
    )
)

private val incomeCategories = listOf(
    IncomeCategory(
        name = "Salary",
        amount = 30000,
        color = Color(0xFF2F8061)
    ),
    IncomeCategory(
        name = "Other income",
        amount = 5000,
        color = Color(0xFF5C73C5)
    )
)

private val financialEvents = listOf(
    FinancialEventPreview(
        title = "Team dinner",
        category = "Food",
        originalExpense = 3000,
        reimbursement = 2000
    ),
    FinancialEventPreview(
        title = "Office travel",
        category = "Transport",
        originalExpense = 5000,
        reimbursement = 4000
    )
)

@Composable
fun ReportsPreview() {

    var selectedFlow by remember {
        mutableIntStateOf(0)
    }

    var selectedExpenseIndex by remember {
        mutableIntStateOf(0)
    }

    var selectedIncomeIndex by remember {
        mutableIntStateOf(0)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFDCDDD8)),
        contentAlignment = Alignment.TopCenter
    ) {

        Column(
            modifier = Modifier
                .width(390.dp)
                .fillMaxHeight()
                .background(VarselBackground)
                .verticalScroll(
                    rememberScrollState()
                )
                .padding(
                    horizontal = 18.dp,
                    vertical = 22.dp
                ),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {

            ReportsHeader()

            NetCashFlowCard()

            MoneyFlowCard(
                selectedFlow = selectedFlow,
                onFlowSelected = {
                    selectedFlow = it
                },
                selectedExpenseIndex = selectedExpenseIndex,
                onExpenseSelected = {
                    selectedExpenseIndex = it
                },
                selectedIncomeIndex = selectedIncomeIndex,
                onIncomeSelected = {
                    selectedIncomeIndex = it
                }
            )

            ActualExpenseInsight()

            FinancialEventsSection()

            Spacer(
                modifier = Modifier.height(18.dp)
            )
        }
    }
}

@Composable
private fun ReportsHeader() {

    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {

        Text(
            text = "Reports",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold
            ),
            color = VarselPrimary
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {

            Surface(
                shape = RoundedCornerShape(14.dp),
                color = VarselSurface
            ) {

                Row(
                    modifier = Modifier.padding(
                        horizontal = 4.dp,
                        vertical = 2.dp
                    ),
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    TextButton(
                        onClick = {}
                    ) {
                        Text(
                            text = "‹",
                            fontSize = 22.sp
                        )
                    }

                    Text(
                        text = "August 2026",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = VarselPrimary
                    )

                    TextButton(
                        onClick = {}
                    ) {
                        Text(
                            text = "›",
                            fontSize = 22.sp
                        )
                    }
                }
            }

            AssistChip(
                onClick = {},
                label = {
                    Text("Month")
                }
            )
        }
    }
}

@Composable
private fun NetCashFlowCard() {

    val netCashFlow = 35000 - 22550

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = VarselPrimary
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 0.dp
        )
    ) {

        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {

                Text(
                    text = "NET CASH FLOW",
                    style = MaterialTheme.typography.labelMedium.copy(
                        letterSpacing = 1.2.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = Color(0xFFB8BCB8)
                )

                Surface(
                    shape = CircleShape,
                    color = Color(0xFF2B302C)
                ) {

                    Box(
                        modifier = Modifier.size(34.dp),
                        contentAlignment = Alignment.Center
                    ) {

                        Text(
                            text = "↗",
                            color = IncomeColor,
                            fontSize = 18.sp
                        )
                    }
                }
            }

            Text(
                text = formatCurrency(netCashFlow),
                style = MaterialTheme.typography.displaySmall.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = Color.White
            )

            Text(
                text = "You earned more than you spent this month.",
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFFB8BCB8)
            )

            HorizontalDivider(
                color = Color(0xFF343934)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {

                FlowSummary(
                    label = "Income",
                    amount = 35000,
                    color = Color(0xFF72C49D),
                    modifier = Modifier.weight(1f)
                )

                FlowSummary(
                    label = "Expense",
                    amount = 22550,
                    color = Color(0xFFE98987),
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun FlowSummary(
    label: String,
    amount: Int,
    color: Color,
    modifier: Modifier
) {

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {

        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = Color(0xFF969B96)
        )

        Text(
            text = formatCurrency(amount),
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.SemiBold
            ),
            color = color
        )
    }
}

@Composable
private fun MoneyFlowCard(
    selectedFlow: Int,
    onFlowSelected: (Int) -> Unit,
    selectedExpenseIndex: Int,
    onExpenseSelected: (Int) -> Unit,
    selectedIncomeIndex: Int,
    onIncomeSelected: (Int) -> Unit
) {

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = VarselSurface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 1.dp
        )
    ) {

        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {

            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {

                Text(
                    text = "Money Flow",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = VarselPrimary
                )

                Text(
                    text = "Explore where your money came from and where it went.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = VarselSecondary
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {

                FlowFilterChip(
                    text = "Expenses",
                    selected = selectedFlow == 0,
                    onClick = {
                        onFlowSelected(0)
                    },
                    modifier = Modifier.weight(1f)
                )

                FlowFilterChip(
                    text = "Income",
                    selected = selectedFlow == 1,
                    onClick = {
                        onFlowSelected(1)
                    },
                    modifier = Modifier.weight(1f)
                )
            }

            if (selectedFlow == 0) {

                ExpenseFlow(
                    selectedIndex = selectedExpenseIndex,
                    onSelected = onExpenseSelected
                )

            } else {

                IncomeFlow(
                    selectedIndex = selectedIncomeIndex,
                    onSelected = onIncomeSelected
                )
            }
        }
    }
}

@Composable
private fun FlowFilterChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier
) {

    FilterChip(
        selected = selected,
        onClick = onClick,
        label = {
            Text(
                text = text,
                modifier = Modifier.fillMaxWidth()
            )
        },
        modifier = modifier
    )
}

@Composable
private fun ExpenseFlow(
    selectedIndex: Int,
    onSelected: (Int) -> Unit
) {

    val selectedCategory = expenseCategories[selectedIndex]
    val total = expenseCategories.sumOf {
        it.amount
    }

    DonutChart(
        categories = expenseCategories.map {
            it.amount
        },
        colors = expenseCategories.map {
            it.color
        },
        selectedIndex = selectedIndex,
        centerTitle = selectedCategory.name,
        centerValue = formatCurrency(selectedCategory.amount),
        modifier = Modifier
            .fillMaxWidth()
            .height(260.dp)
    )

    Column(
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {

        expenseCategories.forEachIndexed { index, category ->

            CategoryRow(
                categoryName = category.name,
                amount = category.amount,
                total = total,
                color = category.color,
                selected = index == selectedIndex,
                onClick = {
                    onSelected(index)
                }
            )
        }
    }

    SelectedCategoryInsight(
        category = selectedCategory
    )
}

@Composable
private fun IncomeFlow(
    selectedIndex: Int,
    onSelected: (Int) -> Unit
) {

    val selectedCategory = incomeCategories[selectedIndex]
    val total = incomeCategories.sumOf {
        it.amount
    }

    DonutChart(
        categories = incomeCategories.map {
            it.amount
        },
        colors = incomeCategories.map {
            it.color
        },
        selectedIndex = selectedIndex,
        centerTitle = selectedCategory.name,
        centerValue = formatCurrency(selectedCategory.amount),
        modifier = Modifier
            .fillMaxWidth()
            .height(260.dp)
    )

    Column(
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {

        incomeCategories.forEachIndexed { index, category ->

            CategoryRow(
                categoryName = category.name,
                amount = category.amount,
                total = total,
                color = category.color,
                selected = index == selectedIndex,
                onClick = {
                    onSelected(index)
                }
            )
        }
    }

    IncomeInsight(
        category = selectedCategory,
        total = total
    )
}

@Composable
private fun DonutChart(
    categories: List<Int>,
    colors: List<Color>,
    selectedIndex: Int,
    centerTitle: String,
    centerValue: String,
    modifier: Modifier
) {

    val total = categories.sum()

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {

        Canvas(
            modifier = Modifier.size(230.dp)
        ) {

            val strokeWidth = 30.dp.toPx()

            var currentAngle = -90f

            categories.forEachIndexed { index, value ->

                val sweepAngle =
                    (value.toFloat() / total.toFloat()) * 360f

                val selected = index == selectedIndex

                drawArc(
                    color = colors[index].copy(
                        alpha = if (selected) 1f else 0.28f
                    ),
                    startAngle = currentAngle,
                    sweepAngle = sweepAngle - 2f,
                    useCenter = false,
                    topLeft = Offset(
                        strokeWidth / 2,
                        strokeWidth / 2
                    ),
                    size = Size(
                        width = size.width - strokeWidth,
                        height = size.height - strokeWidth
                    ),
                    style = Stroke(
                        width = if (selected) {
                            strokeWidth + 5.dp.toPx()
                        } else {
                            strokeWidth
                        },
                        cap = StrokeCap.Round
                    )
                )

                currentAngle += sweepAngle
            }
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {

            Text(
                text = centerValue,
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = VarselPrimary
            )

            Text(
                text = centerTitle,
                style = MaterialTheme.typography.bodyMedium,
                color = VarselSecondary
            )
        }
    }
}

@Composable
private fun CategoryRow(
    categoryName: String,
    amount: Int,
    total: Int,
    color: Color,
    selected: Boolean,
    onClick: () -> Unit
) {

    val percentage =
        ((amount.toFloat() / total.toFloat()) * 100f)
            .roundToInt()

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                onClick = onClick
            ),
        shape = RoundedCornerShape(16.dp),
        color = if (selected) {
            color.copy(alpha = 0.09f)
        } else {
            Color.Transparent
        }
    ) {

        Row(
            modifier = Modifier.padding(
                horizontal = 12.dp,
                vertical = 11.dp
            ),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Box(
                modifier = Modifier
                    .size(10.dp)
                    .background(
                        color = color,
                        shape = CircleShape
                    )
            )

            Spacer(
                modifier = Modifier.width(10.dp)
            )

            Text(
                text = categoryName,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = if (selected) {
                        FontWeight.SemiBold
                    } else {
                        FontWeight.Normal
                    }
                ),
                color = VarselPrimary,
                modifier = Modifier.weight(1f)
            )

            Text(
                text = "$percentage%",
                style = MaterialTheme.typography.labelMedium,
                color = VarselSecondary
            )

            Spacer(
                modifier = Modifier.width(12.dp)
            )

            Text(
                text = formatCurrency(amount),
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                color = VarselPrimary
            )
        }
    }
}

@Composable
private fun SelectedCategoryInsight(
    category: ExpenseCategory
) {

    Surface(
        shape = RoundedCornerShape(18.dp),
        color = Color(0xFFF4F4F0)
    ) {

        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {

            Text(
                text = "${category.name} expense",
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = VarselPrimary
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {

                InsightValue(
                    label = "Normal",
                    value = category.normalAmount,
                    modifier = Modifier.weight(1f)
                )

                InsightValue(
                    label = "Financial events",
                    value = category.financialEventAmount,
                    modifier = Modifier.weight(1f)
                )
            }

            Text(
                text = "The Financial Event amount shown here is the final personal expense after reimbursement.",
                style = MaterialTheme.typography.bodySmall,
                color = VarselSecondary
            )
        }
    }
}

@Composable
private fun InsightValue(
    label: String,
    value: Int,
    modifier: Modifier
) {

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(3.dp)
    ) {

        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = VarselSecondary
        )

        Text(
            text = formatCurrency(value),
            style = MaterialTheme.typography.titleSmall.copy(
                fontWeight = FontWeight.Bold
            ),
            color = VarselPrimary
        )
    }
}

@Composable
private fun IncomeInsight(
    category: IncomeCategory,
    total: Int
) {

    val percentage =
        ((category.amount.toFloat() / total.toFloat()) * 100f)
            .roundToInt()

    Surface(
        shape = RoundedCornerShape(18.dp),
        color = Color(0xFFF1F6F3)
    ) {

        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {

            Text(
                text = category.name,
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = VarselPrimary
            )

            Text(
                text = "$percentage% of this month's income",
                style = MaterialTheme.typography.bodySmall,
                color = VarselSecondary
            )
        }
    }
}

@Composable
private fun ActualExpenseInsight() {

    val grossExpense = 22550
    val reimbursements = 4250
    val personalCost = grossExpense - reimbursements

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFEFF4EF)
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 0.dp
        )
    ) {

        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {

                Column(
                    verticalArrangement = Arrangement.spacedBy(3.dp)
                ) {

                    Text(
                        text = "Your actual cost",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = VarselPrimary
                    )

                    Text(
                        text = "After financial-event reimbursements",
                        style = MaterialTheme.typography.bodySmall,
                        color = VarselSecondary
                    )
                }

                Text(
                    text = formatCurrency(personalCost),
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = IncomeColor
                )
            }

            HorizontalDivider(
                color = Color(0xFFD9E1D9)
            )

            ImpactRow(
                label = "Recorded expenses",
                amount = grossExpense,
                color = VarselPrimary
            )

            ImpactRow(
                label = "Reimbursements",
                amount = -reimbursements,
                color = IncomeColor
            )

            ImpactRow(
                label = "Personal cost",
                amount = personalCost,
                color = VarselPrimary,
                emphasize = true
            )
        }
    }
}

@Composable
private fun ImpactRow(
    label: String,
    amount: Int,
    color: Color,
    emphasize: Boolean = false
) {

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {

        Text(
            text = label,
            style = if (emphasize) {
                MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Bold
                )
            } else {
                MaterialTheme.typography.bodyMedium
            },
            color = VarselSecondary
        )

        Text(
            text = if (amount < 0) {
                "-${formatCurrency(-amount)}"
            } else {
                formatCurrency(amount)
            },
            style = if (emphasize) {
                MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Bold
                )
            } else {
                MaterialTheme.typography.bodyMedium
            },
            color = color
        )
    }
}

@Composable
private fun FinancialEventsSection() {

    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {

            Column(
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {

                Text(
                    text = "Financial Events",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = VarselPrimary
                )

                Text(
                    text = "Tracked separately from ordinary transactions",
                    style = MaterialTheme.typography.bodySmall,
                    color = VarselSecondary
                )
            }

            Surface(
                shape = CircleShape,
                color = VarselPrimary
            ) {

                Box(
                    modifier = Modifier.size(32.dp),
                    contentAlignment = Alignment.Center
                ) {

                    Text(
                        text = financialEvents.size.toString(),
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = Color.White
                    )
                }
            }
        }

        financialEvents.forEach { event ->

            FinancialEventCard(
                event = event
            )
        }
    }
}

@Composable
private fun FinancialEventCard(
    event: FinancialEventPreview
) {

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = VarselSurface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 1.dp
        )
    ) {

        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {

                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {

                    Text(
                        text = event.title,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = VarselPrimary
                    )

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFF0F1EE)
                    ) {

                        Text(
                            text = event.category,
                            style = MaterialTheme.typography.labelSmall,
                            color = VarselSecondary,
                            modifier = Modifier.padding(
                                horizontal = 9.dp,
                                vertical = 5.dp
                            )
                        )
                    }
                }

                Text(
                    text = formatCurrency(event.finalExpense),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = VarselPrimary
                )
            }

            HorizontalDivider(
                color = VarselMuted
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {

                EventAmount(
                    label = "Expense",
                    amount = event.originalExpense,
                    color = ExpenseColor
                )

                EventAmount(
                    label = "Reimbursed",
                    amount = event.reimbursement,
                    color = ReimbursementColor
                )

                EventAmount(
                    label = "Personal cost",
                    amount = event.finalExpense,
                    color = IncomeColor
                )
            }
        }
    }
}

@Composable
private fun EventAmount(
    label: String,
    amount: Int,
    color: Color
) {

    Column(
        verticalArrangement = Arrangement.spacedBy(3.dp)
    ) {

        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = VarselSecondary
        )

        Text(
            text = formatCurrency(amount),
            style = MaterialTheme.typography.labelLarge.copy(
                fontWeight = FontWeight.SemiBold
            ),
            color = color
        )
    }
}

private fun formatCurrency(
    amount: Int
): String {

    return "₹${amount.toString().reversed().chunked(3).joinToString(",").reversed()}"
}
