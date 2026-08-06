package com.varsel.expensetracker.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.varsel.expensetracker.data.local.entity.CustomRuleEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object (DAO) for user-defined categorization rules.
 * Handles persistent learning when a user overrides transaction categories manually.
 */
@Dao
interface CustomRuleDao {

    /**
     * Retrieves all custom user rules observed as a reactive Flow for UI rule-management screens.
     */
    @Query("SELECT * FROM custom_rules ORDER BY id DESC")
    fun getAllRulesFlow(): Flow<List<CustomRuleEntity>>

    /**
     * One-shot snapshot fetch of all custom rules for background categorizer execution.
     */
    @Query("SELECT * FROM custom_rules")
    suspend fun getAllRules(): List<CustomRuleEntity>

    /**
     * Inserts or replaces a user rule. 
     * If a rule pattern already exists (e.g., "JOES CORNER SHOP"), it updates the assigned category.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateRule(rule: CustomRuleEntity)

    /**
     * Deletes a specific custom user rule.
     */
    @Delete
    suspend fun deleteRule(rule: CustomRuleEntity)
}
