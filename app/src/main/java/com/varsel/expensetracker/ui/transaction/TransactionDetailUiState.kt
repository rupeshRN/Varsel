package com.varsel.expensetracker.ui.transaction

import com.varsel.expensetracker.domain.model.Transaction
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
         * Transactions currently linked to this transaction.
         *
         * Normally this will contain:
         *
         * - the current transaction
         * - one or more related transactions
         */
        val linkedTransactions: List<Transaction> = emptyList(),

                /**
         * Reimbursement transactions that the user can
         * manually choose to link.
         *
         * These are suggestions/candidates only.
         * No automatic linking is performed.
         */
        val reimbursementCandidates: List<Transaction> =
            emptyList(),

        /**
         * True while a link/unlink operation is being
         * persisted.
         */
        val isLinking: Boolean = false

    ) : TransactionDetailUiState

    data class Error(

        val message: String

    ) : TransactionDetailUiState
}
