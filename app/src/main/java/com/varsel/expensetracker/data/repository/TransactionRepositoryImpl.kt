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

/**
 * Concrete implementation of the TransactionRepository interface.
 * 
 * Acts as the Single Source of Truth for financial transaction data in the application.
 * Responsibilities include:
 *  - Mapping Room Entities (TransactionEntity) to pure Domain Models (Transaction).
 *  - Interfacing with TransactionDao, CategoryDao, and CustomRuleDao.
 *  - Executing offline automated transaction categorization via SmartCategorizerEngine.
 */
class TransactionRepositoryImpl @Inject constructor(
    private val transactionDao: TransactionDao,
    private val categoryDao: CategoryDao,
    private val customRuleDao: CustomRuleDao
) : TransactionRepository {

    /**
     * Observes all recorded transactions and converts the Room entity list stream
     * into a list stream of domain models for UI consumption.
     */
    override fun getAllTransactions(): Flow<List<Transaction>> {
        return transactionDao.getAllTransactions().map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    /**
     * Filters transactions by type ("INCOME" or "EXPENSE") and transforms them to domain models.
     */
    override fun getTransactionsByType(type: TransactionType): Flow<List<Transaction>> {
        return transactionDao.getTransactionsByType(type.name).map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    /**
     * Fetches transaction entries occurring within a given epoch millisecond timestamp range.
     */
    override fun getTransactionsBetweenDates(startDate: Long, endDate: Long): Flow<List<Transaction>> {
        return transactionDao.getTransactionsBetweenDates(startDate, endDate).map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    /**
     * Calculates total sum for a specific transaction type within a date boundary.
     * Returns 0.0 if the SQL SUM result is null.
     */
    override fun getTotalAmountByTypeAndDateRange(
        type: TransactionType,
        startDate: Long,
        endDate: Long
    ): Flow<Double> {
        return transactionDao.getTotalAmountByTypeAndDateRange(type.name, startDate, endDate)
            .map { totalSum -> totalSum ?: 0.0 }
    }

    /**
     * Persists a domain transaction entry into Room by converting it to an entity.
     */
    override fun insertTransaction(transaction: Transaction): Long {
        return transactionDao.insertTransaction(transaction.toEntity())
    }

    /**
     * Batch inserts a collection of domain transactions into Room storage.
     */
    override suspend fun insertTransactions(transactions: List<Transaction>) {
        transactionDao.insertTransactions(transactions.map { it.toEntity() })
    }

    /**
     * Updates an existing transaction entity in Room DB.
     */
    override suspend fun updateTransaction(transaction: Transaction) {
        transactionDao.updateTransaction(transaction.toEntity())
    }

    /**
     * Deletes a transaction entity from Room DB.
     */
    override suspend fun deleteTransaction(transaction: Transaction) {
        transactionDao.deleteTransaction(transaction.toEntity())
    }

    /**
     * Executes the 3-Tier offline categorization flow before saving the record:
     * 1. Fetches current snapshot of DB categories, custom rules, and past history.
     * 2. Evaluates the description string against SmartCategorizerEngine.
     * 3. Constructs, inserts, and returns the categorized domain Transaction.
     */
    override suspend fun autoCategorizeAndSave(
        rawDescription: String,
        amount: Double,
        type: TransactionType,
        timestamp: Long,
        bankName: String?,
        refNo: String?
    ): Transaction {
        // Retrieve offline snapshot data required by the SmartCategorizerEngine
        val categories = categoryDao.getAllCategoriesSnapshot()
        val customRules = customRuleDao.getAllRules()
        val historicalTransactions = transactionDao.getCategorizedTransactionsSnapshot()

        // Predict appropriate budget category
        val assignedCategory = SmartCategorizerEngine.categorizeTransaction(
            rawDescription = rawDescription,
            categories = categories,
            customRules = customRules,
            historicalTransactions = historicalTransactions
        )

        // Construct entity for SQLite persistence
        val entity = TransactionEntity(
            amount = amount,
            type = type.name,
            description = rawDescription,
            dateTimestamp = timestamp,
            categoryName = assignedCategory,
            bankName = bankName,
            referenceNumber = refNo
        )

        val insertedId = transactionDao.insertTransaction(entity)
        return entity.copy(id = insertedId).toDomainModel()
    }

    // =================================================================================
    // Entity <-> Domain Model Transformation Functions
    // =================================================================================

    /** Transforms a Room database Entity into a clean Domain Model. */
    private fun TransactionEntity.toDomainModel(): Transaction {
        return Transaction(
            id = id,
            amount = amount,
            type = TransactionType.valueOf(type),
            description = description,
            dateTimestamp = dateTimestamp,
            categoryName = categoryName,
            bankName = bankName,
            referenceNumber = referenceNumber,
            rawOcrText = rawOcrText
        )
    }

    /** Transforms a Domain Model into a Room database Entity. */
    private fun Transaction.toEntity(): TransactionEntity {
        return TransactionEntity(
            id = id,
            amount = amount,
            type = type.name,
            description = description,
            dateTimestamp = dateTimestamp,
            categoryName = categoryName,
            bankName = bankName,
            referenceNumber = referenceNumber,
            rawOcrText = rawOcrText
 
        )
    }
}
