
package com.varsel.expensetracker.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.yourdomain.expensetracker.data.local.entity.TransactionEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object (DAO) for executing database CRUD operations on transactions.
 * Exposes reactive Kotlin Flows for UI observation and suspend functions for safe I/O.
 */
@Dao
interface TransactionDao {

    // ==========================================
    // READ OPERATIONS (Reactive Streaming via Flow)
    // ==========================================

    /**
     * Retrieves all transactions ordered by date (newest first).
     */
    @Query("SELECT * FROM transactions ORDER BY dateTimestamp DESC")
    fun getAllTransactions(): Flow<List<TransactionEntity>>

    /**
     * Filters transactions by type ("INCOME" or "EXPENSE").
     */
    @Query("SELECT * FROM transactions WHERE type = :type ORDER BY dateTimestamp DESC")
    fun getTransactionsByType(type: String): Flow<List<TransactionEntity>>

    /**
     * Retrieves transactions within a specific date range.
     * Useful for monthly/weekly budget filters and analytics views.
     */
    @Query("SELECT * FROM transactions WHERE dateTimestamp BETWEEN :startDate AND :endDate ORDER BY dateTimestamp DESC")
    fun getTransactionsBetweenDates(startDate: Long, endDate: Long): Flow<List<TransactionEntity>>

    /**
     * Calculates total income or total expenses for a date range.
     * Returns null if no transactions match the query.
     */
    @Query("SELECT SUM(amount) FROM transactions WHERE type = :type AND dateTimestamp BETWEEN :startDate AND :endDate")
    fun getTotalAmountByTypeAndDateRange(type: String, startDate: Long, endDate: Long): Flow<Double?>

    /**
     * Checks if a reference number already exists in the database.
     * Used by StatementParserEngine to skip duplicate entries during scanning.
     */
    @Query("SELECT * FROM transactions WHERE referenceNumber = :referenceNumber LIMIT 1")
    suspend fun getTransactionByReference(referenceNumber: String): TransactionEntity?

    // ==========================================
    // WRITE OPERATIONS (Coroutines Suspend Functions)
    // ==========================================

    /**
     * Inserts a single transaction. Returns the generated auto-increment ID.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: TransactionEntity): Long

    /**
     * Batch inserts multiple parsed transactions from a bank statement in a single transaction.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransactions(transactions: List<TransactionEntity>)

    /**
     * Updates an existing transaction (e.g., user reassigned the category).
     */
    @Update
    suspend fun updateTransaction(transaction: TransactionEntity)

    /**
     * Deletes a specific transaction.
     */
    @Delete
    suspend fun deleteTransaction(transaction: TransactionEntity)

    /**
     * Purges all transactions from local storage.
     */
    @Query("DELETE FROM transactions")
    suspend fun
  deleteAll()
}
