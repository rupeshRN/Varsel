package com.varsel.expensetracker.ui.transaction

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.varsel.expensetracker.domain.model.Transaction
import com.varsel.expensetracker.domain.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TransactionViewModel @Inject constructor(
    private val repository: TransactionRepository
) : ViewModel() {

    private val _transactions = MutableStateFlow<List<Transaction>>(emptyList())
    val transactions: StateFlow<List<Transaction>> = _transactions.asStateFlow()

    init {
        loadTransactions()
    }

    private fun loadTransactions() {
        viewModelScope.launch {
            repository.getAllTransactions().collect { list ->
                _transactions.value = list
            }
        }
    }

    fun addTransaction(
        amount: Double,
        type: String,
        description: String,
        category: String,
        dateTimestamp: Long,
        referenceNumber: String?
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            // INLINE FIX: Updated Transaction constructor to match domain model fields
            // - Replaced 'categoryId' with 'category' (String)
            // - Replaced 'timestamp' with 'dateTimestamp' (Long)
            val transaction = Transaction(
                amount = amount,
                type = type,
                description = description,
                category = category,
                dateTimestamp = dateTimestamp,
                referenceNumber = referenceNumber
            )
            repository.insertTransactions(listOf(transaction))
        }
    }
}
