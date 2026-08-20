package com.varsel.expensetracker.ui.financialevent

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.varsel.expensetracker.data.local.dao.CategoryDao
import com.varsel.expensetracker.domain.model.Transaction
import com.varsel.expensetracker.domain.model.TransactionLinkGroup
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
import javax.inject.Inject

@HiltViewModel
class FinancialEventViewModel @Inject constructor(

    private val transactionRepository:
        TransactionRepository,

    private val transactionLinkGroupRepository:
        TransactionLinkGroupRepository,

    private val categoryDao:
        CategoryDao

) : ViewModel() {

    private val _uiState =
        MutableStateFlow<FinancialEventUiState>(
            FinancialEventUiState.Loading
        )

    val uiState:
        StateFlow<FinancialEventUiState> =
        _uiState.asStateFlow()

    private var currentTransactionLinkId:
        String? = null

    private var observeJob:
        Job? = null

    //--------------------------------------------------
    // Load financial event
    //--------------------------------------------------

    fun loadFinancialEvent(
        transactionLinkId: String
    ) {

        if (
            currentTransactionLinkId ==
                transactionLinkId &&
            observeJob?.isActive == true
        ) {
            return
        }

        currentTransactionLinkId =
            transactionLinkId

        observeJob?.cancel()

        observeJob =
            viewModelScope.launch {

                transactionRepository
                    .getAllTransactions()
                    .collectLatest { transactions ->

                        rebuildState(

                            transactionLinkId =
                                transactionLinkId,

                            transactions =
                                transactions
                        )
                    }
            }
    }

    //--------------------------------------------------
    // Rebuild screen state
    //--------------------------------------------------

    private suspend fun rebuildState(

        transactionLinkId: String,

        transactions: List<Transaction>

    ) {

        val group =
            transactionLinkGroupRepository
                .getGroup(
                    transactionLinkId
                )

        if (
            group == null
        ) {

            _uiState.value =
                FinancialEventUiState.Error(
                    "Financial event not found."
                )

            return
        }

        //--------------------------------------------------
        // Existing transactions in this Financial Event
        //--------------------------------------------------

        val linkedTransactions =
            transactions
                .filter {

                    it.transactionLinkId ==
                        transactionLinkId
                }

        //--------------------------------------------------
        // Expenses
        //--------------------------------------------------

        val expenses =
            linkedTransactions
                .filter {

                    it.type ==
                        TransactionType.EXPENSE
                }
                .sortedByDescending {

                    it.dateTimestamp
                }

        //--------------------------------------------------
        // Reimbursements
        //
        // Any income linked to this Financial Event is
        // treated as a reimbursement in the Financial
        // Event UI.
        //
        // The DAO also explicitly changes the role to
        // REIMBURSEMENT when an income is added through
        // Add Reimbursements.
        //--------------------------------------------------

        val reimbursements =
            linkedTransactions
                .filter {

                    it.type ==
                        TransactionType.INCOME
                }
                .sortedByDescending {

                    it.dateTimestamp
                }

        //--------------------------------------------------
        // Available expenses
        //
        // Only completely unlinked expenses are available.
        //--------------------------------------------------

        val availableExpenses =
            transactions
                .filter {

                    it.type ==
                        TransactionType.EXPENSE &&

                    it.transactionLinkId ==
                        null
                }
                .sortedByDescending {

                    it.dateTimestamp
                }

        //--------------------------------------------------
        // Available reimbursements
        //
        // IMPORTANT:
        //
        // ALL unlinked income transactions are shown.
        //
        // The user does NOT need to manually change the
        // income role before selecting it.
        //--------------------------------------------------

        val availableReimbursements =
            transactions
                .filter {

                    it.type ==
                        TransactionType.INCOME &&

                    it.transactionLinkId ==
                        null
                }
                .sortedByDescending {

                    it.dateTimestamp
                }

        //--------------------------------------------------
        // Existing application categories
        //--------------------------------------------------

        val categories =
            categoryDao
                .getAllCategoriesSnapshot()
                .map {

                    it.name
                }
                .filter {

                    it.isNotBlank()
                }
                .distinct()
                .sorted()

        //--------------------------------------------------
        // Totals
        //--------------------------------------------------

        val totalExpenses =
            expenses.sumOf {

                it.amount
            }

        val totalReimbursements =
            reimbursements.sumOf {

                it.amount
            }

        //--------------------------------------------------
        // Preserve transient UI state
        //--------------------------------------------------

        val current =
            _uiState.value as?
                FinancialEventUiState.Loaded

        val isEditingGroup =
            current?.isEditingGroup
                ?: false

        //--------------------------------------------------
        // IMPORTANT:
        //
        // isUpdating must be reset after Room emits the
        // updated transaction list.
        //
        // This allows the user to make multiple changes
        // without leaving and re-entering the screen.
        //--------------------------------------------------

        val isUpdating =
            false

        _uiState.value =
            FinancialEventUiState.Loaded(

                group =
                    group,

                expenses =
                    expenses,

                reimbursements =
                    reimbursements,

                availableExpenses =
                    availableExpenses,

                availableReimbursements =
                    availableReimbursements,

                categories =
                    categories,

                totalExpenses =
                    totalExpenses,

                totalReimbursements =
                    totalReimbursements,

                isUpdating =
                    isUpdating,

                isEditingGroup =
                    isEditingGroup
            )
    }

    //--------------------------------------------------
    // Add multiple expenses
    //--------------------------------------------------

    fun addExpenses(
        transactionIds: Set<Long>
    ) {

        val current =
            _uiState.value as?
                FinancialEventUiState.Loaded
                ?: return

        if (
            current.isUpdating ||
            transactionIds.isEmpty()
        ) {
            return
        }

        val validIds =
            current.availableExpenses
                .filter {

                    it.id in
                        transactionIds
                }
                .map {

                    it.id
                }
                .distinct()

        if (
            validIds.isEmpty()
        ) {
            return
        }

        viewModelScope.launch {

            _uiState.value =
                current.copy(

                    isUpdating =
                        true
                )

            transactionRepository
                .linkTransactions(

                    transactionIds =
                        validIds,

                    transactionLinkId =
                        current.group
                            .transactionLinkId
                )
        }
    }

    //--------------------------------------------------
    // Add a single expense
    //
    // Kept for compatibility with existing callers.
    //--------------------------------------------------

    fun addExpense(
        transactionId: Long
    ) {

        addExpenses(
            setOf(
                transactionId
            )
        )
    }

    //--------------------------------------------------
    // Add multiple reimbursements
    //
    // IMPORTANT:
    //
    // The UI/button remains:
    //
    //     Add Reimbursements
    //
    // The picker contains ALL unlinked incomes.
    //
    // The repository operation used here is specifically
    // linkReimbursements(), which causes the DAO to:
    //
    //     1. assign the Financial Event link ID
    //     2. set role = REIMBURSEMENT
    //
    // Therefore the income becomes a proper reimbursement
    // as part of the Financial Event operation.
    //--------------------------------------------------

    fun addReimbursements(
        transactionIds: Set<Long>
    ) {

        val current =
            _uiState.value as?
                FinancialEventUiState.Loaded
                ?: return

        if (
            current.isUpdating ||
            transactionIds.isEmpty()
        ) {
            return
        }

        val validIds =
            current.availableReimbursements
                .filter {

                    it.id in
                        transactionIds
                }
                .map {

                    it.id
                }
                .distinct()

        if (
            validIds.isEmpty()
        ) {
            return
        }

        viewModelScope.launch {

            _uiState.value =
                current.copy(

                    isUpdating =
                        true
                )

            //--------------------------------------------------
            // IMPORTANT CHANGE:
            //
            // Do NOT use linkTransactions() here.
            //
            // linkReimbursements() also changes the role
            // of each selected income to REIMBURSEMENT.
            //--------------------------------------------------

            transactionRepository
                .linkReimbursements(

                    transactionIds =
                        validIds,

                    transactionLinkId =
                        current.group
                            .transactionLinkId
                )
        }
    }

    //--------------------------------------------------
    // Add a single reimbursement
    //
    // Kept for compatibility with existing callers.
    //--------------------------------------------------

    fun addReimbursement(
        transactionId: Long
    ) {

        addReimbursements(
            setOf(
                transactionId
            )
        )
    }

    //--------------------------------------------------
    // Remove transaction from event
    //--------------------------------------------------

    fun removeTransaction(
        transactionId: Long
    ) {

        val current =
            _uiState.value as?
                FinancialEventUiState.Loaded
                ?: return

        if (
            current.isUpdating
        ) {
            return
        }

        val belongsToEvent =
            current.expenses.any {

                it.id ==
                    transactionId
            } ||
            current.reimbursements.any {

                it.id ==
                    transactionId
            }

        if (
            !belongsToEvent
        ) {
            return
        }

        viewModelScope.launch {

            _uiState.value =
                current.copy(

                    isUpdating =
                        true
                )

            transactionRepository
                .unlinkTransaction(
                    transactionId
                )
        }
    }

    //--------------------------------------------------
    // Start editing group
    //--------------------------------------------------

    fun startEditingGroup() {

        val current =
            _uiState.value as?
                FinancialEventUiState.Loaded
                ?: return

        _uiState.value =
            current.copy(

                isEditingGroup =
                    true
            )
    }

    //--------------------------------------------------
    // Cancel editing group
    //--------------------------------------------------

    fun cancelEditingGroup() {

        val current =
            _uiState.value as?
                FinancialEventUiState.Loaded
                ?: return

        _uiState.value =
            current.copy(

                isEditingGroup =
                    false
            )
    }

    //--------------------------------------------------
    // Save group metadata
    //--------------------------------------------------

    fun saveGroup(

        groupName:
            String,

        category:
            String

    ) {

        val current =
            _uiState.value as?
                FinancialEventUiState.Loaded
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
        // Category must come from the application's
        // existing category list.
        //--------------------------------------------------

        val categoryExists =
            current.categories.any {

                it.equals(
                    cleanCategory,
                    ignoreCase = true
                )
            }

        if (
            !categoryExists
        ) {
            return
        }

        //--------------------------------------------------
        // Use canonical spelling from category list.
        //--------------------------------------------------

        val selectedCategory =
            current.categories
                .first {

                    it.equals(
                        cleanCategory,
                        ignoreCase = true
                    )
                }

        viewModelScope.launch {

            val updatedGroup =
                TransactionLinkGroup(

                    transactionLinkId =
                        current.group
                            .transactionLinkId,

                    groupName =
                        cleanName,

                    category =
                        selectedCategory,

                    createdAt =
                        current.group.createdAt
                )

            transactionLinkGroupRepository
                .saveGroup(
                    updatedGroup
                )

            _uiState.value =
                current.copy(

                    group =
                        updatedGroup,

                    isEditingGroup =
                        false
                )
        }
    }
}
