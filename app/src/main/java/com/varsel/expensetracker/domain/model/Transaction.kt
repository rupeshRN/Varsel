package com.varsel.expensetracker.domain.model

/**
 * Pure Kotlin domain model representing an individual income or expense record.
 * 
 * Unlike TransactionEntity, this data class is completely decoupled from Room database 
 * annotations (@Entity, @ColumnInfo, @PrimaryKey). It is consumed directly by 
 * ViewModels and Jetpack Compose UI screens to keep business logic isolated 
 * from persistent storage details.
 */
data class Transaction(
    /** Unique record ID (defaults to 0 for new, un-persisted items) */
    val id: Long = 0,

    /** Total monetary value of the transaction */
    val amount: Double,

    /** Classification type: INCOME or EXPENSE */
    val type: TransactionType,

    /** Merchant name, payor/payee details, or bank transaction note */
    val description: String,

    /** Execution time in epoch milliseconds */
    val dateTimestamp: Long,

    /** Assigned category name (defaults to "Uncategorized") */
    val categoryName: String = "Uncategorized",

    /** Optional institution name (e.g., "HDFC Bank", "Chase") */
    val bankName: String? = null,

    /** Optional reference or UTR/transaction identification number */
    val referenceNumber: String? = null,

    /** Raw OCR text snippet retained for audit tracking and verification */
    val rawOcrText: String? = null
)

/**
 * Enumeration defining valid transaction directions in the application.
 */
enum class TransactionType {
    INCOME,
    
  EXPENSE
}
