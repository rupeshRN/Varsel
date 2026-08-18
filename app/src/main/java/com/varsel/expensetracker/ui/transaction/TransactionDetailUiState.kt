package com.varsel.expensetracker.ui.transaction

import com.varsel.expensetracker.domain.model.Transaction
import com.varsel.expensetracker.domain.model.TransactionLinkGroup
import com.varsel.expensetracker.domain.model.TransactionRole

sealed interface TransactionDetailUiState {

    object Loading : TransactionDetailUiState

    data class Loaded(

        val transaction: Transaction,

        val editableDescription: String,

        val selectedCategory: String,

        val selectedRole: TransactionRole,

        val hasChanges: Boolean,

        val isSaving: Boolean = false,

        //--------------------------------------------------
        // Manual transaction linking
        //--------------------------------------------------

        /**
         * All transactions currently belonging to the same
         * financial event.
         */
        val linkedTransactions: List<Transaction> =
            emptyList(),

        /**
         * Transactions that the user can manually select
         * and add to the current financial event.
         *
         * When the current transaction is an EXPENSE:
         *     unlinked REIMBURSEMENT incomes are shown.
         *
         * When the current transaction is a REIMBURSEMENT:
         *     unlinked expenses are shown.
         *
         * No automatic linking is performed.
         */
        val linkableTransactions: List<Transaction> =
            emptyList(),

        /**
         * True while a link/unlink operation is being
         * persisted.
         */
        val isLinking: Boolean = false,

        //--------------------------------------------------
        // Optional report group
        //--------------------------------------------------

        val transactionLinkGroup:
            TransactionLinkGroup? = null,

        val showCreateGroupPrompt: Boolean = false,

        val isSavingGroup: Boolean = false

    ) : TransactionDetailUiState

    data class Error(

        val message: String

    ) : TransactionDetailUiState
}
