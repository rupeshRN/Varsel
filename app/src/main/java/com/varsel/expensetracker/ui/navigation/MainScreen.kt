package com.varsel.expensetracker.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
fun MainScreen() {

    var currentDestination by remember {

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

    ) { paddingValues ->

        Box(
            modifier = Modifier
                .fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {

            when (currentDestination) {

                AppDestination.Home -> {

                    Text("Home Screen")
                }

                AppDestination.Transactions -> {

                    Text("Transactions Screen")
                }

                AppDestination.Reports -> {

                    Text("Reports Screen")
                }

                AppDestination.More -> {

                    Text("More Screen")
                }
            }
        }
    }
}
