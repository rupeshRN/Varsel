package com.varsel.expensetracker.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.varsel.expensetracker.data.local.dao.CategoryDao
import com.varsel.expensetracker.data.local.dao.CustomRuleDao
import com.varsel.expensetracker.data.local.dao.StatementSnapshotDao
import com.varsel.expensetracker.data.local.dao.TransactionDao
import com.varsel.expensetracker.data.local.entity.CategoryEntity
import com.varsel.expensetracker.data.local.entity.CustomRuleEntity
import com.varsel.expensetracker.data.local.entity.StatementSnapshotEntity
import com.varsel.expensetracker.data.local.entity.TransactionEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Provider

@Database(
    entities = [
        TransactionEntity::class,
        CategoryEntity::class,
        CustomRuleEntity::class,
        StatementSnapshotEntity::class
    ],
    version = 7,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun transactionDao(): TransactionDao

    abstract fun categoryDao(): CategoryDao

    abstract fun customRuleDao(): CustomRuleDao

    abstract fun statementSnapshotDao(): StatementSnapshotDao

    companion object {

    val MIGRATION_3_4 = object : Migration(3, 4) {

        override fun migrate(
            database: SupportSQLiteDatabase
        ) {
            database.execSQL(
                """
                ALTER TABLE transactions
                ADD COLUMN accountId TEXT
                """.trimIndent()
            )

            database.execSQL(
                """
                ALTER TABLE transactions
                ADD COLUMN accountLast4 TEXT
                """.trimIndent()
            )

            database.execSQL(
                """
                ALTER TABLE statement_snapshots
                ADD COLUMN accountId TEXT
                """.trimIndent()
            )

            database.execSQL(
                """
                ALTER TABLE statement_snapshots
                ADD COLUMN accountLast4 TEXT
                """.trimIndent()
            )
        }
    }

    val MIGRATION_4_5 = object : Migration(4, 5) {

        override fun migrate(
            database: SupportSQLiteDatabase
        ) {
            // No schema changes were introduced in this development version.
        }
    }

    val MIGRATION_5_6 = object : Migration(5, 6) {

        override fun migrate(
            database: SupportSQLiteDatabase
        ) {
            database.execSQL(
                """
                ALTER TABLE transactions
                ADD COLUMN role TEXT NOT NULL DEFAULT 'NORMAL'
                """.trimIndent()
            )
        }
    }

        /**
     * Adds support for manually linking related transactions.
     *
     * The value is nullable because existing transactions are
     * not linked automatically.
     */
    val MIGRATION_6_7 = object : Migration(6, 7) {

        override fun migrate(
            database: SupportSQLiteDatabase
        ) {
            database.execSQL(
                """
                ALTER TABLE transactions
                ADD COLUMN transactionLinkId TEXT
                """.trimIndent()
            )
        }
    }
}
    class SeedCallback(
        private val categoryDaoProvider: Provider<CategoryDao>
    ) : RoomDatabase.Callback() {

        override fun onCreate(
            db: SupportSQLiteDatabase
        ) {
            super.onCreate(db)

            CoroutineScope(Dispatchers.IO).launch {
                seedDefaultCategories(
                    categoryDaoProvider.get()
                )
            }
        }

        private suspend fun seedDefaultCategories(
            categoryDao: CategoryDao
        ) {
            val defaultCategories = listOf(

                CategoryEntity(
                    name = "Salary",
                    iconName = "ic_salary",
                    colorHex = "#4CAF50",
                    keywords =
                        "SALARY,PAYROLL,ACH CREDIT,NEFT CREDIT,STIPEND"
                ),

                CategoryEntity(
                    name = "Groceries",
                    iconName = "ic_cart",
                    colorHex = "#FF9800",
                    keywords =
                        "WALMART,DMART,SUPERMARKET,GROCERY,BIGBASKET,PRODUCE,WHOLEFOODS"
                ),

                CategoryEntity(
                    name = "Dining & Food",
                    iconName = "ic_restaurant",
                    colorHex = "#E91E63",
                    keywords =
                        "STARBUCKS,MCDONALD,SWIGGY,ZOMATO,RESTAURANT,CAFE,BAKERY,PIZZA"
                ),

                CategoryEntity(
                    name = "Fuel & Transport",
                    iconName = "ic_car",
                    colorHex = "#9C27B0",
                    keywords =
                        "SHELL,PETROL,UBER,OLA,PARKING,TOLL,METRO,CHEVRON,GASOLINE"
                ),

                CategoryEntity(
                    name = "Utilities",
                    iconName = "ic_lightning",
                    colorHex = "#2196F3",
                    keywords =
                        "ELECTRIC,WATER,AIRTEL,JIO,BROADBAND,VERIZON,ATT,GAS BILL"
                ),

                CategoryEntity(
                    name = "Healthcare",
                    iconName = "ic_hospital",
                    colorHex = "#F44336",
                    keywords =
                        "PHARMACY,HOSPITAL,CLINIC,CVS,WALGREENS,MEDICARE,APOLLO"
                ),

                CategoryEntity(
                    name = "Shopping",
                    iconName = "ic_bag",
                    colorHex = "#00BCD4",
                    keywords =
                        "AMAZON,FLIPKART,TARGET,ZARA,CLOTHING,FOOTWEAR,MALL"
                ),

                CategoryEntity(
                    name = "Uncategorized",
                    iconName = "ic_help",
                    colorHex = "#9E9E9E",
                    keywords = ""
                )
            )

            categoryDao.insertCategories(
                defaultCategories
            )
        }
    }
}
