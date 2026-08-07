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
sealed class Screen(val route: String) {
    object Dashboard : Screen("dashboard")
    object Import : Screen("import")
    object Category : Screen("category")
}

@Composable
fun SetupNavGraph(
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Dashboard.route
    ) {
        // INLINE FIX: Correctly imported and resolved DashboardScreen reference
        composable(Screen.Dashboard.route) {
            val viewModel: DashboardViewModel = hiltViewModel()
            DashboardScreen(
                viewModel = viewModel,
                onNavigateToImport = { navController.navigate(Screen.Import.route) },
                onNavigateToCategories = { navController.navigate(Screen.Category.route) }
            )
        }

        // INLINE FIX: Correctly imported and resolved ImportScreen reference
        composable(Screen.Import.route) {
            val viewModel: ImportViewModel = hiltViewModel()
            ImportScreen(
                viewModel = viewModel,
                onBackClick = { navController.popBackStack() }
            )
        }

        // INLINE FIX: Correctly imported and resolved CategoryScreen reference
        composable(Screen.Category.route) {
            val viewModel: CategoryViewModel = hiltViewModel()
            CategoryScreen(
                viewModel = viewModel,
                onBackClick = { navController.popBackStack() }
            )
        }
    }
}
