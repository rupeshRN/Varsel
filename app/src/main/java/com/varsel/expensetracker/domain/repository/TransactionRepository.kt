package com.varsel.expensetracker.domain.repository

import com.varsel.expensetracker.domain.model.Transaction
import kotlinx.coroutines.flow.Flow

interface TransactionRepository {

    //--------------------------------------------------
    // Transactions
    //--------------------------------------------------

    fun getAllTransactions():
        Flow<List<Transaction>>

    suspend fun insertTransactions(
        transactions:
            List<Transaction>
    )

    suspend fun insertTransaction(
        transaction:
            Transaction
    )

    suspend fun updateTransaction(
        transaction:
            Transaction
    )

    suspend fun deleteTransaction(
        transaction:
            Transaction
    )

    suspend fun getTransactionById(
        id: Long
    ):
        Transaction?

    suspend fun findExistingFingerprints(
        fingerprints:
            List<String>
    ):
        Set<String>

    //--------------------------------------------------
    // Financial Event linking
    //--------------------------------------------------

    /**
     * Links transactions to a Financial Event.
     *
     * Uses transactionLinkId.
     */
    suspend fun linkTransactions(

        transactionIds:
            List<Long>,

        transactionLinkId:
            String
    )

    //--------------------------------------------------
    // Transfer linking
    //--------------------------------------------------

    /**
     * Links exactly two transactions as one transfer.
     *
     * transferOutTransactionId:
     *     outgoing side of the transfer
     *
     * transferInTransactionId:
     *     incoming side of the transfer
     *
     * transferLinkId:
     *     shared internal transfer relationship ID
     */
    suspend fun linkTransferTransactions(

        transferOutTransactionId:
            Long,

        transferInTransactionId:
            Long,

        transferLinkId:
            String
    )

    //--------------------------------------------------
    // Financial Event unlink
    //--------------------------------------------------

    suspend fun unlinkTransaction(
        transactionId:
            Long
    )

    //--------------------------------------------------
    // Transfer unlink
    //--------------------------------------------------

    suspend fun unlinkTransfer(
        transactionId:
            Long
    )

    //--------------------------------------------------
    // Get paired transfer
    //--------------------------------------------------

    suspend fun getLinkedTransfer(
        transferLinkId:
            String,

        currentTransactionId:
            Long
    ):
        Transaction?
}
