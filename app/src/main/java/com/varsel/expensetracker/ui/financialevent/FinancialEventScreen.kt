package com.varsel.expensetracker.ui.financialevent

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.varsel.expensetracker.domain.model.TransactionType
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FinancialEventScreen(
    transactionLinkId: String,
    onBackClick: () -> Unit
) {

    val viewModel: FinancialEventViewModel =
        hiltViewModel()

    val uiState by
        viewModel.uiState
            .collectAsStateWithLifecycle()

    val scrollState =
        rememberScrollState()

    LaunchedEffect(transactionLinkId) {

        viewModel.loadFinancialEvent(
            transactionLinkId
        )
    }

    Scaffold(

        topBar = {

            CenterAlignedTopAppBar(

                title = {
                    Text("Financial Event")
                },

                navigationIcon = {

                    IconButton(
                        onClick = onBackClick
                    ) {

                        Icon(
                            imageVector =
                                Icons.Default.ArrowBack,

                            contentDescription =
                                "Back"
                        )
                    }
                }
            )
        }

    ) { padding ->

        Column(

            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(scrollState)
                    .padding(24.dp),

            verticalArrangement =
                Arrangement.spacedBy(16.dp)

        ) {

            when (val state = uiState) {

                FinancialEventUiState.Loading -> {

                    Text("Loading financial event...")
                }

                is FinancialEventUiState.Error -> {

                    Text(
                        text = state.message
                    )
                }

                is FinancialEventUiState.Loaded -> {

                    val event =
                        state.group

                    Text(
                        text =
                            event.groupName,

                        style =
                            androidx.compose.material3
                                .MaterialTheme
                                .typography
                                .headlineSmall
                    )

                    Text(
                        text =
                            event.category,

                        style =
                            androidx.compose.material3
                                .MaterialTheme
                                .typography
                                .bodyMedium,

                        color =
                            androidx.compose.material3
                                .MaterialTheme
                                .colorScheme
                                .onSurfaceVariant
                    )

                    Spacer(
                        modifier =
                            Modifier.padding(
                                top = 4.dp
                            )
                    )

                    //--------------------------------------------------
                    // Expenses
                    //--------------------------------------------------

                    Text(
                        text = "Expenses",

                        style =
                            androidx.compose.material3
                                .MaterialTheme
                                .typography
                                .titleMedium
                    )

                    if (state.expenses.isEmpty()) {

                        Text(
                            text =
                                "No expenses linked.",

                            style =
                                androidx.compose.material3
                                    .MaterialTheme
                                    .typography
                                    .bodyMedium
                        )

                    } else {

                        state.expenses.forEach { transaction ->

                            FinancialEventTransactionRow(

                                description =
                                    transaction.description,

                                amount =
                                    transaction.amount,

                                dateTimestamp =
                                    transaction.dateTimestamp
                            )
                        }
                    }

                    //--------------------------------------------------
                    // Reimbursements
                    //--------------------------------------------------

                    Text(
                        text =
                            "Reimbursements",

                        style =
                            androidx.compose.material3
                                .MaterialTheme
                                .typography
                                .titleMedium
                    )

                    if (
                        state.reimbursements.isEmpty()
                    ) {

                        Text(
                            text =
                                "No reimbursements linked.",

                            style =
                                androidx.compose.material3
                                    .MaterialTheme
                                    .typography
                                    .bodyMedium
                        )

                    } else {

                        state.reimbursements.forEach {
                            transaction ->

                            FinancialEventTransactionRow(

                                description =
                                    transaction.description,

                                amount =
                                    transaction.amount,

                                dateTimestamp =
                                    transaction.dateTimestamp
                            )
                        }
                    }

                    Spacer(
                        modifier =
                            Modifier.padding(
                                top = 8.dp
                            )
                    )

                    //--------------------------------------------------
                    // Summary
                    //--------------------------------------------------

                    Text(
                        text =
                            "Total Expenses: " +
                                "₹%,.2f"
                                    .format(
                                        state.totalExpenses
                                    ),

                        style =
                            androidx.compose.material3
                                .MaterialTheme
                                .typography
                                .bodyLarge
                    )

                    Text(
                        text =
                            "Total Reimbursed: " +
                                "₹%,.2f"
                                    .format(
                                        state.totalReimbursements
                                    ),

                        style =
                            androidx.compose.material3
                                .MaterialTheme
                                .typography
                                .bodyLarge
                    )

                    Text(
                        text =
                            "My Actual Expense: " +
                                "₹%,.2f"
                                    .format(
                                        state.actualExpense
                                    ),

                        style =
                            androidx.compose.material3
                                .MaterialTheme
                                .typography
                                .titleMedium,

                        fontWeight =
                            androidx.compose.ui.text.font
                                .FontWeight.Bold
                    )

                    Spacer(
                        modifier =
                            Modifier.padding(
                                bottom = 24.dp
                            )
                    )
                }
            }
        }
    }
}

@Composable
private fun FinancialEventTransactionRow(
    description: String,
    amount: Double,
    dateTimestamp: Long
) {

    val date =
        SimpleDateFormat(
            "dd MMM yyyy",
            Locale.ENGLISH
        ).format(
            Date(dateTimestamp)
        )

    androidx.compose.material3.Card(
        modifier =
            Modifier.fillMaxSize()
    ) {

        Column(
            modifier =
                Modifier.padding(16.dp),

            verticalArrangement =
                Arrangement.spacedBy(4.dp)
        ) {

            androidx.compose.foundation.layout.Row(

                modifier =
                    Modifier.fillMaxSize(),

                horizontalArrangement =
                    Arrangement.SpaceBetween
            ) {

                Text(
                    text = description,

                    modifier =
                        Modifier.weight(1f),

                    style =
                        androidx.compose.material3
                            .MaterialTheme
                            .typography
                            .bodyMedium
                )

                Text(
                    text =
                        "₹%,.2f".format(amount),

                    style =
                        androidx.compose.material3
                            .MaterialTheme
                            .typography
                            .bodyMedium
                )
            }

            Text(
                text = date,

                style =
                    androidx.compose.material3
                        .MaterialTheme
                        .typography
                        .bodySmall,

                color =
                    androidx.compose.material3
                        .MaterialTheme
                        .colorScheme
                        .onSurfaceVariant
            )
        }
    }
}
