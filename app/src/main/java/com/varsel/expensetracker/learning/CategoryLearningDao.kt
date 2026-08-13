package com.varsel.expensetracker.learning

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface CategoryLearningDao {

    //--------------------------------------------------
    // Find a learned category by normalized description
    //--------------------------------------------------

    @Query(
        """
        SELECT * FROM category_learning
        WHERE normalizedDescription = :description
        LIMIT 1
        """
    )
    suspend fun findByDescription(
        description: String
    ): CategoryLearningEntity?

    //--------------------------------------------------
    // Insert or replace a learned mapping
    //--------------------------------------------------

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(
        entity: CategoryLearningEntity
    )

    //--------------------------------------------------
    // Update an existing mapping
    //--------------------------------------------------

    @Update
    suspend fun update(
        entity: CategoryLearningEntity
    )

    //--------------------------------------------------
    // Delete a mapping
    //--------------------------------------------------

    @Query(
        """
        DELETE FROM category_learning
        WHERE normalizedDescription = :description
        """
    )
    suspend fun delete(
        description: String
    )

    //--------------------------------------------------
    // Retrieve all learned mappings
    // (useful for future backup/debug UI)
    //--------------------------------------------------

    @Query(
        """
        SELECT *
        FROM category_learning
        ORDER BY lastUsedAt DESC
        """
    )
    suspend fun getAll(): List<CategoryLearningEntity>
}
