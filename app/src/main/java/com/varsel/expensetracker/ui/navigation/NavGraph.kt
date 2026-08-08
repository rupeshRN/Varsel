package com.varsel.expensetracker.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.varsel.expensetracker.ui.category.CategoryScreen
import com.varsel.expensetracker.ui.dashboard.DashboardScreen
import com.varsel.expensetracker.ui.import_statement.ImportScreen
import com.varsel.expensetracker.ui.transaction.TransactionScreen

sealed class Screen(val route: String) {
    object Dashboard : Screen("dashboard")
    object Transactions : Screen("transactions")
    object Categories : Screen("categories")
    object ImportStatement : Screen("import_statement")
}

@Composable
fun NavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Screen.Dashboard.route
    ) {
        composable(Screen.Dashboard.route) {
            DashboardScreen(
                onNavigateToTransactions = { navController.navigate(Screen.Transactions.route) },
                onNavigateToImport = { navController.navigate(Screen.ImportStatement.route) },
                onNavigateToCategories = { navController.navigate(Screen.Categories.route) }
            )
        }
        composable(Screen.Transactions.route) {
            TransactionScreen(
                onBackClick = { navController.popBackStack() }
            )
        }
        composable(Screen.Categories.route) {
            CategoryScreen(
                onBackClick = { navController.popBackStack() }
            )
        }
        composable(Screen.ImportStatement.route) {
            ImportScreen(
                onBackClick = { navController.popBackStack() }
            )
        }
    }
}
