package no.uio.ifi.in2000.met2025.ui.screens.weathercardscreen.components.windcomponents

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import ucar.nc2.ft2.coverage.SubsetParams

@Composable
fun AWTimeDisplay(
    time: String,
    style: androidx.compose.ui.text.TextStyle
) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = time,
            style = style,
            modifier = Modifier.padding(bottom = 8.dp).semantics {
                heading()
                contentDescription = "Time window: ${SubsetParams.time}"
            }
        )
    }
}