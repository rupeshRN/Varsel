package com.varsel.expensetracker.ui.transaction

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.varsel.expensetracker.domain.model.Transaction
import com.varsel.expensetracker.domain.model.TransactionType
import com.varsel.expensetracker.domain.repository.TransactionRepository
import com.varsel.expensetracker.ui.model.TransactionUiMapper
import com.varsel.expensetracker.ui.model.TransactionUiModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TransactionViewModel @Inject constructor(

    private val repository: TransactionRepository,

    private val transactionUiMapper: TransactionUiMapper

) : ViewModel() {

    private val _transactions =
        MutableStateFlow<List<TransactionUiModel>>(emptyList())

    val transactions: StateFlow<List<TransactionUiModel>> =
        _transactions.asStateFlow()

    init {

        loadTransactions()

    }

    private fun loadTransactions() {

        viewModelScope.launch {

            repository
                .getAllTransactions()
                .collectLatest { transactions ->

                    _transactions.value =
                        transactionUiMapper.map(transactions)

                }

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
