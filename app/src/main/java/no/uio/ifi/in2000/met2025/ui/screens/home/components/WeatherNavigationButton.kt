package no.uio.ifi.in2000.met2025.ui.screens.home.components

import android.widget.Toast
import androidx.compose.foundation.layout.size
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import no.uio.ifi.in2000.met2025.R

@Composable
fun WeatherNavigationButton(
    modifier: Modifier = Modifier,
    latInput: String,
    lonInput: String,
    onNavigate: (Double, Double) -> Unit,
    context: android.content.Context
) {
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
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary,
    ) {
        Icon(
            painter = painterResource(id = R.drawable.weather_icon),
            contentDescription = "Navigate to Weather",
            modifier = Modifier.size(50.dp)
        )
    }
}
