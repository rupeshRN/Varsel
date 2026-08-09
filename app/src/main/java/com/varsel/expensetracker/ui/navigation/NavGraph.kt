package com.varsel.expensetracker.ui.navigation

import androidx.compose.runtime.Composable
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

    AppShell(

        currentDestination = AppDestination.Home,

        onDestinationSelected = {
            // Temporary
            // Navigation logic comes in Milestone 2
        }

    ) { padding ->

        when (AppDestination.Home) {

            AppDestination.Home -> {

                DashboardScreen(

                    viewModel = hiltViewModel(),

                    onNavigateToImport = {
                        // handled later inside More
                    },

                    onNavigateToAllTransactions = {
                        // handled by bottom navigation
                    },

                    onNavigateToCategories = {
                        // handled later inside More
                    }
                )
            }

            AppDestination.Transactions -> {

                TransactionScreen(

                    viewModel = hiltViewModel(),

                    onBackClick = {}
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
