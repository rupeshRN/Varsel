package com.varsel.expensetracker.ui.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.unit.dp

@Composable
fun AppShell(
    content: @Composable (
        currentDestination: AppDestination,
        innerPadding: PaddingValues
    ) -> Unit
) {

    var currentDestination by rememberSaveable {
        mutableStateOf<AppDestination>(
            AppDestination.Home
        )
    }

    Scaffold(

        bottomBar = {

            BottomNavigationBar(

                currentDestination = currentDestination,

                onDestinationSelected = {

                    currentDestination = it
                }
            )
        }

    ) { padding ->

        content(
            currentDestination,
            padding
        )
    }
}
