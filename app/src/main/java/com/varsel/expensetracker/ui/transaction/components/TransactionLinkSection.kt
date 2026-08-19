package com.varsel.expensetracker.ui.transaction.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenu
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.material3.ExperimentalMaterial3Api
import com.varsel.expensetracker.domain.model.Transaction
import com.varsel.expensetracker.domain.model.TransactionLinkGroup
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun TransactionLinkSection(

    linkedTransactions:
        List<Transaction>,

    linkableTransactions:
        List<Transaction>,

    selectedTransactionIds:
        Set<Long>,

    isLinking:
        Boolean,

    transactionLinkGroup:
        TransactionLinkGroup?,

    showCreateGroupPrompt:
        Boolean,

    isSavingGroup:
        Boolean,

    categories:
        List<String>,

    onToggleCandidate:
        (Long) -> Unit,

    onLinkSelected:
        () -> Unit,

    onUnlink:
        () -> Unit,

    onDismissCreateGroupPrompt:
        () -> Unit,

    onCreateReportGroup:
        (
            groupName: String,
            category: String
        ) -> Unit

) {

    Column(

        modifier =
            Modifier.fillMaxWidth(),

        verticalArrangement =
            Arrangement.spacedBy(
                12.dp
            )

    ) {

        //--------------------------------------------------
        // Section title
        //--------------------------------------------------

        Text(

            text =
                "Transaction Linking",

            style =
                MaterialTheme
                    .typography
                    .titleMedium
        )

        //--------------------------------------------------
        // Existing linked transactions
        //--------------------------------------------------

        if (
            linkedTransactions.isNotEmpty()
        ) {

            Text(

                text =
                    "Linked Transactions",

                style =
                    MaterialTheme
                        .typography
                        .titleSmall
            )

            linkedTransactions.forEach {

                transaction ->

                LinkedTransactionRow(

                    transaction =
                        transaction
                )
            }

            Spacer(
                modifier =
                    Modifier.height(
                        4.dp
                    )
            )

            OutlinedButton(

                onClick =
                    onUnlink,

                enabled =
                    !isLinking,

                modifier =
                    Modifier.fillMaxWidth()

            ) {

                Text(
                    "Unlink Current Transaction"
                )
            }
        }

        //--------------------------------------------------
        // Available transactions
        //--------------------------------------------------

        if (
            linkableTransactions.isNotEmpty()
        ) {

            Spacer(
                modifier =
                    Modifier.height(
                        4.dp
                    )
            )

            Text(

                text =
                    "Possible Transactions to Link",

                style =
                    MaterialTheme
                        .typography
                        .titleSmall
            )

            Text(

                text =
                    "Select the transactions that " +
                        "belong to this financial event.",

                style =
                    MaterialTheme
                        .typography
                        .bodySmall,

                color =
                    MaterialTheme
                        .colorScheme
                        .onSurfaceVariant
            )

            //--------------------------------------------------
            // Candidate selection
            //--------------------------------------------------

            linkableTransactions.forEach {

                transaction ->

                val selected =
                    transaction.id in
                        selectedTransactionIds

                FilterChip(

                    selected =
                        selected,

                    onClick = {

                        onToggleCandidate(
                            transaction.id
                        )
                    },

                    label = {

                        CandidateLabel(
                            transaction =
                                transaction
                        )
                    },

                    modifier =
                        Modifier.fillMaxWidth()
                )
            }

            //--------------------------------------------------
            // Selected total
            //--------------------------------------------------

            if (
                selectedTransactionIds.isNotEmpty()
            ) {

                val selectedAmount =
                    linkableTransactions
                        .filter {

                            it.id in
                                selectedTransactionIds
                        }
                        .sumOf {

                            it.amount
                        }

                Text(

                    text =
                        "Selected total: " +
                            "₹%,.2f"
                                .format(
                                    selectedAmount
                                ),

                    style =
                        MaterialTheme
                            .typography
                            .bodyMedium,

                    color =
                        MaterialTheme
                            .colorScheme
                            .primary
                )

                //--------------------------------------------------
                // Link button
                //--------------------------------------------------

                Button(

                    onClick =
                        onLinkSelected,

                    enabled =
                        !isLinking &&
                        selectedTransactionIds
                            .isNotEmpty(),

                    modifier =
                        Modifier.fillMaxWidth()

                ) {

                    Text(

                        if (
                            isLinking
                        ) {

                            "Linking..."

                        } else {

                            "Link Selected Transactions"
                        }
                    )
                }
            }
        }

        //--------------------------------------------------
        // No linkable transactions
        //--------------------------------------------------

        if (
            linkableTransactions.isEmpty() &&
            linkedTransactions.isEmpty()
        ) {

            Text(

                text =
                    "No transactions available " +
                        "for linking.",

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

        //--------------------------------------------------
        // Existing report group
        //--------------------------------------------------

        if (
            transactionLinkGroup != null
        ) {

            ReportGroupCard(

                group =
                    transactionLinkGroup
            )
        }

        //--------------------------------------------------
        // Create report group dialog
        //--------------------------------------------------

        if (
            showCreateGroupPrompt
        ) {

            CreateReportGroupDialog(

                categories =
                    categories,

                isSaving =
                    isSavingGroup,

                onDismiss =
                    onDismissCreateGroupPrompt,

                onCreate =
                    onCreateReportGroup
            )
        }
    }
}

@Composable
private fun CandidateLabel(

    transaction:
        Transaction

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

    Column {

        Text(

            text =
                "₹%,.2f"
                    .format(
                        transaction.amount
                    ),

            style =
                MaterialTheme
                    .typography
                    .bodyMedium
        )

        Text(

            text =
                "${transaction.description} • $date",

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
}

@Composable
private fun LinkedTransactionRow(

    transaction:
        Transaction

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

    Row(

        modifier =
            Modifier
                .fillMaxWidth()
                .padding(
                    vertical = 4.dp
                ),

        horizontalArrangement =
            Arrangement.SpaceBetween

    ) {

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

            text =
                "₹%,.2f"
                    .format(
                        transaction.amount
                    ),

            style =
                MaterialTheme
                    .typography
                    .bodyMedium
        )
    }
}

@Composable
private fun ReportGroupCard(

    group:
        TransactionLinkGroup

) {

    Column(

        modifier =
            Modifier
                .fillMaxWidth()
                .padding(
                    top = 4.dp
                )

    ) {

        Text(

            text =
                "Report Group",

            style =
                MaterialTheme
                    .typography
                    .titleSmall
        )

        Text(

            text =
                group.groupName,

            style =
                MaterialTheme
                    .typography
                    .bodyMedium,

            fontWeight =
                FontWeight.SemiBold
        )

        Text(

            text =
                group.category,

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
}

@OptIn(
    ExperimentalMaterial3Api::class
)
@Composable
private fun CreateReportGroupDialog(

    categories:
        List<String>,

    isSaving:
        Boolean,

    onDismiss:
        () -> Unit,

    onCreate:
        (
            groupName: String,
            category: String
        ) -> Unit

) {

    var groupName by
        remember {

            mutableStateOf("")
        }

    var category by
        remember {

            mutableStateOf(
                categories.firstOrNull()
                    ?: ""
            )
        }

    var categoryExpanded by
        remember {

            mutableStateOf(
                false
            )
        }

    AlertDialog(

        onDismissRequest = {

            if (!isSaving) {

                onDismiss()
            }
        },

        title = {

            Text(
                "Create Financial Event"
            )
        },

        text = {

            Column(

                verticalArrangement =
                    Arrangement.spacedBy(
                        12.dp
                    )

            ) {

                Text(

                    text =
                        "These transactions contain " +
                            "multiple expenses. Give this " +
                            "financial event a name and category " +
                            "for reporting.",

                    style =
                        MaterialTheme
                            .typography
                            .bodyMedium
                )

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
                            "Group name"
                        )
                    },

                    singleLine = true,

                    enabled =
                        !isSaving,

                    modifier =
                        Modifier.fillMaxWidth()
                )

                //--------------------------------------------------
                // Category dropdown
                //--------------------------------------------------

                ExposedDropdownMenuBox(

                    expanded =
                        categoryExpanded,

                    onExpandedChange = {

                        if (
                            !isSaving &&
                            categories.isNotEmpty()
                        ) {

                            categoryExpanded =
                                !categoryExpanded
                        }
                    },

                    modifier =
                        Modifier.fillMaxWidth()

                ) {

                    OutlinedTextField(

                        value =
                            category,

                        onValueChange = {},

                        readOnly =
                            true,

                        label = {

                            Text(
                                "Report category"
                            )
                        },

                        trailingIcon = {

                            ExposedDropdownMenuDefaults
                                .TrailingIcon(

                                    expanded =
                                        categoryExpanded
                                )
                        },

                        enabled =
                            !isSaving &&
                            categories.isNotEmpty(),

                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .menuAnchor()
                    )

                    ExposedDropdownMenu(

                        expanded =
                            categoryExpanded,

                        onDismissRequest = {

                            categoryExpanded =
                                false
                        }

                    ) {

                        categories.forEach {

                            availableCategory ->

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
                                },

                                enabled =
                                    !isSaving
                            )
                        }
                    }
                }

                if (
                    categories.isEmpty()
                ) {

                    Text(

                        text =
                            "No categories are available. " +
                                "Create a category first.",

                        style =
                            MaterialTheme
                                .typography
                                .bodySmall,

                        color =
                            MaterialTheme
                                .colorScheme
                                .error
                    )
                }
            }
        },

        confirmButton = {

            TextButton(

                onClick = {

                    onCreate(

                        groupName,

                        category
                    )
                },

                enabled =
                    !isSaving &&
                    groupName.isNotBlank() &&
                    category.isNotBlank()

            ) {

                Text(

                    if (isSaving) {

                        "Saving..."

                    } else {

                        "Create"
                    }
                )
            }
        },

        dismissButton = {

            TextButton(

                onClick =
                    onDismiss,

                enabled =
                    !isSaving

            ) {

                Text(
                    "Later"
                )
            }
        }
    )
}
