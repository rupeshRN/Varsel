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

        val transaction:
            Transaction,

        val editableDescription:
            String,

        val selectedCategory:
            String,

        val selectedRole:
            TransactionRole,

        val hasChanges:
            Boolean,

        val isSaving:
            Boolean = false,

        //--------------------------------------------------
        // Categories
        //--------------------------------------------------

        /**
         * Existing application categories.
         *
         * Used by the Financial Event / Report Group
         * creation and editing dialogs.
         */
        val categories:
            List<String> = emptyList(),

        //--------------------------------------------------
        // Financial Event
        //--------------------------------------------------

        /**
         * All transactions currently belonging to
         * the same Financial Event.
         *
         * The old "Possible Transactions to Link"
         * picker has been removed from Transaction Details.
         *
         * Adding/removing transactions is handled from
         * the Financial Event screen.
         */
        val linkedTransactions:
            List<Transaction> = emptyList(),

        /**
         * True while the current transaction is being
         * linked or unlinked from a Financial Event.
         */
        val isLinking:
            Boolean = false,

        //--------------------------------------------------
        // Financial Event group
        //--------------------------------------------------

        /**
         * Existing Financial Event / Report Group
         * associated with this transaction.
         */
        val transactionLinkGroup:
            TransactionLinkGroup? = null,

        /**
         * Controls the Create Financial Event dialog.
         */
        val showCreateGroupPrompt:
            Boolean = false,

        /**
         * True while a Financial Event is being created.
         */
        val isSavingGroup:
            Boolean = false,

        //--------------------------------------------------
        // Transfer linking
        //--------------------------------------------------

        /**
         * Temporary error shown when the user attempts
         * to link an invalid Transfer In / Transfer Out
         * pair.
         *
         * This is intentionally part of Loaded state rather
         * than changing the whole screen to Error.
         *
         * Example:
         *
         * "Transfer amounts don't match. Please make sure
         * you selected the correct Transfer In / Transfer Out."
         *
         * The value is cleared after the user dismisses it
         * or after a successful transfer operation.
         */
        val transferErrorMessage:
            String? = null

    ) : TransactionDetailUiState

    //--------------------------------------------------
    // Fatal screen error
    //--------------------------------------------------

    data class Error(

        val message:
            String

    ) : TransactionDetailUiState
}
