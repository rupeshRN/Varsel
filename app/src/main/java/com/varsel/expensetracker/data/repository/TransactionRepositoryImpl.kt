package com.varsel.expensetracker.data.repository

import com.varsel.expensetracker.data.local.dao.TransactionDao
import com.varsel.expensetracker.data.local.entity.TransactionEntity
import com.varsel.expensetracker.domain.model.Transaction
import com.varsel.expensetracker.domain.model.TransactionType
import com.varsel.expensetracker.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class TransactionRepositoryImpl @Inject constructor(
    private val transactionDao: TransactionDao
) : TransactionRepository {

    override fun getAllTransactions(): Flow<List<Transaction>> {
        return transactionDao.getAllTransactions().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun insertTransactions(transactions: List<Transaction>) {
        transactionDao.insertTransactions(transactions.map { it.toEntity() })
    }

    override suspend fun insertTransaction(transaction: Transaction) {
        transactionDao.insertTransaction(transaction.toEntity())
    }

    override suspend fun updateTransaction(transaction: Transaction) {
        transactionDao.updateTransaction(transaction.toEntity())
    }

    override suspend fun deleteTransaction(transaction: Transaction) {
        transactionDao.deleteTransaction(transaction.toEntity())
    }

    override suspend fun getTransactionById(id: Long): Transaction? {
        return transactionDao.getTransactionById(id)?.toDomain()
    }

    override suspend fun findExistingFingerprints(
    fingerprints: List<String>
): Set<String> {

    if (fingerprints.isEmpty()) {
        return emptySet()
    }

    return transactionDao
        .findExistingFingerprints(fingerprints)
        .toSet()
}
}

fun TransactionEntity.toDomain() = Transaction(
    id = id,
    amount = amount,
    type = if (type == "INCOME") {
        TransactionType.INCOME
    } else {
        TransactionType.EXPENSE
    },
    description = description,
    category = category,
    dateTimestamp = dateTimestamp,
    referenceNumber = referenceNumber,
    transactionFingerprint = transactionFingerprint
)

fun Transaction.toEntity() = TransactionEntity(
    id = id,
    amount = amount,
    type = if (type == TransactionType.INCOME) {
        "INCOME"
    } else {
        "EXPENSE"
    },
    description = description,
    category = category,
    dateTimestamp = dateTimestamp,
    referenceNumber = referenceNumber,
    transactionFingerprint = transactionFingerprint
)
