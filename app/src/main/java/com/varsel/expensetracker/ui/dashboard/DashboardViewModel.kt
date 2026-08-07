package com.varsel.expensetracker.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.varsel.expensetracker.domain.model.Transaction
import com.varsel.expensetracker.domain.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        loadDashboardData()
    }

    private fun loadDashboardData() {
        viewModelScope.launch {
            transactionRepository.getAllTransactions().collectLatest { transactions ->
                val totalIncome = transactions.filter { it.type == com.varsel.expensetracker.domain.model.TransactionType.INCOME }.sumOf { it.amount }
                val totalExpense = transactions.filter { it.type == com.varsel.expensetracker.domain.model.TransactionType.EXPENSE }.sumOf { it.amount }
                val netBalance = totalIncome - totalExpense

                _uiState.value = _uiState.value.copy(
                    recentTransactions = transactions.sortedByDescending { it.dateTimestamp }.take(10),
                    totalBalance = netBalance,
                    totalIncome = totalIncome,
                    totalExpense = totalExpense,
                    isLoading = false
                )
            }
        }
    }

    fun updateTransaction(transaction: Transaction) {
        viewModelScope.launch(Dispatchers.IO) {
            transactionRepository.updateTransaction(transaction)
        }
    }
}

data class DashboardUiState(
    val recentTransactions: List<Transaction> = emptyList(),
    val totalBalance: Double = 0.0,
    val totalIncome: Double = 0.0,
    val totalExpense: Double = 0.0,
    val isLoading: Boolean = true
)
