package no.uio.ifi.in2000.met2025.ui.screens.mapScreen.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import no.uio.ifi.in2000.met2025.R
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.BlendMode



@Composable
fun SunIconsOverlayWithText(
    sunrise: String,
    sunset: String,
    iconTint: Color = Color.White
) {
    Column(modifier = Modifier.padding(4.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Image(
                painter       = painterResource(id = R.drawable.sunrise),
                contentDescription = "Sunrise",
                modifier      = Modifier.size(20.dp),
                colorFilter   = ColorFilter.tint(iconTint, blendMode = BlendMode.SrcIn)
            )
            Spacer(Modifier.width(6.dp))
            Text(text = sunrise, color = Color.White)
        }
        Spacer(Modifier.height(4.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Image(
                painter       = painterResource(id = R.drawable.sunset),
                contentDescription = "Sunset",
                modifier      = Modifier.size(20.dp),
                colorFilter   = ColorFilter.tint(iconTint, blendMode = BlendMode.SrcIn)
            )
            Spacer(Modifier.width(6.dp))
            Text(text = sunset, color = Color.White)
        }
    }
}
