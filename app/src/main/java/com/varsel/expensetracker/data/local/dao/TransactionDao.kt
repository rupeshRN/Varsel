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
    // EXPENSE -> LENT
    // INCOME  -> REIMBURSEMENT
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
    // Link two transactions as a Transfer
    //
    // The first transaction must be the outgoing side.
    // The second transaction must be the incoming side.
    //
    // The caller supplies the IDs explicitly.
    //--------------------------------------------------

    @Query(
        """
        UPDATE transactions
        SET
            transactionLinkId = :transactionLinkId,
            role = CASE
                WHEN id = :transferOutTransactionId
                    THEN 'TRANSFER_OUT'

                WHEN id = :transferInTransactionId
                    THEN 'TRANSFER_IN'

                ELSE role
            END
        WHERE id IN (
            :transferOutTransactionId,
            :transferInTransactionId
        )
        """
    )
    suspend fun linkTransferTransactions(
        transferOutTransactionId: Long,
        transferInTransactionId: Long,
        transactionLinkId: String
    )

    //--------------------------------------------------
    // Remove transaction from Financial Event
    //
    // This restores NORMAL.
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
