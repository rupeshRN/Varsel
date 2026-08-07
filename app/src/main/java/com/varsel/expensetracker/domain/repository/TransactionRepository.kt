package com.varsel.expensetracker.domain.repository

import com.varsel.expensetracker.domain.model.Transaction
import com.varsel.expensetracker.domain.model.TransactionType
import kotlinx.coroutines.flow.Flow

/**
 * Domain-level Repository interface defining core data access operations for financial entries.
 * 
 * ViewModels depend strictly on this abstraction contract rather than concrete database classes 
 * or Room DAOs. This keeps the presentation layer decoupled from persistent storage mechanisms.
 */
interface TransactionRepository {

    /**
     * Observes all recorded transactions ordered by execution timestamp (newest first).
     * Returns a reactive Kotlin Flow stream that automatically emits updates when underlying data changes.
     */
    fun getAllTransactions(): Flow<List<Transaction>>

    /**
     * Observes transactions filtered by direction type (INCOME or EXPENSE).
     * 
     * @param type The TransactionType enum filter value.
     */
    fun getTransactionsByType(type: TransactionType): Flow<List<Transaction>>

    /**
     * Observes transactions occurring within an epoch millisecond time window (inclusive).
     * 
     * @param startDate Start boundary timestamp in milliseconds.
     * @param endDate End boundary timestamp in milliseconds.
     */
    fun getTransactionsBetweenDates(startDate: Long, endDate: Long): Flow<List<Transaction>>

    /**
     * Calculates and observes the aggregate sum for a specific transaction type within a given time range.
     * 
     * @param type TransactionType (INCOME or EXPENSE).
     * @param startDate Start timestamp in milliseconds.
     * @param endDate End timestamp in milliseconds.
     * @return Flow emitting total monetary sum as Double (defaults to 0.0 if no entries match).
     */
    fun getTotalAmountByTypeAndDateRange(type: TransactionType, startDate: Long, endDate: Long): Flow<Double>

    /**
     * Inserts a single transaction entry into persistent storage.
     * 
     * @param transaction Domain model instance to save.
     * @return Auto-generated record ID (Long).
     */
    suspend fun insertTransaction(transaction: Transaction): Long

    /**
     * Batch inserts a collection of transactions (primarily utilized during bank statement imports).
     * 
     * @param transactions List of domain model entries to save.
     */
    suspend fun insertTransactions(transactions: List<Transaction>)

    /**
     * Updates an existing transaction record in persistent storage.
     */
    suspend fun updateTransaction(transaction: Transaction)

    /**
     * Deletes a specific transaction record from persistent storage.
     */
    suspend fun deleteTransaction(transaction: Transaction)

    /**
     * Runs raw transaction inputs through SmartCategorizerEngine offline,
     * assigns the matching category based on memory rules or history, and saves it.
     * 
     * @return The freshly created and persisted domain Transaction instance.
     */
    suspend fun autoCategorizeAndSave(
        rawDescription: String,
        amount: Double,
        type: TransactionType,
        timestamp: Long,
        bankName: String? = null,
        refNo: String? = null
    ): Transaction
}
