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

    private var currentTransactionId:
        Long? = null

    private var transactionObservationJob:
        Job? = null

    //--------------------------------------------------
    // Temporary linking state
    //
    // Kept for compatibility with existing code.
    // The old "Possible Transactions to Link" UI is
    // no longer displayed.
    //--------------------------------------------------

    private val _selectedTransactionIds =
        MutableStateFlow<Set<Long>>(
            emptySet()
        )

    val selectedTransactionIds:
        StateFlow<Set<Long>> =
        _selectedTransactionIds.asStateFlow()

    //--------------------------------------------------
    // Load transaction
    //--------------------------------------------------

    fun loadTransaction(
        transactionId: Long
    ) {

        currentTransactionId =
            transactionId

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

                    linkableTransactions =
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

                        updateLinkingState(

                            transactionId =
                                transactionId,

                            allTransactions =
                                allTransactions
                        )
                    }
            }
    }

    //--------------------------------------------------
    // Build financial event state
    //--------------------------------------------------

    private suspend fun updateLinkingState(

        transactionId:
            Long,

        allTransactions:
            List<Transaction>

    ) {

        val currentState =
            _uiState.value as?
                TransactionDetailUiState.Loaded
                ?: return

        val currentTransaction =
            allTransactions
                .firstOrNull {
                    it.id == transactionId
                }
                ?: currentState.transaction

        //--------------------------------------------------
        // Existing financial-event link
        //--------------------------------------------------

        val transactionLinkId =
            currentTransaction
                .transactionLinkId

        //--------------------------------------------------
        // Existing linked transactions
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
        // IMPORTANT:
        //
        // Possible transaction linking is no longer part
        // of Transaction Details.
        //
        // Financial Event management is now handled from
        // the Financial Event screen.
        //--------------------------------------------------

        _selectedTransactionIds.value =
            emptySet()

        //--------------------------------------------------
        // Existing Financial Event group
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
        // The user now explicitly chooses:
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

                linkableTransactions =
                    emptyList(),

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
                        current.transaction.description ||

                    current.selectedCategory !=
                        current.transaction.category ||

                    current.selectedRole !=
                        current.transaction.role
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
                        current.transaction.category ||

                    current.editableDescription !=
                        current.transaction.description ||

                    current.selectedRole !=
                        current.transaction.role
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
                        current.transaction.role ||

                    current.editableDescription !=
                        current.transaction.description ||

                    current.selectedCategory !=
                        current.transaction.category
            )
    }

    //--------------------------------------------------
    // Legacy selection methods
    //
    // Kept so existing callers do not break while the
    // old linking UI is removed.
    //--------------------------------------------------

    fun toggleReimbursementSelection(
        transactionId: Long
    ) {
        // Intentionally disabled.
    }

    fun toggleCandidate(
        transactionId: Long
    ) {
        // Intentionally disabled.
    }

    fun isReimbursementSelected(
        transactionId: Long
    ): Boolean {
        return false
    }

    fun getSelectedReimbursementIds():
        Set<Long> {

        return emptySet()
    }

    fun linkSelectedTransactions() {
        // Intentionally disabled.
    }

    //--------------------------------------------------
    // Unlink current transaction
    //--------------------------------------------------

    fun unlinkCurrentTransaction() {

        val current =
            _uiState.value as?
                TransactionDetailUiState.Loaded
                ?: return

        if (
            current.transaction
                .transactionLinkId == null
        ) {
            return
        }

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

            _selectedTransactionIds.value =
                emptySet()
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

        if (
            current.isSavingGroup
        ) {
            return
        }

        val cleanName =
            groupName.trim()

        val cleanCategory =
            category.trim()

        //--------------------------------------------------
        // Validation
        //--------------------------------------------------

        if (
            cleanName.isBlank() ||
            cleanCategory.isBlank()
        ) {
            return
        }

        //--------------------------------------------------
        // Validate category
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
            // Create a link ID if this transaction does not
            // already belong to a Financial Event.
            //--------------------------------------------------

            val transactionLinkId =
                current.transaction
                    .transactionLinkId
                    ?: UUID.randomUUID()
                        .toString()

            //--------------------------------------------------
            // Creating a Financial Event establishes the
            // transaction's financial role.
            //
            // EXPENSE -> LENT
            // INCOME  -> REIMBURSEMENT
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
            // Update current transaction
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
            // Create Financial Event group
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

                    linkableTransactions =
                        emptyList(),

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
            // Learn user correction
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
            // Persist transaction
            //--------------------------------------------------

            transactionRepository
                .updateTransaction(
                    updatedTransaction
                )

            _saveCompleted.value =
                true

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
