package no.uio.ifi.in2000.met2025.ui.screens.atmosphericwind.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun WindShearRow(
    backgroundColor: Color,
    speedText: String,
    directionText: String,
    style: androidx.compose.ui.text.TextStyle
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .background(
                color = backgroundColor,
                shape = RoundedCornerShape(4.dp)
            )
            .padding(4.dp) // Add border and padding for visual offset
    ) {
        Box(modifier = Modifier.weight(1f))

        Text(
            text = speedText,
            style = style,
            modifier = Modifier.weight(1f)
        )

        Text(
            text = directionText,
            style = style,
            modifier = Modifier.weight(1f)
        )
    }
}