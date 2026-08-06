
package com.varsel.expensetracker.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.yourdomain.expensetracker.data.local.dao.CategoryDao
import com.yourdomain.expensetracker.data.local.dao.TransactionDao
import com.yourdomain.expensetracker.data.local.entity.CategoryEntity
import com.yourdomain.expensetracker.data.local.entity.TransactionEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Provider

/**
 * Encrypted Room Database class for the Expense Tracker.
 * Configures database entities, versioning, DAOs, and database seeding.
 */
@Database(
    entities = [TransactionEntity::class, CategoryEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun transactionDao(): TransactionDao
    abstract fun categoryDao(): CategoryDao

    /**
     * Room Database Callback used to seed default category data
     * when the encrypted database file is created for the very first time.
     */
    class SeedCallback(
        private val categoryDaoProvider: Provider<CategoryDao>
    ) : RoomDatabase.Callback() {

        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)

            // Database seeding executes asynchronously off the UI thread
            CoroutineScope(Dispatchers.IO).launch {
                seedDefaultCategories(categoryDaoProvider.get())
            }
        }

        private suspend fun seedDefaultCategories(categoryDao: CategoryDao) {
            val defaultCategories = listOf(
                CategoryEntity(name = "Salary", iconName = "ic_salary", colorHex = "#4CAF50"),
                CategoryEntity(name = "Groceries", iconName = "ic_cart", colorHex = "#FF9800"),
                CategoryEntity(name = "Utilities", iconName = "ic_lightning", colorHex = "#2196F3"),
                CategoryEntity(name = "Dining & Food", iconName = "ic_restaurant", colorHex = "#E91E63"),
                CategoryEntity(name = "Fuel & Transport", iconName = "ic_car", colorHex = "#9C27B0"),
                CategoryEntity(name = "Healthcare", iconName = "ic_hospital", colorHex = "#F44336"),
                CategoryEntity(name = "Shopping", iconName = "ic_bag", colorHex = "#00BCD4"),
                CategoryEntity(name = "Uncategorized", iconName = "ic_help", colorHex = "#9E9E9E")
            )
            categoryDao.insertCategories(defaultCategories)
        }

    }
}
