package com.varsel.expensetracker.ui.transaction

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.varsel.expensetracker.domain.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TransactionDetailViewModel @Inject constructor(

    private val transactionRepository: TransactionRepository

) : ViewModel() {

    private val _uiState =
        MutableStateFlow<TransactionDetailUiState>(
            TransactionDetailUiState.Loading
        )

    val uiState: StateFlow<TransactionDetailUiState> =
        _uiState.asStateFlow()

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

                            editableDescription = transaction.description,
                        
                            selectedCategory = transaction.category,
                        
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

            editableDescription = description,

            hasChanges =

                description !=
                    current.transaction.description ||

                current.selectedCategory !=
                    current.transaction.category

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

            selectedCategory = category,

            hasChanges =

                category !=
                    current.transaction.category ||

                current.editableDescription !=
                    current.transaction.description

        )

}

}
