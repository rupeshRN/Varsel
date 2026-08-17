package com.varsel.expensetracker.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.varsel.expensetracker.data.local.entity.TransactionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {

    @Query("SELECT * FROM transactions ORDER BY dateTimestamp DESC")
    fun getAllTransactions(): Flow<List<TransactionEntity>>

    @Transaction
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransactions(transactions: List<TransactionEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: TransactionEntity)

    @Update
    suspend fun updateTransaction(transaction: TransactionEntity)

    @Delete
    suspend fun deleteTransaction(transaction: TransactionEntity)

    @Query("SELECT * FROM transactions WHERE id = :id")
    suspend fun getTransactionById(id: Long): TransactionEntity?

    @Query(
    """
    SELECT transactionFingerprint
    FROM transactions
    WHERE transactionFingerprint IN (:fingerprints)
    AND transactionFingerprint IS NOT NULL
    """
)
suspend fun findExistingFingerprints(
    fingerprints: List<String>
): List<String>

/**
     * Assign the same internal financial-event link
     * to multiple transactions.
     */
    @Query(
        """
        UPDATE transactions
        SET transactionLinkId = :transactionLinkId
        WHERE id IN (:transactionIds)
        """
    )
    suspend fun linkTransactions(
        transactionIds: List<Long>,
        transactionLinkId: String
    )
    /**
     * Remove a transaction from its linked financial event.
     */
    @Query(
        """
        UPDATE transactions
        SET transactionLinkId = NULL
        WHERE id = :transactionId
        """
    )
    suspend fun unlinkTransaction(
        transactionId: Long
    )

        /**
     * Return all transactions belonging to a link group.
     */
    @Query(
        """
        SELECT *
        FROM transactions
        WHERE transactionLinkId = :transactionLinkId
        ORDER BY dateTimestamp ASC
        """
    )
    suspend fun getLinkedTransactions(
        transactionLinkId: String
    ): List<TransactionEntity>
    
    /**
     * Return reimbursement transactions that have not
     * already been manually linked.
     *
     * The current transaction is excluded.
     */
    @Query(
        """
        SELECT *
        FROM transactions
        WHERE type = 'INCOME'
        AND role = 'REIMBURSEMENT'
        AND transactionLinkId IS NULL
        AND id != :currentTransactionId
        ORDER BY dateTimestamp DESC
        """
    )
    suspend fun getUnlinkedReimbursements(
        currentTransactionId: Long
    ): List<TransactionEntity>
}
