package com.varsel.expensetracker.ui.transaction

import com.varsel.expensetracker.domain.model.Transaction
import com.varsel.expensetracker.domain.model.TransactionLinkGroup
import com.varsel.expensetracker.domain.model.TransactionRole

sealed interface TransactionDetailUiState {

    //--------------------------------------------------
    // Loading
    //--------------------------------------------------

    object Loading : TransactionDetailUiState

    //--------------------------------------------------
    // Loaded
    //--------------------------------------------------

    data class Loaded(

        //--------------------------------------------------
        // Current transaction
        //--------------------------------------------------

        val transaction: Transaction,

        val editableDescription: String,

        val selectedCategory: String,

        val selectedRole: TransactionRole,

        val hasChanges: Boolean,

        val isSaving: Boolean = false,

        //--------------------------------------------------
        // Application categories
        //--------------------------------------------------

        val categories: List<String> =
            emptyList(),

        //--------------------------------------------------
        // Financial Event / transaction grouping
        //--------------------------------------------------

        /**
         * All transactions currently belonging to the
         * same Financial Event.
         */
        val linkedTransactions: List<Transaction> =
            emptyList(),

        /**
         * Transactions that can potentially be added
         * to the current Financial Event.
         *
         * This is retained for Financial Event handling.
         */
        val linkableTransactions: List<Transaction> =
            emptyList(),

        /**
         * True while a Financial Event link/unlink
         * operation is being persisted.
         */
        val isLinking: Boolean = false,

        //--------------------------------------------------
        // Financial Event / Report Group
        //--------------------------------------------------

        val transactionLinkGroup:
            TransactionLinkGroup? = null,

        val showCreateGroupPrompt: Boolean = false,

        val isSavingGroup: Boolean = false,

        //--------------------------------------------------
        // Transfer In / Transfer Out
        //--------------------------------------------------

        /**
         * The opposite-side transaction currently linked
         * to this transfer.
         *
         * For TRANSFER_OUT:
         *     this is the TRANSFER_IN transaction.
         *
         * For TRANSFER_IN:
         *     this is the TRANSFER_OUT transaction.
         */
        val linkedTransfer:
            Transaction? = null,

        /**
         * Transactions that may be selected as the
         * opposite side of this transfer.
         *
         * The ViewModel is responsible for determining
         * whether a candidate is valid.
         */
        val transferCandidates:
            List<Transaction> =
                emptyList(),

        /**
         * True while a transfer link/unlink operation
         * is being persisted.
         */
        val isTransferLinking:
            Boolean = false,

        /**
         * User-facing validation error from a transfer
         * linking attempt.
         *
         * Example:
         * the selected Transfer In and Transfer Out
         * amounts do not match.
         */
        val transferErrorMessage:
            String? = null

    ) : TransactionDetailUiState

    //--------------------------------------------------
    // Error
    //--------------------------------------------------

    data class Error(

        val message: String

    ) : TransactionDetailUiState
}
