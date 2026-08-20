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
        transactions: List<Transaction>
    )

    suspend fun insertTransaction(
        transaction: Transaction
    )

    suspend fun updateTransaction(
        transaction: Transaction
    )

    suspend fun deleteTransaction(
        transaction: Transaction
    )

    suspend fun getTransactionById(
        id: Long
    ): Transaction?

    suspend fun findExistingFingerprints(
        fingerprints: List<String>
    ): Set<String>

    //--------------------------------------------------
    // Financial Event linking
    //--------------------------------------------------

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
     * Links one outgoing transaction with one incoming
     * transaction as an internal account transfer.
     *
     * The outgoing transaction becomes TRANSFER_OUT.
     *
     * The incoming transaction becomes TRANSFER_IN.
     */
    suspend fun linkTransferTransactions(

        transferOutTransactionId:
            Long,

        transferInTransactionId:
            Long,

        transactionLinkId:
            String
    )

    //--------------------------------------------------
    // Remove relationship
    //--------------------------------------------------

    suspend fun unlinkTransaction(

        transactionId:
            Long
    )
}
