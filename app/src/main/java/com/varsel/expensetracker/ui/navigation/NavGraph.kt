package com.varsel.expensetracker.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.varsel.expensetracker.ui.category.CategoryScreen
import com.varsel.expensetracker.ui.dashboard.DashboardScreen
import com.varsel.expensetracker.ui.import_statement.ImportScreen
import com.varsel.expensetracker.ui.transaction.TransactionScreen
import androidx.hilt.navigation.compose.hiltViewModel

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
    ) 

    composable(Screen.Dashboard.route) {
    DashboardScreen(
        viewModel = hiltViewModel(),
        onNavigateToImport = {
            navController.navigate(Screen.ImportStatement.route)
        },
        onNavigateToAllTransactions = {
            navController.navigate(Screen.Transactions.route)
        },
        onNavigateToCategories = {
            navController.navigate(Screen.Categories.route)
        }
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
