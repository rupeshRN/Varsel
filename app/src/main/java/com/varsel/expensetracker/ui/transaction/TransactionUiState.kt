package com.varsel.expensetracker.ui.transaction

import com.varsel.expensetracker.ui.model.TransactionUiModel

data class TransactionUiState(

    val transactions: List<TransactionUiModel> = emptyList(),

    val selectedMonth: String = currentMonth(),

    val selectedFilter: TransactionFilter = TransactionFilter.All,

    val searchQuery: String = "",

    val isLoading: Boolean = false

)

enum class TransactionFilter {

    All,

    Income,

    Expense

}

private fun currentMonth(): String {

    return java.time.LocalDate.now()
        .month
        .name
        .lowercase()
        .replaceFirstChar { it.uppercase() }

}
