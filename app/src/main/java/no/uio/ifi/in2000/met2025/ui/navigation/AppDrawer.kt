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
        currentRoute == Screen.Maps.route                               -> "Map"
        currentRoute?.startsWith("weather/") == true                    -> "Weather"
        currentRoute == Screen.RocketConfigList.route                   -> "Rocket Profiles"
        currentRoute?.startsWith("rocket_config_edit") == true          -> "Edit Rocket Profile"
        currentRoute == Screen.WeatherConfigList.route                         -> "Weather Settings"
        currentRoute?.startsWith("weather_edit") == true                 -> "Edit Weather Settings"
        currentRoute == Screen.Configs.route                           -> "Config"
        currentRoute == Screen.LaunchSite.route                         -> "Launch Sites"
        else                                                            -> "Help"
    }

    val infoText = when (infoTitle) {
        "Map" -> "Explore the map: set and save launch-site markers, navigate between sites, " +
                "view elevation and coordinates, pan and zoom, fetch and compute weather data " +
                "for the selected location, and calculate ballistic trajectories using LocationForecast 2.0 " +
                "and Isobaric GRIB-2 data."

        "Weather" -> "Browse a day-pager of hourly launch windows. For each window, view the hourly forecast, " +
                "fetch and display three-hour isobaric wind data on demand, and see which windows " +
                "already have wind data loaded. Use the filter menu to apply your Weather Settings thresholds."

        "Rocket Profiles" -> "View all saved rocket configuration profiles. A standard default profile is provided; " +
                "press the plus button to create your own. Default threshold values are preconfigured."

        "Edit Rocket Profile" -> "Edit the selected rocket profile’s parameters—thrust, payload mass, and stages. " +
                "Save when you’re done."

        "Weather Settings" -> "Configure your weather thresholds and preferences. Select or edit saved settings " +
                "that determine which launch windows are shown."

        "Config" -> "From here you can choose to manage Weather Settings or Rocket Profiles."

        "Edit Weather Settings" -> "Configure which parameters are used to filter launch windows."

        "Launch Sites" -> "Browse saved launch sites, edit names."

        else -> ""
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
                    selected = currentRoute == Screen.Maps.route,
                    onClick  = {
                        navController.navigateSingleTopTo(Screen.Maps.route)
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
                    selected = currentRoute?.startsWith("weather/") == true,
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
                    label    = { Text("Rocket Config") },
                    selected = currentRoute == Screen.RocketConfigList.route,
                    onClick  = {
                        navController.navigateSingleTopTo(Screen.RocketConfigList.route)
                        closeDrawer()
                    },
                    modifier = Modifier
                        .padding(NavigationDrawerItemDefaults.ItemPadding)
                        .semantics {
                            role = Role.Button
                            contentDescription = "Navigate to Rocket Config"
                            stateDescription = if (selected) "Selected" else "Not selected"
                        }
                )
                NavigationDrawerItem(
                    label    = { Text("Weather Config") },
                    selected = currentRoute == Screen.WeatherConfigList.route,
                    onClick  = {
                        navController.navigateSingleTopTo(Screen.WeatherConfigList.route)
                        closeDrawer()
                    },
                    modifier = Modifier
                        .padding(NavigationDrawerItemDefaults.ItemPadding)
                        .semantics {
                            role = Role.Button
                            contentDescription = "Navigate to Weather Config"
                            stateDescription = if (selected) "Selected" else "Not selected"
                        }
                )
//                NavigationDrawerItem(
//                    label    = { Text("Config") },
//                    selected = currentRoute == Screen.Configs.route,
//                    onClick  = {
//                        navController.navigateSingleTopTo(Screen.Configs.route)
//                        closeDrawer()
//                    },
//                    modifier = Modifier
//                        .padding(NavigationDrawerItemDefaults.ItemPadding)
//                        .semantics {
//                            role = Role.Button
//                            contentDescription = "Navigate to Config"
//                            stateDescription = if (selected) "Selected" else "Not selected"
//                        }
//                )
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
