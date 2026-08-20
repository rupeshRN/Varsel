package com.varsel.expensetracker.ui.transaction

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import com.varsel.expensetracker.ui.transaction.components.BottomActionBar
import com.varsel.expensetracker.ui.transaction.components.CategorySection
import com.varsel.expensetracker.ui.transaction.components.DescriptionSection
import com.varsel.expensetracker.ui.transaction.components.TransactionInfoSection
import com.varsel.expensetracker.ui.transaction.components.TransactionLinkSection
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionDetailScreen(

    transactionId:
        Long,

    viewModel:
        TransactionDetailViewModel,

    onBackClick:
        () -> Unit,

    onFinancialEventClick:
        (String) -> Unit

) {

    val uiState by
        viewModel.uiState
            .collectAsStateWithLifecycle()

    val saveCompleted by
        viewModel.saveCompleted
            .collectAsStateWithLifecycle()

    val scrollState =
        rememberScrollState()

    //--------------------------------------------------
    // Load transaction
    //--------------------------------------------------

    LaunchedEffect(transactionId) {

        viewModel.loadTransaction(
            transactionId
        )
    }

    //--------------------------------------------------
    // Handle successful save
    //--------------------------------------------------

    LaunchedEffect(saveCompleted) {

        if (
            saveCompleted
        ) {

            viewModel.consumeSaveCompleted()

            onBackClick()
        }
    }

    Scaffold(

        topBar = {

            CenterAlignedTopAppBar(

                title = {

                    Text(
                        "Transaction Details"
                    )
                },

                navigationIcon = {

                    IconButton(

                        onClick =
                            onBackClick
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

            if (
                state != null
            ) {

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
                    .verticalScroll(
                        scrollState
                    )
                    .padding(24.dp),

            verticalArrangement =
                Arrangement.spacedBy(
                    16.dp
                )
        ) {

            when (
                val state =
                    uiState
            ) {

                //--------------------------------------------------
                // Loading
                //--------------------------------------------------

                TransactionDetailUiState.Loading -> {

                    Text(
                        "Loading..."
                    )
                }

                //--------------------------------------------------
                // Error
                //--------------------------------------------------

                is TransactionDetailUiState.Error -> {

                    Text(
                        state.message
                    )
                }

                //--------------------------------------------------
                // Loaded
                //--------------------------------------------------

                is TransactionDetailUiState.Loaded -> {

                    val transaction =
                        state.transaction

                    //--------------------------------------------------
                    // Description
                    //--------------------------------------------------

                    DescriptionSection(

                        description =
                            state
                                .editableDescription,

                        onDescriptionChanged =
                            viewModel::updateDescription
                    )

                    //--------------------------------------------------
                    // Category
                    //--------------------------------------------------

                    CategorySection(

                        selectedCategory =
                            state
                                .selectedCategory,

                        onCategorySelected =
                            viewModel::updateCategory
                    )

                    //--------------------------------------------------
                    // Transaction Role
                    //--------------------------------------------------

                    TransactionRoleSection(

                        transactionType =
                            transaction.type,

                        selectedRole =
                            state
                                .selectedRole,

                        onRoleSelected =
                            viewModel::updateRole
                    )

                    //--------------------------------------------------
                    // Financial Event
                    //
                    // IMPORTANT:
                    //
                    // There is no longer a "Possible Transactions
                    // to Link" section here.
                    //
                    // Financial Event creation/management is now
                    // the responsibility of the Financial Event
                    // section.
                    //--------------------------------------------------

                    TransactionLinkSection(

                        linkedTransactions =
                            state
                                .linkedTransactions,

                        transactionLinkGroup =
                            state
                                .transactionLinkGroup,

                        showCreateGroupPrompt =
                            state
                                .showCreateGroupPrompt,

                        isSavingGroup =
                            state
                                .isSavingGroup,

                        categories =
                            state
                                .categories,

                        onManageFinancialEvent = {

                            val linkId =
                                transaction
                                    .transactionLinkId

                            if (
                                linkId != null
                            ) {

                                onFinancialEventClick(
                                    linkId
                                )
                            }
                        },

                        onShowCreateFinancialEvent =
                            viewModel::showCreateGroupPrompt,

                        onUnlink =
                            viewModel::unlinkCurrentTransaction,

                        onDismissCreateGroupPrompt =
                            viewModel::dismissCreateGroupPrompt,

                        onCreateReportGroup =
                            viewModel::createReportGroup
                    )

                    //--------------------------------------------------
                    // Transaction Information
                    //--------------------------------------------------

                    TransactionInfoSection(

                        amount =
                            "₹%.2f"
                                .format(
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

                    //--------------------------------------------------
                    // Bottom spacing
                    //--------------------------------------------------

                    Spacer(

                        modifier =
                            Modifier.padding(
                                bottom = 24.dp
                            )
                    )
                }
            }
        }
    }
}
