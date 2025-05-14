package no.uio.ifi.in2000.met2025.ui.navigation

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import no.uio.ifi.in2000.met2025.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTopBar(
    navController: NavHostController,
    currentThemeDark: Boolean,
    onToggleTheme: () -> Unit,
    onOpenDrawer: () -> Unit,
    goToConfig: () -> Unit
) {
    val currentRoute = navController
        .currentBackStackEntryAsState()
        .value
        ?.destination
        ?.route

    val title = when {
        currentRoute == Screen.Maps.route                               -> "Map"
        currentRoute == Screen.LaunchSite.route                         -> "Launch Sites"
        currentRoute?.startsWith("weather/") == true                    -> "Weather"
        currentRoute == Screen.RocketConfigList.route                   -> "Rocket Configs"
        currentRoute?.startsWith("rocket_config_edit") == true          -> "Edit Rocket Config"
        currentRoute == Screen.WeatherConfigList.route                         -> "Weather Configs"
        currentRoute?.startsWith("weather_edit") == true                 -> "Edit Weather Configs"
        currentRoute == Screen.Configs.route                           -> "Config"
        else                                                             -> ""
    }

    TopAppBar(
        modifier = Modifier
            .background(Color.Black)
            .semantics {
                contentDescription = "$title screen toolbar"
            },
        title = {
            Text(
                text = title,
                color = Color.White,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold
                ),
                modifier = Modifier.semantics {
                    heading()
                }
            )
        },
        navigationIcon = {
            IconButton(
                onClick = onOpenDrawer,
                modifier = Modifier.semantics {
                    role = Role.Button
                    contentDescription = "Open navigation drawer"
                }
            ) {
                Image(
                    painter = painterResource(R.drawable.soarlogo),
                    contentDescription = null,
                    modifier = Modifier.size(40.dp)
                )
            }
        },
        actions = {
            // Config toggle button
            IconButton(
                onClick = {
                    if (currentRoute == Screen.Configs.route) {
                        // already on the Config screen → go back
                        navController.popBackStack()
                    } else {
                        // not on Config → navigate there
                        goToConfig()
                    }
                },
                modifier = Modifier
                    .semantics {
                        role = Role.Button
                        contentDescription = if (currentRoute == Screen.Configs.route)
                            "Close settings" else "Open settings"
                    }
            ) {
                Icon(
                    imageVector = Icons.Filled.Settings,
                    contentDescription = null,
                    tint = Color.White
                )
            }

            IconButton(
                onClick = onToggleTheme,
                modifier = Modifier.semantics {
                    role = Role.Button
                    contentDescription = if (currentThemeDark)
                        "Switch to light theme"
                    else
                        "Switch to dark theme"
                }
            ) {
                Icon(
                    painter = painterResource(
                        if (currentThemeDark) R.drawable.light_mode
                        else               R.drawable.dark_mode
                    ),
                    contentDescription = null,
                    tint = Color.White
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Black)
    )
}
