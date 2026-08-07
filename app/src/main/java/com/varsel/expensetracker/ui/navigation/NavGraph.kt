package com.varsel.expensetracker.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.varsel.expensetracker.ui.category.CategoryScreen
import com.varsel.expensetracker.ui.category.CategoryViewModel
import com.varsel.expensetracker.ui.dashboard.DashboardScreen
import com.varsel.expensetracker.ui.dashboard.DashboardViewModel
import com.varsel.expensetracker.ui.import_statement.ImportScreen
import com.varsel.expensetracker.ui.import_statement.ImportViewModel
import com.varsel.expensetracker.ui.transaction.TransactionScreen
import com.varsel.expensetracker.ui.transaction.TransactionViewModel

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Dashboard : Screen("dashboard", "Dashboard", Icons.Default.Home)
    object Transactions : Screen("transactions", "Transactions", Icons.Default.List)
    object Categories : Screen("categories", "Categories", Icons.Default.Category)
    object Import : Screen("import", "Import", Icons.Default.Home)
}

@Composable
fun NavGraph(
    navController: NavHostController = rememberNavController()
) {
    val bottomNavItems = listOf(
        Screen.Dashboard,
        Screen.Transactions,
        Screen.Categories
    )

    Scaffold(
        bottomBar = {
            val navBackStackEntry = navController.currentBackStackEntryAsState()
            val currentRoute = navBackStackEntry.value?.destination?.route

            // Show bottom navigation bar only on primary tabs
            if (currentRoute in bottomNavItems.map { it.route }) {
                NavigationBar {
                    bottomNavItems.forEach { screen ->
                        NavigationBarItem(
                            icon = { Icon(screen.icon, contentDescription = screen.title) },
                            label = { Text(screen.title) },
                            selected = currentRoute == screen.route,
                            onClick = {
                                if (currentRoute != screen.route) {
                                    navController.navigate(screen.route) {
                                        popUpTo(navController.graph.startDestinationId) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Dashboard.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Dashboard.route) {
                val viewModel: DashboardViewModel = hiltViewModel()
                DashboardScreen(
                    viewModel = viewModel,
                    onNavigateToImport = { navController.navigate("import") },
                    onNavigateToCategories = { navController.navigate(Screen.Categories.route) },
                    onNavigateToAllTransactions = { navController.navigate(Screen.Transactions.route) },
                    onNavigateToSettings = { /* Implement settings navigation here */ }
                )
            }

            composable(Screen.Transactions.route) {
                val viewModel: TransactionViewModel = hiltViewModel()
                TransactionScreen(viewModel = viewModel)
            }

            composable(Screen.Categories.route) {
                val viewModel: CategoryViewModel = hiltViewModel()
                CategoryScreen(
                    viewModel = viewModel,
                    onBackClick = { navController.popBackStack() }
                )
            }

            composable("import") {
                val viewModel: ImportViewModel = hiltViewModel()
                ImportScreen(
                    viewModel = viewModel,
                    onBackClick = { navController.popBackStack() }
                )
            }
        }
    }
}
