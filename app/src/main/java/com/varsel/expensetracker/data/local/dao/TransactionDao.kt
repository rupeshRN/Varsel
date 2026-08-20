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

    //--------------------------------------------------
    // Observe all transactions
    //--------------------------------------------------

    @Query(
        """
        SELECT *
        FROM transactions
        ORDER BY dateTimestamp DESC
        """
    )
    fun getAllTransactions():
        Flow<List<TransactionEntity>>

    //--------------------------------------------------
    // Insert transactions
    //--------------------------------------------------

    @Transaction
    @Insert(
        onConflict = OnConflictStrategy.REPLACE
    )
    suspend fun insertTransactions(
        transactions: List<TransactionEntity>
    )

    @Insert(
        onConflict = OnConflictStrategy.REPLACE
    )
    suspend fun insertTransaction(
        transaction: TransactionEntity
    )

    //--------------------------------------------------
    // Update transaction
    //--------------------------------------------------

    @Update
    suspend fun updateTransaction(
        transaction: TransactionEntity
    )

    //--------------------------------------------------
    // Delete transaction
    //--------------------------------------------------

    @Delete
    suspend fun deleteTransaction(
        transaction: TransactionEntity
    )

    //--------------------------------------------------
    // Get transaction
    //--------------------------------------------------

    @Query(
        """
        SELECT *
        FROM transactions
        WHERE id = :id
        """
    )
    suspend fun getTransactionById(
        id: Long
    ): TransactionEntity?

    //--------------------------------------------------
    // Existing fingerprints
    //--------------------------------------------------

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

    //--------------------------------------------------
    // Link transactions to Financial Event
    //
    // IMPORTANT:
    //
    // Linking also assigns the Financial Event role:
    //
    // EXPENSE -> LENT
    // INCOME  -> REIMBURSEMENT
    //
    // This is deliberately done in the DAO so every
    // caller gets identical behaviour.
    //--------------------------------------------------

    @Query(
        """
        UPDATE transactions
        SET
            transactionLinkId = :transactionLinkId,
            role = CASE
                WHEN type = 'EXPENSE'
                    THEN 'LENT'
                WHEN type = 'INCOME'
                    THEN 'REIMBURSEMENT'
                ELSE role
            END
        WHERE id IN (:transactionIds)
        """
    )
    suspend fun linkTransactions(
        transactionIds: List<Long>,
        transactionLinkId: String
    )

    //--------------------------------------------------
    // Remove transaction from Financial Event
    //
    // The transaction itself remains.
    //
    // Role is restored to NORMAL because the Financial
    // Event classification no longer applies.
    //--------------------------------------------------

    @Query(
        """
        UPDATE transactions
        SET
            transactionLinkId = NULL,
            role = 'NORMAL'
        WHERE id = :transactionId
        """
    )
    suspend fun unlinkTransaction(
        transactionId: Long
    )
}
