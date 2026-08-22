import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
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

private val Background = Color(0xFFF5F5F2)
private val Surface = Color.White
private val Ink = Color(0xFF181A18)
private val Muted = Color(0xFF6B706B)

private val ExpenseColor = Color(0xFFD15A58)
private val IncomeColor = Color(0xFF2F8061)
private val IncomeBlue = Color(0xFF5C73C5)

private data class Category(
    val name: String,
    val amount: Int,
    val normal: Int,
    val financialEvent: Int,
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
    val expense: Int,
    val reimbursed: Int
) {
    val finalCost: Int
        get() = expense - reimbursed
}

private val expenseCategories = listOf(
    Category(
        name = "Food",
        amount = 7_000,
        normal = 6_000,
        financialEvent = 1_000,
        color = Color(0xFFD36A5A)
    ),
    Category(
        name = "Shopping",
        amount = 4_100,
        normal = 4_100,
        financialEvent = 0,
        color = Color(0xFF8A69A8)
    ),
    Category(
        name = "Transport",
        amount = 2_500,
        normal = 1_500,
        financialEvent = 1_000,
        color = Color(0xFF5D82B8)
    ),
    Category(
        name = "Bills",
        amount = 2_100,
        normal = 2_100,
        financialEvent = 0,
        color = Color(0xFFC29A4A)
    ),
    Category(
        name = "Other",
        amount = 2_600,
        normal = 2_600,
        financialEvent = 0,
        color = Color(0xFF6D8D78)
    )
)

private val incomeCategories = listOf(
    IncomeCategory(
        name = "Salary",
        amount = 30_000,
        color = IncomeColor
    ),
    IncomeCategory(
        name = "Other income",
        amount = 5_000,
        color = IncomeBlue
    )
)

private val financialEvents = listOf(
    FinancialEventPreview(
        title = "Team dinner",
        category = "Food",
        expense = 3_000,
        reimbursed = 2_000
    ),
    FinancialEventPreview(
        title = "Office travel",
        category = "Transport",
        expense = 5_000,
        reimbursed = 4_000
    ),
    FinancialEventPreview(
        title = "Client lunch",
        category = "Food",
        expense = 1_800,
        reimbursed = 1_200
    )
)

@Composable
fun ReportsPreview() {
    var selectedFlow by remember { mutableIntStateOf(0) }

    // -1 = Overall
    // >= 0 = selected category
    var selectedCategory by remember { mutableIntStateOf(-1) }

    var normalExpanded by remember { mutableStateOf(false) }
    var financialEventsExpanded by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFDCDDD8)),
        contentAlignment = Alignment.TopCenter
    ) {
        /*
         * Responsive phone-sized canvas.
         *
         * We deliberately use widthIn instead of a fixed width so the preview
         * behaves better when the GitHub Pages viewport is narrower/wider.
         */
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth()
                .widthIn(max = 390.dp)
                .background(Background)
                .verticalScroll(rememberScrollState())
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
                onFlowSelected = { flow ->
                    selectedFlow = flow
                    selectedCategory = -1
                    normalExpanded = false
                    financialEventsExpanded = false
                },
                selectedCategory = selectedCategory,
                onCategorySelected = { category ->
                    selectedCategory = category
                    normalExpanded = false
                    financialEventsExpanded = false
                },
                normalExpanded = normalExpanded,
                financialEventsExpanded = financialEventsExpanded,
                onNormalClicked = {
                    normalExpanded = !normalExpanded
                    financialEventsExpanded = false
                },
                onFinancialEventsClicked = {
                    financialEventsExpanded = !financialEventsExpanded
                    normalExpanded = false
                }
            )

            FinancialEventsSection()

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

/* -------------------------------------------------------------------------- */
/* Header                                                                     */
/* -------------------------------------------------------------------------- */

@Composable
private fun ReportsHeader() {
    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {

        Text(
            text = "Reports",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold
            ),
            color = Ink
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {

            Surface(
                shape = RoundedCornerShape(14.dp),
                color = Surface,
                shadowElevation = 1.dp
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    TextButton(
                        onClick = {}
                    ) {
                        Text(
                            text = "‹",
                            fontSize = 22.sp,
                            color = Ink
                        )
                    }

                    Text(
                        text = "August 2026",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = Ink
                    )

                    TextButton(
                        onClick = {}
                    ) {
                        Text(
                            text = "›",
                            fontSize = 22.sp,
                            color = Ink
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

/* -------------------------------------------------------------------------- */
/* Net Cash Flow                                                              */
/* -------------------------------------------------------------------------- */

@Composable
private fun NetCashFlowCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = Ink
        )
    ) {

        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            Text(
                text = "NET CASH FLOW",
                style = MaterialTheme.typography.labelMedium.copy(
                    letterSpacing = 1.2.sp,
                    fontWeight = FontWeight.Bold
                ),
                color = Color(0xFFB8BCB8)
            )

            Text(
                text = "₹12.5K",
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
                horizontalArrangement = Arrangement.SpaceBetween
            ) {

                NetAmount(
                    label = "Income",
                    value = "₹35K",
                    color = Color(0xFF72C49D)
                )

                NetAmount(
                    label = "Expense",
                    value = "₹22.5K",
                    color = Color(0xFFE98987)
                )
            }
        }
    }
}

@Composable
private fun NetAmount(
    label: String,
    value: String,
    color: Color
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(3.dp)
    ) {

        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = Color(0xFF969B96)
        )

        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.SemiBold
            ),
            color = color
        )
    }
}

/* -------------------------------------------------------------------------- */
/* Money Flow                                                                 */
/* -------------------------------------------------------------------------- */

@Composable
private fun MoneyFlowCard(
    selectedFlow: Int,
    onFlowSelected: (Int) -> Unit,
    selectedCategory: Int,
    onCategorySelected: (Int) -> Unit,
    normalExpanded: Boolean,
    financialEventsExpanded: Boolean,
    onNormalClicked: () -> Unit,
    onFinancialEventsClicked: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = Surface
        )
    ) {

        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            Text(
                text = "Money Flow",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = Ink
            )

            Text(
                text = "Explore where your money came from and where it went.",
                style = MaterialTheme.typography.bodyMedium,
                color = Muted
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {

                FlowSelector(
                    text = "Expenses",
                    selected = selectedFlow == 0,
                    color = ExpenseColor,
                    onClick = {
                        onFlowSelected(0)
                    },
                    modifier = Modifier.weight(1f)
                )

                FlowSelector(
                    text = "Income",
                    selected = selectedFlow == 1,
                    color = IncomeColor,
                    onClick = {
                        onFlowSelected(1)
                    },
                    modifier = Modifier.weight(1f)
                )
            }

            if (selectedFlow == 0) {

                ExpenseMoneyFlow(
                    selectedCategory = selectedCategory,
                    onCategorySelected = onCategorySelected,
                    normalExpanded = normalExpanded,
                    financialEventsExpanded = financialEventsExpanded,
                    onNormalClicked = onNormalClicked,
                    onFinancialEventsClicked = onFinancialEventsClicked
                )

            } else {

                IncomeMoneyFlow()
            }
        }
    }
}

@Composable
private fun FlowSelector(
    text: String,
    selected: Boolean,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier
) {
    Surface(
        modifier = modifier
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        color = if (selected) {
            color.copy(alpha = 0.14f)
        } else {
            Color(0xFFF1F2EF)
        }
    ) {

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            contentAlignment = Alignment.Center
        ) {

            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = if (selected) {
                        FontWeight.Bold
                    } else {
                        FontWeight.Medium
                    }
                ),
                color = if (selected) {
                    color
                } else {
                    Muted
                }
            )
        }
    }
}

/* -------------------------------------------------------------------------- */
/* Expense View                                                               */
/* -------------------------------------------------------------------------- */

@Composable
private fun ExpenseMoneyFlow(
    selectedCategory: Int,
    onCategorySelected: (Int) -> Unit,
    normalExpanded: Boolean,
    financialEventsExpanded: Boolean,
    onNormalClicked: () -> Unit,
    onFinancialEventsClicked: () -> Unit
) {
    val total = expenseCategories.sumOf { it.amount }

    val category =
        if (selectedCategory >= 0) {
            expenseCategories.getOrNull(selectedCategory)
        } else {
            null
        }

    /*
     * Overall is selected by default.
     *
     * The donut therefore opens showing the complete expense picture,
     * rather than opening on Food.
     */
    ExpenseDonutChart(
        values = expenseCategories.map { it.amount },
        colors = expenseCategories.map { it.color },
        selectedIndex = selectedCategory,
        centerValue = if (category == null) {
            shortCurrency(total)
        } else {
            shortCurrency(category.amount)
        },
        centerLabel = category?.name ?: "Overall",
        modifier = Modifier
            .fillMaxWidth()
            .height(250.dp)
    )

    CategoryListRow(
        name = "Overall",
        amount = total,
        total = total,
        color = Color(0xFF555955),
        selected = selectedCategory == -1,
        onClick = {
            onCategorySelected(-1)
        }
    )

    expenseCategories.forEachIndexed { index, categoryItem ->

        CategoryListRow(
            name = categoryItem.name,
            amount = categoryItem.amount,
            total = total,
            color = categoryItem.color,
            selected = selectedCategory == index,
            onClick = {
                onCategorySelected(index)
            }
        )
    }

    /*
     * Only display the drill-down when a real category has been selected.
     */
    if (category != null) {

        CategoryBreakdownCard(
            category = category,
            normalExpanded = normalExpanded,
            financialEventsExpanded = financialEventsExpanded,
            onNormalClicked = onNormalClicked,
            onFinancialEventsClicked = onFinancialEventsClicked
        )
    }
}

/* -------------------------------------------------------------------------- */
/* Income View                                                                */
/* -------------------------------------------------------------------------- */

@Composable
private fun IncomeMoneyFlow() {
    val total = incomeCategories.sumOf { it.amount }

    ExpenseDonutChart(
        values = incomeCategories.map { it.amount },
        colors = incomeCategories.map { it.color },
        selectedIndex = -1,
        centerValue = shortCurrency(total),
        centerLabel = "Overall",
        modifier = Modifier
            .fillMaxWidth()
            .height(250.dp)
    )

    incomeCategories.forEach { category ->

        CategoryListRow(
            name = category.name,
            amount = category.amount,
            total = total,
            color = category.color,
            selected = false,
            onClick = {}
        )
    }
}

/* -------------------------------------------------------------------------- */
/* Donut Chart                                                                */
/* -------------------------------------------------------------------------- */

@Composable
private fun ExpenseDonutChart(
    values: List<Int>,
    colors: List<Color>,
    selectedIndex: Int,
    centerValue: String,
    centerLabel: String,
    modifier: Modifier
) {
    val total = values.sum()

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {

        Canvas(
            modifier = Modifier.size(220.dp)
        ) {

            if (total > 0) {

                var startAngle = -90f

                val baseStroke = 32.dp.toPx()

                values.forEachIndexed { index, value ->

                    val sweepAngle =
                        value.toFloat() / total.toFloat() * 360f

                    val isSelected =
                        selectedIndex == index

                    val isOverall =
                        selectedIndex == -1

                    val alpha =
                        if (isOverall || isSelected) {
                            1f
                        } else {
                            0.20f
                        }

                    val strokeWidth =
                        if (isSelected) {
                            baseStroke + 5.dp.toPx()
                        } else {
                            baseStroke
                        }

                    drawArc(
                        color = colors[index].copy(alpha = alpha),
                        startAngle = startAngle,
                        sweepAngle = (sweepAngle - 2f).coerceAtLeast(0f),
                        useCenter = false,
                        topLeft = Offset(
                            baseStroke / 2f,
                            baseStroke / 2f
                        ),
                        size = Size(
                            width = size.width - baseStroke,
                            height = size.height - baseStroke
                        ),
                        style = Stroke(
                            width = strokeWidth,
                            cap = StrokeCap.Round
                        )
                    )

                    startAngle += sweepAngle
                }
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
                color = Ink
            )

            Text(
                text = centerLabel,
                style = MaterialTheme.typography.bodyMedium,
                color = Muted
            )
        }
    }
}

/* -------------------------------------------------------------------------- */
/* Category List                                                              */
/* -------------------------------------------------------------------------- */

@Composable
private fun CategoryListRow(
    name: String,
    amount: Int,
    total: Int,
    color: Color,
    selected: Boolean,
    onClick: () -> Unit
) {
    val percentage =
        if (total == 0) {
            0
        } else {
            (amount.toFloat() / total.toFloat() * 100f)
                .roundToInt()
        }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        color = if (selected) {
            color.copy(alpha = 0.10f)
        } else {
            Color.Transparent
        }
    ) {

        Row(
            modifier = Modifier.padding(
                horizontal = 10.dp,
                vertical = 10.dp
            ),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Box(
                modifier = Modifier
                    .size(9.dp)
                    .background(
                        color = color,
                        shape = CircleShape
                    )
            )

            Spacer(modifier = Modifier.width(10.dp))

            Text(
                text = name,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = if (selected) {
                        FontWeight.Bold
                    } else {
                        FontWeight.Normal
                    }
                ),
                color = Ink
            )

            Text(
                text = "$percentage%",
                style = MaterialTheme.typography.labelSmall,
                color = Muted
            )

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = shortCurrency(amount),
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                color = Ink
            )
        }
    }
}

/* -------------------------------------------------------------------------- */
/* Category Breakdown                                                         */
/* -------------------------------------------------------------------------- */

@Composable
private fun CategoryBreakdownCard(
    category: Category,
    normalExpanded: Boolean,
    financialEventsExpanded: Boolean,
    onNormalClicked: () -> Unit,
    onFinancialEventsClicked: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = Color(0xFFF4F4F0)
    ) {

        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {

            Text(
                text = "${category.name} breakdown",
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = Ink
            )

            Spacer(modifier = Modifier.height(4.dp))

            DrillDownRow(
                label = "Normal transactions",
                amount = shortCurrency(category.normal),
                expanded = normalExpanded,
                onClick = onNormalClicked
            )

            if (normalExpanded) {

                TransactionPreview(
                    category = category.name
                )
            }

            HorizontalDivider(
                color = Color(0xFFE1E2DE)
            )

            DrillDownRow(
                label = "Financial events",
                amount = shortCurrency(category.financialEvent),
                expanded = financialEventsExpanded,
                onClick = onFinancialEventsClicked
            )

            if (financialEventsExpanded) {

                FinancialEventPreviewList(
                    category = category.name
                )
            }
        }
    }
}

/* -------------------------------------------------------------------------- */
/* Drill Down                                                                 */
/* -------------------------------------------------------------------------- */

@Composable
private fun DrillDownRow(
    label: String,
    amount: String,
    expanded: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 11.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        Text(
            text = label,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.SemiBold
            ),
            color = Ink
        )

        Text(
            text = amount,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.Bold
            ),
            color = Ink
        )

        Spacer(modifier = Modifier.width(8.dp))

        Text(
            text = if (expanded) "⌃" else "›",
            style = MaterialTheme.typography.titleMedium,
            color = Muted
        )
    }
}

/* -------------------------------------------------------------------------- */
/* Normal Transactions Preview                                                */
/* -------------------------------------------------------------------------- */

@Composable
private fun TransactionPreview(
    category: String
) {
    Column(
        modifier = Modifier.padding(
            start = 8.dp,
            bottom = 8.dp
        ),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {

        Text(
            text = "Recent $category transactions",
            style = MaterialTheme.typography.labelMedium,
            color = Muted
        )

        TransactionLine(
            date = "21 Aug",
            description = "Restaurant",
            amount = "₹850"
        )

        TransactionLine(
            date = "19 Aug",
            description = "Groceries",
            amount = "₹1.45K"
        )

        TextButton(
            onClick = {}
        ) {
            Text("View all transactions →")
        }
    }
}

@Composable
private fun TransactionLine(
    date: String,
    description: String,
    amount: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {

        Text(
            text = date,
            modifier = Modifier.width(52.dp),
            style = MaterialTheme.typography.bodySmall,
            color = Muted
        )

        Text(
            text = description,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodySmall,
            color = Ink
        )

        Text(
            text = amount,
            style = MaterialTheme.typography.bodySmall.copy(
                fontWeight = FontWeight.SemiBold
            ),
            color = Ink
        )
    }
}

/* -------------------------------------------------------------------------- */
/* Financial Event Drill Down                                                 */
/* -------------------------------------------------------------------------- */

@Composable
private fun FinancialEventPreviewList(
    category: String
) {
    val matchingEvents =
        financialEvents.filter {
            it.category == category
        }

    Column(
        modifier = Modifier.padding(
            start = 8.dp,
            bottom = 8.dp
        ),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {

        Text(
            text = "Financial events in $category",
            style = MaterialTheme.typography.labelMedium,
            color = Muted
        )

        if (matchingEvents.isEmpty()) {

            Text(
                text = "No financial events in this category.",
                style = MaterialTheme.typography.bodySmall,
                color = Muted
            )

        } else {

            matchingEvents.forEach { event ->

                Column(
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {

                    Text(
                        text = event.title,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = Ink
                    )

                    Text(
                        text =
                            "${shortCurrency(event.expense)} expense → " +
                                "${shortCurrency(event.reimbursed)} reimbursed → " +
                                "${shortCurrency(event.finalCost)} final",
                        style = MaterialTheme.typography.labelSmall,
                        color = Muted
                    )
                }
            }
        }

        TextButton(
            onClick = {}
        ) {
            Text("View all financial events →")
        }
    }
}

/* -------------------------------------------------------------------------- */
/* Financial Events Section                                                   */
/* -------------------------------------------------------------------------- */

@Composable
private fun FinancialEventsSection() {

    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {

            Column(
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {

                Text(
                    text = "Financial Events",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = Ink
                )

                Text(
                    text = "A quick view of recent events",
                    style = MaterialTheme.typography.bodySmall,
                    color = Muted
                )
            }

            Surface(
                shape = CircleShape,
                color = Ink
            ) {

                Text(
                    text = financialEvents.size.toString(),
                    modifier = Modifier.padding(8.dp),
                    color = Color.White,
                    style = MaterialTheme.typography.labelMedium
                )
            }
        }

        /*
         * Deliberately limit the dashboard to two events.
         * The full event history belongs on the dedicated event report.
         */
        financialEvents
            .take(2)
            .forEach { event ->

                FinancialEventCard(event)
            }

        TextButton(
            modifier = Modifier.align(Alignment.End),
            onClick = {}
        ) {
            Text("View all financial events →")
        }
    }
}

@Composable
private fun FinancialEventCard(
    event: FinancialEventPreview
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Surface
        )
    ) {

        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {

                Column(
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {

                    Text(
                        text = event.title,
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = Ink
                    )

                    Text(
                        text = event.category,
                        style = MaterialTheme.typography.labelSmall,
                        color = Muted
                    )
                }

                Text(
                    text = shortCurrency(event.finalCost),
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = Ink
                )
            }

            Text(
                text =
                    "${shortCurrency(event.expense)} expense  →  " +
                        "${shortCurrency(event.reimbursed)} reimbursed",
                style = MaterialTheme.typography.bodySmall,
                color = Muted
            )
        }
    }
}

/* -------------------------------------------------------------------------- */
/* Currency Formatting                                                        */
/* -------------------------------------------------------------------------- */

private fun shortCurrency(
    value: Int
): String {

    return when {

        value >= 1_00_00_000 -> {

            val amount =
                value / 1_00_00_000f

            "₹${formatCompact(amount)}Cr"
        }

        value >= 1_00_000 -> {

            val amount =
                value / 1_00_000f

            "₹${formatCompact(amount)}L"
        }

        value >= 1_000 -> {

            val amount =
                value / 1_000f

            "₹${formatCompact(amount)}K"
        }

        else -> {
            "₹$value"
        }
    }
}

private fun formatCompact(
    value: Float
): String {
    val rounded = (value * 10f).roundToInt() / 10f

    return if (rounded % 1f == 0f) {
        rounded.toInt().toString()
    } else {
        rounded.toString()
    }
}
