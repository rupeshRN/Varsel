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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.varsel.expensetracker.domain.model.Transaction
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FinancialEventScreen(

    transactionLinkId: String,

    onBackClick: () -> Unit

) {

    val viewModel:
        FinancialEventViewModel =
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
                        onClick =
                            onBackClick
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

            when (
                val state = uiState
            ) {

                FinancialEventUiState.Loading -> {

                    Text(
                        "Loading financial event..."
                    )
                }

                is FinancialEventUiState.Error -> {

                    Text(
                        state.message
                    )
                }

                is FinancialEventUiState.Loaded -> {

                    //--------------------------------------------------
                    // Header
                    //--------------------------------------------------

                    Card(
                        modifier =
                            Modifier.fillMaxWidth()
                    ) {

                        Column(
                            modifier =
                                Modifier.padding(
                                    16.dp
                                ),

                            verticalArrangement =
                                Arrangement.spacedBy(
                                    8.dp
                                )
                        ) {

                            Text(
                                text =
                                    state.group
                                        .groupName,

                                style =
                                    MaterialTheme
                                        .typography
                                        .headlineSmall,

                                fontWeight =
                                    FontWeight.Bold
                            )

                            Text(
                                text =
                                    state.group
                                        .category,

                                style =
                                    MaterialTheme
                                        .typography
                                        .bodyMedium,

                                color =
                                    MaterialTheme
                                        .colorScheme
                                        .onSurfaceVariant
                            )

                            OutlinedButton(
                                onClick =
                                    viewModel
                                        ::startEditingGroup,

                                enabled =
                                    !state.isUpdating,

                                modifier =
                                    Modifier
                                        .fillMaxWidth()
                            ) {

                                Text(
                                    "Edit Financial Event"
                                )
                            }
                        }
                    }

                    //--------------------------------------------------
                    // Expenses
                    //--------------------------------------------------

                    Text(
                        text =
                            "Expenses",

                        style =
                            MaterialTheme
                                .typography
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

                            color =
                                MaterialTheme
                                    .colorScheme
                                    .onSurfaceVariant
                        )

                    } else {

                        state.expenses.forEach {
                            transaction ->

                            FinancialEventTransactionRow(

                                transaction =
                                    transaction,

                                onRemove = {

                                    viewModel
                                        .removeTransaction(
                                            transaction.id
                                        )
                                },

                                enabled =
                                    !state.isUpdating
                            )
                        }
                    }

                    //--------------------------------------------------
                    // Add expense
                    //--------------------------------------------------

                    if (
                        state.availableExpenses
                            .isNotEmpty()
                    ) {

                        Text(
                            text =
                                "Add Expense",

                            style =
                                MaterialTheme
                                    .typography
                                    .titleSmall
                        )

                        state.availableExpenses
                            .forEach {
                                transaction ->

                                AvailableTransactionRow(

                                    transaction =
                                        transaction,

                                    buttonText =
                                        "Add",

                                    onClick = {

                                        viewModel
                                            .addExpense(
                                                transaction.id
                                            )
                                    },

                                    enabled =
                                        !state.isUpdating
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
                            MaterialTheme
                                .typography
                                .titleMedium,

                        fontWeight =
                            FontWeight.SemiBold
                    )

                    if (
                        state.reimbursements
                            .isEmpty()
                    ) {

                        Text(
                            text =
                                "No reimbursements linked.",

                            color =
                                MaterialTheme
                                    .colorScheme
                                    .onSurfaceVariant
                        )

                    } else {

                        state.reimbursements
                            .forEach {
                                transaction ->

                                FinancialEventTransactionRow(

                                    transaction =
                                        transaction,

                                    onRemove = {

                                        viewModel
                                            .removeTransaction(
                                                transaction.id
                                            )
                                    },

                                    enabled =
                                        !state.isUpdating
                                )
                            }
                    }

                    //--------------------------------------------------
                    // Add reimbursement
                    //--------------------------------------------------

                    if (
                        state.availableReimbursements
                            .isNotEmpty()
                    ) {

                        Text(
                            text =
                                "Add Reimbursement",

                            style =
                                MaterialTheme
                                    .typography
                                    .titleSmall
                        )

                        state.availableReimbursements
                            .forEach {
                                transaction ->

                                AvailableTransactionRow(

                                    transaction =
                                        transaction,

                                    buttonText =
                                        "Add",

                                    onClick = {

                                        viewModel
                                            .addReimbursement(
                                                transaction.id
                                            )
                                    },

                                    enabled =
                                        !state.isUpdating
                                )
                            }
                    }

                    //--------------------------------------------------
                    // Summary
                    //--------------------------------------------------

                    Card(
                        modifier =
                            Modifier.fillMaxWidth()
                    ) {

                        Column(
                            modifier =
                                Modifier.padding(
                                    16.dp
                                ),

                            verticalArrangement =
                                Arrangement.spacedBy(
                                    8.dp
                                )
                        ) {

                            Text(
                                text =
                                    "Event Summary",

                                style =
                                    MaterialTheme
                                        .typography
                                        .titleMedium,

                                fontWeight =
                                    FontWeight.SemiBold
                            )

                            Text(
                                text =
                                    "Total Expenses: " +
                                        "₹%,.2f".format(
                                            state
                                                .totalExpenses
                                        )
                            )

                            Text(
                                text =
                                    "Total Reimbursed: " +
                                        "₹%,.2f".format(
                                            state
                                                .totalReimbursements
                                        )
                            )

                            Text(
                                text =
                                    "My Actual Expense: " +
                                        "₹%,.2f".format(
                                            state
                                                .actualExpense
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

                    //--------------------------------------------------
                    // Edit dialog
                    //--------------------------------------------------

                    if (
                        state.isEditingGroup
                    ) {

                        EditFinancialEventDialog(

                            initialName =
                                state.group
                                    .groupName,

                            initialCategory =
                                state.group
                                    .category,

                            onDismiss =
                                viewModel
                                    ::cancelEditingGroup,

                            onSave =
                                viewModel
                                    ::saveGroup
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FinancialEventTransactionRow(

    transaction: Transaction,

    onRemove: () -> Unit,

    enabled: Boolean

) {

    val date =
        SimpleDateFormat(
            "dd MMM yyyy",
            Locale.ENGLISH
        ).format(
            Date(
                transaction.dateTimestamp
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
                Arrangement.spacedBy(
                    6.dp
                )
        ) {

            Row(

                modifier =
                    Modifier.fillMaxWidth(),

                horizontalArrangement =
                    Arrangement
                        .SpaceBetween
            ) {

                Column(
                    modifier =
                        Modifier.weight(1f)
                ) {

                    Text(
                        transaction.description,

                        style =
                            MaterialTheme
                                .typography
                                .bodyMedium
                    )

                    Text(
                        date,

                        style =
                            MaterialTheme
                                .typography
                                .bodySmall,

                        color =
                            MaterialTheme
                                .colorScheme
                                .onSurfaceVariant
                    )
                }

                Text(
                    "₹%,.2f".format(
                        transaction.amount
                    ),

                    fontWeight =
                        FontWeight.SemiBold
                )
            }

            OutlinedButton(

                onClick =
                    onRemove,

                enabled =
                    enabled,

                modifier =
                    Modifier.fillMaxWidth()
            ) {

                Text("Remove from Event")
            }
        }
    }
}

@Composable
private fun AvailableTransactionRow(

    transaction: Transaction,

    buttonText: String,

    onClick: () -> Unit,

    enabled: Boolean

) {

    val date =
        SimpleDateFormat(
            "dd MMM yyyy",
            Locale.ENGLISH
        ).format(
            Date(
                transaction.dateTimestamp
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
                Arrangement.spacedBy(
                    6.dp
                )
        ) {

            Row(

                modifier =
                    Modifier.fillMaxWidth(),

                horizontalArrangement =
                    Arrangement
                        .SpaceBetween
            ) {

                Column(
                    modifier =
                        Modifier.weight(1f)
                ) {

                    Text(
                        transaction.description,

                        style =
                            MaterialTheme
                                .typography
                                .bodyMedium
                    )

                    Text(
                        date,

                        style =
                            MaterialTheme
                                .typography
                                .bodySmall,

                        color =
                            MaterialTheme
                                .colorScheme
                                .onSurfaceVariant
                    )
                }

                Text(
                    "₹%,.2f".format(
                        transaction.amount
                    ),

                    fontWeight =
                        FontWeight.SemiBold
                )
            }

            Button(

                onClick =
                    onClick,

                enabled =
                    enabled,

                modifier =
                    Modifier.fillMaxWidth()
            ) {

                Text(buttonText)
            }
        }
    }
}

@Composable
private fun EditFinancialEventDialog(

    initialName: String,

    initialCategory: String,

    onDismiss: () -> Unit,

    onSave: (
        String,
        String
    ) -> Unit

) {

    var groupName by
        remember(initialName) {
            mutableStateOf(
                initialName
            )
        }

    var category by
        remember(initialCategory) {
            mutableStateOf(
                initialCategory
            )
        }

    AlertDialog(

        onDismissRequest =
            onDismiss,

        title = {
            Text(
                "Edit Financial Event"
            )
        },

        text = {

            Column(
                verticalArrangement =
                    Arrangement.spacedBy(
                        12.dp
                    )
            ) {

                OutlinedTextField(

                    value =
                        groupName,

                    onValueChange = {
                        groupName = it
                    },

                    label = {
                        Text("Event name")
                    },

                    singleLine = true,

                    modifier =
                        Modifier.fillMaxWidth()
                )

                OutlinedTextField(

                    value =
                        category,

                    onValueChange = {
                        category = it
                    },

                    label = {
                        Text("Category")
                    },

                    singleLine = true,

                    modifier =
                        Modifier.fillMaxWidth()
                )
            }
        },

        confirmButton = {

            Button(

                onClick = {

                    onSave(
                        groupName,
                        category
                    )
                },

                enabled =
                    groupName.isNotBlank() &&
                    category.isNotBlank()
            ) {

                Text("Save")
            }
        },

        dismissButton = {

            OutlinedButton(
                onClick =
                    onDismiss
            ) {

                Text("Cancel")
            }
        }
    )
}
