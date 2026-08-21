package com.varsel.expensetracker.data.repository

import com.varsel.expensetracker.data.local.dao.TransactionDao
import com.varsel.expensetracker.data.local.entity.TransactionEntity
import com.varsel.expensetracker.domain.model.Transaction
import com.varsel.expensetracker.domain.model.TransactionRole
import com.varsel.expensetracker.domain.model.TransactionType
import com.varsel.expensetracker.domain.repository.TransactionRepository
import com.varsel.expensetracker.domain.repository.TransferLinkResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID
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

        if (transactions.isEmpty()) {
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
            .getTransactionById(id)
            ?.toDomain()
    }

    //--------------------------------------------------
    // Existing fingerprints
    //--------------------------------------------------

    override suspend fun findExistingFingerprints(
        fingerprints: List<String>
    ): Set<String> {

        if (fingerprints.isEmpty()) {
            return emptySet()
        }

        return transactionDao
            .findExistingFingerprints(
                fingerprints
            )
            .toSet()
    }

    //--------------------------------------------------
    // Financial Event linking
    //--------------------------------------------------

    override suspend fun linkTransactions(

        transactionIds:
            List<Long>,

        transactionLinkId:
            String

    ) {

        if (transactionIds.isEmpty()) {
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
    // Financial Event unlink
    //--------------------------------------------------

    override suspend fun unlinkTransaction(
        transactionId: Long
    ) {

        transactionDao
            .unlinkTransaction(
                transactionId
            )
    }

    //--------------------------------------------------
    // Transfer linking
    //--------------------------------------------------
    //
    // IMPORTANT:
    //
    // A transfer is valid only when:
    //
    //     TRANSFER_OUT
    //          +
    //     TRANSFER_IN
    //
    // and both amounts are exactly equal.
    //
    // Validation happens here BEFORE the DAO is
    // called, so an invalid transfer can never be
    // persisted by this repository method.
    //--------------------------------------------------

    override suspend fun linkTransfer(

        transferOutTransactionId:
            Long,

        transferInTransactionId:
            Long

    ): TransferLinkResult {

        //--------------------------------------------------
        // Same transaction cannot be both sides.
        //--------------------------------------------------

        if (
            transferOutTransactionId ==
                transferInTransactionId
        ) {

            return TransferLinkResult.InvalidTransactionPair
        }

        //--------------------------------------------------
        // Load both transactions.
        //--------------------------------------------------

        val transferOut =
            transactionDao
                .getTransactionById(
                    transferOutTransactionId
                )
                ?.toDomain()

        val transferIn =
            transactionDao
                .getTransactionById(
                    transferInTransactionId
                )
                ?.toDomain()

        //--------------------------------------------------
        // Transaction existence validation.
        //--------------------------------------------------

        if (
            transferOut == null ||
            transferIn == null
        ) {

            return TransferLinkResult.TransactionNotFound
        }

        //--------------------------------------------------
        // Validate transaction types / roles.
        //
        // We intentionally validate the ROLE here,
        // because the user must explicitly classify
        // the transactions as Transfer Out / Transfer In
        // before linking them.
        //--------------------------------------------------

        if (
            transferOut.role !=
                TransactionRole.TRANSFER_OUT ||

            transferIn.role !=
                TransactionRole.TRANSFER_IN
        ) {

            return TransferLinkResult.InvalidTransactionPair
        }

        //--------------------------------------------------
        // Both transactions must be income/expense
        // according to their original bank movement.
        //
        // TRANSFER_OUT is normally an expense-side
        // transaction.
        //
        // TRANSFER_IN is normally an income-side
        // transaction.
        //--------------------------------------------------

        if (
            transferOut.type !=
                TransactionType.EXPENSE ||

            transferIn.type !=
                TransactionType.INCOME
        ) {

            return TransferLinkResult.InvalidTransactionPair
        }

        //--------------------------------------------------
        // Exact amount validation.
        //
        // No tolerance is intentionally used.
        //
        // Example:
        //
        // 1000.00 == 1000.00 -> valid
        // 1000.00 != 999.99  -> invalid
        //
        // The user explicitly requested exact matching.
        //--------------------------------------------------

        if (
            transferOut.amount !=
                transferIn.amount
        ) {

            return TransferLinkResult.AmountMismatch(

                transferOutAmount =
                    transferOut.amount,

                transferInAmount =
                    transferIn.amount
            )
        }

        //--------------------------------------------------
        // Both transactions must not already belong
        // to another transfer.
        //--------------------------------------------------

        if (
            transferOut.transferLinkId !=
                null ||

            transferIn.transferLinkId !=
                null
        ) {

            return TransferLinkResult.AlreadyLinked
        }

        //--------------------------------------------------
        // Create one shared transfer ID.
        //--------------------------------------------------

        val transferLinkId =
            UUID.randomUUID()
                .toString()

        //--------------------------------------------------
        // Persist only after ALL validation succeeds.
        //--------------------------------------------------

        transactionDao
            .linkTransferTransactions(

                transferOutTransactionId =
                    transferOutTransactionId,

                transferInTransactionId =
                    transferInTransactionId,

                transferLinkId =
                    transferLinkId
            )

        return TransferLinkResult.Success
    }

    //--------------------------------------------------
    // Transfer unlink
    //--------------------------------------------------

    override suspend fun unlinkTransfer(
        transactionId: Long
    ) {

        transactionDao
            .unlinkTransfer(
                transactionId
            )
    }

    //--------------------------------------------------
    // Get paired transfer
    //--------------------------------------------------

    override suspend fun getLinkedTransferTransactions(
        transferLinkId: String
    ): List<Transaction> {

        /*
         * The current DAO exposes a method for retrieving
         * the other side of a transfer rather than all
         * transactions by transferLinkId.
         *
         * Therefore this method is intentionally not
         * implemented through a new DAO query in this
         * step.
         *
         * The transfer UI currently works with the
         * current transaction + getLinkedTransfer().
         */
        return emptyList()
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
                type == "INCOME"
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

        transferLinkId =
            transferLinkId,

        role =
            try {

                TransactionRole.valueOf(
                    role
                )

            } catch (
                e:
                    IllegalArgumentException
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

        transferLinkId =
            transferLinkId,

        role =
            role.name
    )
}
