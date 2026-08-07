package com.varsel.expensetracker.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.hilt.navigation.compose.hiltViewModel

/**
 * Sealed hierarchy defining type-safe navigation routes, titles, and Material icons.
 */
sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Dashboard : Screen("dashboard", "Dashboard", Icons.Default.Home)
    object Import : Screen("import", "Import", Icons.Default.UploadFile)
    object Categories : Screen("categories", "Categories", Icons.Default.Category)
}

/**
 * List of primary screens accessible via the bottom navigation bar.
 */
val bottomNavScreens = listOf(
    Screen.Dashboard,
    Screen.Import,
    Screen.Categories
)

/**
 * Main application navigation container wrapping the Material 3 NavigationBar and NavHost.
 */
@Composable
fun MainNavigation(
    navController: NavHostController = rememberNavController()
) {
    // Observe current backstack entry to highlight the correct active bottom nav icon
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Scaffold(
        bottomBar = {
            NavigationBar {
                bottomNavScreens.forEach { screen ->
                    NavigationBarItem(
                        icon = { Icon(screen.icon, contentDescription = screen.title) },
                        label = { Text(screen.title) },
                        selected = currentRoute == screen.route,
                        onClick = {
                            if (currentRoute != screen.route) {
                                navController.navigate(screen.route) {
                                    // Pop up to root destination to prevent building deep backstacks
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    // Avoid multiple copies of the same screen on backstack
                                    launchSingleTop = true
                                    // Restore screen state when reselecting tab
                                    restoreState = true
                                }
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Dashboard.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            // Dashboard & Financial Summary Route
            composable(Screen.Dashboard.route) {
                // DashboardScreen component is added in File 22
                DashboardScreen()
            }

            // PDF/Image Statement Parsing Route
            composable(Screen.Import.route) {
                // ImportScreen component is added in File 23
                ImportScreen()
            }

            // Category & Budgeting Rules Route
            composable(Screen.Categories.route) {
                // CategoryScreen component is added in File 24
                CategoryScreen()
            }
        }
    }
}
