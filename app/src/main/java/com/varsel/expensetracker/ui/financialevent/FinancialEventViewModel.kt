package com.varsel.expensetracker.ui.financialevent

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
import javax.inject.Inject

@HiltViewModel
class FinancialEventViewModel @Inject constructor(

    private val transactionRepository:
        TransactionRepository,

    private val transactionLinkGroupRepository:
        TransactionLinkGroupRepository

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

    private var observeJob: Job? = null

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

        if (group == null) {

            _uiState.value =
                FinancialEventUiState.Error(
                    "Financial event not found."
                )

            return
        }

        //--------------------------------------------------
        // Transactions already belonging to this event
        //--------------------------------------------------

        val linkedTransactions =
            transactions
                .filter {
                    it.transactionLinkId ==
                        transactionLinkId
                }

        val expenses =
            linkedTransactions
                .filter {
                    it.type ==
                        TransactionType.EXPENSE
                }
                .sortedByDescending {
                    it.dateTimestamp
                }

        val reimbursements =
            linkedTransactions
                .filter {

                    it.type ==
                        TransactionType.INCOME &&

                    it.role ==
                        TransactionRole.REIMBURSEMENT
                }
                .sortedByDescending {
                    it.dateTimestamp
                }

        //--------------------------------------------------
        // Available transactions
        //
        // We deliberately do NOT automatically select or
        // match anything.
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

        val availableReimbursements =
            transactions
                .filter {

                    it.type ==
                        TransactionType.INCOME &&

                    it.role ==
                        TransactionRole.REIMBURSEMENT &&

                    it.transactionLinkId ==
                        null
                }
                .sortedByDescending {
                    it.dateTimestamp
                }

        val totalExpenses =
            expenses.sumOf {
                it.amount
            }

        val totalReimbursements =
            reimbursements.sumOf {
                it.amount
            }

        val current =
            _uiState.value

        val isEditingGroup =
            (
                current as?
                    FinancialEventUiState.Loaded
            )?.isEditingGroup
                ?: false

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

        totalExpenses =
            totalExpenses,

        totalReimbursements =
            totalReimbursements,

        // Database/Room refresh completed.
        isUpdating =
            false,

        isEditingGroup =
            isEditingGroup
    )
    }

    //--------------------------------------------------
    // Add an expense
    //--------------------------------------------------

    fun addExpense(
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

        val transaction =
            current.availableExpenses
                .firstOrNull {
                    it.id == transactionId
                }
                ?: return

        viewModelScope.launch {

            _uiState.value =
                current.copy(
                    isUpdating = true
                )

            transactionRepository
                .linkTransactions(

                    transactionIds =
                        listOf(transaction.id),

                    transactionLinkId =
                        current.group.transactionLinkId
                )
        }
    }

    //--------------------------------------------------
    // Add a reimbursement
    //--------------------------------------------------

    fun addReimbursement(
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

        val transaction =
            current.availableReimbursements
                .firstOrNull {
                    it.id == transactionId
                }
                ?: return

        viewModelScope.launch {

            _uiState.value =
                current.copy(
                    isUpdating = true
                )

            transactionRepository
                .linkTransactions(

                    transactionIds =
                        listOf(transaction.id),

                    transactionLinkId =
                        current.group.transactionLinkId
                )
        }
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
                it.id == transactionId
            } ||
            current.reimbursements.any {
                it.id == transactionId
            }

        if (!belongsToEvent) {
            return
        }

        viewModelScope.launch {

            _uiState.value =
                current.copy(
                    isUpdating = true
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
                isEditingGroup = true
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
                isEditingGroup = false
            )
    }

    //--------------------------------------------------
    // Save group metadata
    //--------------------------------------------------

    fun saveGroup(

        groupName: String,

        category: String

    ) {

        val current =
            _uiState.value as?
                FinancialEventUiState.Loaded
                ?: return

        val cleanName =
            groupName.trim()

        val cleanCategory =
            category.trim()

        if (
            cleanName.isBlank() ||
            cleanCategory.isBlank()
        ) {
            return
        }

        viewModelScope.launch {

            val updatedGroup =
                TransactionLinkGroup(

                    transactionLinkId =
                        current.group.transactionLinkId,

                    groupName =
                        cleanName,

                    category =
                        cleanCategory,

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
