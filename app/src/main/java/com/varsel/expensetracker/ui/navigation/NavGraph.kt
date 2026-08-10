package com.varsel.expensetracker.ui.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.varsel.expensetracker.ui.category.CategoryScreen
import com.varsel.expensetracker.ui.dashboard.DashboardScreen
import com.varsel.expensetracker.ui.import_statement.ImportScreen
import com.varsel.expensetracker.ui.transaction.TransactionScreen
import androidx.compose.ui.Modifier

@Composable
fun NavGraph(
    navController: NavHostController,
    innerPadding: PaddingValues
) {

    NavHost(
        navController = navController,
        startDestination = AppDestination.Home.route,
        modifier = Modifier.padding(innerPadding)
    ) {

        composable(AppDestination.Home.route) {

            DashboardScreen(
                viewModel = hiltViewModel(),

                onNavigateToImport = {
                    navController.navigate("import_statement")
                },

                onNavigateToAllTransactions = {
                    navController.navigate(AppDestination.Transactions.route)
                },

                onNavigateToCategories = {
                    navController.navigate("categories")
                }
            )
        }

        composable(AppDestination.Transactions.route) {

            TransactionScreen(
                viewModel = hiltViewModel(),
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }

        composable(AppDestination.Reports.route) {

            androidx.compose.material3.Text("Reports - Coming Soon")
        }

        composable(AppDestination.More.route) {

            androidx.compose.material3.Text("More - Coming Soon")
        }

        composable("categories") {

            CategoryScreen(
                viewModel = hiltViewModel(),
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }

        composable("import_statement") {

            ImportScreen(
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }
    }
}
