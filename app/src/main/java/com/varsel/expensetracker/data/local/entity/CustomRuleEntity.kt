package com.varsel.expensetracker.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Stores user-learned knowledge.
 *
 * pattern               -> Raw bank statement text
 * preferredDescription  -> User's preferred display description
 * categoryName          -> User's preferred category
 */
@Entity(
    tableName = "custom_rules",
    indices = [
        Index(
            value = ["pattern"],
            unique = true
        )
    ]
)
data class CustomRuleEntity(

    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "pattern")
    val pattern: String,

    @ColumnInfo(name = "preferredDescription")
    val preferredDescription: String,

    @ColumnInfo(name = "categoryName")
    val categoryName: String

)
