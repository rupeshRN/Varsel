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
    // Current transaction
    //--------------------------------------------------

    private var currentTransactionId:
        Long? = null

    //--------------------------------------------------
    // Transaction observation job
    //--------------------------------------------------

    private var transactionObservationJob:
        Job? = null

    //--------------------------------------------------
    // Temporary transaction selections
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
    // Observe transactions
    //--------------------------------------------------

    private fun observeTransactions(
        transactionId: Long
    ) {

        transactionObservationJob =
            viewModelScope.launch {

                transactionRepository
                    .getAllTransactions()
                    .collectLatest {

                        allTransactions ->

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
    // Build linking UI state
    //
    // IMPORTANT:
    // This is suspend because getGroup() is suspend.
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
                    it.id ==
                        transactionId
                }
                ?: currentState.transaction

        //--------------------------------------------------
        // Existing financial-event link
        //--------------------------------------------------

        val transactionLinkId =
            currentTransaction
                .transactionLinkId

        val linkedTransactions =
            transactionLinkId
                ?.let { linkId ->

                    allTransactions
                        .filter {

                            it.transactionLinkId ==
                                linkId
                        }
                }
                .orEmpty()

        //--------------------------------------------------
        // Linkable transactions
        //
        // Preserve current manual-linking behaviour.
        //
        // Expense:
        //     unlinked reimbursement incomes.
        //
        // Reimbursement:
        //     unlinked expenses.
        //--------------------------------------------------

        val linkableTransactions =
            when {

                currentTransaction.type ==
                    TransactionType.EXPENSE -> {

                    allTransactions
                        .filter { transaction ->

                            transaction.id !=
                                transactionId &&

                            transaction.type ==
                                TransactionType.INCOME &&

                            transaction.role ==
                                TransactionRole.REIMBURSEMENT &&

                            transaction.transactionLinkId ==
                                null
                        }
                }

                currentTransaction.type ==
                    TransactionType.INCOME &&

                currentTransaction.role ==
                    TransactionRole.REIMBURSEMENT -> {

                    allTransactions
                        .filter { transaction ->

                            transaction.id !=
                                transactionId &&

                            transaction.type ==
                                TransactionType.EXPENSE &&

                            transaction.transactionLinkId ==
                                null
                        }
                }

                else -> {

                    emptyList()
                }
            }
            .sortedByDescending {
                it.dateTimestamp
            }

        //--------------------------------------------------
        // Remove selections that are no longer available.
        //--------------------------------------------------

        val validSelectedIds =
            _selectedTransactionIds
                .value
                .filter { selectedId ->

                    linkableTransactions.any {
                        candidate ->

                        candidate.id ==
                            selectedId
                    }
                }
                .toSet()

        _selectedTransactionIds.value =
            validSelectedIds

        //--------------------------------------------------
        // Existing Financial Event / Report Group
        //
        // getGroup() is suspend, therefore this whole
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
        // Determine whether Financial Event group prompt
        // should be shown.
        //
        // Current behaviour:
        // offer creation once the event contains more
        // than one expense.
        //--------------------------------------------------

        val expenseCount =
            linkedTransactions
                .count {

                    it.type ==
                        TransactionType.EXPENSE
                }

        val shouldOfferGroup =
            transactionLinkId != null &&
            expenseCount > 1 &&
            existingGroup == null

        //--------------------------------------------------
        // Preserve transient UI state.
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
                    linkableTransactions,

                transactionLinkGroup =
                    existingGroup,

                showCreateGroupPrompt =
                    currentState
                        .showCreateGroupPrompt ||
                    shouldOfferGroup,

                isLinking =
                    false
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
    // Toggle linkable transaction
    //--------------------------------------------------

    fun toggleReimbursementSelection(
        transactionId: Long
    ) {

        val current =
            _uiState.value as?
                TransactionDetailUiState.Loaded
                ?: return

        val isCandidate =
            current.linkableTransactions
                .any {

                    it.id ==
                        transactionId
                }

        if (!isCandidate) {
            return
        }

        val currentSelection =
            _selectedTransactionIds.value

        _selectedTransactionIds.value =
            if (
                transactionId in
                    currentSelection
            ) {

                currentSelection -
                    transactionId

            } else {

                currentSelection +
                    transactionId
            }
    }

    //--------------------------------------------------
    // Generic candidate toggle
    //
    // Used by the newer TransactionLinkSection.
    //--------------------------------------------------

    fun toggleCandidate(
        transactionId: Long
    ) {

        toggleReimbursementSelection(
            transactionId
        )
    }

    //--------------------------------------------------
    // Check selection
    //--------------------------------------------------

    fun isReimbursementSelected(
        transactionId: Long
    ): Boolean {

        return transactionId in
            _selectedTransactionIds.value
    }

    //--------------------------------------------------
    // Selected transaction IDs
    //--------------------------------------------------

    fun getSelectedReimbursementIds():
        Set<Long> {

        return _selectedTransactionIds.value
    }

    //--------------------------------------------------
    // Link selected transactions
    //--------------------------------------------------

    fun linkSelectedTransactions() {

        val current =
            _uiState.value as?
                TransactionDetailUiState.Loaded
                ?: return

        val selectedIds =
            _selectedTransactionIds.value

        if (
            selectedIds.isEmpty()
        ) {
            return
        }

        viewModelScope.launch {

            _uiState.value =
                current.copy(
                    isLinking = true
                )

            //--------------------------------------------------
            // Reuse existing link ID when one exists.
            //--------------------------------------------------

            val transactionLinkId =
                current.transaction
                    .transactionLinkId
                    ?: UUID.randomUUID()
                        .toString()

            //--------------------------------------------------
            // Current transaction +
            // selected transactions.
            //--------------------------------------------------

            val transactionIds =
                buildList {

                    add(
                        current.transaction.id
                    )

                    addAll(
                        selectedIds
                    )
                }
                    .distinct()

            //--------------------------------------------------
            // Persist relationship.
            //--------------------------------------------------

            transactionRepository
                .linkTransactions(

                    transactionIds =
                        transactionIds,

                    transactionLinkId =
                        transactionLinkId
                )

            //--------------------------------------------------
            // Clear temporary selection.
            //--------------------------------------------------

            _selectedTransactionIds.value =
                emptySet()

            //--------------------------------------------------
            // Room Flow will refresh the UI.
            //--------------------------------------------------
        }
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
                .transactionLinkId ==
            null
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
    // Group prompt
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
    // Create Financial Event / Report Group
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

        val transactionLinkId =
            current.transaction
                .transactionLinkId
                ?: return

        val cleanName =
            groupName.trim()

        val cleanCategory =
            category.trim()

        //--------------------------------------------------
        // Basic validation
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

            _uiState.value =
                current.copy(

                    transactionLinkGroup =
                        group,

                    showCreateGroupPrompt =
                        false,

                    isSavingGroup =
                        false
                )
        }
    }

    //--------------------------------------------------
    // Delete Financial Event / Report Group
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
