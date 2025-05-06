package no.uio.ifi.in2000.met2025.ui.navigation

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.size
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
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
    onOpenDrawer: () -> Unit
) {
    val currentRoute = navController
        .currentBackStackEntryAsState()
        .value
        ?.destination
        ?.route

    // Mirror the route names exactly as in your drawer info
    val title = when {
        currentRoute == Screen.Home.route                              -> "Map"
        currentRoute == Screen.LaunchSite.route                        -> "Launch Sites"
        currentRoute?.startsWith("weather?") == true             -> "Weather"
        currentRoute == Screen.RocketConfigList.route                  -> "Rocket Profiles"
        currentRoute?.startsWith("rocket_config_edit") == true   -> "Edit Rocket Profile"
        currentRoute == Screen.ConfigList.route                        -> "Weather Settings"
        currentRoute?.startsWith("config_edit") == true          -> "Edit Weather Settings"
        currentRoute == Screen.Settings.route                          -> "Settings"
        else                                                           -> ""
    }

    TopAppBar(
        modifier       = Modifier.background(Color.Black),
        title          = {
            Text(
                text = title,
                color = Color.White,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold
                )
            )
        },
        navigationIcon = {
            IconButton(onClick = onOpenDrawer) {
                Image(
                    painter           = painterResource(R.drawable.placeholder_logo),
                    contentDescription = "Open navigation drawer",
                    modifier          = Modifier.size(40.dp)
                )
            }
        },
        actions = {
            IconButton(onClick = onToggleTheme) {
                Icon(
                    painter           = painterResource(
                        if (currentThemeDark) R.drawable.sun_icon
                        else               R.drawable.moon_icon
                    ),
                    contentDescription = "Toggle theme",
                    tint               = Color.White
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Black)
    )
}