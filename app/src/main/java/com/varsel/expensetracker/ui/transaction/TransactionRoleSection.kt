package com.varsel.expensetracker.ui.transaction

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.varsel.expensetracker.domain.model.TransactionRole
import com.varsel.expensetracker.domain.model.TransactionType

@Composable
fun TransactionRoleSection(

    transactionType: TransactionType,

    selectedRole: TransactionRole,

    onRoleSelected: (TransactionRole) -> Unit

) {

    val availableRoles =
        when (transactionType) {

            TransactionType.EXPENSE ->
                listOf(
                    TransactionRole.NORMAL,
                    TransactionRole.LENT
                )

            TransactionType.INCOME ->
                listOf(
                    TransactionRole.NORMAL,
                    TransactionRole.REIMBURSEMENT
                )
        }

    Column(

        modifier =
            androidx.compose.ui.Modifier.fillMaxWidth(),

        verticalArrangement =
            Arrangement.spacedBy(
                androidx.compose.ui.unit.dp(8.dp)
            )
    ) {

        Text(
            text = "Transaction Role",
            style = MaterialTheme.typography.titleSmall
        )

        Row(

            modifier =
                androidx.compose.ui.Modifier.fillMaxWidth(),

            horizontalArrangement =
                Arrangement.spacedBy(
                    androidx.compose.ui.unit.dp(8.dp)
                )
        ) {

            availableRoles.forEach { role ->

                FilterChip(

                    selected =
                        selectedRole == role,

                    onClick = {
                        onRoleSelected(role)
                    },

                    label = {
                        Text(
                            when (role) {

                                TransactionRole.NORMAL ->
                                    "Normal"

                                TransactionRole.LENT ->
                                    "Lent"

                                TransactionRole.REIMBURSEMENT ->
                                    "Reimbursement"
                            }
                        )
                    }
                )
            }
        }
    }
}
