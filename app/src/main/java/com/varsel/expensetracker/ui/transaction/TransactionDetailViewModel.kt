package com.varsel.expensetracker.ui.transaction

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.varsel.expensetracker.data.local.dao.CategoryDao
import com.varsel.expensetracker.data.repository.CustomRuleRepository
import com.varsel.expensetracker.domain.model.Transaction
import com.varsel.expensetracker.domain.model.TransactionLinkGroup
import com.varsel.expensetracker.domain.model.TransactionRole
import com.varsel.expensetracker.domain.model.TransactionType
import com.varsel.expensetracker.domain.repository.TransactionLinkGroupRepository
import com.varsel.expensetracker.domain.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class TransactionDetailViewModel @Inject constructor(

    private val transactionRepository:
        TransactionRepository,

    private val customRuleRepository:
        CustomRuleRepository,

    private val transactionLinkGroupRepository:
        TransactionLinkGroupRepository,

    private val categoryDao:
        CategoryDao

) : ViewModel() {

    private val _uiState =
        MutableStateFlow<TransactionDetailUiState>(
            TransactionDetailUiState.Loading
        )

    val uiState:
        StateFlow<TransactionDetailUiState> =
        _uiState.asStateFlow()

    private val _saveCompleted =
        MutableStateFlow(false)

    val saveCompleted:
        StateFlow<Boolean> =
        _saveCompleted.asStateFlow()

    //--------------------------------------------------
    // Transaction observation job
    //--------------------------------------------------

    private var transactionObservationJob:
        Job? = null

    //--------------------------------------------------
    // Load transaction
    //--------------------------------------------------

    fun loadTransaction(
        transactionId: Long
    ) {

        transactionObservationJob?.cancel()

        viewModelScope.launch {

            val transaction =
                transactionRepository
                    .getTransactionById(
                        transactionId
                    )

            if (
                transaction == null
            ) {

                _uiState.value =
                    TransactionDetailUiState.Error(
                        "Transaction not found."
                    )

                return@launch
            }

            //--------------------------------------------------
            // Load existing application categories.
            //
            // These are used by the Financial Event /
            // Report Group creation dialog.
            //--------------------------------------------------

            val categories =
                loadCategories()

            _uiState.value =
                TransactionDetailUiState.Loaded(

                    transaction =
                        transaction,

                    editableDescription =
                        transaction.description,

                    selectedCategory =
                        transaction.category,

                    selectedRole =
                        transaction.role,

                    hasChanges =
                        false,

                    isSaving =
                        false,

                    categories =
                        categories,

                    linkedTransactions =
                        emptyList(),

                    isLinking =
                        false,

                    transactionLinkGroup =
                        null,

                    showCreateGroupPrompt =
                        false,

                    isSavingGroup =
                        false
                )

            //--------------------------------------------------
            // Observe transaction changes.
            //--------------------------------------------------

            observeTransactions(
                transactionId
            )
        }
    }

    //--------------------------------------------------
    // Load categories
    //--------------------------------------------------

    private suspend fun loadCategories():
        List<String> {

        return categoryDao
            .getAllCategoriesSnapshot()
            .map {
                it.name.trim()
            }
            .filter {
                it.isNotBlank()
            }
            .distinct()
            .sorted()
    }

    //--------------------------------------------------
    // Observe transaction changes
    //--------------------------------------------------

    private fun observeTransactions(
        transactionId: Long
    ) {

        transactionObservationJob =
            viewModelScope.launch {

                transactionRepository
                    .getAllTransactions()
                    .collectLatest { allTransactions ->

                        updateFinancialEventState(

                            transactionId =
                                transactionId,

                            allTransactions =
                                allTransactions
                        )
                    }
            }
    }

    //--------------------------------------------------
    // Build Financial Event state
    //
    // IMPORTANT:
    //
    // Transaction Details no longer contains the old
    // "Possible Transactions to Link" picker.
    //
    // The Financial Event screen is responsible for:
    //
    // - adding expenses
    // - adding reimbursements
    // - removing transactions
    // - managing the event
    //
    // Transaction Details only displays transactions
    // already belonging to the Financial Event.
    //--------------------------------------------------

    private suspend fun updateFinancialEventState(

        transactionId:
            Long,

        allTransactions:
            List<Transaction>

    ) {

        val currentState =
            _uiState.value as?
                TransactionDetailUiState.Loaded
                ?: return

        //--------------------------------------------------
        // Get the latest version of the current transaction.
        //--------------------------------------------------

        val currentTransaction =
            allTransactions
                .firstOrNull {

                    it.id ==
                        transactionId
                }
                ?: currentState.transaction

        //--------------------------------------------------
        // Existing Financial Event link
        //--------------------------------------------------

        val transactionLinkId =
            currentTransaction
                .transactionLinkId

        //--------------------------------------------------
        // Existing transactions belonging to the same
        // Financial Event.
        //--------------------------------------------------

        val linkedTransactions =
            transactionLinkId
                ?.let { linkId ->

                    allTransactions
                        .filter {

                            it.transactionLinkId ==
                                linkId
                        }
                        .sortedByDescending {

                            it.dateTimestamp
                        }
                }
                .orEmpty()

        //--------------------------------------------------
        // Existing Financial Event group.
        //
        // getGroup() is suspend, therefore this entire
        // state-building function is suspend.
        //--------------------------------------------------

        val existingGroup =
            transactionLinkId
                ?.let { linkId ->

                    transactionLinkGroupRepository
                        .getGroup(
                            linkId
                        )
                }

        //--------------------------------------------------
        // Do NOT automatically open the Create Financial
        // Event dialog.
        //
        // The user explicitly chooses:
        //
        // Create Financial Event
        //
        // or
        //
        // Manage Financial Event
        //--------------------------------------------------

        _uiState.value =
            currentState.copy(

                transaction =
                    currentTransaction,

                categories =
                    currentState.categories,

                linkedTransactions =
                    linkedTransactions,

                transactionLinkGroup =
                    existingGroup,

                showCreateGroupPrompt =
                    currentState
                        .showCreateGroupPrompt,

                isLinking =
                    false
            )
    }

    //--------------------------------------------------
    // Show Create Financial Event dialog
    //--------------------------------------------------

    fun showCreateGroupPrompt() {

        val current =
            _uiState.value as?
                TransactionDetailUiState.Loaded
                ?: return

        if (
            current.isSavingGroup
        ) {
            return
        }

        //--------------------------------------------------
        // A Financial Event already exists.
        //
        // The screen should use Manage Financial Event
        // instead of Create Financial Event.
        //--------------------------------------------------

        if (
            current.transactionLinkGroup != null
        ) {
            return
        }

        _uiState.value =
            current.copy(

                showCreateGroupPrompt =
                    true
            )
    }

    //--------------------------------------------------
    // Description
    //--------------------------------------------------

    fun updateDescription(
        description: String
    ) {

        val current =
            _uiState.value as?
                TransactionDetailUiState.Loaded
                ?: return

        _uiState.value =
            current.copy(

                editableDescription =
                    description,

                hasChanges =
                    description !=
                        current.transaction
                            .description ||

                    current.selectedCategory !=
                        current.transaction
                            .category ||

                    current.selectedRole !=
                        current.transaction
                            .role
            )
    }

    //--------------------------------------------------
    // Category
    //--------------------------------------------------

    fun updateCategory(
        category: String
    ) {

        val current =
            _uiState.value as?
                TransactionDetailUiState.Loaded
                ?: return

        _uiState.value =
            current.copy(

                selectedCategory =
                    category,

                hasChanges =
                    category !=
                        current.transaction
                            .category ||

                    current.editableDescription !=
                        current.transaction
                            .description ||

                    current.selectedRole !=
                        current.transaction
                            .role
            )
    }

    //--------------------------------------------------
    // Transaction role
    //--------------------------------------------------

    fun updateRole(
        role: TransactionRole
    ) {

        val current =
            _uiState.value as?
                TransactionDetailUiState.Loaded
                ?: return

        _uiState.value =
            current.copy(

                selectedRole =
                    role,

                hasChanges =
                    role !=
                        current.transaction
                            .role ||

                    current.editableDescription !=
                        current.transaction
                            .description ||

                    current.selectedCategory !=
                        current.transaction
                            .category
            )
    }

    //--------------------------------------------------
    // Unlink current transaction
    //--------------------------------------------------

    fun unlinkCurrentTransaction() {

        val current =
            _uiState.value as?
                TransactionDetailUiState.Loaded
                ?: return

        //--------------------------------------------------
        // Nothing to unlink.
        //--------------------------------------------------

        if (
            current.transaction
                .transactionLinkId ==
            null
        ) {
            return
        }

        //--------------------------------------------------
        // Prevent duplicate unlink operations.
        //--------------------------------------------------

        if (
            current.isLinking
        ) {
            return
        }

        viewModelScope.launch {

            _uiState.value =
                current.copy(
                    isLinking = true
                )

            transactionRepository
                .unlinkTransaction(
                    current.transaction.id
                )

            //--------------------------------------------------
            // Room Flow will refresh the Financial Event
            // state.
            //--------------------------------------------------
        }
    }

    //--------------------------------------------------
    // Dismiss Create Financial Event dialog
    //--------------------------------------------------

    fun dismissCreateGroupPrompt() {

        val current =
            _uiState.value as?
                TransactionDetailUiState.Loaded
                ?: return

        _uiState.value =
            current.copy(

                showCreateGroupPrompt =
                    false
            )
    }

    //--------------------------------------------------
    // Create Financial Event
    //--------------------------------------------------

    fun createReportGroup(

        groupName:
            String,

        category:
            String

    ) {

        val current =
            _uiState.value as?
                TransactionDetailUiState.Loaded
                ?: return

        //--------------------------------------------------
        // Prevent duplicate creation.
        //--------------------------------------------------

        if (
            current.isSavingGroup
        ) {
            return
        }

        //--------------------------------------------------
        // Clean input.
        //--------------------------------------------------

        val cleanName =
            groupName.trim()

        val cleanCategory =
            category.trim()

        //--------------------------------------------------
        // Basic validation.
        //--------------------------------------------------

        if (
            cleanName.isBlank() ||
            cleanCategory.isBlank()
        ) {
            return
        }

        //--------------------------------------------------
        // Validate category against the existing
        // application category list.
        //--------------------------------------------------

        val selectedCategory =
            current.categories
                .firstOrNull {

                    it.equals(
                        cleanCategory,
                        ignoreCase = true
                    )
                }
                ?: return

        viewModelScope.launch {

            _uiState.value =
                current.copy(

                    isSavingGroup =
                        true
                )

            //--------------------------------------------------
            // Create a Financial Event link ID.
            //
            // Normally this transaction is not already
            // linked because this method is only exposed
            // through Create Financial Event.
            //--------------------------------------------------

            val transactionLinkId =
                current.transaction
                    .transactionLinkId
                    ?: UUID.randomUUID()
                        .toString()

            //--------------------------------------------------
            // Creating a Financial Event establishes the
            // transaction's Financial Event role.
            //
            // EXPENSE / DEBIT -> LENT
            // INCOME / CREDIT -> REIMBURSEMENT
            //--------------------------------------------------

            val financialEventRole =
                when (
                    current.transaction.type
                ) {

                    TransactionType.EXPENSE,
                    TransactionType.DEBIT ->
                        TransactionRole.LENT

                    TransactionType.INCOME,
                    TransactionType.CREDIT ->
                        TransactionRole.REIMBURSEMENT
                }

            //--------------------------------------------------
            // Update current transaction.
            //--------------------------------------------------

            val updatedTransaction =
                current.transaction.copy(

                    transactionLinkId =
                        transactionLinkId,

                    role =
                        financialEventRole
                )

            transactionRepository
                .updateTransaction(
                    updatedTransaction
                )

            //--------------------------------------------------
            // Create Financial Event group.
            //--------------------------------------------------

            val group =
                TransactionLinkGroup(

                    transactionLinkId =
                        transactionLinkId,

                    groupName =
                        cleanName,

                    category =
                        selectedCategory,

                    createdAt =
                        System.currentTimeMillis()
                )

            transactionLinkGroupRepository
                .saveGroup(
                    group
                )

            //--------------------------------------------------
            // Update UI immediately.
            //
            // Room Flow will also refresh the state.
            //--------------------------------------------------

            _uiState.value =
                current.copy(

                    transaction =
                        updatedTransaction,

                    linkedTransactions =
                        listOf(
                            updatedTransaction
                        ),

                    transactionLinkGroup =
                        group,

                    showCreateGroupPrompt =
                        false,

                    isSavingGroup =
                        false,

                    isLinking =
                        false
                )
        }
    }

    //--------------------------------------------------
    // Delete Financial Event
    //--------------------------------------------------

    fun deleteReportGroup() {

        val current =
            _uiState.value as?
                TransactionDetailUiState.Loaded
                ?: return

        val transactionLinkId =
            current.transaction
                .transactionLinkId
                ?: return

        viewModelScope.launch {

            transactionLinkGroupRepository
                .deleteGroup(
                    transactionLinkId
                )

            _uiState.value =
                current.copy(

                    transactionLinkGroup =
                        null
                )
        }
    }

    //--------------------------------------------------
    // Save transaction changes
    //--------------------------------------------------

    fun saveChanges() {

        val current =
            _uiState.value as?
                TransactionDetailUiState.Loaded
                ?: return

        //--------------------------------------------------
        // Prevent duplicate save operations.
        //--------------------------------------------------

        if (
            current.isSaving
        ) {
            return
        }

        viewModelScope.launch {

            _uiState.value =
                current.copy(

                    isSaving =
                        true
                )

            //--------------------------------------------------
            // Build updated transaction.
            //--------------------------------------------------

            val updatedTransaction =
                current.transaction.copy(

                    description =
                        current
                            .editableDescription,

                    category =
                        current
                            .selectedCategory,

                    role =
                        current
                            .selectedRole
                )

            //--------------------------------------------------
            // Learn user correction.
            //--------------------------------------------------

            if (

                current.transaction
                    .description !=
                    current.editableDescription ||

                current.transaction
                    .category !=
                    current.selectedCategory

            ) {

                customRuleRepository
                    .saveRule(

                        pattern =
                            current.transaction
                                .description,

                        displayDescription =
                            current.editableDescription,

                        categoryName =
                            current.selectedCategory
                    )
            }

            //--------------------------------------------------
            // Persist transaction.
            //--------------------------------------------------

            transactionRepository
                .updateTransaction(
                    updatedTransaction
                )

            //--------------------------------------------------
            // Notify screen that save completed.
            //--------------------------------------------------

            _saveCompleted.value =
                true

            //--------------------------------------------------
            // Update local state.
            //--------------------------------------------------

            _uiState.value =
                current.copy(

                    transaction =
                        updatedTransaction,

                    selectedRole =
                        updatedTransaction
                            .role,

                    hasChanges =
                        false,

                    isSaving =
                        false
                )
        }
    }

    //--------------------------------------------------
    // Save completion
    //--------------------------------------------------

    fun consumeSaveCompleted() {

        _saveCompleted.value =
            false
    }
}
