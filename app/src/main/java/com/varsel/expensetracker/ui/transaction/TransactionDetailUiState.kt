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
        // Categories
        //--------------------------------------------------

        /**
         * Existing application categories.
         *
         * Used by the Financial Event / Report Group
         * creation dialog so the user selects a category
         * from the application's existing category list.
         */
        val categories: List<String> =
            emptyList(),

        //--------------------------------------------------
        // Financial Event
        //--------------------------------------------------

        /**
         * All transactions currently belonging to the
         * same Financial Event.
         *
         * This is display-only from Transaction Details.
         *
         * The old "Possible Transactions to Link" picker
         * has been removed. Adding and removing transactions
         * from a Financial Event is now handled by the
         * Financial Event screen.
         */
        val linkedTransactions: List<Transaction> =
            emptyList(),

        /**
         * True while the current transaction is being
         * linked or unlinked from a Financial Event.
         */
        val isLinking: Boolean = false,

        //--------------------------------------------------
        // Financial Event group
        //--------------------------------------------------

        /**
         * Existing Financial Event / Report Group associated
         * with this transaction.
         */
        val transactionLinkGroup:
            TransactionLinkGroup? = null,

        /**
         * Controls the Create Financial Event dialog.
         */
        val showCreateGroupPrompt: Boolean = false,

        /**
         * True while a Financial Event is being created.
         */
        val isSavingGroup: Boolean = false

    ) : TransactionDetailUiState

    data class Error(

        val message: String

    ) : TransactionDetailUiState
}
