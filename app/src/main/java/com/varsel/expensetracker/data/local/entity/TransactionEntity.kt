package com.varsel.expensetracker.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Room Database Entity representing an individual income or expense entry.
 * Stores core transaction details along with statement scanning metadata.
 */
@Entity(
    tableName = "transactions",
    indices = [
        Index(value = ["dateTimestamp"]),
        Index(value = ["type"])
    ]
)
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "amount")
    val amount: Double,

    @ColumnInfo(name = "type")
    val type: String, // "INCOME" or "EXPENSE"

    @ColumnInfo(name = "description")
    val description: String,

    @ColumnInfo(name = "dateTimestamp")
    val dateTimestamp: Long, // Epoch timestamp in milliseconds

    @ColumnInfo(name = "categoryName")
    val categoryName: String = "Uncategorized",

    @ColumnInfo(name = "bankName")
    val bankName: String? = null,

    @ColumnInfo(name = "referenceNumber")
    val referenceNumber: String? = null,

    @ColumnInfo(name = "rawOcrText")
    val rawOcrText: String
  ? = null
)
