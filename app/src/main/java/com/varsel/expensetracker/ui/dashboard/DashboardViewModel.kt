package com.varsel.expensetracker.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.varsel.expensetracker.domain.model.Transaction
import com.varsel.expensetracker.ui.model.TransactionUiModel
import com.varsel.expensetracker.domain.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.varsel.expensetracker.ui.model.TransactionUiMapper

data class DashboardUiState(
    val totalBalance: Double = 0.0,
    val totalIncome: Double = 0.0,
    val totalExpense: Double = 0.0,
    val recentTransactions: List<TransactionUiModel> = emptyList(),
    val isLoading: Boolean = true
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val transactionUiMapper: TransactionUiMapper
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        loadTransactions()
    }

    private fun loadTransactions() {
        viewModelScope.launch(Dispatchers.IO) {
            transactionRepository.getAllTransactions().collect { transactions ->
                val totalIncome = transactions.filter { it.type == com.varsel.expensetracker.domain.model.TransactionType.INCOME }.sumOf { it.amount }
                val totalExpense = transactions.filter { it.type == com.varsel.expensetracker.domain.model.TransactionType.EXPENSE }.sumOf { it.amount }
                val totalBalance = totalIncome - totalExpense

                _uiState.update {
                    it.copy(
                        totalBalance = totalBalance,
                        totalIncome = totalIncome,
                        totalExpense = totalExpense,
                        recentTransactions = transactions.take(10),
                        isLoading = false
                    )
                }
            }
        }
    }

    fun updateTransaction(transaction: Transaction) {
        viewModelScope.launch(Dispatchers.IO) {
            transactionRepository.updateTransaction(transaction)
        }
    }
}
