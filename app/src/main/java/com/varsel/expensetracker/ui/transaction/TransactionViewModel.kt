package com.varsel.expensetracker.ui.transaction

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.varsel.expensetracker.domain.model.Transaction
import com.varsel.expensetracker.domain.model.TransactionType
import com.varsel.expensetracker.domain.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Immutable UI State for the Dashboard and Transaction Ledger screens.
 */
data class TransactionUiState(
    val transactions: List<Transaction> = emptyList(),
    val totalIncome: Double = 0.0,
    val totalExpense: Double = 0.0,
    val netBalance: Double = 0.0,
    val searchQuery: String = "",
    val selectedCategoryId: Long? = null,
    val isLoading: Boolean = true,
    val errorMessage: String? = null
)

/**
 * ViewModel managing dashboard balance metrics, transaction filters, 
 * search operations, and ledger CRUD actions.
 */
@HiltViewModel
class TransactionViewModel @Inject constructor(
    private val repository: TransactionRepository
) : ViewModel() {

    // User filter states
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategoryId = MutableStateFlow<Long?>(null)
    val selectedCategoryId: StateFlow<Long?> = _selectedCategoryId.asStateFlow()

    /**
     * Reactive UI State pipeline combining database flow with user filter states.
     * Automatically updates the Compose UI whenever DB entries or search/filter criteria change.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<TransactionUiState> = combine(
        _searchQuery,
        _selectedCategoryId
    ) { query, categoryId ->
        Pair(query, categoryId)
    }.flatMapLatest { (query, categoryId) ->
        // Fetch raw stream from Room repository
        repository.getAllTransactions().map { allTransactions ->
            
            // Filter transactions dynamically in memory based on search & category
            val filtered = allTransactions.filter { transaction ->
                val matchesQuery = query.isBlank() || 
                    transaction.description.contains(query, ignoreCase = true) ||
                    (transaction.referenceNumber?.contains(query, ignoreCase = true) == true)

                val matchesCategory = categoryId == null || transaction.categoryId == categoryId

                matchesQuery && matchesCategory
            }

            // Calculate aggregate financial summary numbers
            val income = allTransactions
                .filter { it.type == TransactionType.INCOME }
                .sumOf { it.amount }

            val expense = allTransactions
                .filter { it.type == TransactionType.EXPENSE }
                .sumOf { it.amount }

            val balance = income - expense

            TransactionUiState(
                transactions = filtered,
                totalIncome = income,
                totalExpense = expense,
                netBalance = balance,
                searchQuery = query,
                selectedCategoryId = categoryId,
                isLoading = false
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = TransactionUiState(isLoading = true)
    )

    /**
     * Updates the transaction search filter.
     */
    fun onSearchQueryChange(newQuery: String) {
        _searchQuery.value = newQuery
    }

    /**
     * Applies or clears category-specific filtering in the ledger view.
     */
    fun onCategoryFilterChange(categoryId: Long?) {
        _selectedCategoryId.value = categoryId
    }

    /**
     * Inserts a single manual transaction (Income or Expense).
     */
    fun addTransaction(
        amount: Double,
        type: TransactionType,
        description: String,
        categoryId: Long?,
        timestamp: Long = System.currentTimeMillis(),
        referenceNumber: String? = null
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val transaction = Transaction(
                amount = amount,
                type = type,
                description = description,
                categoryId = categoryId,
                timestamp = timestamp,
                referenceNumber = referenceNumber,
                isAutoParsed = false
            )
            repository.insertTransaction(transaction)
        }
    }

    /**
     * Updates an existing transaction entry.
     */
    fun updateTransaction(transaction: Transaction) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateTransaction(transaction)
        }
    }

    /**
     * Deletes a transaction from the encrypted local database.
     */
    fun deleteTransaction(transaction: Transaction) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteTransaction(transaction)
 
        }
    }
}
