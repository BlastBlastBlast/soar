package no.uio.ifi.in2000.met2025.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import no.uio.ifi.in2000.met2025.domain.helpers.parseLatLon

/**
 * A combined input & display component.
 *
 * @param coordinates    The full-text value (e.g. what user pastes).
 * @param onCoordinatesChange  Called when user edits the big field.
 * @param latitudeText   Current latitude string.
 * @param onLatitudeChange   Called when user edits the lat field.
 * @param longitudeText  Current longitude string.
 * @param onLongitudeChange  Called when user edits the lon field.
 */
@Composable
fun LatLonDisplay(
    coordinates: String,
    onCoordinatesChange: (String) -> Unit,
    latitudeText: String,
    onLatitudeChange: (String) -> Unit,
    longitudeText: String,
    onLongitudeChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.padding(8.dp)) {
        OutlinedTextField(
            value = coordinates,
            onValueChange = { new ->
                onCoordinatesChange(new)
                // every time the big field changes, try to re‑parse
                parseLatLon(new)?.let { (lat, lon) ->
                    onLatitudeChange(lat.toString())
                    onLongitudeChange(lon.toString())
                }
            },
            label = { Text("Coordinates") },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor   = MaterialTheme.colorScheme.onPrimary,
                unfocusedBorderColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.5f)
            )
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = latitudeText,
                onValueChange = { newLat ->
                    onLatitudeChange(newLat)
                },
                label = { Text("Lat") },
                modifier = Modifier.weight(1f),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor   = MaterialTheme.colorScheme.onPrimary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.5f)
                )
            )
            Spacer(modifier = Modifier.width(8.dp))
            OutlinedTextField(
                value = longitudeText,
                onValueChange = { newLon ->
                    onLongitudeChange(newLon)
                },
                label = { Text("Lon") },
                modifier = Modifier.weight(1f),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor   = MaterialTheme.colorScheme.onPrimary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.5f)
                )
            )
        }
    }
}

/*
Text("Enter a place or paste coordinates:")
LatLonInputDisplay(
    coordinates       = coords,
    onCoordinatesChange = { coords = it },
    latitudeText      = lat,
    onLatitudeChange    = { lat = it },
    longitudeText     = lon,
    onLongitudeChange   = { lon = it }
)*/