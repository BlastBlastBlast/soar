package no.uio.ifi.in2000.met2025.ui.screens.home.maps

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import no.uio.ifi.in2000.met2025.ui.theme.WarmOrange

@Composable
fun MarkerLabel(
    name: String,
    lat: String,
    lon: String,
    elevation: String? = null,       // ← new parameter
    onClick: () -> Unit,
    onDoubleClick: () -> Unit,
    onLongPress: () -> Unit,
    fontSize: TextUnit = 8.sp
) {
    Surface(
        shape = RoundedCornerShape(4.dp),
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap       = { onClick() },
                    onDoubleTap = { onDoubleClick() },
                    onLongPress = { onLongPress() }
                )
            }
    ) {
        Column {
            // — Title row —
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        WarmOrange,
                        shape = RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp)
                    )
                    .padding(horizontal = 6.dp, vertical = 2.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text           = name,
                    fontSize       = fontSize,
                    color          = MaterialTheme.colorScheme.onPrimary,
                    textDecoration = TextDecoration.Underline
                )
            }

            // — Body rows: lat, lon, (optional) elev —
            Column(
                modifier           = Modifier
                    .padding(horizontal = 6.dp, vertical = 4.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Row {
                    Text(
                        text     = "Lat: ",
                        fontSize = fontSize,
                        color    = MaterialTheme.colorScheme.onPrimary
                    )
                    Text(
                        text        = lat,
                        fontSize    = fontSize,
                        fontWeight  = FontWeight.Bold,
                        color       = MaterialTheme.colorScheme.onPrimary
                    )
                }
                Row {
                    Text(
                        text     = "Lon: ",
                        fontSize = fontSize,
                        color    = MaterialTheme.colorScheme.onPrimary
                    )
                    Text(
                        text        = lon,
                        fontSize    = fontSize,
                        fontWeight  = FontWeight.Bold,
                        color       = MaterialTheme.colorScheme.onPrimary
                    )
                }
                // only show elevation if non-null
                elevation?.let { elevText ->
                    Row {
                        Text(
                            text     = "Elev: ",
                            fontSize = fontSize,
                            color    = MaterialTheme.colorScheme.onPrimary
                        )
                        Text(
                            text        = elevText,
                            fontSize    = fontSize,
                            fontWeight  = FontWeight.Bold,
                            color       = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            }
        }
    }
}
