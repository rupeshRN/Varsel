package com.varsel.expensetracker.domain.repository

import com.varsel.expensetracker.domain.model.Transaction
import kotlinx.coroutines.flow.Flow

interface TransactionRepository {
    fun getAllTransactions(): Flow<List<Transaction>>
    suspend fun insertTransactions(transactions: List<Transaction>)
    suspend fun insertTransaction(transaction: Transaction)
    suspend fun updateTransaction(transaction: Transaction)
    suspend fun deleteTransaction(transaction: Transaction)
    suspend fun getTransactionById(id: Long): Transaction?
    suspend fun findExistingFingerprints(fingerprints: List<String>): Set<String>
    /**
     * Assigns the same internal link ID to multiple transactions.
     *
     * Used when the user manually links transactions that
     * belong to the same financial event.
     */
    suspend fun linkTransactions(
        transactionIds: List<Long>,
        transactionLinkId: String
    )

    /**
     * Removes the relationship from a transaction.
     *
     * The transaction itself is not deleted.
     */
    suspend fun unlinkTransaction(
        transactionId: Long
    )
}
