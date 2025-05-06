package no.uio.ifi.in2000.met2025.ui.navigation

import androidx.compose.runtime.Composable
import no.uio.ifi.in2000.met2025.ui.navigation.AppScaffold

@Composable
fun App(
    darkTheme: Boolean,
    toggleTheme: () -> Unit
) {
    AppScaffold(
        darkTheme = darkTheme,
        toggleTheme = toggleTheme
    )
}