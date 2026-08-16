package com.varsel.expensetracker.ui.transaction

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.varsel.expensetracker.data.repository.CustomRuleRepository
import com.varsel.expensetracker.domain.model.TransactionRole
import com.varsel.expensetracker.domain.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
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

    fun loadTransaction(
        transactionId: Long
    ) {

        viewModelScope.launch {

            val transaction =
                transactionRepository.getTransactionById(
                    transactionId
                )

            _uiState.value =

                if (transaction != null) {

                    TransactionDetailUiState.Loaded(

                        transaction = transaction,

                        editableDescription =
                            transaction.description,

                        selectedCategory =
                            transaction.category,

                        selectedRole =
                            transaction.role,

                        hasChanges = false,

                        isSaving = false
                    )

                } else {

                    TransactionDetailUiState.Error(
                        "Transaction not found."
                    )
                }
        }
    }

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
            // Role is intentionally NOT stored in the
            // learning rule. Role is transaction-specific.
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
            //
            // transactionFingerprint, accountId,
            // accountLast4 and all other immutable
            // transaction identity fields are preserved
            // by transaction.copy().
            //--------------------------------------------------

            transactionRepository.updateTransaction(
                updatedTransaction
            )

            _saveCompleted.value = true

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

    fun consumeSaveCompleted() {

        _saveCompleted.value = false
    }
}
