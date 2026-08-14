package com.varsel.expensetracker.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.varsel.expensetracker.data.local.dao.CategoryDao
import com.varsel.expensetracker.data.local.dao.CustomRuleDao
import com.varsel.expensetracker.data.local.dao.TransactionDao
import com.varsel.expensetracker.data.local.entity.CategoryEntity
import com.varsel.expensetracker.data.local.entity.CustomRuleEntity
import com.varsel.expensetracker.data.local.entity.TransactionEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Provider

/**
 * Main Room Database class for the Expense Tracker application.
 * Defines entity tables, DB versioning, DAO access methods, 
 * and initial auto-seeding callback logic.
 */
@Database(
    entities = [
        TransactionEntity::class,
        CategoryEntity::class,
        CustomRuleEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    /** Abstracts Data Access Object for Transactions */
    abstract fun transactionDao(): TransactionDao

    /** Abstracts Data Access Object for Categories */
    abstract fun categoryDao(): CategoryDao

    /** Abstracts Data Access Object for Custom User Rules */
    abstract fun customRuleDao(): CustomRuleDao

    /**
     * Room Database Callback invoked when the database is created for the first time.
     * Asynchronously pre-populates default budget categories equipped with dynamic matching keywords.
     */
    class SeedCallback(
        private val categoryDaoProvider: Provider<CategoryDao>
    ) : RoomDatabase.Callback() {

        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            // Execute database population safely on an IO background thread
            CoroutineScope(Dispatchers.IO).launch {
                seedDefaultCategories(categoryDaoProvider.get())
            }
        }

        /**
         * Inserts default categories with pre-populated keywords into SQLite on initial launch.
         */
        private suspend fun seedDefaultCategories(categoryDao: CategoryDao) {
            val defaultCategories = listOf(
                CategoryEntity(
                    name = "Salary",
                    iconName = "ic_salary",
                    colorHex = "#4CAF50",
                    keywords = "SALARY,PAYROLL,ACH CREDIT,NEFT CREDIT,STIPEND"
                ),
                CategoryEntity(
                    name = "Groceries",
                    iconName = "ic_cart",
                    colorHex = "#FF9800",
                    keywords = "WALMART,DMART,SUPERMARKET,GROCERY,BIGBASKET,PRODUCE,WHOLEFOODS"
                ),
                CategoryEntity(
                    name = "Dining & Food",
                    iconName = "ic_restaurant",
                    colorHex = "#E91E63",
                    keywords = "STARBUCKS,MCDONALD,SWIGGY,ZOMATO,RESTAURANT,CAFE,BAKERY,PIZZA"
                ),
                CategoryEntity(
                    name = "Fuel & Transport",
                    iconName = "ic_car",
                    colorHex = "#9C27B0",
                    keywords = "SHELL,PETROL,UBER,OLA,PARKING,TOLL,METRO,CHEVRON,GASOLINE"
                ),
                CategoryEntity(
                    name = "Utilities",
                    iconName = "ic_lightning",
                    colorHex = "#2196F3",
                    keywords = "ELECTRIC,WATER,AIRTEL,JIO,BROADBAND,VERIZON,ATT,GAS BILL"
                ),
                CategoryEntity(
                    name = "Healthcare",
                    iconName = "ic_hospital",
                    colorHex = "#F44336",
                    keywords = "PHARMACY,HOSPITAL,CLINIC,CVS,WALGREENS,MEDICARE,APOLLO"
                ),
                CategoryEntity(
                    name = "Shopping",
                    iconName = "ic_bag",
                    colorHex = "#00BCD4",
                    keywords = "AMAZON,FLIPKART,TARGET,ZARA,CLOTHING,FOOTWEAR,MALL"
                ),
                CategoryEntity(
                    name = "Uncategorized",
                    iconName = "ic_help",
                    colorHex = "#9E9E9E",
                    keywords = ""
                )
            )
            categoryDao.insertCategories(defaultCategories)
        }

    }
}
