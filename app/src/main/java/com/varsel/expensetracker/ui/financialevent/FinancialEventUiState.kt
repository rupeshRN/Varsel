package com.varsel.expensetracker.ui.financialevent

import com.varsel.expensetracker.domain.model.Transaction
import com.varsel.expensetracker.domain.model.TransactionLinkGroup

sealed interface FinancialEventUiState {

    data object Loading :
        FinancialEventUiState

    data class Loaded(

        val group: TransactionLinkGroup,

        val expenses: List<Transaction>,

        val reimbursements: List<Transaction>,

        val totalExpenses: Double,

        val totalReimbursements: Double

    ) : FinancialEventUiState {

        val actualExpense: Double
            get() =
                totalExpenses -
                    totalReimbursements
    }

    data class Error(

        val message: String

    ) : FinancialEventUiState
}
