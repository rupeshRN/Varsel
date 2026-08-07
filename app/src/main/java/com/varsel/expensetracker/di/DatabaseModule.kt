package com.varsel.expensetracker.di

import android.content.Context
import androidx.room.Room
import com.varsel.expensetracker.data.local.AppDatabase
import com.varsel.expensetracker.data.local.dao.CategoryDao
import com.varsel.expensetracker.data.local.dao.CustomRuleDao
import com.varsel.expensetracker.data.local.dao.TransactionDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import net.sqlcipher.database.SQLiteDatabase
import net.sqlcipher.database.SupportFactory
import java.security.SecureRandom
import javax.inject.Provider
import javax.inject.Singleton

/**
 * Hilt Dependency Injection module for database components.
 * Configures SQLCipher AES-256 database encryption, builds the Singleton Room database instance,
 * and provides DAO instances across the application dependency graph.
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    private const val PREFS_NAME = "encrypted_db_secure_prefs"
    private const val PASSPHRASE_KEY = "db_passphrase_key"

    /**
     * Retrieves or generates a secure 256-bit passphrase used to encrypt the SQLite database.
     * Uses Java's SecureRandom to guarantee cryptographically strong key generation on first launch.
     *
     * @param context Application context for accessing SharedPreferences.
     * @return ByteArray representing the raw database passphrase.
     */
    @Provides
    @Singleton
    fun provideDatabasePassphrase(@ApplicationContext context: Context): ByteArray {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        var keyString = prefs.getString(PASSPHRASE_KEY, null)

        if (keyString == null) {
            // Generate 32 cryptographically secure random bytes (256 bits)
            val randomBytes = ByteArray(32)
            SecureRandom().nextBytes(randomBytes)
            keyString = randomBytes.joinToString("") { "%02x".format(it) }
            
            // Persist key securely in device-protected preferences
            prefs.edit().putString(PASSPHRASE_KEY, keyString).apply()
        }

        return SQLiteDatabase.getBytes(keyString.toCharArray())
    }

    /**
     * Builds and provides the encrypted Room database instance.
     * Integrates SQLCipher SupportFactory for transparent AES-256 encryption and attaches
     * the SeedCallback to pre-populate default budget categories on first initialization.
     *
     * @param context Application context.
     * @param passphrase Generated encryption key for SQLCipher.
     * @param categoryDaoProvider Deferred Provider for CategoryDao to prevent circular dependency during DB seeding.
     */
    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext context: Context,
        passphrase: ByteArray,
        categoryDaoProvider: Provider<CategoryDao>
    ): AppDatabase {
        // Initialize SQLCipher factory for Room encryption
        val factory = SupportFactory(passphrase)

        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "encrypted_expense_tracker.db"
        )
        .openHelperFactory(factory) // Enforces SQLCipher AES-256 encryption on file storage
        .addCallback(AppDatabase.SeedCallback(categoryDaoProvider)) // Auto-seeds initial categories
        .fallbackToDestructiveMigration()
        .build()
    }

    /** Provides singleton instance of TransactionDao for database queries. */
    @Provides
    fun provideTransactionDao(db: AppDatabase): TransactionDao = db.transactionDao()

    /** Provides singleton instance of CategoryDao for category management. */
    @Provides
    fun provideCategoryDao(db: AppDatabase): CategoryDao = db.categoryDao()

    /** Provides singleton instance of CustomRuleDao for user rule persistent learning. */
    @Provides
    fun provideCustomRuleDao(db: AppDatabase): CustomRuleDao = db.customRuleDao()
}
