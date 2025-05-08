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

import androidx.compose.ui.semantics.*

@Composable
fun AppDrawer(
    navController: NavHostController,
    closeDrawer: () -> Unit
) {
    val backStack    by navController.currentBackStackEntryAsState()
    val currentRoute = backStack?.destination?.route

    val infoTitle = when {
        currentRoute == Screen.Home.route                               -> "Map"
        currentRoute?.startsWith("weather?") == true                    -> "Weather"
        currentRoute == Screen.RocketConfigList.route                   -> "Rocket Profiles"
        currentRoute?.startsWith("rocket_config_edit") == true          -> "Edit Rocket Profile"
        currentRoute == Screen.ConfigList.route                         -> "Weather Settings"
        currentRoute?.startsWith("config_edit") == true                 -> "Edit Weather Settings"
        currentRoute == Screen.Settings.route                           -> "Settings"
        currentRoute == Screen.LaunchSite.route                         -> "Launch Sites"
        else                                                            -> "Help"
    }

    // Precompute the info text for screen readers
    val infoText = when (infoTitle) {
        "Map"               -> "Here you can zoom around the map, set waypoints or see your rocket’s current position."
        "Weather"           -> "Displays forecast for your chosen coordinates. Temperatures in °C, precipitation in mm."
        "Rocket Profiles"   -> "Create and manage your rocket parameter sets. Tap plus to add a new profile."
        "Edit Rocket Profile" -> "Adjust thrust, payload mass, and stages. Save when you’re done."
        "Weather Settings"  -> "Manage your saved weather configurations. Select one to apply or edit it."
        "Settings"          -> "From here you can choose to manage either Weather Settings or Rocket Profiles."
        "Edit Weather Settings" -> "Fine-tune your forecast preferences, like units or data sources."
        "Launch Sites"      -> "Browse available launch pads, see pad specs, and upcoming launch windows."
        else                -> "Select a screen from above to see context-sensitive tips."
    }

    val bodyScroll = rememberScrollState()

    ModalDrawerSheet(
        modifier = Modifier
            .semantics {
                // Announce the drawer as a navigation region
                contentDescription = "App navigation drawer"
            }
    ) {
        Column(
            Modifier
                .fillMaxHeight()
                .widthIn(max = 300.dp)
        ) {
            // 1) Header: handle + close
            Box(
                Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp, horizontal = 8.dp)
            ) {
                // Decorative drag handle — hidden from a11y
                Box(
                    Modifier
                        .size(width = 36.dp, height = 4.dp)
                        .clip(MaterialTheme.shapes.small)
                        .background(Color.Gray)
                        .align(Alignment.TopCenter)
                        .semantics { contentDescription = null.toString() }
                )
                IconButton(
                    onClick = closeDrawer,
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .semantics {
                            role = Role.Button
                            contentDescription = "Close navigation drawer"
                        }
                ) {
                    Icon(Icons.Default.Close, contentDescription = null)
                }
            }

            HorizontalDivider()

            // 2) Navigation items
            Column(Modifier.padding(horizontal = 8.dp)) {
                NavigationDrawerItem(
                    label    = { Text("Map") },
                    selected = currentRoute == Screen.Home.route,
                    onClick  = {
                        navController.navigateSingleTopTo(Screen.Home.route)
                        closeDrawer()
                    },
                    modifier = Modifier
                        .padding(NavigationDrawerItemDefaults.ItemPadding)
                        .semantics {
                            role = Role.Button
                            contentDescription = "Navigate to Map"
                            stateDescription = if (selected) "Selected" else "Not selected"
                        }
                )
                NavigationDrawerItem(
                    label    = { Text("Weather") },
                    selected = currentRoute?.startsWith("weather?") == true,
                    onClick  = {
                        navController.navigateSingleTopTo(Screen.Weather.createRoute(0.0, 0.0))
                        closeDrawer()
                    },
                    modifier = Modifier
                        .padding(NavigationDrawerItemDefaults.ItemPadding)
                        .semantics {
                            role = Role.Button
                            contentDescription = "Navigate to Weather"
                            stateDescription = if (selected) "Selected" else "Not selected"
                        }
                )
                NavigationDrawerItem(
                    label    = { Text("Rocket Profiles") },
                    selected = currentRoute == Screen.RocketConfigList.route,
                    onClick  = {
                        navController.navigateSingleTopTo(Screen.RocketConfigList.route)
                        closeDrawer()
                    },
                    modifier = Modifier
                        .padding(NavigationDrawerItemDefaults.ItemPadding)
                        .semantics {
                            role = Role.Button
                            contentDescription = "Navigate to Rocket Profiles"
                            stateDescription = if (selected) "Selected" else "Not selected"
                        }
                )
                NavigationDrawerItem(
                    label    = { Text("Weather Settings") },
                    selected = currentRoute == Screen.ConfigList.route,
                    onClick  = {
                        navController.navigateSingleTopTo(Screen.ConfigList.route)
                        closeDrawer()
                    },
                    modifier = Modifier
                        .padding(NavigationDrawerItemDefaults.ItemPadding)
                        .semantics {
                            role = Role.Button
                            contentDescription = "Navigate to Weather Settings"
                            stateDescription = if (selected) "Selected" else "Not selected"
                        }
                )
                NavigationDrawerItem(
                    label    = { Text("Settings") },
                    selected = currentRoute == Screen.Settings.route,
                    onClick  = {
                        navController.navigateSingleTopTo(Screen.Settings.route)
                        closeDrawer()
                    },
                    modifier = Modifier
                        .padding(NavigationDrawerItemDefaults.ItemPadding)
                        .semantics {
                            role = Role.Button
                            contentDescription = "Navigate to Settings"
                            stateDescription = if (selected) "Selected" else "Not selected"
                        }
                )
                NavigationDrawerItem(
                    label    = { Text("Launch Sites") },
                    selected = currentRoute == Screen.LaunchSite.route,
                    onClick  = {
                        navController.navigateSingleTopTo(Screen.LaunchSite.route)
                        closeDrawer()
                    },
                    modifier = Modifier
                        .padding(NavigationDrawerItemDefaults.ItemPadding)
                        .semantics {
                            role = Role.Button
                            contentDescription = "Navigate to Launch Sites"
                            stateDescription = if (selected) "Selected" else "Not selected"
                        }
                )
            }

            HorizontalDivider(Modifier.padding(vertical = 8.dp))

            // 3) Info title as a heading
            Text(
                text = infoTitle,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier
                    .padding(start = 16.dp, bottom = 4.dp)
                    .semantics { heading() }
            )

            // 4) Info body
            Column(
                Modifier
                    .weight(1f)
                    .verticalScroll(bodyScroll)
                    .padding(horizontal = 16.dp)
                    .semantics {
                        contentDescription = infoText
                    }
            ) {
                Text(infoText)
                Spacer(Modifier.height(8.dp))
            }
        }
    }
}
