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

        val linkedTransactions: List<Transaction> =
            emptyList(),

        val reimbursementCandidates: List<Transaction> =
            emptyList(),

        val isLinking: Boolean = false,

        //--------------------------------------------------
        // Optional report group
        //--------------------------------------------------

        /**
         * Existing report-group metadata for this
         * transaction link, if one exists.
         */
        val transactionLinkGroup: TransactionLinkGroup? =
            null,

        /**
         * True when the UI should offer the user the
         * option to create a report group.
         *
         * This is intentionally NOT automatically true
         * for every transaction link.
         */
        val showCreateGroupPrompt: Boolean = false,

        /**
         * True while group metadata is being persisted.
         */
        val isSavingGroup: Boolean = false

    ) : TransactionDetailUiState

    data class Error(

        val message: String

    ) : TransactionDetailUiState
}
