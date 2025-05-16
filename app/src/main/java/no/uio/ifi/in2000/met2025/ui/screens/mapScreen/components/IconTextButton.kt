package no.uio.ifi.in2000.met2025.ui.screens.mapScreen.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import no.uio.ifi.in2000.met2025.ui.theme.WarmOrange

@Composable
fun IconTextButton(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    contentDescription: String?,
    line1: String,
    line2: String? = null,
    onClick: () -> Unit,
    // tweak these if you want different sizing/roundness
    height: Dp = 64.dp,
    cornerRadius: Dp = 12.dp,
    borderColor: Color = WarmOrange
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.height(height),
        shape = RoundedCornerShape(cornerRadius),
        border = BorderStroke(1.dp, borderColor),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor   = MaterialTheme.colorScheme.onSurface
        )
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = contentDescription)
            Spacer(Modifier.width(8.dp))
            Column {
                Text(
                    line1,
                    textAlign = TextAlign.Start,
                    maxLines  = 1,
                    overflow   = TextOverflow.Ellipsis
                )
                line2?.let {
                    Text(
                        it,
                        textAlign = TextAlign.Start,
                        maxLines  = 1,
                        overflow   = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}
