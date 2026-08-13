package com.varsel.expensetracker.learning

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "category_learning")
data class CategoryLearningEntity(

    @PrimaryKey
    val normalizedDescription: String,

    val categoryName: String,

    val hitCount: Int = 1,

    val createdAt: Long = System.currentTimeMillis(),

    val lastUsedAt: Long = System.currentTimeMillis()
)
