package com.varsel.expensetracker.ui.mapper

import com.varsel.expensetracker.domain.model.Transaction
import com.varsel.expensetracker.domain.model.TransactionType
import com.varsel.expensetracker.ui.dashboard.DashboardUiState
import com.varsel.expensetracker.ui.model.BalanceSummaryUiModel
import com.varsel.expensetracker.ui.mapper.TransactionUiMapper
import javax.inject.Inject

class DashboardUiMapper @Inject constructor(

    private val transactionUiMapper: TransactionUiMapper

) {

    fun map(

        transactions: List<Transaction>

    ): DashboardUiState {

        val income = transactions

            .filter {

                it.type == TransactionType.INCOME

            }

            .sumOf {

                it.amount

            }

        val expense = transactions

            .filter {

                it.type == TransactionType.EXPENSE

            }

            .sumOf {

                it.amount

            }

        val balance = income - expense

        return DashboardUiState(

            balanceSummary = BalanceSummaryUiModel(

                totalBalance = balance,

                totalIncome = income,

                totalExpense = expense,

                savings = balance,

                accounts = emptyList()

            ),

            recentTransactions =

                transactions

                    .take(10)

                    .map {

                        transactionUiMapper.map(it)

                    },

            isLoading = false
        )
    }
}
