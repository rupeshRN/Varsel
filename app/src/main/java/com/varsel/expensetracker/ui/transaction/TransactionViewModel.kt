package com.varsel.expensetracker.ui.transaction

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.varsel.expensetracker.domain.model.Transaction
import com.varsel.expensetracker.domain.model.TransactionType
import com.varsel.expensetracker.domain.repository.TransactionRepository
import com.varsel.expensetracker.ui.model.TransactionUiMapper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TransactionViewModel @Inject constructor(

    private val repository: TransactionRepository,

    private val transactionUiMapper: TransactionUiMapper

) : ViewModel() {

    private val _uiState =
        MutableStateFlow(TransactionUiState())

    val uiState: StateFlow<TransactionUiState> =
        _uiState.asStateFlow()

    init {

        loadTransactions()

    }

    private fun loadTransactions() {

        viewModelScope.launch {

            repository
                .getAllTransactions()
                .collectLatest { transactions ->

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

    _uiState.update {

        it.copy(

            transactions =
                transactionUiMapper.map(transactions),

            monthlyIncome = income,

            monthlyExpense = expense,

            isLoading = false

        )

    }

}

        }

    }

    fun updateSearchQuery(query: String) {

        _uiState.update {

            it.copy(searchQuery = query)

        }

    }

    fun updateSelectedMonth(month: String) {

        _uiState.update {

            it.copy(selectedMonth = month)

        }

    }

    fun updateFilter(filter: TransactionFilter) {

        _uiState.update {

            it.copy(selectedFilter = filter)

        }

    }

    fun addTransaction(

        amount: Double,

        type: TransactionType,

        description: String,

        category: String,

        dateTimestamp: Long,

        referenceNumber: String?

    ) {

        viewModelScope.launch(Dispatchers.IO) {

            repository.insertTransactions(

                listOf(

                    Transaction(

                        amount = amount,

                        type = type,

                        description = description,

                        category = category,

                        dateTimestamp = dateTimestamp,

                        referenceNumber = referenceNumber

                    )

                )

            )

        }

    }

}
