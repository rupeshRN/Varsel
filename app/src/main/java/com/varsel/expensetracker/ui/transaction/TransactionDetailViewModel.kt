package com.varsel.expensetracker.ui.transaction

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.varsel.expensetracker.data.repository.CustomRuleRepository
import com.varsel.expensetracker.domain.model.Transaction
import com.varsel.expensetracker.domain.model.TransactionLinkGroup
import com.varsel.expensetracker.domain.model.TransactionRole
import com.varsel.expensetracker.domain.model.TransactionType
import com.varsel.expensetracker.domain.repository.TransactionLinkGroupRepository
import com.varsel.expensetracker.domain.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
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
        TransactionLinkGroupRepository

) : ViewModel() {

    private val _uiState =
        MutableStateFlow<TransactionDetailUiState>(
            TransactionDetailUiState.Loading
        )

    val uiState: StateFlow<TransactionDetailUiState> =
        _uiState.asStateFlow()

    private val _saveCompleted =
        MutableStateFlow(false)

    val saveCompleted: StateFlow<Boolean> =
        _saveCompleted.asStateFlow()

    //--------------------------------------------------
    // Current transaction
    //--------------------------------------------------

    private var currentTransactionId: Long? = null

    //--------------------------------------------------
    // Temporary selections
    //--------------------------------------------------

    private val _selectedTransactionIds =
        MutableStateFlow<Set<Long>>(emptySet())

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

        _selectedTransactionIds.value =
            emptySet()

        viewModelScope.launch {

            val transaction =
                transactionRepository
                    .getTransactionById(
                        transactionId
                    )

            if (transaction == null) {

                _uiState.value =
                    TransactionDetailUiState.Error(
                        "Transaction not found."
                    )

                return@launch
            }

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
    // Observe all transactions
    //--------------------------------------------------

    private fun observeTransactions(
        transactionId: Long
    ) {

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
    // Build linking UI state
    //--------------------------------------------------

    private suspend fun updateLinkingState(

        transactionId: Long,

        allTransactions: List<Transaction>

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
        // Existing link
        //--------------------------------------------------

        val transactionLinkId =
            currentTransaction.transactionLinkId

        val linkedTransactions =
            transactionLinkId
                ?.let { linkId ->

                    allTransactions
                        .filter {
                            it.transactionLinkId ==
                                linkId
                        }
                        .sortedBy {
                            it.dateTimestamp
                        }
                }
                .orEmpty()

        //--------------------------------------------------
        // Determine what can be manually linked.
        //
        // IMPORTANT:
        //
        // Expense:
        //     show unlinked reimbursement incomes.
        //
        // Reimbursement:
        //     show unlinked expenses.
        //
        // Normal income:
        //     no candidates.
        //
        // Nothing is automatically linked.
        //--------------------------------------------------

        val linkableTransactions =
            when {

                //--------------------------------------------------
                // Current transaction is an expense.
                //--------------------------------------------------

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
                        .sortedByDescending {
                            it.dateTimestamp
                        }
                }

                //--------------------------------------------------
                // Current transaction is a reimbursement.
                //--------------------------------------------------

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
                        .sortedByDescending {
                            it.dateTimestamp
                        }
                }

                //--------------------------------------------------
                // Normal income or any other transaction.
                //--------------------------------------------------

                else -> {

                    emptyList()
                }
            }

        //--------------------------------------------------
        // Remove selections that are no longer valid.
        //--------------------------------------------------

        val validSelectedIds =
            _selectedTransactionIds
                .value
                .filter { selectedId ->

                    linkableTransactions.any {
                        it.id == selectedId
                    }
                }
                .toSet()

        _selectedTransactionIds.value =
            validSelectedIds

        //--------------------------------------------------
        // Existing report group
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
        // Determine whether a report group is applicable.
        //
        // A group is needed only when the financial event
        // contains more than one expense.
        //
        // 1 expense -> multiple reimbursements
        //     No group required.
        //
        // Multiple expenses -> 1 reimbursement
        //     Group available.
        //
        // Multiple expenses -> multiple reimbursements
        //     Group available.
        //--------------------------------------------------

        val expenseCount =
            linkedTransactions.count {

                it.type ==
                    TransactionType.EXPENSE
            }

        val shouldOfferGroup =
            transactionLinkId != null &&
            expenseCount > 1 &&
            existingGroup == null

        //--------------------------------------------------
        // Update UI state
        //--------------------------------------------------

        _uiState.value =
            currentState.copy(

                transaction =
                    currentTransaction,

                linkedTransactions =
                    linkedTransactions,

                linkableTransactions =
                    linkableTransactions,

                transactionLinkGroup =
                    existingGroup,

                showCreateGroupPrompt =
                    currentState.showCreateGroupPrompt ||
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
    // Toggle transaction selection
    //--------------------------------------------------

    fun toggleReimbursementSelection(
        transactionId: Long
    ) {

        val current =
            _uiState.value as?
                TransactionDetailUiState.Loaded
                ?: return

        val isLinkable =
            current.linkableTransactions.any {

                it.id == transactionId
            }

        if (!isLinkable) {
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

        if (selectedIds.isEmpty()) {
            return
        }

        viewModelScope.launch {

            _uiState.value =
                current.copy(
                    isLinking = true
                )

            //--------------------------------------------------
            // Reuse an existing link ID if this transaction
            // is already part of a financial event.
            //
            // Otherwise create a new internal UUID.
            //--------------------------------------------------

            val transactionLinkId =
                current.transaction
                    .transactionLinkId
                    ?: UUID.randomUUID()
                        .toString()

            //--------------------------------------------------
            // Current transaction + selected transactions
            //--------------------------------------------------

            val transactionIds =
                mutableListOf<Long>()

            transactionIds.add(
                current.transaction.id
            )

            transactionIds.addAll(
                selectedIds
            )

            val distinctTransactionIds =
                transactionIds.distinct()

            //--------------------------------------------------
            // Persist relationship
            //--------------------------------------------------

            transactionRepository
                .linkTransactions(

                    transactionIds =
                        distinctTransactionIds,

                    transactionLinkId =
                        transactionLinkId
                )

            //--------------------------------------------------
            // Clear temporary selections
            //--------------------------------------------------

            _selectedTransactionIds.value =
                emptySet()

            //--------------------------------------------------
            // Room Flow refreshes the UI.
            //--------------------------------------------------
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
    // Create report group
    //--------------------------------------------------

    fun createReportGroup(

        groupName: String,

        category: String

    ) {

        val current =
            _uiState.value as?
                TransactionDetailUiState.Loaded
                ?: return

        val transactionLinkId =
            current.transaction
                .transactionLinkId
                ?: return

        if (
            groupName.isBlank() ||
            category.isBlank()
        ) {
            return
        }

        viewModelScope.launch {

            _uiState.value =
                current.copy(
                    isSavingGroup = true
                )

            val group =
                TransactionLinkGroup(

                    transactionLinkId =
                        transactionLinkId,

                    groupName =
                        groupName.trim(),

                    category =
                        category.trim(),

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
    // Delete report group
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

            //--------------------------------------------------
            // The report group is intentionally retained.
            // Room Flow will refresh the linking state.
            //--------------------------------------------------
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
                    isSaving = true
                )

            val updatedTransaction =
                current.transaction.copy(

                    description =
                        current.editableDescription,

                    category =
                        current.selectedCategory,

                    role =
                        current.selectedRole
                )

            //--------------------------------------------------
            // Learn user correction
            //--------------------------------------------------

            if (

                current.transaction.description !=
                    current.editableDescription ||

                current.transaction.category !=
                    current.selectedCategory

            ) {

                customRuleRepository.saveRule(

                    pattern =
                        current.transaction.description,

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
                        updatedTransaction.role,

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
