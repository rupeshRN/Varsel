package com.varsel.expensetracker.ui.transaction

import com.varsel.expensetracker.domain.model.Transaction

sealed interface TransactionDetailUiState {

    object Loading : TransactionDetailUiState

    data class Loaded(

        val transaction: Transaction

    ) : TransactionDetailUiState

    data class Error(

        val message: String

    ) : TransactionDetailUiState

}
