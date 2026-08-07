package com.varsel.expensetracker.domain.repository

import com.varsel.expensetracker.domain.model.Transaction
import com.varsel.expensetracker.domain.model.TransactionType
import kotlinx.coroutines.flow.Flow

interface TransactionRepository {
    fun getAllTransactions(): Flow<List<Transaction>>
    fun getTransactionsByType(type: TransactionType): Flow<List<Transaction>>
    suspend fun insertTransactions(transactions: List<Transaction>)
    suspend fun insertTransaction(transaction: Transaction)
}
