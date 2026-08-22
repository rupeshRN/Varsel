package com.varsel.expensetracker.ui.reports

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

/**
 * Production Reports screen entry point.
 *
 * This file intentionally stays small.
 * The actual visual sections will be split into dedicated components.
 */
@Composable
fun ReportsScreen(
    viewModel: ReportsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    ReportsScreenContent(
        uiState = uiState,
        onPreviousMonth = viewModel::previousMonth,
        onNextMonth = viewModel::nextMonth,
        onFlowSelected = viewModel::selectFlow,
        onExpenseCategorySelected = viewModel::selectExpenseCategory,
        onIncomeCategorySelected = viewModel::selectIncomeCategory,
        onRetry = viewModel::retry
    )
}

/**
 * Stateless Reports screen content.
 *
 * Keeping this separate makes the screen easier to preview and test.
 * ViewModel wiring stays in the public ReportsScreen entry point.
 */
@Composable
private fun ReportsScreenContent(
    uiState: ReportsUiState,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onFlowSelected: (ReportsFlow) -> Unit,
    onExpenseCategorySelected: (String?) -> Unit,
    onIncomeCategorySelected: (String?) -> Unit,
    onRetry: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        when {
            uiState.isLoading -> {
                CircularProgressIndicator()
            }

            uiState.errorMessage != null -> {
                Text(uiState.errorMessage)
            }

            else -> {
                Text("Reports")
            }
        }
    }
}
