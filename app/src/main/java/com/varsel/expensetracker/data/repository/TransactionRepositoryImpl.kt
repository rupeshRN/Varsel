package com.varsel.expensetracker.data.repository

import com.varsel.expensetracker.data.local.dao.TransactionDao
import com.varsel.expensetracker.data.local.entity.TransactionEntity
import com.varsel.expensetracker.domain.model.Transaction
import com.varsel.expensetracker.domain.model.TransactionRole
import com.varsel.expensetracker.domain.model.TransactionType
import com.varsel.expensetracker.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class TransactionRepositoryImpl @Inject constructor(

    private val transactionDao:
        TransactionDao

) : TransactionRepository {

    //--------------------------------------------------
    // Observe all transactions
    //--------------------------------------------------

    override fun getAllTransactions():
        Flow<List<Transaction>> {

        return transactionDao
            .getAllTransactions()
            .map { entities ->

                entities.map {
                    it.toDomain()
                }
            }
    }

    //--------------------------------------------------
    // Insert multiple transactions
    //--------------------------------------------------

    override suspend fun insertTransactions(
        transactions: List<Transaction>
    ) {

        if (
            transactions.isEmpty()
        ) {
            return
        }

        transactionDao
            .insertTransactions(

                transactions.map {
                    it.toEntity()
                }
            )
    }

    //--------------------------------------------------
    // Insert single transaction
    //--------------------------------------------------

    override suspend fun insertTransaction(
        transaction: Transaction
    ) {

        transactionDao
            .insertTransaction(
                transaction.toEntity()
            )
    }

    //--------------------------------------------------
    // Update transaction
    //--------------------------------------------------

    override suspend fun updateTransaction(
        transaction: Transaction
    ) {

        transactionDao
            .updateTransaction(
                transaction.toEntity()
            )
    }

    //--------------------------------------------------
    // Delete transaction
    //--------------------------------------------------

    override suspend fun deleteTransaction(
        transaction: Transaction
    ) {

        transactionDao
            .deleteTransaction(
                transaction.toEntity()
            )
    }

    //--------------------------------------------------
    // Get transaction by ID
    //--------------------------------------------------

    override suspend fun getTransactionById(
        id: Long
    ): Transaction? {

        return transactionDao
            .getTransactionById(
                id
            )
            ?.toDomain()
    }

    //--------------------------------------------------
    // Existing fingerprints
    //--------------------------------------------------

    override suspend fun findExistingFingerprints(
        fingerprints: List<String>
    ): Set<String> {

        if (
            fingerprints.isEmpty()
        ) {
            return emptySet()
        }

        return transactionDao
            .findExistingFingerprints(
                fingerprints
            )
            .toSet()
    }

    //--------------------------------------------------
    // Link transactions to Financial Event
    //
    // IMPORTANT:
    //
    // TransactionDao now handles BOTH:
    //
    // EXPENSE -> LENT
    // INCOME  -> REIMBURSEMENT
    //
    // Therefore this repository method deliberately
    // uses only linkTransactions().
    //
    // There is NO linkReimbursements() call.
    //--------------------------------------------------

    override suspend fun linkTransactions(

        transactionIds:
            List<Long>,

        transactionLinkId:
            String

    ) {

        if (
            transactionIds.isEmpty()
        ) {
            return
        }

        transactionDao
            .linkTransactions(

                transactionIds =
                    transactionIds,

                transactionLinkId =
                    transactionLinkId
            )
    }

    //--------------------------------------------------
    // Unlink transaction from Financial Event
    //
    // DAO clears:
    //
    // transactionLinkId
    // role -> NORMAL
    //--------------------------------------------------

    override suspend fun unlinkTransaction(

        transactionId:
            Long

    ) {

        transactionDao
            .unlinkTransaction(
                transactionId
            )
    }
}

//======================================================
// Entity -> Domain
//======================================================

fun TransactionEntity.toDomain():
    Transaction {

    return Transaction(

        id =
            id,

        amount =
            amount,

        type =
            if (
                type ==
                    "INCOME"
            ) {

                TransactionType.INCOME

            } else {

                TransactionType.EXPENSE
            },

        description =
            description,

        category =
            category,

        dateTimestamp =
            dateTimestamp,

        referenceNumber =
            referenceNumber,

        transactionFingerprint =
            transactionFingerprint,

        accountId =
            accountId,

        accountLast4 =
            accountLast4,

        transactionLinkId =
            transactionLinkId,

        role =
            try {

                TransactionRole.valueOf(
                    role
                )

            } catch (
                e: IllegalArgumentException
            ) {

                TransactionRole.NORMAL
            }
    )
}

//======================================================
// Domain -> Entity
//======================================================

fun Transaction.toEntity():
    TransactionEntity {

    return TransactionEntity(

        id =
            id,

        amount =
            amount,

        type =
            if (
                type ==
                    TransactionType.INCOME
            ) {

                "INCOME"

            } else {

                "EXPENSE"
            },

        description =
            description,

        category =
            category,

        dateTimestamp =
            dateTimestamp,

        referenceNumber =
            referenceNumber,

        transactionFingerprint =
            transactionFingerprint,

        accountId =
            accountId,

        accountLast4 =
            accountLast4,

        transactionLinkId =
            transactionLinkId,

        role =
            role.name
    )
    }
