package com.varsel.expensetracker.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Stores custom rules learned when a user manually reclassifies a transaction.
 */
@Entity(
    tableName = "custom_rules",
    indices = [Index(value = ["pattern"], unique = true)]
)
data class CustomRuleEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "pattern")
    val pattern: String, // e.g., "JOES CORNER SHOP"

    @ColumnInfo(name = "categoryName")
    val categoryName: String // e.g., "Groceries
  "
)
