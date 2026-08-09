package com.varsel.expensetracker.ui.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable

@Composable
fun AppShell(

    currentDestination: AppDestination,

    onDestinationSelected: (AppDestination) -> Unit,

    content: @Composable (
        PaddingValues
    ) -> Unit
) {

    Scaffold(

        bottomBar = {

            BottomNavigationBar(

                currentDestination = currentDestination,

                onDestinationSelected = onDestinationSelected
            )
        }

    ) { padding ->

        content(padding)
    }
}
