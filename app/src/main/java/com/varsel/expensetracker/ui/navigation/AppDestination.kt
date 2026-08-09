package com.varsel.expensetracker.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Assessment
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.ListAlt
import androidx.compose.material.icons.outlined.MoreHoriz
import androidx.compose.material.icons.outlined.UploadFile
import androidx.compose.ui.graphics.vector.ImageVector

sealed class AppDestination(

    val route: String,
    val title: String,
    val icon: ImageVector
) {

    data object Home : AppDestination(
        "home",
        "Home",
        Icons.Outlined.Home
    )

    data object Transactions : AppDestination(
        "transactions",
        "Transactions",
        Icons.Outlined.ListAlt
    )

    data object Reports : AppDestination(
        "reports",
        "Reports",
        Icons.Outlined.Assessment
    )

    data object More : AppDestination(
        "more",
        "More",
        Icons.Outlined.MoreHoriz
    )

    companion object {

        val bottomBarItems = listOf(
            Home,
            Transactions,
            Reports,
            More
        )
    }
}
