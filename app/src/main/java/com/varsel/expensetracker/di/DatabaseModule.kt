
package com.varsel.expensetracker.di

import android.content.Context
import androidx.room.Room
import com.yourdomain.expensetracker.data.local.AppDatabase
import com.yourdomain.expensetracker.data.local.dao.CategoryDao
import com.yourdomain.expensetracker.data.local.dao.TransactionDao
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
 * Hilt module providing database instances, DAOs, and SQLCipher 256-bit AES encryption keys.
 * Installed in SingletonComponent to persist across the entire application lifecycle.
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    private const val PREFS_NAME = "encrypted_db_secure_prefs"
    private const val PASSPHRASE_KEY = "db_passphrase_key"

    /**
     * Generates or retrieves a cryptographically secure 256-bit passphrase.
     * The passphrase is stored locally and converted into bytes using SQLCipher's SQLiteDatabase.
     */
    @Provides
    @Singleton
    fun provideDatabasePassphrase(@ApplicationContext context: Context): ByteArray {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        var keyString = prefs.getString(PASSPHRASE_KEY, null)

        if (keyString == null) {
            // Generate 32 bytes (256 bits) of random entropy on first boot
            val randomBytes = ByteArray(32)
            SecureRandom().nextBytes(randomBytes)
            keyString = randomBytes.joinToString("") { "%02x".format(it) }
            
            prefs.edit().putString(PASSPHRASE_KEY, keyString).apply()
        }

        return SQLiteDatabase.getBytes(keyString.toCharArray())
    }

    /**
     * Constructs the Room Database instance configured with SQLCipher's SupportFactory.
     */
    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext context: Context,
        passphrase: ByteArray,
        categoryDaoProvider: Provider<CategoryDao>
    ): AppDatabase {
        // Create SQLCipher Factory with the 256-bit encryption key
        val factory = SupportFactory(passphrase)

        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "encrypted_expense_tracker.db"
        )
        .openHelperFactory(factory) // Replaces default SQLite open helper with SQLCipher
        .addCallback(AppDatabase.SeedCallback(categoryDaoProvider)) // Category auto-seeding
        .fallbackToDestructiveMigration()
        .build()
    }

    /**
     * Provides a singleton instance of TransactionDao.
     */
    @Provides
    @Singleton
    fun provideTransactionDao(db: AppDatabase): TransactionDao = db.transactionDao()

    /**
     * Provides a singleton instance of CategoryDao.
     */
    @Provides
    @Singleton
    fun provideCategoryDao(db: AppDatabase): CategoryDao = db.categ
  oryDao()
}
