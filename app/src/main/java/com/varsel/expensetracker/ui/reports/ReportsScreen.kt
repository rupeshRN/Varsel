package com.varsel.expensetracker.ui.reports

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.varsel.expensetracker.ui.reports.components.ReportsHeader

/**
 * Production Reports screen.
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
        onFilterClick = {
            /*
             * The filter sheet will be connected in the next step.
             */
        }
    )
}

@Composable
private fun ReportsScreenContent(
    uiState: ReportsUiState,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onFilterClick: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize()
    ) {

        when {

            uiState.isLoading -> {

                CircularProgressIndicator(
                    modifier = Modifier.align(
                        Alignment.Center
                    )
                )
            }

            uiState.errorMessage != null -> {

                Text(
                    text = uiState.errorMessage,
                    modifier = Modifier.align(
                        Alignment.Center
                    )
                )
            }

            else -> {

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement =
                        Arrangement.spacedBy(16.dp)
                ) {

                    ReportsHeader(
                        selectedMonth =
                            uiState.selectedMonth,
                        accountFilterLabel =
                            uiState.accountFilterLabel,
                        hasActiveAccountFilter =
                            !uiState.isAllAccountsSelected,
                        onPreviousMonth =
                            onPreviousMonth,
                        onNextMonth =
                            onNextMonth,
                        onFilterClick =
                            onFilterClick
                    )

                    /*
                     * Remaining report sections will be
                     * added here progressively.
                     */
                }
            }
        }
    }
}
