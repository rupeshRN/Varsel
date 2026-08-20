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
    //
    // IMPORTANT:
    //
    // One centralized linking operation is used for
    // BOTH expenses and incomes.
    //
    // The DAO determines the role:
    //
    // EXPENSE -> LENT
    // INCOME  -> REIMBURSEMENT
    //--------------------------------------------------

    suspend fun linkTransactions(

        transactionIds:
            List<Long>,

        transactionLinkId:
            String
    )

    //--------------------------------------------------
    // Remove transaction from Financial Event
    //--------------------------------------------------

    suspend fun unlinkTransaction(

        transactionId:
            Long
    )
}
