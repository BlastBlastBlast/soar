package no.uio.ifi.in2000.met2025.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.rememberNavController
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import kotlinx.coroutines.launch
import no.uio.ifi.in2000.met2025.ui.configprofiles.ConfigEditViewModel
import no.uio.ifi.in2000.met2025.ui.screens.home.HomeScreenViewModel
import no.uio.ifi.in2000.met2025.ui.screens.rocketconfig.RocketConfigEditViewModel
import no.uio.ifi.in2000.met2025.ui.screens.weathercardscreen.WeatherCardViewmodel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppScaffold(
    darkTheme: Boolean,
    toggleTheme: () -> Unit
) {
    // — nav & drawer state (unchanged) —
    val navController   = rememberNavController()
    val drawerState     = rememberDrawerState(DrawerValue.Closed)
    val scope           = rememberCoroutineScope()
    val sysUiController = rememberSystemUiController()

    SideEffect {
        sysUiController.setSystemBarsColor(
            color     = Color.Black,
            darkIcons = false
        )
    }

    // — hoist all your view-models here, once —
    val homeVM: HomeScreenViewModel = hiltViewModel()
    val weatherVM: WeatherCardViewmodel = hiltViewModel()
    val configEditVM: ConfigEditViewModel = hiltViewModel()
    val rocketConfigEditVM: RocketConfigEditViewModel = hiltViewModel()

    ModalNavigationDrawer(
        drawerState     = drawerState,
        gesturesEnabled = true,  // you can still pass gesturesEnabled if you want
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
                    onOpenDrawer     = { scope.launch { drawerState.open() } }
                )
            }
        ) { innerPadding ->
            NavigationGraph(
                navController               = navController,
                innerPadding                = innerPadding,
                homeScreenViewModel         = homeVM,
                weatherCardViewModel        = weatherVM,
                configEditViewModel         = configEditVM,
                rocketConfigEditViewModel   = rocketConfigEditVM
            )
        }
    }
}