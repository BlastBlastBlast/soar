package no.uio.ifi.in2000.met2025.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState

@Composable
fun AppDrawer(
    navController: NavController,
    closeDrawer: () -> Unit
) {
    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route

    fun navTo(route: String) {
        navController.navigate(route) {
            popUpTo(navController.graph.startDestinationId)
            launchSingleTop = true
        }
        closeDrawer()
    }

    ModalDrawerSheet {
        listOf(
            "Home" to Screen.Home.route,
            "Weather" to Screen.Weather.createRoute(0.0,0.0),
            "Launch Sites" to Screen.LaunchSite.route,
            "Rocket Configs" to Screen.RocketConfigList.route
        ).forEach { (label, route) ->
            NavigationDrawerItem(
                label    = { Text(label) },
                selected = currentRoute == route,
                onClick  = { navTo(route) },
                modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
            )
        }
    }
}