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
        "SELECT * FROM transactions ORDER BY dateTimestamp DESC"
    )
    fun getAllTransactions():
        Flow<List<TransactionEntity>>

    //--------------------------------------------------
    // Insert multiple transactions
    //--------------------------------------------------

    @Transaction
    @Insert(
        onConflict =
            OnConflictStrategy.REPLACE
    )
    suspend fun insertTransactions(
        transactions:
            List<TransactionEntity>
    )

    //--------------------------------------------------
    // Insert single transaction
    //--------------------------------------------------

    @Insert(
        onConflict =
            OnConflictStrategy.REPLACE
    )
    suspend fun insertTransaction(
        transaction:
            TransactionEntity
    )

    //--------------------------------------------------
    // Update transaction
    //--------------------------------------------------

    @Update
    suspend fun updateTransaction(
        transaction:
            TransactionEntity
    )

    //--------------------------------------------------
    // Delete transaction
    //--------------------------------------------------

    @Delete
    suspend fun deleteTransaction(
        transaction:
            TransactionEntity
    )

    //--------------------------------------------------
    // Get transaction by ID
    //--------------------------------------------------

    @Query(
        "SELECT * FROM transactions WHERE id = :id"
    )
    suspend fun getTransactionById(
        id: Long
    ): TransactionEntity?

    //--------------------------------------------------
    // Find existing fingerprints
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
        fingerprints:
            List<String>
    ): List<String>

    //--------------------------------------------------
    // Generic transaction linking
    //--------------------------------------------------
    //
    // IMPORTANT:
    // This only assigns transactionLinkId.
    //
    // It does NOT change the transaction role.
    //
    // Existing transaction-detail linking continues
    // to use this operation.
    //--------------------------------------------------

    @Query(
        """
        UPDATE transactions
        SET transactionLinkId = :transactionLinkId
        WHERE id IN (:transactionIds)
        """
    )
    suspend fun linkTransactions(
        transactionIds:
            List<Long>,

        transactionLinkId:
            String
    )

    //--------------------------------------------------
    // Financial Event reimbursement linking
    //--------------------------------------------------
    //
    // This is intentionally separate from
    // linkTransactions().
    //
    // Selected income transactions become:
    //
    //     transactionLinkId = Financial Event ID
    //     role = REIMBURSEMENT
    //
    // This allows the Financial Event screen to select
    // ordinary income transactions and explicitly convert
    // them into reimbursements.
    //--------------------------------------------------

    @Query(
        """
        UPDATE transactions
        SET
            transactionLinkId = :transactionLinkId,
            role = 'REIMBURSEMENT'
        WHERE id IN (:transactionIds)
        AND type = 'INCOME'
        """
    )
    suspend fun linkReimbursements(
        transactionIds:
            List<Long>,

        transactionLinkId:
            String
    )

    //--------------------------------------------------
    // Remove transaction from Financial Event
    //--------------------------------------------------

    @Query(
        """
        UPDATE transactions
        SET transactionLinkId = NULL
        WHERE id = :transactionId
        """
    )
    suspend fun unlinkTransaction(
        transactionId:
            Long
    )

    //--------------------------------------------------
    // Get all transactions belonging to a Financial Event
    //--------------------------------------------------

    @Query(
        """
        SELECT *
        FROM transactions
        WHERE transactionLinkId = :transactionLinkId
        ORDER BY dateTimestamp ASC
        """
    )
    suspend fun getLinkedTransactions(
        transactionLinkId:
            String
    ): List<TransactionEntity>

    //--------------------------------------------------
    // Get unlinked reimbursement transactions
    //--------------------------------------------------
    //
    // Kept for the existing transaction-detail linking
    // functionality.
    //
    // This query intentionally returns only incomes that
    // are ALREADY marked as REIMBURSEMENT.
    //
    // FinancialEventScreen uses the broader income list
    // and linkReimbursements() for its Add Reimbursements
    // workflow.
    //--------------------------------------------------

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
        currentTransactionId:
            Long
    ): List<TransactionEntity>
}
