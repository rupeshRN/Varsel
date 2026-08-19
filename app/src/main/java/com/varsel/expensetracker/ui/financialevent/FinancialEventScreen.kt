package com.varsel.expensetracker.ui.financialevent

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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
                    .verticalScroll(
                        scrollState
                    )
                    .padding(24.dp),

            verticalArrangement =
                Arrangement.spacedBy(16.dp)
        ) {

            when (val state = uiState) {

                FinancialEventUiState.Loading -> {

                    Text(
                        text =
                            "Loading financial event..."
                    )
                }

                is FinancialEventUiState.Error -> {

                    Text(
                        text =
                            state.message
                    )
                }

                is FinancialEventUiState.Loaded -> {

                    //--------------------------------------------------
                    // Event header
                    //--------------------------------------------------

                    Text(
                        text =
                            state.group.groupName,

                        style =
                            MaterialTheme.typography
                                .headlineSmall,

                        fontWeight =
                            FontWeight.Bold
                    )

                    Text(
                        text =
                            state.group.category,

                        style =
                            MaterialTheme.typography
                                .bodyMedium,

                        color =
                            MaterialTheme.colorScheme
                                .onSurfaceVariant
                    )

                    //--------------------------------------------------
                    // Expenses
                    //--------------------------------------------------

                    Text(
                        text =
                            "Expenses",

                        style =
                            MaterialTheme.typography
                                .titleMedium,

                        fontWeight =
                            FontWeight.SemiBold
                    )

                    if (
                        state.expenses.isEmpty()
                    ) {

                        Text(
                            text =
                                "No expenses linked.",

                            style =
                                MaterialTheme.typography
                                    .bodyMedium,

                            color =
                                MaterialTheme.colorScheme
                                    .onSurfaceVariant
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
                            MaterialTheme.typography
                                .titleMedium,

                        fontWeight =
                            FontWeight.SemiBold
                    )

                    if (
                        state.reimbursements.isEmpty()
                    ) {

                        Text(
                            text =
                                "No reimbursements linked.",

                            style =
                                MaterialTheme.typography
                                    .bodyMedium,

                            color =
                                MaterialTheme.colorScheme
                                    .onSurfaceVariant
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

                    //--------------------------------------------------
                    // Summary
                    //--------------------------------------------------

                    Spacer(
                        modifier =
                            Modifier.padding(
                                top = 8.dp
                            )
                    )

                    Card(
                        modifier =
                            Modifier.fillMaxWidth()
                    ) {

                        Column(
                            modifier =
                                Modifier.padding(16.dp),

                            verticalArrangement =
                                Arrangement.spacedBy(8.dp)
                        ) {

                            Text(
                                text =
                                    "Event Summary",

                                style =
                                    MaterialTheme.typography
                                        .titleMedium,

                                fontWeight =
                                    FontWeight.SemiBold
                            )

                            Text(
                                text =
                                    "Total Expenses: " +
                                        "₹%,.2f".format(
                                            state.totalExpenses
                                        )
                            )

                            Text(
                                text =
                                    "Total Reimbursed: " +
                                        "₹%,.2f".format(
                                            state.totalReimbursements
                                        )
                            )

                            Text(
                                text =
                                    "My Actual Expense: " +
                                        "₹%,.2f".format(
                                            state.actualExpense
                                        ),

                                fontWeight =
                                    FontWeight.Bold
                            )
                        }
                    }

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

    Card(
        modifier =
            Modifier.fillMaxWidth()
    ) {

        Row(

            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp),

            horizontalArrangement =
                Arrangement.SpaceBetween
        ) {

            Column(
                modifier =
                    Modifier.weight(1f),

                verticalArrangement =
                    Arrangement.spacedBy(4.dp)
            ) {

                Text(
                    text =
                        description,

                    style =
                        MaterialTheme.typography
                            .bodyMedium
                )

                Text(
                    text =
                        date,

                    style =
                        MaterialTheme.typography
                            .bodySmall,

                    color =
                        MaterialTheme.colorScheme
                            .onSurfaceVariant
                )
            }

            Text(
                text =
                    "₹%,.2f".format(amount),

                style =
                    MaterialTheme.typography
                        .bodyMedium,

                fontWeight =
                    FontWeight.SemiBold
            )
        }
    }
}
