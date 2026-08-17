package com.varsel.expensetracker.ui.transaction

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.varsel.expensetracker.data.repository.CustomRuleRepository
import com.varsel.expensetracker.domain.model.Transaction
import com.varsel.expensetracker.domain.model.TransactionRole
import com.varsel.expensetracker.domain.model.TransactionType
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

    private val transactionRepository: TransactionRepository,

    private val customRuleRepository: CustomRuleRepository

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
    // Current transaction ID
    //--------------------------------------------------

    private var currentTransactionId: Long? = null

    //--------------------------------------------------
    // Temporary reimbursement selections
    //
    // These are NOT persisted until the user confirms.
    //--------------------------------------------------

    private val _selectedTransactionIds =
        MutableStateFlow<Set<Long>>(emptySet())

    val selectedTransactionIds: StateFlow<Set<Long>> =
        _selectedTransactionIds.asStateFlow()

    //--------------------------------------------------
    // Load transaction
    //--------------------------------------------------

    fun loadTransaction(
        transactionId: Long
    ) {

        currentTransactionId = transactionId

        viewModelScope.launch {

            val transaction =
                transactionRepository.getTransactionById(
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

                    transaction = transaction,

                    editableDescription =
                        transaction.description,

                    selectedCategory =
                        transaction.category,

                    selectedRole =
                        transaction.role,

                    hasChanges = false,

                    isSaving = false,

                    linkedTransactions =
                        emptyList(),

                    reimbursementCandidates =
                        emptyList(),

                    isLinking = false
                )

            //--------------------------------------------------
            // Observe all transactions.
            //--------------------------------------------------

            observeTransactions(
                transactionId
            )
        }
    }

    //--------------------------------------------------
    // Observe transactions
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
    // Build linking-related UI state
    //--------------------------------------------------

    private fun updateLinkingState(
        transactionId: Long,
        allTransactions: List<Transaction>
    ) {

        val currentState =
            _uiState.value as?
                TransactionDetailUiState.Loaded
                ?: return

        val currentTransaction =
            allTransactions.firstOrNull {
                it.id == transactionId
            }
                ?: currentState.transaction

        //--------------------------------------------------
        // Existing link group
        //--------------------------------------------------

        val linkedTransactions =
            currentTransaction.transactionLinkId
                ?.let { linkId ->

                    allTransactions.filter {
                        it.transactionLinkId == linkId
                    }

                }
                .orEmpty()

        //--------------------------------------------------
        // Reimbursement candidates
        //
        // Deliberately NO automatic matching.
        //--------------------------------------------------

        val reimbursementCandidates =
            allTransactions
                .filter { transaction ->

                    transaction.id != transactionId &&

                    transaction.type ==
                        TransactionType.INCOME &&

                    transaction.role ==
                        TransactionRole.REIMBURSEMENT &&

                    transaction.transactionLinkId == null
                }
                .sortedByDescending {
                    it.dateTimestamp
                }

        //--------------------------------------------------
        // Remove selections that are no longer available.
        //--------------------------------------------------

        val validSelectedIds =
            _selectedTransactionIds.value
                .filter { selectedId ->

                    reimbursementCandidates.any { candidate ->

                        candidate.id == selectedId
                    }
                }
                .toSet()

        _selectedTransactionIds.value =
            validSelectedIds

        //--------------------------------------------------
        // Preserve editable fields.
        //--------------------------------------------------

        _uiState.value =
            currentState.copy(

                transaction =
                    currentTransaction,

                linkedTransactions =
                    linkedTransactions,

                reimbursementCandidates =
                    reimbursementCandidates,

                isLinking = false
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
    // Toggle reimbursement selection
    //--------------------------------------------------

    fun toggleReimbursementSelection(
        transactionId: Long
    ) {

        val current =
            _uiState.value as?
                TransactionDetailUiState.Loaded
                ?: return

        val isCandidate =
            current.reimbursementCandidates.any {
                it.id == transactionId
            }

        if (!isCandidate) {
            return
        }

        val currentSelection =
            _selectedTransactionIds.value

        _selectedTransactionIds.value =
            if (transactionId in currentSelection) {

                currentSelection - transactionId

            } else {

                currentSelection + transactionId
            }
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
    // Selected transactions
    //--------------------------------------------------

    fun getSelectedReimbursementIds(): Set<Long> {

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
            // Reuse an existing link ID if available.
            // Otherwise create a new internal UUID.
            //--------------------------------------------------

            val transactionLinkId =
                current.transaction.transactionLinkId
                    ?: UUID.randomUUID().toString()

            //--------------------------------------------------
            // Current transaction + selected reimbursements
            //--------------------------------------------------

            val transactionIds =
                mutableListOf<Long>()

            transactionIds.add(
                current.transaction.id
            )

            transactionIds.addAll(
                selectedIds
            )

            //--------------------------------------------------
            // Remove duplicates.
            //--------------------------------------------------

            val distinctTransactionIds =
                transactionIds.distinct()

            //--------------------------------------------------
            // Persist relationship.
            //--------------------------------------------------

            transactionRepository.linkTransactions(

                transactionIds =
                    distinctTransactionIds,

                transactionLinkId =
                    transactionLinkId
            )

            //--------------------------------------------------
            // Clear temporary selections.
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
            current.transaction.transactionLinkId == null
        ) {
            return
        }

        viewModelScope.launch {

            _uiState.value =
                current.copy(
                    isLinking = true
                )

            transactionRepository.unlinkTransaction(

                transactionId =
                    current.transaction.id
            )

            _selectedTransactionIds.value =
                emptySet()

            //--------------------------------------------------
            // Room Flow refreshes the UI.
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
            // Learn user correction.
            //
            // Role remains transaction-specific.
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
            // Persist transaction.
            //--------------------------------------------------

            transactionRepository.updateTransaction(
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

                    hasChanges = false,

                    isSaving = false
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