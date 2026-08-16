package com.varsel.expensetracker.ui.transaction

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.varsel.expensetracker.domain.model.TransactionRole
import com.varsel.expensetracker.ui.transaction.components.BottomActionBar
import com.varsel.expensetracker.ui.transaction.components.CategorySection
import com.varsel.expensetracker.ui.transaction.components.DescriptionSection
import com.varsel.expensetracker.ui.transaction.components.TransactionInfoSection
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionDetailScreen(

    transactionId: Long,

    viewModel: TransactionDetailViewModel,

    onBackClick: () -> Unit

) {

    val uiState by
        viewModel.uiState.collectAsStateWithLifecycle()

    val saveCompleted by
        viewModel.saveCompleted.collectAsStateWithLifecycle()

    LaunchedEffect(transactionId) {

        viewModel.loadTransaction(transactionId)
    }

    LaunchedEffect(saveCompleted) {

        if (saveCompleted) {

            viewModel.consumeSaveCompleted()

            onBackClick()
        }
    }

    Scaffold(

        topBar = {

            CenterAlignedTopAppBar(

                title = {
                    Text("Transaction Details")
                },

                navigationIcon = {

                    IconButton(
                        onClick = onBackClick
                    ) {

                        Icon(
                            imageVector =
                                Icons.Default.ArrowBack,

                            contentDescription =
                                "Back"
                        )
                    }
                }
            )
        },

        bottomBar = {

            val state =
                uiState as?
                    TransactionDetailUiState.Loaded

            if (state != null) {

                BottomActionBar(

                    onDeleteClick = {

                        // E2.4
                    },

                    onSaveClick =
                        viewModel::saveChanges,

                    saveEnabled =
                        state.hasChanges &&
                        !state.isSaving
                )
            }
        }

    ) { padding ->

        Column(

            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(24.dp),

            verticalArrangement =
                Arrangement.spacedBy(16.dp)
        ) {

            when (val state = uiState) {

                TransactionDetailUiState.Loading -> {

                    Text("Loading...")
                }

                is TransactionDetailUiState.Error -> {

                    Text(state.message)
                }

                is TransactionDetailUiState.Loaded -> {

                    val transaction =
                        state.transaction

                    DescriptionSection(

                        description =
                            state.editableDescription,

                        onDescriptionChanged =
                            viewModel::updateDescription
                    )

                    CategorySection(

                        selectedCategory =
                            state.selectedCategory,

                        onCategorySelected =
                            viewModel::updateCategory
                    )

                    TransactionRoleSection(

                        transactionType =
                            transaction.type,

                        selectedRole =
                            state.selectedRole,

                        onRoleSelected =
                            viewModel::updateRole
                    )

                    TransactionInfoSection(

                        amount =
                            "₹%.2f".format(
                                transaction.amount
                            ),

                        date =
                            SimpleDateFormat(
                                "dd MMM yyyy",
                                Locale.ENGLISH
                            ).format(
                                Date(
                                    transaction.dateTimestamp
                                )
                            ),

                        type =
                            transaction.type.name
                    )
                }
            }
        }
    }
}
