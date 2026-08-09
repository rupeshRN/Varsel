package com.varsel.expensetracker.ui.model

data class BalanceSummaryUiModel(

    val totalBalance: Double,

    val totalIncome: Double,

    val totalExpense: Double,

    val savings: Double,

    val accounts: List<AccountBalanceUiModel> = emptyList()
)

data class AccountBalanceUiModel(

    val bankName: String,

    val accountDisplayName: String,

    val balance: Double
)
