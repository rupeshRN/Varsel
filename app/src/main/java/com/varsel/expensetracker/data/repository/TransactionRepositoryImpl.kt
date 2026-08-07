package com.varsel.expensetracker.data.repository

import com.varsel.expensetracker.data.local.dao.CategoryDao
import com.varsel.expensetracker.data.local.dao.CustomRuleDao
import com.varsel.expensetracker.data.local.dao.TransactionDao
import com.varsel.expensetracker.data.local.entity.TransactionEntity
import com.varsel.expensetracker.domain.model.Transaction
import com.varsel.expensetracker.domain.repository.TransactionRepository
import com.varsel.expensetracker.util.SmartCategorizerEngine
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
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

    override suspend fun insertTransactions(transactions: List<Transaction>) {
        val entities = transactions.map { it.toEntity() }
        transactionDao.insertTransactions(entities)
    }

    /**
     * Auto-categorizes an incoming raw transaction using the categorizer engine.
     */
    suspend fun autoCategorizeAndInsert(transaction: Transaction) {
        // Fetch real-time categories and rules to pass to categorizerEngine
        val categories = categoryDao.getAllCategories().first()
        val customRules = customRuleDao.getAllRules().first()

        // INLINE FIX: Updated call to 'categorizeTransaction' with named arguments matching new signature
        val assignedCategoryName = categorizerEngine.categorizeTransaction(
            rawDescription = transaction.description,
            categories = categories,
            customRules = customRules,
            historicalTransactions = emptyList()
        )

        // INLINE FIX: Construct updated Transaction model using domain properties ('category', 'dateTimestamp')
        val processedTransaction = transaction.copy(
            category = assignedCategoryName ?: transaction.category.ifBlank { "Uncategorized" }
        )

        transactionDao.insertTransaction(processedTransaction.toEntity())
    }
}

// Mapper extension functions
private fun TransactionEntity.toDomainModel(): Transaction {
    return Transaction(
        id = id,
        amount = amount,
        type = type,
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
        type = type,
        description = description,
        category = category,
        dateTimestamp = dateTimestamp,
        referenceNumber = referenceNumber
    )
}
