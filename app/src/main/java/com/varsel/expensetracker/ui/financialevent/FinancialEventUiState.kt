package com.varsel.expensetracker.ui.financialevent

import com.varsel.expensetracker.domain.model.Transaction
import com.varsel.expensetracker.domain.model.TransactionLinkGroup
import com.varsel.expensetracker.data.local.entity.CategoryEntity

sealed interface FinancialEventUiState {

    data object Loading : FinancialEventUiState

    data class Loaded(

        val group: TransactionLinkGroup,

        val expenses: List<Transaction>,

        val reimbursements: List<Transaction>,

        /**
         * Expenses that are not currently part of this
         * financial event and can be added manually.
         */
        val availableExpenses: List<Transaction>,

        /**
         * Reimbursement transactions that are not currently
         * part of this financial event and can be added manually.
         */
        val availableReimbursements: List<Transaction>,

        val totalExpenses: Double,

        val totalReimbursements: Double,

        val isUpdating: Boolean = false,

        val isEditingGroup: Boolean = false,

        val categories: List<CategoryEntity> = emptyList(),

    ) : FinancialEventUiState {

        val actualExpense: Double
            get() =
                totalExpenses - totalReimbursements
    }

    data class Error(

        val message: String

    ) : FinancialEventUiState
}
