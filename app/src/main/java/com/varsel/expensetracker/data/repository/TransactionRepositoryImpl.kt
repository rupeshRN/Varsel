package com.varsel.expensetracker.data.repository

import com.varsel.expensetracker.data.local.dao.CategoryDao
import com.varsel.expensetracker.data.local.dao.CustomRuleDao
import com.varsel.expensetracker.data.local.dao.TransactionDao
import com.varsel.expensetracker.data.local.entity.TransactionEntity
import com.varsel.expensetracker.domain.model.Transaction
import com.varsel.expensetracker.domain.model.TransactionType
import com.varsel.expensetracker.domain.repository.TransactionRepository
import com.varsel.expensetracker.util.SmartCategorizerEngine
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TransactionRepositoryImpl @Inject constructor(
    private val transactionDao: TransactionDao,
    private val categoryDao: CategoryDao,
    private val customRuleDao: CustomRuleDao,
    private val categorizerEngine: SmartCategorizerEngine
) : TransactionRepository {

    override fun getAllTransactions(): Flow<List<Transaction>> {
        return transactionDao.getAllTransactions().map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    override fun getTransactionsByType(type: TransactionType): Flow<List<Transaction>> {
        return transactionDao.getAllTransactions().map { entities ->
            entities.filter { it.type == type.name }.map { it.toDomainModel() }
        }
    }

    override suspend fun insertTransactions(transactions: List<Transaction>) {
        val entities = transactions.map { it.toEntity() }
        transactionDao.insertTransactions(entities)
    }

    override suspend fun insertTransaction(transaction: Transaction) {
        transactionDao.insertTransaction(transaction.toEntity())
    }
}

private fun TransactionEntity.toDomainModel(): Transaction {
    return Transaction(
        id = id,
        amount = amount,
        type = try { TransactionType.valueOf(type) } catch (e: Exception) { TransactionType.DEBIT },
        description = description,
        category = category,
        dateTimestamp = dateTimestamp,
        referenceNumber = referenceNumber
    )
}

private fun Transaction.toEntity(): TransactionEntity {
    return TransactionEntity(
        id = id,
        amount = amount,
        type = type.name,
        description = description,
        category = category,
        dateTimestamp = dateTimestamp,
        referenceNumber = referenceNumber
    )
}
