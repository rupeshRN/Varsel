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
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import com.varsel.expensetracker.domain.model.Transaction
import com.varsel.expensetracker.domain.model.TransactionLinkGroup
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun TransactionLinkSection(

    linkedTransactions:
        List<Transaction>,

    transactionLinkGroup:
        TransactionLinkGroup?,

    showCreateGroupPrompt:
        Boolean,

    isSavingGroup:
        Boolean,

    categories:
        List<String>,

    onManageFinancialEvent:
        () -> Unit,

    onShowCreateFinancialEvent:
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
        // Financial Event section
        //--------------------------------------------------

        Text(

            text =
                "Financial Event",

            style =
                MaterialTheme
                    .typography
                    .titleMedium
        )

        //--------------------------------------------------
        // Existing financial event
        //--------------------------------------------------

        if (
            transactionLinkGroup != null
        ) {

            ReportGroupCard(

                group =
                    transactionLinkGroup
            )

            Button(

                onClick =
                    onManageFinancialEvent,

                modifier =
                    Modifier.fillMaxWidth()
            ) {

                Text(
                    "Manage Financial Event"
                )
            }

        } else {

            //--------------------------------------------------
            // No financial event yet
            //--------------------------------------------------

            OutlinedButton(

                onClick =
                    onShowCreateFinancialEvent,

                enabled =
                    !isSavingGroup,

                modifier =
                    Modifier.fillMaxWidth()
            ) {

                Text(
                    "Create Financial Event"
                )
            }
        }

        //--------------------------------------------------
        // Existing linked transactions
        //
        // This is intentionally kept.
        //
        // The "Possible Transactions to Link" section has
        // been removed completely.
        //--------------------------------------------------

        if (
            linkedTransactions.isNotEmpty()
        ) {

            Spacer(
                modifier =
                    Modifier.height(
                        4.dp
                    )
            )

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
                    !isSavingGroup,

                modifier =
                    Modifier.fillMaxWidth()
            ) {

                Text(
                    "Unlink Current Transaction"
                )
            }
        }

        //--------------------------------------------------
        // Create Financial Event dialog
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
                "Financial Event",

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
        remember(categories) {

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

                categoryExpanded =
                    false

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
                        "Create a financial event for " +
                            "this transaction. You can manage " +
                            "the event and its linked transactions " +
                            "after it is created.",

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

                    singleLine =
                        true,

                    enabled =
                        !isSaving,

                    modifier =
                        Modifier.fillMaxWidth()
                )

                //--------------------------------------------------
                // Category
                //--------------------------------------------------

                Column(

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

                        enabled =
                            !isSaving &&
                            categories.isNotEmpty(),

                        modifier =
                            Modifier.fillMaxWidth()
                    )

                    Spacer(
                        modifier =
                            Modifier.height(
                                4.dp
                            )
                    )

                    OutlinedButton(

                        onClick = {

                            categoryExpanded =
                                !categoryExpanded
                        },

                        enabled =
                            !isSaving &&
                            categories.isNotEmpty(),

                        modifier =
                            Modifier.fillMaxWidth()
                    ) {

                        Row(

                            modifier =
                                Modifier.fillMaxWidth(),

                            horizontalArrangement =
                                Arrangement.SpaceBetween
                        ) {

                            Text(
                                "Choose category"
                            )

                            Text(

                                if (
                                    categoryExpanded
                                ) {
                                    "▲"
                                } else {
                                    "▼"
                                }
                            )
                        }
                    }

                    DropdownMenu(

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

                    if (
                        isSaving
                    ) {

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
