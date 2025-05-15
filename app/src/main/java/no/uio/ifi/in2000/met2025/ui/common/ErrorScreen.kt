package no.uio.ifi.in2000.met2025.ui.common

import android.graphics.drawable.Drawable
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import no.uio.ifi.in2000.met2025.R
import no.uio.ifi.in2000.met2025.ui.theme.WarmOrange

@Composable
fun ErrorScreen(
    errorMsg: String,
    buttonText: String,
    onReload: () -> Unit,
    drawable: Painter
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(WarmOrange),
        contentAlignment = Alignment.Center
    ) {

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,  // center text+button
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Error: $errorMsg",
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .padding(16.dp)
                    .semantics {
                        liveRegion = LiveRegionMode.Assertive
                        contentDescription = "Error: $errorMsg"
                    }
            )
            Spacer(modifier = Modifier.height(12.dp))
            Image(
                painter = drawable,
                contentDescription = "Background image error screen",
                modifier = Modifier.size(200.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedButton(
                onClick = { onReload() },
                border    = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
                shape     = RoundedCornerShape(8.dp),
                colors    = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.primary,
                    containerColor = MaterialTheme.colorScheme.onSurface
                ),
                modifier = Modifier
                    .padding(horizontal = 8.dp, vertical = 4.dp)
                    .semantics {
                        contentDescription = "$buttonText button"
                        role = Role.Button
                    }
            ) {
                Text(buttonText)
            }
        }
    }
}