package com.varsel.expensetracker.data.repository

import com.varsel.expensetracker.data.local.dao.CustomRuleDao
import com.varsel.expensetracker.data.local.entity.CustomRuleEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CustomRuleRepository @Inject constructor(

    private val customRuleDao: CustomRuleDao

) {

    //--------------------------------------------------
    // Observe all rules
    //--------------------------------------------------

    fun getAllRules(): Flow<List<CustomRuleEntity>> {

        return customRuleDao.getAllRules()

    }

    //--------------------------------------------------
    // Lookup
    //--------------------------------------------------

    suspend fun findRule(

        pattern: String

    ): CustomRuleEntity? {

        return customRuleDao.findRuleByPattern(pattern)

    }

    //--------------------------------------------------
    // Save / Replace
    //--------------------------------------------------

    suspend fun saveRule(

        pattern: String,

        categoryName: String

    ) {

        customRuleDao.insertCustomRule(

            CustomRuleEntity(

                pattern = pattern,

                categoryName = categoryName

            )

        )

    }

    //--------------------------------------------------
    // Delete
    //--------------------------------------------------

    suspend fun deleteRule(

        rule: CustomRuleEntity

    ) {

        customRuleDao.deleteRule(rule)

    }

}
