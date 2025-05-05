package no.uio.ifi.in2000.met2025.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.compose.rememberNavController
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppScaffold(
    darkTheme: Boolean,
    toggleTheme: () -> Unit
) {
    val navController = rememberNavController()
    val drawerState   = rememberDrawerState(DrawerValue.Closed)
    val scope         = rememberCoroutineScope()
    val systemUiCtrl  = rememberSystemUiController()

    SideEffect {
        systemUiCtrl.setSystemBarsColor(Color.Black, darkIcons = false)
    }

    ModalNavigationDrawer(
        drawerState     = drawerState,
        gesturesEnabled = true,
        modifier        = Modifier.windowInsetsPadding(WindowInsets.safeDrawing),
        drawerContent   = {
            AppDrawer(
                navController    = navController,
                closeDrawer      = { scope.launch { drawerState.close() } }
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
                navController = navController,
                innerPadding  = innerPadding
            )
        }
    }
}