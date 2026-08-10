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
import java.time.Instant
import java.time.YearMonth
import java.time.ZoneId

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

val months = transactions

    .map {

        YearMonth.from(

            Instant.ofEpochMilli(it.dateTimestamp)
                .atZone(ZoneId.systemDefault())

        )

    }

    .distinct()

    .sortedDescending()

val availableMonths = months.map { yearMonth ->

    TransactionMonth(

        yearMonth = yearMonth,

        displayName = yearMonth.month.name
            .lowercase()
            .replaceFirstChar {

                it.uppercase()

            }
            .take(3)

    )

}
                    
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

    _uiState.update { state ->

    state.copy(

        transactions =
            transactionUiMapper.map(transactions),

        availableMonths = availableMonths,

        selectedMonth =

            state.selectedMonth
                ?: availableMonths.firstOrNull(),

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

    fun updateSelectedMonth(

    month: TransactionMonth

) {

    _uiState.update {

        it.copy(

            selectedMonth = month

        )

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
