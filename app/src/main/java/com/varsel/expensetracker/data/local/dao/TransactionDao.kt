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
 * Data Access Object (DAO) interface for managing [TransactionEntity] database queries.
 * Provides reactive data streams using Kotlin Coroutines [Flow] and asynchronous suspend functions.
 */
@Dao
interface TransactionDao {

    /**
     * Retrieves all transaction records from the database ordered by timestamp in descending order.
     * Returns a reactive [Flow] to automatically emit updates when database contents change.
     */
    @Query("SELECT * FROM transactions ORDER BY dateTimestamp DESC")
    fun getAllTransactions(): Flow<List<TransactionEntity>>

    /**
     * Retrieves transactions filtered by a specific category.
     * 
     * INLINE FIX: Uses the exact column name 'category' matching [TransactionEntity] 
     * to prevent KSP compilation errors.
     */
    @Query("SELECT * FROM transactions WHERE category = :category ORDER BY dateTimestamp DESC")
    fun getTransactionsByCategory(category: String): Flow<List<TransactionEntity>>

    /**
     * Retrieves transactions filtered by transaction type (e.g., CREDIT, DEBIT).
     */
    @Query("SELECT * FROM transactions WHERE type = :type ORDER BY dateTimestamp DESC")
    fun getTransactionsByType(type: String): Flow<List<TransactionEntity>>

    /**
     * Inserts a single transaction into the database, replacing it if a primary key conflict occurs.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: TransactionEntity)

    /**
     * Inserts a batch of transactions into the database efficiently, replacing any conflicts.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransactions(transactions: List<TransactionEntity>)

    /**
     * Updates an existing transaction record in the database.
     */
    @Update
    suspend fun updateTransaction(transaction: TransactionEntity)

    /**
     * Deletes a specified transaction record from the database.
     */
    @Delete
    suspend fun deleteTransaction(transaction: TransactionEntity)
}
