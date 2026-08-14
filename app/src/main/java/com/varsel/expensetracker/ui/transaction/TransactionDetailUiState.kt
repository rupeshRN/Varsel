package com.varsel.expensetracker.ui.transaction

import com.varsel.expensetracker.domain.model.Transaction

sealed interface TransactionDetailUiState {

    object Loading : TransactionDetailUiState

    data class Loaded(

        val transaction: Transaction,

        val editableDescription: String,

        val selectedCategory: String,

        val hasChanges: Boolean,

        val isSaving: Boolean = false

    ) : TransactionDetailUiState

    data class Error(

        val message: String

    ) : TransactionDetailUiState

}
