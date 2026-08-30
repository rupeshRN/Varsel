package com.varsel.expensetracker.ui.dashboard

<<<<<<< HEAD
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
=======
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Construction
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.SwapHoriz
>>>>>>> source-repo/main
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
<<<<<<< HEAD
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.varsel.expensetracker.domain.model.Transaction
import com.varsel.expensetracker.domain.model.TransactionType
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import com.varsel.expensetracker.ui.model.TransactionUiModel
import com.varsel.expensetracker.ui.dashboard.components.BalanceCard
import com.varsel.expensetracker.ui.dashboard.components.DashboardLoanWidget
import com.varsel.expensetracker.ui.dashboard.components.GreetingHeader
import com.varsel.expensetracker.ui.dashboard.components.InsightsCard
import com.varsel.expensetracker.ui.dashboard.components.DashboardRecentSection
=======
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.varsel.expensetracker.ui.dashboard.components.BalanceCard
import com.varsel.expensetracker.ui.dashboard.components.DashboardLoanWidget
import com.varsel.expensetracker.ui.dashboard.components.DashboardRecentSection
import com.varsel.expensetracker.ui.dashboard.components.GreetingHeader
import com.varsel.expensetracker.ui.dashboard.components.InsightsCard
import com.varsel.expensetracker.ui.dashboard.components.QuickActionBar
import com.varsel.expensetracker.ui.model.TransactionUiModel

private sealed class FeatureDialogState {
    object None : FeatureDialogState()
    data class UnderDevelopment(
        val title: String,
        val message: String,
        val icon: ImageVector
    ) : FeatureDialogState()
}
>>>>>>> source-repo/main

@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    onNavigateToAllTransactions: () -> Unit,
<<<<<<< HEAD
    onNavigateToSettings: () -> Unit = {},
    onNavigateToLoans: () -> Unit = {}
) {
    // Lifecycle-aware flow collection to prevent background CPU cycles
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var selectedTransaction by remember { mutableStateOf<Transaction?>(null) }
=======
    onNavigateToImport: () -> Unit = {},
    onNavigateToAnalytics: () -> Unit = {},
    onNavigateToTransactionDetail: (Long) -> Unit = {},
    onNavigateToSettings: () -> Unit = {},
    onNavigateToLoans: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var featureDialog by remember { mutableStateOf<FeatureDialogState>(FeatureDialogState.None) }
>>>>>>> source-repo/main

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
<<<<<<< HEAD

    if (uiState.isLoading) {

        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }

    } else {

        LazyColumn(

            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),

            verticalArrangement = Arrangement.spacedBy(16.dp)

        ) {

            item(key = "greeting") {

                GreetingHeader(
                    onSettingsClick = onNavigateToSettings
                )

            }

            item(key = "balance_card") {

                BalanceCard(
                    summary = uiState.balanceSummary
                )

            }

            item(key = "loans_widget") {

                DashboardLoanWidget(
                    loans = uiState.loans,
                    onNavigateToLoans = onNavigateToLoans
                )

            }

            item(key = "insights") {

                InsightsCard()

            }

            item(
                key = "recent_transactions"
            ) {

                DashboardRecentSection(

                    transactions = uiState.recentTransactions,

                    onViewAll = onNavigateToAllTransactions,

                    onTransactionClick = {

                        // TODO: Reconnect edit dialog
                        // after UI → Domain migration.

                    }
                )
            }
        }
    }
}
}

@Composable
fun TransactionItemCard(
    transaction: Transaction,
    onClick: () -> Unit
) {
    val dateFormatter = DateTimeFormatter.ofPattern("dd-MMM-yyyy", Locale.ENGLISH)
    val dateStr = try {
        val date = Instant.ofEpochMilli(transaction.dateTimestamp)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()
        date.format(dateFormatter)
    } catch (e: Exception) {
        "Recent"
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = transaction.description.ifEmpty { "Transaction" },
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = transaction.category.ifEmpty { "Uncategorized" },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "• $dateStr",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Spacer(modifier = Modifier.width(8.dp))
            val isIncome = transaction.type == TransactionType.INCOME
            val amountPrefix = if (isIncome) "+" else "-"
            val amountColor = if (isIncome) Color(0xFF2E7D32) else Color(0xFFC62828)

            Text(
                text = "$amountPrefix₹%.2f".format(transaction.amount),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = amountColor
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun EditTransactionCategoryDialog(
    transaction: Transaction,
    onDismiss: () -> Unit,
    onSave: (Transaction) -> Unit
) {
    var categoryInput by remember { mutableStateOf(transaction.category) }
    var descriptionInput by remember { mutableStateOf(transaction.description) }

    val suggestedCategories = listOf("Food & Drink", "Groceries", "Transport", "Shopping", "Utilities", "Salary", "Income", "Subscriptions", "Uncategorized")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Transaction") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = descriptionInput,
                    onValueChange = { descriptionInput = it },
                    label = { Text("Description / Name") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = categoryInput,
                    onValueChange = { categoryInput = it },
                    label = { Text("Category") },
                    modifier = Modifier.fillMaxWidth()
                )

                Text("Quick Categories:", style = MaterialTheme.typography.labelMedium)
                
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    suggestedCategories.forEach { cat ->
                        SuggestionChip(
                            onClick = { categoryInput = cat },
                            label = { Text(cat) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val updated = transaction.copy(
                        description = descriptionInput.ifEmpty { transaction.description },
                        category = categoryInput.ifEmpty { "Uncategorized" }
                    )
                    onSave(updated)
                }
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
=======
        if (uiState.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp),
                contentPadding = PaddingValues(top = 8.dp, bottom = 24.dp)
            ) {
                item(key = "greeting") {
                    GreetingHeader(
                        onSettingsClick = onNavigateToSettings
                    )
                }

                item(key = "balance_card") {
                    BalanceCard(
                        summary = uiState.balanceSummary
                    )
                }

                item(key = "quick_actions") {
                    QuickActionBar(
                        onImportClick = onNavigateToImport,
                        onAddTransactionClick = {
                            featureDialog = FeatureDialogState.UnderDevelopment(
                                title = "Manual Entry In Development",
                                message = "Varsel is designed to automatically ingest, categorize, and reconcile transactions directly from your bank statements with zero manual input.\n\nManual expense and income creation is planned for users who prefer manual bookkeeping.",
                                icon = Icons.Outlined.Construction
                            )
                        },
                        onTransferClick = {
                            featureDialog = FeatureDialogState.UnderDevelopment(
                                title = "Account Transfer",
                                message = "Transfer transactions are automatically identified and linked between your accounts during Statement Import.\n\nDirect manual transfer entry is currently under development.",
                                icon = Icons.Outlined.SwapHoriz
                            )
                        },
                        onAnalyticsClick = onNavigateToAnalytics
                    )
                }

                item(key = "insights") {
                    InsightsCard(
                        insights = uiState.insights,
                        onNavigateToAnalytics = onNavigateToAnalytics,
                        onNavigateToTransactions = onNavigateToAllTransactions
                    )
                }

                item(key = "loans_widget") {
                    DashboardLoanWidget(
                        loans = uiState.loans,
                        onNavigateToLoans = onNavigateToLoans
                    )
                }

                item(key = "recent_transactions") {
                    DashboardRecentSection(
                        transactions = uiState.recentTransactions,
                        onViewAll = onNavigateToAllTransactions,
                        onTransactionClick = { transactionUiModel ->
                            onNavigateToTransactionDetail(transactionUiModel.id)
                        }
                    )
                }
            }
        }

        // Under development feature dialog
        when (val state = featureDialog) {
            is FeatureDialogState.UnderDevelopment -> {
                AlertDialog(
                    onDismissRequest = { featureDialog = FeatureDialogState.None },
                    icon = {
                        Icon(
                            imageVector = state.icon,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(28.dp)
                        )
                    },
                    title = {
                        Text(
                            text = state.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    text = {
                        Text(
                            text = state.message,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    confirmButton = {
                        TextButton(
                            onClick = { featureDialog = FeatureDialogState.None }
                        ) {
                            Text("Got It")
                        }
                    },
                    shape = RoundedCornerShape(20.dp)
                )
            }
            FeatureDialogState.None -> Unit
        }
    }
>>>>>>> source-repo/main
}
