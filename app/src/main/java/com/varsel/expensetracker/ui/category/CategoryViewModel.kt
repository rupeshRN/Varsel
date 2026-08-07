package com.varsel.expensetracker.ui.category

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.varsel.expensetracker.data.local.dao.CategoryDao
import com.varsel.expensetracker.data.local.dao.CustomRuleDao
import com.varsel.expensetracker.data.local.entity.CategoryEntity
import com.varsel.expensetracker.data.local.entity.CustomRuleEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Immutable UI State for the Category Management and Custom Rules screen.
 */
data class CategoryUiState(
    val categories: List<CategoryEntity> = emptyList(),
    val customRules: List<CustomRuleEntity> = emptyList(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null
)

/**
 * ViewModel responsible for managing budget categories, monthly budget thresholds,
 * and user-defined merchant auto-categorization override rules.
 */
@HiltViewModel
class CategoryViewModel @Inject constructor(
    private val categoryDao: CategoryDao,
    private val customRuleDao: CustomRuleDao
) : ViewModel() {

    /**
     * Reactive StateFlow stream uniting categories and merchant rules directly from 
     * encrypted SQLCipher local storage to the Jetpack Compose UI.
     */
    val uiState: StateFlow<CategoryUiState> = combine(
        categoryDao.getAllCategories(),
        customRuleDao.getAllRules()
    ) { categories, rules ->
        CategoryUiState(
            categories = categories,
            customRules = rules,
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = CategoryUiState(isLoading = true)
    )

    /**
     * Inserts a new budget category (e.g., Food, Groceries, Rent) into Room database.
     */
    fun addCategory(
        name: String,
        colorHex: String,
        iconName: String,
        monthlyBudgetLimit: Double = 0.0
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val newCategory = CategoryEntity(
                name = name.trim(),
                colorHex = colorHex,
                iconName = iconName,
                monthlyBudgetLimit = monthlyBudgetLimit
            )
            categoryDao.insertCategory(newCategory)
        }
    }

    /**
     * Updates an existing category (e.g., modifying color, icon, or monthly budget cap).
     */
    fun updateCategory(category: CategoryEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            categoryDao.updateCategory(category)
        }
    }

    /**
     * Deletes a category from local encrypted storage.
     */
    fun deleteCategory(category: CategoryEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            categoryDao.deleteCategory(category)
        }
    }

    /**
     * Teaches the categorization engine a custom rule by mapping a merchant keyword pattern 
     * to a specific category ID (e.g., keyword "STARBUCKS" -> Category: Coffee & Snacks).
     */
    fun addCustomRule(merchantPattern: String, categoryId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            val rule = CustomRuleEntity(
                merchantPattern = merchantPattern.trim().uppercase(),
                categoryId = categoryId
            )
            customRuleDao.insertRule(rule)
        }
    }

    /**
     * Removes a merchant auto-categorization override rule.
     */
    fun deleteCustomRule(rule: CustomRuleEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            customRuleDao.deleteRule(rule)
        }
    }
}
