package com.varsel.expensetracker.ui.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.varsel.expensetracker.domain.model.Transaction
import com.varsel.expensetracker.domain.model.TransactionType
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    onNavigateToImport: () -> Unit,
    onNavigateToCategories: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Expense Tracker Dashboard") }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            Button(onClick = onNavigateToImport) {
                Text("Import Statement")
            }
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = onNavigateToCategories) {
                Text("Manage Categories")
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text("Recent Transactions", style = MaterialTheme.typography.titleMedium)

            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(uiState.recentTransactions) { transaction ->
                    TransactionItem(transaction = transaction)
                }
            }
        }
    }
}

@Composable
fun TransactionItem(transaction: Transaction) {
    val formattedDate = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
        .format(Date(transaction.dateTimestamp))

    val isCredit = transaction.type == TransactionType.CREDIT || transaction.type == TransactionType.INCOME

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(text = transaction.description, style = MaterialTheme.typography.bodyLarge)
                Text(text = formattedDate, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(text = transaction.category, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
            }
            Text(
                text = "${if (isCredit) "+" else "-"}$${transaction.amount}",
                style = MaterialTheme.typography.bodyLarge,
                color = if (isCredit) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.error
            )
        }
    }
}
