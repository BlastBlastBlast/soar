package no.uio.ifi.in2000.met2025.ui.navigation

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import kotlinx.coroutines.launch
import androidx.compose.runtime.CompositionLocalProvider
import no.uio.ifi.in2000.met2025.ui.theme.LocalIsDarkTheme

/**
 * AppScaffold
 *
 * Sets up the overall app UI structure with:
 * - A modal navigation drawer (AppDrawer) configurable by gesture and route
 * - A top app bar (AppTopBar) that toggles theme and opens drawer or config screen
 * - Safe drawing insets padding for system UI
 * - System bar color setup (always black with light icons)
 *
 * Special notes:
 * - Hoists all ViewModels via provideAppViewModels().
 * - Disables drawer gestures on the Map screen to avoid conflict with map panning.
 */
@Composable
fun AppScaffold(
    darkTheme: Boolean,
    toggleTheme: () -> Unit
) {
    val navController   = rememberNavController()
    val drawerState     = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope           = rememberCoroutineScope()
    val sysUiController = rememberSystemUiController()

    SideEffect {
        sysUiController.setSystemBarsColor(
            color     = Color.Black,
            darkIcons = false
        )
    }

    // ↓ hoist all VMs in one call ↓
    val vm = provideAppViewModels()

    // observe the current route
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    // gesturesEnabled = true when:
    //  • we’re not on Home (allow drag-to-open), OR
    //  • drawer is already open (always allow drag-to-close)
    val gesturesEnabled = (currentRoute != Screen.Maps.route) || drawerState.isOpen

    CompositionLocalProvider(LocalIsDarkTheme provides darkTheme) {
        ModalNavigationDrawer(
            drawerState     = drawerState,
            gesturesEnabled = gesturesEnabled,
            modifier        = Modifier.windowInsetsPadding(WindowInsets.safeDrawing),
            drawerContent   = {
                AppDrawer(
                    navController = navController,
                    closeDrawer   = { scope.launch { drawerState.close() } }
                )
            }
        ) {
            Scaffold(
                topBar = {
                    AppTopBar(
                        navController    = navController,
                        currentThemeDark = darkTheme,
                        onToggleTheme    = toggleTheme,
                        onOpenDrawer     = { scope.launch { drawerState.open() } },
                        goToConfig       = { navController.navigateSingleTopTo(Screen.Configs.route) }
                    )
                }
            ) { innerPadding ->
                NavigationGraph(
                    navController               = navController,
                    innerPadding                = innerPadding,
                    mapScreenViewModel         = vm.maps,
                    weatherCardViewModel        = vm.weather,
                    configViewModel           = vm.configs,
                )
            }
        }
    }
}