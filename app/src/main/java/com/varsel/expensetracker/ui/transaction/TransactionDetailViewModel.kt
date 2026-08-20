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
import com.varsel.expensetracker.domain.repository.TransferLinkResult
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
                        false,

                    transferErrorMessage =
                        null
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
    //--------------------------------------------------
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
        // Get latest current transaction.
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
        // Do NOT automatically open Create Financial Event.
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
        // Financial Event already exists.
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
    // Transfer linking
    //--------------------------------------------------
    //
    // The current transaction is one side of the transfer.
    //
    // The supplied transaction is the other side.
    //
    // The repository performs the authoritative validation:
    //
    // TRANSFER_OUT + TRANSFER_IN
    // AND
    // exact same amount.
    //--------------------------------------------------

    fun linkTransfer(
        otherTransactionId: Long
    ) {

        val current =
            _uiState.value as?
                TransactionDetailUiState.Loaded
                ?: return

        //--------------------------------------------------
        // Prevent duplicate operation.
        //--------------------------------------------------

        if (
            current.isLinking
        ) {
            return
        }

        //--------------------------------------------------
        // Cannot link transaction to itself.
        //--------------------------------------------------

        if (
            current.transaction.id ==
                otherTransactionId
        ) {

            _uiState.value =
                current.copy(

                    transferErrorMessage =
                        "Please choose a different transaction for the transfer."
                )

            return
        }

        //--------------------------------------------------
        // Determine which side is Transfer Out and which
        // side is Transfer In.
        //--------------------------------------------------

        viewModelScope.launch {

            _uiState.value =
                current.copy(

                    isLinking =
                        true,

                    transferErrorMessage =
                        null
                )

            val otherTransaction =
                transactionRepository
                    .getTransactionById(
                        otherTransactionId
                    )

            if (
                otherTransaction == null
            ) {

                _uiState.value =
                    current.copy(

                        isLinking =
                            false,

                        transferErrorMessage =
                            "The selected transaction could not be found."
                    )

                return@launch
            }

            //--------------------------------------------------
            // Determine pair from roles.
            //--------------------------------------------------

            val transferOutId:
                Long

            val transferInId:
                Long

            when {

                current.transaction.role ==
                    TransactionRole.TRANSFER_OUT &&

                otherTransaction.role ==
                    TransactionRole.TRANSFER_IN -> {

                    transferOutId =
                        current.transaction.id

                    transferInId =
                        otherTransaction.id
                }

                current.transaction.role ==
                    TransactionRole.TRANSFER_IN &&

                otherTransaction.role ==
                    TransactionRole.TRANSFER_OUT -> {

                    transferOutId =
                        otherTransaction.id

                    transferInId =
                        current.transaction.id
                }

                else -> {

                    _uiState.value =
                        current.copy(

                            isLinking =
                                false,

                            transferErrorMessage =
                                "Please select one Transfer In and one Transfer Out transaction."
                        )

                    return@launch
                }
            }

            //--------------------------------------------------
            // Repository performs final validation.
            //--------------------------------------------------

            when (
                val result =
                    transactionRepository
                        .linkTransfer(

                            transferOutTransactionId =
                                transferOutId,

                            transferInTransactionId =
                                transferInId
                        )
            ) {

                //--------------------------------------------------
                // Success
                //--------------------------------------------------

                TransferLinkResult.Success -> {

                    _uiState.value =
                        current.copy(

                            isLinking =
                                false,

                            transferErrorMessage =
                                null
                        )
                }

                //--------------------------------------------------
                // Amount mismatch
                //--------------------------------------------------

                is TransferLinkResult.AmountMismatch -> {

                    val outAmount =
                        result
                            .transferOutAmount

                    val inAmount =
                        result
                            .transferInAmount

                    _uiState.value =
                        current.copy(

                            isLinking =
                                false,

                            transferErrorMessage =
                                "The transfer amounts don't match. " +
                                "Transfer Out: ₹$outAmount, " +
                                "Transfer In: ₹$inAmount. " +
                                "Please make sure you selected the correct Transfer In / Transfer Out."
                        )
                }

                //--------------------------------------------------
                // Invalid pair
                //--------------------------------------------------

                TransferLinkResult.InvalidTransactionPair -> {

                    _uiState.value =
                        current.copy(

                            isLinking =
                                false,

                            transferErrorMessage =
                                "These transactions cannot be linked as a transfer. Please select one Transfer In and one Transfer Out."
                        )
                }

                //--------------------------------------------------
                // Transaction not found
                //--------------------------------------------------

                TransferLinkResult.TransactionNotFound -> {

                    _uiState.value =
                        current.copy(

                            isLinking =
                                false,

                            transferErrorMessage =
                                "One of the selected transfer transactions could not be found."
                        )
                }

                //--------------------------------------------------
                // Already linked
                //--------------------------------------------------

                TransferLinkResult.AlreadyLinked -> {

                    _uiState.value =
                        current.copy(

                            isLinking =
                                false,

                            transferErrorMessage =
                                "One of these transactions is already linked to another transfer."
                        )
                }
            }
        }
    }

    //--------------------------------------------------
    // Clear transfer error
    //--------------------------------------------------

    fun clearTransferError() {

        val current =
            _uiState.value as?
                TransactionDetailUiState.Loaded
                ?: return

        _uiState.value =
            current.copy(

                transferErrorMessage =
                    null
            )
    }

    //--------------------------------------------------
    // Unlink transfer
    //--------------------------------------------------

    fun unlinkTransfer() {

        val current =
            _uiState.value as?
                TransactionDetailUiState.Loaded
                ?: return

        //--------------------------------------------------
        // Nothing to unlink.
        //--------------------------------------------------

        if (
            current.transaction
                .transferLinkId == null
        ) {
            return
        }

        //--------------------------------------------------
        // Prevent duplicate operation.
        //--------------------------------------------------

        if (
            current.isLinking
        ) {
            return
        }

        viewModelScope.launch {

            _uiState.value =
                current.copy(

                    isLinking =
                        true,

                    transferErrorMessage =
                        null
                )

            transactionRepository
                .unlinkTransfer(
                    current.transaction.id
                )

            //--------------------------------------------------
            // Room Flow refreshes the transaction state.
            //--------------------------------------------------
        }
    }

    //--------------------------------------------------
    // Unlink current Financial Event transaction
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
                .transactionLinkId == null
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

                    isLinking =
                        true
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
        // Validate category.
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
            // Create Financial Event link ID.
            //--------------------------------------------------

            val transactionLinkId =
                current.transaction
                    .transactionLinkId
                    ?: UUID.randomUUID()
                        .toString()

            //--------------------------------------------------
            // Financial Event role.
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
                        false,

                    transferErrorMessage =
                        null
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
            // Notify screen.
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
                        false,

                    transferErrorMessage =
                        null
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
