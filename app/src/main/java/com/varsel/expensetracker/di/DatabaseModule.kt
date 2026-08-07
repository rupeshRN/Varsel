package com.varsel.expensetracker.di // Defines the package path where this dependency injection module resides

import android.content.Context // Imports Android Context class for application-level operations
import androidx.room.Room // Imports Room database builder utility
import com.varsel.expensetracker.data.local.AppDatabase // Imports the local Room database class
import com.varsel.expensetracker.data.local.dao.CategoryDao // Imports the Category Data Access Object
import com.varsel.expensetracker.data.local.dao.CustomRuleDao // Imports the Custom Rule Data Access Object
import com.varsel.expensetracker.data.local.dao.TransactionDao // Imports the Transaction Data Access Object
import dagger.Module // Marks this class as a Dagger dependency injection module
import dagger.Provides // Indicates that methods inside this module provide dependency instances
import dagger.InstallIn // Specifies the component hierarchy where this module is installed
import dagger.hilt.android.qualifiers.ApplicationContext // Qualifier to inject the application-level context
import dagger.hilt.components.SingletonComponent // Scopes module dependencies to the application's lifecycle (Singleton)
import net.zetetic.database.sqlcipher.SQLiteDatabase // Imports modern SQLCipher SQLiteDatabase for encryption support
import net.zetetic.database.sqlcipher.SupportOpenHelperFactory // Imports modern SQLCipher open helper factory for Room
import java.security.SecureRandom // Imports cryptographic random number generator for secure keys
import javax.inject.Provider // Imports deferred Provider for resolving circular dependencies
import javax.inject.Singleton // Scopes provided instances as singletons

/**
 * Hilt Dependency Injection module for database components.
 * Configures SQLCipher AES-256 database encryption, builds the Singleton Room database instance,
 * and provides DAO instances across the application dependency graph.
 */
@Module // Declares this class as a Hilt module
@InstallIn(SingletonComponent::class) // Installs this module into the SingletonComponent dependency graph
object DatabaseModule { // Defines a singleton object holding database provision methods

    private const val PREFS_NAME = "encrypted_db_secure_prefs" // SharedPreferences file name for storing the encrypted database key
    private const val PASSPHRASE_KEY = "db_passphrase_key" // Key name used to look up the database passphrase string

    /**
     * Retrieves or generates a secure 256-bit passphrase used to encrypt the SQLite database.
     * Uses Java's SecureRandom to guarantee cryptographically strong key generation on first launch.
     *
     * @param context Application context for accessing SharedPreferences.
     * @return ByteArray representing the raw database passphrase.
     */
    @Provides // Informs Hilt how to construct and provide this dependency
    @Singleton // Ensures the passphrase provider instance is scoped as a singleton
    fun provideDatabasePassphrase(@ApplicationContext context: Context): ByteArray { // Function providing the cryptographic passphrase as a ByteArray
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE) // Accesses private SharedPreferences instance
        var keyString = prefs.getString(PASSPHRASE_KEY, null) // Retrieves existing passphrase string if already generated

        if (keyString == null) { // Checks if the passphrase does not yet exist on disk
            // Generate 32 cryptographically secure random bytes (256 bits)
            val randomBytes = ByteArray(32) // Allocates a byte array of length 32 for 256-bit security
            SecureRandom().nextBytes(randomBytes) // Fills the array with cryptographically secure random values
            keyString = randomBytes.joinToString("") { "%02x".format(it) } // Converts raw byte array into a hex string representation
            
            // Persist key securely in device-protected preferences
            prefs.edit().putString(PASSPHRASE_KEY, keyString).apply() // Saves the generated hex key string into SharedPreferences asynchronously
        }

        return SQLiteDatabase.getBytes(keyString.toCharArray()) // Converts the hex passphrase characters into the raw byte array required by SQLCipher
    }

    /**
     * Builds and provides the encrypted Room database instance.
     * Integrates SQLCipher SupportOpenHelperFactory for transparent AES-256 encryption and attaches
     * the SeedCallback to pre-populate default budget categories on first initialization.
     *
     * @param context Application context.
     * @param passphrase Generated encryption key for SQLCipher.
     * @param categoryDaoProvider Deferred Provider for CategoryDao to prevent circular dependency during DB seeding.
     */
    @Provides // Informs Hilt how to provide the AppDatabase instance
    @Singleton // Ensures only one database instance exists across the app lifecycle
    fun provideAppDatabase( // Function building and providing the Room database
        @ApplicationContext context: Context, // Injects application context
        passphrase: ByteArray, // Injects the generated database passphrase byte array
        categoryDaoProvider: Provider<CategoryDao> // Injects deferred Provider for CategoryDao
    ): AppDatabase { // Returns an initialized AppDatabase instance
        // Initialize SQLCipher open helper factory for Room encryption
        val factory = SupportOpenHelperFactory(passphrase) // Instantiates modern SQLCipher open helper factory with the secret passphrase

        return Room.databaseBuilder( // Starts building the Room database instance
            context, // Passes the application context
            AppDatabase::class.java, // Passes the Room database class reference
            "encrypted_expense_tracker.db" // Specifies the local SQLite database file name
        )
        .openHelperFactory(factory) // Attaches the SQLCipher open helper factory to enforce AES-256 file encryption
        .addCallback(AppDatabase.SeedCallback(categoryDaoProvider)) // Attaches callback to pre-populate default categories on first creation
        .fallbackToDestructiveMigration() // Automatically clears and recreates tables on schema version mismatches
        .build() // Finalizes and builds the Room database instance
    }

    /** Provides singleton instance of TransactionDao for database queries. */
    @Provides // Informs Hilt how to provide TransactionDao
    fun provideTransactionDao(db: AppDatabase): TransactionDao = db.transactionDao() // Extracts and returns TransactionDao from the database instance

    /** Provides singleton instance of CategoryDao for category management. */
    @Provides // Informs Hilt how to provide CategoryDao
    fun provideCategoryDao(db: AppDatabase): CategoryDao = db.categoryDao() // Extracts and returns CategoryDao from the database instance

    /** Provides singleton instance of CustomRuleDao for user rule persistent learning. */
    @Provides // Informs Hilt how to provide CustomRuleDao
    fun provideCustomRuleDao(db: AppDatabase): CustomRuleDao = db.customRuleDao() // Extracts and returns CustomRuleDao from the database instance
}
