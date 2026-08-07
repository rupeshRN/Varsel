package com.varsel.expensetracker.ui.navigation

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.varsel.expensetracker.ui.category.CategoryScreen
import com.varsel.expensetracker.ui.category.CategoryViewModel
import com.varsel.expensetracker.ui.dashboard.DashboardScreen
import com.varsel.expensetracker.ui.dashboard.DashboardViewModel
import com.varsel.expensetracker.ui.import_statement.ImportScreen
import com.varsel.expensetracker.ui.import_statement.ImportViewModel
import com.varsel.expensetracker.ui.transaction.TransactionScreen
import com.varsel.expensetracker.ui.transaction.TransactionViewModel

sealed class Screen(val route: String) {
    object Dashboard : Screen("dashboard")
    object Transactions : Screen("transactions")
    object Import : Screen("import")
    object Categories : Screen("categories")
}

@Composable
fun NavGraph(
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Dashboard.route
    ) {
        composable(Screen.Dashboard.route) {
            val viewModel: DashboardViewModel = hiltViewModel()
            DashboardScreen(
                viewModel = viewModel,
                onNavigateToImport = { navController.navigate(Screen.Import.route) },
                onNavigateToAllTransactions = { navController.navigate(Screen.Transactions.route) },
                onNavigateToCategories = { navController.navigate(Screen.Categories.route) }
            )
        }

        composable(Screen.Transactions.route) {
            val viewModel: TransactionViewModel = hiltViewModel()
            TransactionScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Import.route) {
            val viewModel: ImportViewModel = hiltViewModel()
            ImportScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Categories.route) {
            val viewModel: CategoryViewModel = hiltViewModel()
            CategoryScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }
 }
}
