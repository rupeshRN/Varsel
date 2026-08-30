package com.varsel.expensetracker.ui.dashboard

import com.varsel.expensetracker.domain.model.loan.LoanSummary
import com.varsel.expensetracker.ui.model.BalanceSummaryUiModel
<<<<<<< HEAD
import com.varsel.expensetracker.ui.model.TransactionUiModel

data class DashboardUiState(

=======
import com.varsel.expensetracker.ui.model.FinancialInsight
import com.varsel.expensetracker.ui.model.TransactionUiModel

data class DashboardUiState(
>>>>>>> source-repo/main
    val balanceSummary: BalanceSummaryUiModel =
        BalanceSummaryUiModel(
            totalBalance = 0.0,
            totalIncome = 0.0,
            totalExpense = 0.0,
            savings = 0.0,
            accounts = emptyList()
        ),

    val recentTransactions: List<TransactionUiModel> = emptyList(),

    val loans: List<LoanSummary> = emptyList(),

<<<<<<< HEAD
=======
    val insights: List<FinancialInsight> = emptyList(),

>>>>>>> source-repo/main
    val isLoading: Boolean = true
)
