package com.varsel.expensetracker.ui.financialevent

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.varsel.expensetracker.domain.model.TransactionRole
import com.varsel.expensetracker.domain.model.TransactionType
import com.varsel.expensetracker.domain.repository.TransactionLinkGroupRepository
import com.varsel.expensetracker.domain.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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

    fun loadFinancialEvent(
        transactionLinkId: String
    ) {

        viewModelScope.launch {

            _uiState.value =
                FinancialEventUiState.Loading

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

                return@launch
            }

            transactionRepository
                .getAllTransactions()
                .collect { transactions ->

                    val linkedTransactions =
                        transactions.filter {

                            it.transactionLinkId ==
                                transactionLinkId
                        }

                    val expenses =
                        linkedTransactions.filter {

                            it.type ==
                                TransactionType.EXPENSE
                        }

                    val reimbursements =
                        linkedTransactions.filter {

                            it.type ==
                                TransactionType.INCOME &&

                            it.role ==
                                TransactionRole.REIMBURSEMENT
                        }

                    val totalExpenses =
                        expenses.sumOf {
                            it.amount
                        }

                    val totalReimbursements =
                        reimbursements.sumOf {
                            it.amount
                        }

                    _uiState.value =
                        FinancialEventUiState.Loaded(

                            group =
                                group,

                            expenses =
                                expenses,

                            reimbursements =
                                reimbursements,

                            totalExpenses =
                                totalExpenses,

                            totalReimbursements =
                                totalReimbursements
                        )
                }
        }
    }
}
