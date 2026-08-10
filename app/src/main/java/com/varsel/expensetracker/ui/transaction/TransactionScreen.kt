package com.varsel.expensetracker.ui.transaction

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.varsel.expensetracker.ui.dashboard.components.RecentTransactionCard
import com.varsel.expensetracker.ui.transaction.components.MonthlySummaryCard
import com.varsel.expensetracker.ui.transaction.components.MonthSelector
import com.varsel.expensetracker.ui.transaction.components.TransactionFilterBar
import com.varsel.expensetracker.ui.transaction.components.TransactionHeader

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionScreen(
    viewModel: TransactionViewModel,
    onBackClick: () -> Unit
) {

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val months = listOf(
        "Jan",
        "Feb",
        "Mar",
        "Apr",
        "May",
        "Jun",
        "Jul",
        "Aug",
        "Sep",
        "Oct",
        "Nov",
        "Dec"
    )

    val filters = TransactionFilter.entries

    Scaffold(

        topBar = {

            CenterAlignedTopAppBar(

                title = {

                    Text("Transactions")

                },

                navigationIcon = {

                    IconButton(

                        onClick = onBackClick

                    ) {

                        Icon(

                            imageVector = Icons.Default.ArrowBack,

                            contentDescription = "Back"

                        )

                    }

                }

            )

        }

    ) { padding ->

        if (uiState.isLoading) {

            Box(

                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),

                contentAlignment = Alignment.Center

            ) {

                CircularProgressIndicator()

            }

        } else {

            LazyColumn(

                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),

                contentPadding = PaddingValues(16.dp),

                verticalArrangement = Arrangement.spacedBy(16.dp)

            ) {
                                item {

                    TransactionHeader(

                        transactionCount =
                            uiState.transactions.size

                    )

                }

                item {

                    MonthlySummaryCard(

                        monthTitle =
                            uiState.selectedMonth,

                        income = 0.0,

                        expense = 0.0

                    )

                }

                item {

                    MonthSelector(

                        months = months,

                        selectedMonth =
                            uiState.selectedMonth,

                        onMonthSelected = {

                            viewModel.updateSelectedMonth(it)

                        }

                    )

                }

                item {

                    TransactionFilterBar(

                        filters = filters,

                        selectedFilter =
                            uiState.selectedFilter,

                        onFilterSelected = {

                            viewModel.updateFilter(it)

                        }

                    )

                }

                item {

                    OutlinedTextField(

                        value =
                            uiState.searchQuery,

                        onValueChange = {

                            viewModel.updateSearchQuery(it)

                        },

                        modifier =
                            Modifier.fillMaxWidth(),

                        leadingIcon = {

                            Icon(

                                imageVector =
                                    Icons.Default.Search,

                                contentDescription = null

                            )

                        },

                        placeholder = {

                            Text(
                                "Search transactions"
                            )

                        },

                        singleLine = true

                    )

                }

                if (uiState.transactions.isEmpty()) {

                    item {

                        Box(

                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 48.dp),

                            contentAlignment =
                                Alignment.Center

                        ) {

                            Text(
                                text = "No transactions found."
                            )

                        }

                    }

                } else {

                    items(

                        items = uiState.transactions,

                        key = { it.id }

                    ) { transaction ->

                        RecentTransactionCard(

                            transaction = transaction

                        )

                    }

                }

                            }

        }

    }

}
