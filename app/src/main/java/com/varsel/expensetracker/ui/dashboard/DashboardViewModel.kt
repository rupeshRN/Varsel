package com.varsel.expensetracker.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.varsel.expensetracker.domain.model.Transaction
import com.varsel.expensetracker.domain.model.TransactionType
import com.varsel.expensetracker.domain.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

data class DashboardUiState(
    val recentTransactions: List<Transaction> = emptyList(),
    val totalBalance: Double = 0.0,
    val isLoading: Boolean = false
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    repository: TransactionRepository
) : ViewModel() {

    val uiState: StateFlow<DashboardUiState> = repository.getAllTransactions()
        .map { transactions ->
            DashboardUiState(
                recentTransactions = transactions.take(10),
                totalBalance = transactions.sumOf { 
                    if (it.type == TransactionType.CREDIT || it.type == TransactionType.INCOME) it.amount else -it.amount 
                },
                isLoading = false
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = DashboardUiState(isLoading = true)
        )
}
