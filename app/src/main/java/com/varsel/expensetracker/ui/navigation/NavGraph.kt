package com.varsel.expensetracker.ui.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.varsel.expensetracker.ui.category.CategoryScreen
import com.varsel.expensetracker.ui.dashboard.DashboardScreen
import com.varsel.expensetracker.ui.import_statement.ImportScreen
import com.varsel.expensetracker.ui.more.MoreScreen
import com.varsel.expensetracker.ui.transaction.TransactionScreen

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
                onNavigateToAllTransactions = {
                    navController.navigate(AppDestination.Transactions.route)
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

            Text("Reports - Coming Soon")
        }

        composable(AppDestination.More.route) {

            MoreScreen(

                onImportClick = {
                    navController.navigate("import_statement")
                },

                onCategoriesClick = {
                    navController.navigate("categories")
                },

                onLearningRulesClick = {
                    // TODO Phase C
                },

                onAppearanceClick = {
                    // TODO Phase D
                },

                onDeveloperClick = {
                    // TODO Phase E
                },

                onAboutClick = {
                    // TODO Phase F
                }
            )
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
