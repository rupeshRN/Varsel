package com.varsel.expensetracker.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val amount: Double,
    val type: String,
    val description: String,
    val category: String,
    val dateTimestamp: Long,
    val referenceNumber: String? = null,
    val transactionFingerprint: String? = null
)
