package com.varsel.expensetracker.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Database entity for budget categories with dynamic keyword storage.
 */
@Entity(
    tableName = "categories",
    indices = [Index(value = ["name"], unique = true)]
)
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "name")
    val name: String,

    @ColumnInfo(name = "iconName")
    val iconName: String = "ic_category_default",

    @ColumnInfo(name = "colorHex")
    val colorHex: String = "#6200EE",

    @ColumnInfo(name = "keywords")
    val keywords: String = "" // Comma-separated dynamic keywords (e.g., "STARBUCKS,MCDONALD,CAFE")
)
