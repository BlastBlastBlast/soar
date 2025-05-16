package no.uio.ifi.in2000.met2025.ui.navigation

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState

import androidx.compose.ui.semantics.*
import androidx.compose.ui.unit.sp
import no.uio.ifi.in2000.met2025.R
import no.uio.ifi.in2000.met2025.ui.theme.LocalIsDarkTheme

/**
 * AppDrawer.kt
 *
 * Defines the navigation drawer UI for the app, including:
 * - A list of navigation items
 * - Contextual help text based on the current screen
 */
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
        currentRoute?.startsWith("rocket_edit") == true          -> "Edit Rocket Profile"
        currentRoute == Screen.WeatherConfigList.route                         -> "Weather Settings"
        currentRoute?.startsWith("weather_edit") == true                 -> "Edit Weather Settings"
        currentRoute == Screen.Configs.route                           -> "Config"
        currentRoute == Screen.LaunchSite.route                         -> "Launch Sites"
        else                                                            -> "Help"
    }

    val infoText = when (infoTitle) {
        // Define the help text paragraphs for each section
        "Map"-> "How to use the map: \n" +
                "- Longpress the map to place a marker" +
                " (or input coordinates at the top of the screen).\n" +
                "- Longpress the label above a marker to edit the name and save it as a launch site.\n" +
                "- Saved sites can be deleted from the launch site screen.\n" +
                "- Double click  a launch site label to pan the camera to, and zoom in on, the chosen site.\n" +
                "- Open the rocket launch trajectory simulation menu by pressing the trajectory button.\n" +
                "- Clear trajectory between launches to make sure calculations and renders happen correctly."

        //this has a bit of a unique display, and is therefore defined more in the actual method.
        "Weather" -> "Forecast screen:\n"

        "Rocket Profiles" -> "Rocket Profiles:\n" +
                "- View all saved rocket configuration profiles.\n" +
                "- A default profile is provided.\n" +
                "- Press the plus button to create your own.\n" +
                "- Default threshold values are preconfigured."

        "Edit Rocket Profile" -> "Edit the selected rocket profile’s parameters:\n" +
                "— Launch direction (Azimuth and Pitch)\n" +
                "— Launch rail length\n" +
                "— Wet mass: Total weight with fuel\n" +
                "— Dry mass: Weight, not counting fuel\n" +
                "— Burn time: Expected fuel consumption time\n" +
                "— Step Size: The intervals of flight at which the simulation renders data\n" +
                "— Thrust: Force produced in Newton\n" +
                "— Cross-Sectional area and Drag Coefficient\n" +
                "— Parachute Cross-Sectional area and Drag Coefficient\n"

        "Weather Settings" -> "Weather Profiles:\n" +
                "- View all saved Weather configuration profiles.\n" +
                "- These profiles define the thresholds for what is evaluated as safe or unsafe." +
                "- A default profile is provided.\n" +
                "- Press the plus button to create your own.\n" +
                "- Default threshold values are preconfigured."


        "Config" -> "From here you can choose to manage Weather Settings or Rocket Profiles."

        "Edit Weather Settings" -> "Define which parameters is used for evaluating " +
                "launch windows as safe or unsafe, and the thresholds for these.\n" +
                "- Change the thresholds by changing the values in the input fields.\n" +
                "- Toggle on or off whether the parameters are used for evaluation."

        "Launch Sites" -> "Browse saved launch sites, edit names or delete sites."

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

            if (infoTitle != "Weather") {
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
            // Weather screen has custom InfoBlock sections
            } else {
                val isLightMode = LocalIsDarkTheme.current
                Column(
                    Modifier
                        .weight(1f)
                        .verticalScroll(bodyScroll)
                        .padding(horizontal = 16.dp)
                        .semantics { contentDescription = infoText }
                ) {
                    Text(infoText)
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "Data explanation:",
                        style = MaterialTheme.typography.titleSmall,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    //names were wrong on import
                    val check = if (isLightMode) R.drawable.check_dark
                        else R.drawable.check_light
                    val x = if (isLightMode) R.drawable.x_dark
                        else R.drawable.x_light
                    InfoBlock(text ="- The blue checkmark means the launch window is safe\n"+
                                    "- The red cross means the launch window is unsafe\n"+
                                    "- The yellow warning means the launch window is close to "+
                                    "the tresholds, and requires caution\n"+
                                    "- The purple cloud icon indicates not sufficient data",
                        iconRes = listOf(check, x, R.drawable.caution, R.drawable.no_data),
                        contentDescription = "Wind Evaluation Icon descriptions")
                    //wind direction pointer description
                    InfoBlock(
                        text = "Wind blows from the direction indicated by the red end, " +
                                "and towards the direction indicated by the white end.\n" +
                                "The degrees express the cardinality of where the wind blows from.\n" +
                                "0° = North \n90° = East \n180° = South \n270° = West",
                        iconRes = listOf(R.drawable.windicator),
                        contentDescription = "Wind Direction Pointer"
                    )
                    //grib icon descriptions
                    val gribFetched = if (isLightMode) R.drawable.grib_light_blue
                        else R.drawable.grib_dark_blue
                    val gribNotFetched = if (isLightMode) R.drawable.grib_light_empty
                        else R.drawable.grib_dark_empty
                    val gribNotAvailable = if (isLightMode) R.drawable.grib_light_purple
                        else R.drawable.grib_dark_purple
                    InfoBlock(
                        text =  "- A filled blue icon shows that grib data is fetched.\n" +
                                "- An empty icon shows that grib data is not yet fetched.\n" +
                                "- A purple icon shows that grib data is not available.\n" +
                                "- When grib data is fetched, it is part of the safety evaluation",
                        iconRes = listOf(gribFetched, gribNotFetched, gribNotAvailable),
                        contentDescription = "Grib Icons"
                    )
                }
            }
        }
    }
}

/**
 * InfoBlock
 *
 * Displays an icon row and descriptive text block, used in the drawer’s help section.
 */
@Composable
fun InfoBlock(
    modifier: Modifier = Modifier,
    text: String,
    @DrawableRes iconRes: List<Int>,
    contentDescription: String? = null,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Row {
            iconRes.forEach() { icon ->
                Icon(
                    painter = painterResource(id = icon),
                    contentDescription = contentDescription,
                    tint = Color.Unspecified,
                    modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(horizontal = 16.dp),
            lineHeight = 20.sp
        )
    }
}
