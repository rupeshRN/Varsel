package com.varsel.expensetracker.ui.transaction.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.varsel.expensetracker.domain.model.Transaction
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun TransactionLinkSection(
    linkedTransactions: List<Transaction>,
    reimbursementCandidates: List<Transaction>,
    selectedTransactionIds: Set<Long>,
    isLinking: Boolean,
    onToggleCandidate: (Long) -> Unit,
    onLinkSelected: () -> Unit,
    onUnlink: () -> Unit
) {

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        Text(
            text = "Transaction Linking",
            style = MaterialTheme.typography.titleMedium
        )

        //--------------------------------------------------
        // Existing linked transactions
        //--------------------------------------------------

        if (linkedTransactions.isNotEmpty()) {

            Text(
                text = "Linked Transactions",
                style = MaterialTheme.typography.titleSmall
            )

            linkedTransactions.forEach { transaction ->

                LinkedTransactionRow(
                    transaction = transaction
                )
            }

            Spacer(
                modifier = Modifier.height(4.dp)
            )

            OutlinedButton(
                onClick = onUnlink,
                enabled = !isLinking,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Unlink Current Transaction")
            }
        }

        //--------------------------------------------------
        // Available reimbursement candidates
        //--------------------------------------------------

        if (reimbursementCandidates.isNotEmpty()) {

            Spacer(
                modifier = Modifier.height(4.dp)
            )

            Text(
                text = "Possible Reimbursements",
                style = MaterialTheme.typography.titleSmall
            )

            Text(
                text =
                    "Select the reimbursement transactions " +
                    "that belong to this expense.",
                style = MaterialTheme.typography.bodySmall,
                color =
                    MaterialTheme.colorScheme.onSurfaceVariant
            )

            reimbursementCandidates.forEach { transaction ->

                val selected =
                    transaction.id in selectedTransactionIds

                FilterChip(
                    selected = selected,

                    onClick = {
                        onToggleCandidate(transaction.id)
                    },

                    label = {

                        CandidateLabel(
                            transaction = transaction
                        )
                    },

                    modifier =
                        Modifier.fillMaxWidth()
                )
            }

            //--------------------------------------------------
            // Selected total
            //--------------------------------------------------

            if (selectedTransactionIds.isNotEmpty()) {

                val selectedAmount =
                    reimbursementCandidates
                        .filter {
                            it.id in selectedTransactionIds
                        }
                        .sumOf {
                            it.amount
                        }

                Text(
                    text =
                        "Selected reimbursement: " +
                        "₹%,.2f".format(selectedAmount),

                    style =
                        MaterialTheme.typography.bodyMedium,

                    color =
                        MaterialTheme.colorScheme.primary
                )

                Button(
                    onClick = onLinkSelected,

                    enabled =
                        !isLinking &&
                        selectedTransactionIds.isNotEmpty(),

                    modifier =
                        Modifier.fillMaxWidth()
                ) {

                    Text(
                        if (isLinking) {
                            "Linking..."
                        } else {
                            "Link Selected Transactions"
                        }
                    )
                }
            }
        }

        //--------------------------------------------------
        // No candidates
        //--------------------------------------------------

        if (
            reimbursementCandidates.isEmpty() &&
            linkedTransactions.isEmpty()
        ) {

            Text(
                text =
                    "No reimbursement transactions " +
                    "available for linking.",

                style =
                    MaterialTheme.typography.bodySmall,

                color =
                    MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun CandidateLabel(
    transaction: Transaction
) {

    val date =
        SimpleDateFormat(
            "dd MMM yyyy",
            Locale.ENGLISH
        ).format(
            Date(transaction.dateTimestamp)
        )

    Column {

        Text(
            text =
                "₹%,.2f".format(
                    transaction.amount
                ),

            style =
                MaterialTheme.typography.bodyMedium
        )

        Text(
            text =
                "${transaction.description} • $date",

            style =
                MaterialTheme.typography.bodySmall,

            color =
                MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun LinkedTransactionRow(
    transaction: Transaction
) {

    val date =
        SimpleDateFormat(
            "dd MMM yyyy",
            Locale.ENGLISH
        ).format(
            Date(transaction.dateTimestamp)
        )

    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),

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
                    MaterialTheme.typography.bodyMedium
            )

            Text(
                text = date,

                style =
                    MaterialTheme.typography.bodySmall,

                color =
                    MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Text(
            text =
                "₹%,.2f".format(
                    transaction.amount
                ),

            style =
                MaterialTheme.typography.bodyMedium
        )
    }
}