package no.uio.ifi.in2000.met2025.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState

@Composable
fun AppDrawer(
    navController: NavHostController,
    closeDrawer: () -> Unit
) {
    val backStack    by navController.currentBackStackEntryAsState()
    val currentRoute = backStack?.destination?.route

    // Map route → display name
    val infoTitle = when {
        currentRoute == Screen.Home.route                               -> "Map"
        currentRoute?.startsWith("weather?") == true             -> "Weather"
        currentRoute == Screen.RocketConfigList.route                   -> "Rocket Profiles"
        currentRoute?.startsWith("rocket_config_edit") == true   -> "Edit Rocket Profile"
        currentRoute == Screen.ConfigList.route                         -> "Weather Settings"
        currentRoute?.startsWith("config_edit") == true          -> "Edit Weather Settings"
        currentRoute == Screen.LaunchSite.route                         -> "Launch Sites"
        else                                                            -> "Help"
    }

    // Scroll state only for the BODY
    val bodyScroll = rememberScrollState()

    ModalDrawerSheet {
        Column(
            Modifier
                .fillMaxHeight()
                .widthIn(max = 300.dp)
        ) {
            // 1) Fixed header: handle + close
            Box(
                Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp, horizontal = 8.dp)
            ) {
                Box(
                    Modifier
                        .size(width = 36.dp, height = 4.dp)
                        .clip(MaterialTheme.shapes.small)
                        .background(Color.Gray)
                        .align(Alignment.TopCenter)
                )
                IconButton(
                    onClick = closeDrawer,
                    modifier = Modifier.align(Alignment.CenterEnd)
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Close drawer")
                }
            }

            HorizontalDivider()

            // 2) Fixed nav items
            Column(Modifier.padding(horizontal = 8.dp)) {
                NavigationDrawerItem(
                    label    = { Text("Map") },
                    selected = currentRoute == Screen.Home.route,
                    onClick  = { navController.navigateSingleTopTo(Screen.Home.route); closeDrawer() },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )
                NavigationDrawerItem(
                    label    = { Text("Weather") },
                    selected = currentRoute?.startsWith("weather?") == true,
                    onClick  = { navController.navigateSingleTopTo(Screen.Weather.createRoute(0.0,0.0)); closeDrawer() },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )
                NavigationDrawerItem(
                    label    = { Text("Rocket Profiles") },
                    selected = currentRoute == Screen.RocketConfigList.route,
                    onClick  = { navController.navigateSingleTopTo(Screen.RocketConfigList.route); closeDrawer() },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )
                NavigationDrawerItem(
                    label    = { Text("Weather Settings") },
                    selected = currentRoute == Screen.ConfigList.route,
                    onClick  = { navController.navigateSingleTopTo(Screen.ConfigList.route); closeDrawer() },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )
                NavigationDrawerItem(
                    label    = { Text("Launch Sites") },
                    selected = currentRoute == Screen.LaunchSite.route,
                    onClick  = { navController.navigateSingleTopTo(Screen.LaunchSite.route); closeDrawer() },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )
            }

            HorizontalDivider(Modifier.padding(vertical = 8.dp))

            // 3) Fixed info title
            Text(
                text = infoTitle,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier
                    .padding(start = 16.dp, bottom = 4.dp)
            )

            // 4) Scrollable info body
            Column(
                Modifier
                    .weight(1f)
                    .verticalScroll(bodyScroll)
                    .padding(horizontal = 16.dp)
            ) {
                when (infoTitle) {
                    "Map" -> {
                        HorizontalDivider()
                        Text("Here you can zoom around the map, set waypoints or see your rocket’s current position.")
                    }
                    "Weather" -> {
                        HorizontalDivider()
                        Text("Displays forecast for your chosen coordinates. Temperatures in °C, precipitation in mm.")
                    }
                    "Rocket Profiles" -> {
                        HorizontalDivider()
                        Text("Create and manage your rocket parameter sets. Tap ➕ to add a new profile.")
                    }
                    "Edit Rocket Profile" -> {
                        HorizontalDivider()
                        Text("Adjust thrust, payload mass, and stages. Save when you’re done.")
                    }
                    "Weather Settings" -> {
                        HorizontalDivider()
                        Text("Manage your saved weather configurations. Select one to apply or edit it.")
                    }
                    "Edit Weather Settings" -> {
                        HorizontalDivider()
                        Text("Fine-tune your forecast preferences, like units or data sources.")
                    }
                    "Launch Sites" -> {
                        HorizontalDivider()
                        Text("Browse available launch pads, see pad specs, and upcoming launch windows.")
                    }
                    else -> {
                        HorizontalDivider()
                        Text("Select a screen from above to see context-sensitive tips.")
                    }
                }

                Spacer(Modifier.height(8.dp))

            }
        }
    }
}