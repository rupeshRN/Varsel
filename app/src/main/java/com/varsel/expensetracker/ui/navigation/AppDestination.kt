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
