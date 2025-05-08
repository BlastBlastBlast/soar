package no.uio.ifi.in2000.met2025.ui.screens.home.maps

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
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
    elevation: String? = null,
    isLoadingElevation: Boolean = false,
    onClick: () -> Unit,
    onDoubleClick: () -> Unit,
    onLongPress: () -> Unit,
    fontSize: TextUnit = 8.sp
) {
// Build a descriptive string for accessibility
    val description = buildString {
        append(name)
        append(". Latitude $lat. Longitude $lon.")
        elevation?.let { append(" Elevation $it.") }
    }

    Surface(
        shape = RoundedCornerShape(4.dp),
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier
            .semantics {
                role = Role.Button
                contentDescription = description
                customActions = listOf(
                    CustomAccessibilityAction(label = "Label") { onClick(); true },
                    CustomAccessibilityAction(label = "Select and move to marker") { onDoubleClick(); true },
                    CustomAccessibilityAction(label = "Edit or save marker") { onLongPress(); true }
                )
            }
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { onClick() },
                    onDoubleTap = { onDoubleClick() },
                    onLongPress = { onLongPress() }
                )
            }
    ) {
        Column {
            // Title bar
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
                    text = name,
                    fontSize = fontSize,
                    color = MaterialTheme.colorScheme.onPrimary,
                    textDecoration = TextDecoration.Underline
                )
            }

            // Body: lat, lon, elevation/spinner
            Column(
                modifier = Modifier
                    .padding(horizontal = 6.dp, vertical = 4.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Lat: ",
                        fontSize = fontSize,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Text(
                        text = lat,
                        fontSize = fontSize,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Lon: ",
                        fontSize = fontSize,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Text(
                        text = lon,
                        fontSize = fontSize,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }

                if (isLoadingElevation) {
                    // Match spinner size to font size to avoid stutter
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.height(fontSize.value.dp)
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(fontSize.value.dp),
                            strokeWidth = (fontSize.value / 2).dp,
                            color = Color.Black
                        )
                    }
                } else {
                    elevation?.let { elevText ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Elev: ",
                                fontSize = fontSize,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                            Text(
                                text = elevText,
                                fontSize = fontSize,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }
                }
            }
        }
    }
}
