package com.varsel.expensetracker.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.varsel.expensetracker.data.local.entity.TransactionEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object (DAO) for managing transaction database operations in Room.
 * Provides reactive Kotlin Flow queries for real-time UI updates alongside suspend 
 * functions for asynchronous write operations.
 */
@Dao
interface TransactionDao {

    /**
     * Retrieves all recorded transactions ordered by date in descending order (newest first).
     * Returns a reactive Flow that automatically emits updated lists whenever the database changes.
     */
    @Query("SELECT * FROM transactions ORDER BY dateTimestamp DESC")
    fun getAllTransactions(): Flow<List<TransactionEntity>>

    /**
     * Filters transactions by type ("INCOME" or "EXPENSE") ordered newest first.
     * 
     * @param type The transaction type string to filter by.
     */
    @Query("SELECT * FROM transactions WHERE type = :type ORDER BY dateTimestamp DESC")
    fun getTransactionsByType(type: String): Flow<List<TransactionEntity>>

    /**
     * Fetches transactions occurring within a specific date range (inclusive).
     * Useful for weekly, monthly, or custom report screens.
     * 
     * @param startDate Start timestamp in epoch milliseconds.
     * @param endDate End timestamp in epoch milliseconds.
     */
    @Query("SELECT * FROM transactions WHERE dateTimestamp BETWEEN :startDate AND :endDate ORDER BY dateTimestamp DESC")
    fun getTransactionsBetweenDates(startDate: Long, endDate: Long): Flow<List<TransactionEntity>>

    /**
     * Calculates the aggregate monetary sum for a specific transaction type within a given date range.
     * Returns a Flow emitting null if no records match the query parameters.
     * 
     * @param type The transaction type ("INCOME" or "EXPENSE").
     * @param startDate Start timestamp in epoch milliseconds.
     * @param endDate End timestamp in epoch milliseconds.
     */
    @Query("SELECT SUM(amount) FROM transactions WHERE type = :type AND dateTimestamp BETWEEN :startDate AND :endDate")
    fun getTotalAmountByTypeAndDateRange(type: String, startDate: Long, endDate: Long): Flow<Double?>

    /**
     * One-shot snapshot fetch of all transactions that have already been assigned a non-default category.
     * Used by SmartCategorizerEngine to analyze user historical categorization patterns offline.
     */
    @Query("SELECT * FROM transactions WHERE categoryName != 'Uncategorized'")
    suspend fun getCategorizedTransactionsSnapshot(): List<TransactionEntity>

    /**
     * Inserts a single transaction record into the database.
     * If a transaction with the same primary key exists, it will be replaced.
     * 
     * @return The auto-generated row ID (Long) of the inserted transaction.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: TransactionEntity): Long

    /**
     * Batch inserts a list of transaction records into the database.
     * Used during bank statement CSV/PDF import operations.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransactions(transactions: List<TransactionEntity>)

    /**
     * Updates an existing transaction record in the database.
     */
    @Update
    suspend fun updateTransaction(transaction: TransactionEntity)

    /**
     * Deletes a specific transaction record from the database.
     */
    @Delete
    suspend fun deleteTransaction(transaction: Transactio
                                  nEntity)
}
