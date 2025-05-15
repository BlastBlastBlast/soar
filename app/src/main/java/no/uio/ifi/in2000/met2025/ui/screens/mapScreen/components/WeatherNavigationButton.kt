/*
 * A button that navigates to the weather screen for given coordinates.
 *
 * Main functionality:
 *  - Attempts to parse latitude and longitude from strings.
 *  - Calls onNavigate(lat, lon) if valid, otherwise shows a Toast error.
 *
 * Special notes:
 *  - Requires a Context to show the Toast.
 */

package no.uio.ifi.in2000.met2025.ui.screens.mapScreen.components

import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import no.uio.ifi.in2000.met2025.R

/**
 * Floating action button to navigate to the weather forecast.
 *
 * @param modifier Modifier for styling and semantics.
 * @param latInput Latitude.
 * @param lonInput Longitude.
 * @param onNavigate Callback invoked with parsed lat/lon when valid.
 * @param context Android Context used to show a Toast on parse failure.
 */
@Composable
fun WeatherNavigationButton(
    modifier: Modifier = Modifier,
    latInput: String,
    lonInput: String,
    onNavigate: (Double, Double) -> Unit,
    context: android.content.Context
) {
    // Floating action button to navigate to the weather forecast
    FloatingActionButton(
        onClick = {
            val lat = latInput.toDoubleOrNull()
            val lon = lonInput.toDoubleOrNull()
            if (lat != null && lon != null) {
                onNavigate(lat, lon)
            } else {
                Toast.makeText(context, "Please enter valid coordinates", Toast.LENGTH_SHORT).show()
            }
        },
        modifier = modifier.semantics {
            contentDescription = "Navigate to weather for current coordinates"
            role = Role.Button
        },
        containerColor = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary,
    ) {
        Column(horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally) {
            Icon(
                painter = painterResource(id = R.drawable.globe_weather),
                contentDescription = "Navigate to Weather",
                modifier = Modifier.size(60.dp)
            )
            Text(
                text = "FORECAST",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onPrimary
            )
        }
    }
}
