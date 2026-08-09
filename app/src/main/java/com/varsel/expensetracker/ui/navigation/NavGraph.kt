package com.varsel.expensetracker.ui.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.varsel.expensetracker.ui.dashboard.DashboardScreen
import com.varsel.expensetracker.ui.more.MoreScreen
import com.varsel.expensetracker.ui.reports.ReportsScreen
import com.varsel.expensetracker.ui.transaction.TransactionScreen

@Composable
fun NavGraph(
    navController: NavHostController
) {

    val navigationViewModel: NavigationViewModel =
        hiltViewModel()

    val currentDestination by
        navigationViewModel
            .currentDestination
            .collectAsStateWithLifecycle()

    AppShell(

        currentDestination = currentDestination,

        onDestinationSelected = {

            navigationViewModel.navigateTo(it)
        }

    ) { innerPadding ->

        when (currentDestination) {

            AppDestination.Home -> {

                DashboardScreen(

                    viewModel = hiltViewModel(),

                    onNavigateToImport = {
                        // Will open from More screen later
                    },

                    onNavigateToAllTransactions = {

                        navigationViewModel.navigateTo(
                            AppDestination.Transactions
                        )
                    },

                    onNavigateToCategories = {
                        // Will open from More screen later
                    }
                )
            }

            AppDestination.Transactions -> {

                TransactionScreen(

                    viewModel = hiltViewModel(),

                    onBackClick = {
                        navigationViewModel.navigateTo(
                            AppDestination.Home
                        )
                    }
                )
            }

            AppDestination.Reports -> {

                ReportsScreen()
            }

            AppDestination.More -> {

                MoreScreen()
            }
        }
    }
}
