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

    /**
     * IDs selected by the user for manual linking.
     *
     * These are UI selections only.
     * Nothing is persisted until linkSelectedTransactions()
     * is explicitly called.
     */
    private val selectedTransactionIds =
        MutableStateFlow<Set<Long>>(emptySet())

    //--------------------------------------------------
    // Load transaction
    //--------------------------------------------------

    fun loadTransaction(
        transactionId: Long
    ) {

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
            // Load relationship information
            //--------------------------------------------------

            refreshLinkingData(
                currentTransactionId = transactionId
            )
        }
    }

    //--------------------------------------------------
    // Refresh linking data
    //--------------------------------------------------

    private suspend fun refreshLinkingData(
        currentTransactionId: Long
    ) {

        val currentState =
            _uiState.value as?
                TransactionDetailUiState.Loaded
                ?: return

        val allTransactions =
            transactionRepository
                .getAllTransactions()
                .let { flow ->

                    kotlinx.coroutines.flow.first(
                        flow
                    )
                }

        val currentTransaction =
            allTransactions.firstOrNull {
                it.id == currentTransactionId
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
        // Only:
        // - INCOME
        // - REIMBURSEMENT
        // - currently unlinked
        // - not the current transaction
        //--------------------------------------------------

        val reimbursementCandidates =
            allTransactions
                .filter {

                    it.id != currentTransactionId &&

                    it.type ==
                        TransactionType.INCOME &&

                    it.role ==
                        TransactionRole.REIMBURSEMENT &&

                    it.transactionLinkId == null
                }
                .sortedByDescending {
                    it.dateTimestamp
                }

        //--------------------------------------------------
        // Remove selections that are no longer available.
        //--------------------------------------------------

        selectedTransactionIds.value =
            selectedTransactionIds.value
                .filter { selectedId ->

                    reimbursementCandidates.any {
                        it.id == selectedId
                    }
                }
                .toSet()

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

        if (
            current.reimbursementCandidates.none {
                it.id == transactionId
            }
        ) {
            return
        }

        val currentSelection =
            selectedTransactionIds.value

        selectedTransactionIds.value =
            if (
                transactionId in currentSelection
            ) {
                currentSelection - transactionId
            } else {
                currentSelection + transactionId
            }
    }

    //--------------------------------------------------
    // Selection state
    //--------------------------------------------------

    fun isReimbursementSelected(
        transactionId: Long
    ): Boolean {

        return transactionId in
            selectedTransactionIds.value
    }

    fun getSelectedReimbursementIds(): Set<Long> {

        return selectedTransactionIds.value
    }

    //--------------------------------------------------
    // Manual link confirmation
    //--------------------------------------------------

    fun linkSelectedTransactions() {

        val current =
            _uiState.value as?
                TransactionDetailUiState.Loaded
                ?: return

        val selectedIds =
            selectedTransactionIds.value

        if (selectedIds.isEmpty()) {
            return
        }

        viewModelScope.launch {

            _uiState.value =
                current.copy(
                    isLinking = true
                )

            //--------------------------------------------------
            // Existing link?
            //
            // If the current transaction already belongs to
            // a link group, reuse that group's ID.
            //
            // Otherwise create a new internal UUID.
            //--------------------------------------------------

            val transactionLinkId =
                current.transaction.transactionLinkId
                    ?: UUID.randomUUID().toString()

            //--------------------------------------------------
            // Current transaction + selected reimbursements
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

            transactionRepository.linkTransactions(

                transactionIds =
                    transactionIds,

                transactionLinkId =
                    transactionLinkId
            )

            //--------------------------------------------------
            // Clear temporary selection.
            //--------------------------------------------------

            selectedTransactionIds.value =
                emptySet()

            //--------------------------------------------------
            // Refresh relationship information.
            //--------------------------------------------------

            refreshLinkingData(
                currentTransactionId =
                    current.transaction.id
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

            selectedTransactionIds.value =
                emptySet()

            refreshLinkingData(
                currentTransactionId =
                    current.transaction.id
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
            // Role is transaction-specific and therefore
            // intentionally NOT stored in the learning rule.
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
