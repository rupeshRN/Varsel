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
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.ExposedDropdownMenu
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
import androidx.compose.material3.DropdownMenuItem

@OptIn(ExperimentalMaterial3Api::class)
private enum class AddTransactionMode {
    EXPENSE,
    REIMBURSEMENT
}

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

    var addMode by
        remember {
            mutableStateOf<AddTransactionMode?>(null)
        }

    LaunchedEffect(transactionLinkId) {

        viewModel.loadFinancialEvent(
            transactionLinkId
        )
    }

    Scaffold(

        topBar = {

            CenterAlignedTopAppBar(

                title = {
                    Text(
                        "Financial Event"
                    )
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
                Arrangement.spacedBy(
                    16.dp
                )
        ) {

            when (
                val state = uiState
            ) {

                //--------------------------------------------------
                // Loading
                //--------------------------------------------------

                FinancialEventUiState.Loading -> {

                    Text(
                        "Loading financial event..."
                    )
                }

                //--------------------------------------------------
                // Error
                //--------------------------------------------------

                is FinancialEventUiState.Error -> {

                    Text(
                        state.message
                    )
                }

                //--------------------------------------------------
                // Loaded
                //--------------------------------------------------

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
                    // Add expenses
                    //--------------------------------------------------

                    if (
                        state.availableExpenses
                            .isNotEmpty()
                    ) {

                        OutlinedButton(

                            onClick = {

                                addMode =
                                    AddTransactionMode
                                        .EXPENSE
                            },

                            enabled =
                                !state.isUpdating,

                            modifier =
                                Modifier.fillMaxWidth()

                        ) {

                            Text(
                                "Add Expenses"
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
                    // Add reimbursements
                    //--------------------------------------------------

                    if (
                        state.availableReimbursements
                            .isNotEmpty()
                    ) {

                        OutlinedButton(

                            onClick = {

                                addMode =
                                    AddTransactionMode
                                        .REIMBURSEMENT
                            },

                            enabled =
                                !state.isUpdating,

                            modifier =
                                Modifier.fillMaxWidth()

                        ) {

                            Text(
                                "Add Reimbursements"
                            )
                        }
                    }

                    //--------------------------------------------------
                    // Event Summary
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

                    //--------------------------------------------------
                    // Month-wise transaction picker
                    //--------------------------------------------------

                    if (
                        addMode != null
                    ) {

                        val transactions =
                            when (addMode) {

                                AddTransactionMode
                                    .EXPENSE ->

                                    state
                                        .availableExpenses

                                AddTransactionMode
                                    .REIMBURSEMENT ->

                                    state
                                        .availableReimbursements

                                null ->
                                    emptyList()
                            }

                        MonthWiseTransactionPicker(

                            title =
                                when (addMode) {

                                    AddTransactionMode
                                        .EXPENSE ->

                                        "Add Expenses"

                                    AddTransactionMode
                                        .REIMBURSEMENT ->

                                        "Add Reimbursements"

                                    null ->
                                        ""
                                },

                            transactions =
                                transactions,

                            isUpdating =
                                state.isUpdating,

                            onDismiss = {

                                addMode =
                                    null
                            },

                            onConfirm = {
                                selectedIds ->

                                when (addMode) {

                                    AddTransactionMode
                                        .EXPENSE -> {

                                        viewModel
                                            .addExpenses(
                                                selectedIds
                                            )
                                    }

                                    AddTransactionMode
                                        .REIMBURSEMENT -> {

                                        viewModel
                                            .addReimbursements(
                                                selectedIds
                                            )
                                    }

                                    null -> Unit
                                }

                                addMode =
                                    null
                            }
                        )
                    }

                    //--------------------------------------------------
                    // Bottom spacing
                    //--------------------------------------------------

                    Spacer(
                        modifier =
                            Modifier.padding(
                                bottom = 24.dp
                            )
                    )

                    //--------------------------------------------------
                    // Edit Financial Event dialog
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

                            categories =
                                state.categories,

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

//--------------------------------------------------
// Month-wise transaction picker
//--------------------------------------------------

@Composable
private fun MonthWiseTransactionPicker(

    title: String,

    transactions: List<Transaction>,

    isUpdating: Boolean,

    onDismiss: () -> Unit,

    onConfirm: (Set<Long>) -> Unit

) {

    var selectedIds by
        remember {
            mutableStateOf(
                emptySet<Long>()
            )
        }

    var expandedMonth by
        remember {

            mutableStateOf(
                transactions
                    .firstOrNull()
                    ?.let {
                        monthKey(it)
                    }
            )
        }

    val groupedTransactions =
        transactions
            .sortedByDescending {
                it.dateTimestamp
            }
            .groupBy {
                monthKey(it)
            }

    AlertDialog(

        onDismissRequest = {

            if (!isUpdating) {
                onDismiss()
            }
        },

        title = {

            Text(
                title
            )
        },

        text = {

            Column(

                modifier =
                    Modifier
                        .fillMaxWidth()
                        .verticalScroll(
                            rememberScrollState()
                        ),

                verticalArrangement =
                    Arrangement.spacedBy(
                        8.dp
                    )
            ) {

                Text(

                    text =
                        "Select transactions by month.",

                    style =
                        MaterialTheme
                            .typography
                            .bodySmall,

                    color =
                        MaterialTheme
                            .colorScheme
                            .onSurfaceVariant
                )

                groupedTransactions
                    .forEach { (_, monthTransactions) ->

                        val month =
                            monthTransactions
                                .first()

                        val key =
                            monthKey(
                                month
                            )

                        val monthSelectedCount =
                            monthTransactions.count {

                                it.id in
                                    selectedIds
                            }

                        Card(
                            modifier =
                                Modifier.fillMaxWidth()
                        ) {

                            Column {

                                OutlinedButton(

                                    onClick = {

                                        expandedMonth =
                                            if (
                                                expandedMonth ==
                                                    key
                                            ) {

                                                null

                                            } else {

                                                key
                                            }
                                    },

                                    enabled =
                                        !isUpdating,

                                    modifier =
                                        Modifier
                                            .fillMaxWidth()
                                ) {

                                    Row(

                                        modifier =
                                            Modifier
                                                .fillMaxWidth(),

                                        horizontalArrangement =
                                            Arrangement
                                                .SpaceBetween
                                    ) {

                                        Text(
                                            monthLabel(
                                                month
                                            )
                                        )

                                        if (
                                            monthSelectedCount >
                                                0
                                        ) {

                                            Text(

                                                "$monthSelectedCount selected",

                                                color =
                                                    MaterialTheme
                                                        .colorScheme
                                                        .primary
                                            )
                                        }
                                    }
                                }

                                if (
                                    expandedMonth ==
                                        key
                                ) {

                                    monthTransactions
                                        .forEach {
                                            transaction ->

                                            TransactionPickerRow(

                                                transaction =
                                                    transaction,

                                                selected =
                                                    transaction.id in
                                                        selectedIds,

                                                enabled =
                                                    !isUpdating,

                                                onToggle = {

                                                    selectedIds =
                                                        if (
                                                            transaction.id in
                                                                selectedIds
                                                        ) {

                                                            selectedIds -
                                                                transaction.id

                                                        } else {

                                                            selectedIds +
                                                                transaction.id
                                                        }
                                                }
                                            )
                                        }
                                }
                            }
                        }
                    }

                if (
                    selectedIds.isNotEmpty()
                ) {

                    Text(

                        text =
                            "${selectedIds.size} transaction(s) selected",

                        style =
                            MaterialTheme
                                .typography
                                .bodyMedium,

                        color =
                            MaterialTheme
                                .colorScheme
                                .primary
                    )
                }
            }
        },

        confirmButton = {

            Button(

                onClick = {

                    onConfirm(
                        selectedIds
                    )
                },

                enabled =
                    selectedIds.isNotEmpty() &&
                    !isUpdating

            ) {

                Text(

                    if (isUpdating) {

                        "Adding..."

                    } else {

                        "Add Selected"
                    }
                )
            }
        },

        dismissButton = {

            OutlinedButton(

                onClick =
                    onDismiss,

                enabled =
                    !isUpdating

            ) {

                Text(
                    "Cancel"
                )
            }
        }
    )
}

//--------------------------------------------------
// Transaction picker row
//--------------------------------------------------

@Composable
private fun TransactionPickerRow(

    transaction: Transaction,

    selected: Boolean,

    enabled: Boolean,

    onToggle: () -> Unit

) {

    Row(

        modifier =
            Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = 8.dp,
                    vertical = 4.dp
                ),

        horizontalArrangement =
            Arrangement.spacedBy(
                8.dp
            )

    ) {

        Checkbox(

            checked =
                selected,

            onCheckedChange = {

                onToggle()
            },

            enabled =
                enabled
        )

        Column(

            modifier =
                Modifier.weight(1f)

        ) {

            Text(

                text =
                    transaction.description,

                style =
                    MaterialTheme
                        .typography
                        .bodyMedium
            )

            Text(

                text =
                    SimpleDateFormat(
                        "dd MMM yyyy",
                        Locale.ENGLISH
                    ).format(
                        Date(
                            transaction.dateTimestamp
                        )
                    ),

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

            text =
                "₹%,.2f".format(
                    transaction.amount
                ),

            style =
                MaterialTheme
                    .typography
                    .bodyMedium
        )
    }
}

//--------------------------------------------------
// Month helpers
//--------------------------------------------------

private fun monthKey(
    transaction: Transaction
): String {

    return SimpleDateFormat(
        "yyyy-MM",
        Locale.ENGLISH
    ).format(
        Date(
            transaction.dateTimestamp
        )
    )
}

private fun monthLabel(
    transaction: Transaction
): String {

    return SimpleDateFormat(
        "MMMM yyyy",
        Locale.ENGLISH
    ).format(
        Date(
            transaction.dateTimestamp
        )
    )
}

//--------------------------------------------------
// Linked transaction row
//--------------------------------------------------

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
                Modifier.padding(
                    16.dp
                ),

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

                Text(
                    "Remove from Event"
                )
            }
        }
    }
}

//--------------------------------------------------
// Edit Financial Event dialog
//
// Category is now a dropdown backed by the existing
// application category list.
//--------------------------------------------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditFinancialEventDialog(

    initialName: String,

    initialCategory: String,

    categories: List<String>,

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

    var categoryExpanded by
        remember {

            mutableStateOf(false)
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

                modifier =
                    Modifier.fillMaxWidth(),

                verticalArrangement =
                    Arrangement.spacedBy(
                        12.dp
                    )
            ) {

                //--------------------------------------------------
                // Event name
                //--------------------------------------------------

                OutlinedTextField(

                    value =
                        groupName,

                    onValueChange = {

                        groupName =
                            it
                    },

                    label = {

                        Text(
                            "Event name"
                        )
                    },

                    singleLine = true,

                    modifier =
                        Modifier.fillMaxWidth()
                )

                //--------------------------------------------------
                // Category dropdown
                //--------------------------------------------------
Column(
    modifier = Modifier.fillMaxWidth()
) {

    Box(
        modifier = Modifier.fillMaxWidth()
    ) {

        OutlinedTextField(
            value = category,
            onValueChange = {},
            readOnly = true,
            label = {
                Text("Category")
            },
            trailingIcon = {
                Text(
                    text =
                        if (categoryExpanded) "▲"
                        else "▼",
                    color =
                        MaterialTheme
                            .colorScheme
                            .onSurfaceVariant
                )
            },
            modifier =
                Modifier.fillMaxWidth()
        )

        Box(
            modifier =
                Modifier
                    .matchParentSize()
                    .clickable {
                        categoryExpanded =
                            !categoryExpanded
                    }
        )
    }

    DropdownMenu(
        expanded = categoryExpanded,
        onDismissRequest = {
            categoryExpanded = false
        }
    ) {

        categories.forEach { availableCategory ->

            DropdownMenuItem(
                text = {
                    Text(
                        availableCategory
                    )
                },
                onClick = {

                    category =
                        availableCategory

                    categoryExpanded =
                        false
                }
            )
        }
    }
}
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
                    category.isNotBlank() &&
                    category in categories

            ) {

                Text(
                    "Save"
                )
            }
        },

        dismissButton = {

            OutlinedButton(

                onClick =
                    onDismiss
            ) {

                Text(
                    "Cancel"
                )
            }
        }
    )
}
