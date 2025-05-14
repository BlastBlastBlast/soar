package no.uio.ifi.in2000.met2025.ui.screens.mapScreen.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OsloTimePicker(
    modifier: Modifier = Modifier,
    initialTime: LocalTime = LocalTime.now(ZoneId.of("Europe/Oslo")),
    onTimeSelected: (LocalTime) -> Unit
) {
    val osloZone = ZoneId.of("Europe/Oslo")
    val formatter = DateTimeFormatter.ofPattern("HH:mm")

    // 1) Keep local state so we can display the text
    var pickedTime by remember { mutableStateOf(initialTime) }

    // 2) Create and pass a TimePickerState to TimePicker
    val state = rememberTimePickerState(
        initialHour   = initialTime.hour,
        initialMinute = initialTime.minute,
        is24Hour      = true
    )

    // 3) Observe changes in the state and notify parent
    LaunchedEffect(state) {
        snapshotFlow { state.hour to state.minute }
            .map { (h, m) -> LocalTime.of(h, m) }
            .distinctUntilChanged()
            .collect { newTime ->
                pickedTime = newTime
                onTimeSelected(newTime)
            }
    }

    Column(modifier.padding(8.dp)) {
        // Tappable text showing current selection
        Text(
            text = pickedTime.format(formatter),
            modifier = Modifier
                .fillMaxWidth()
                .clickable { /* you could toggle visibility here */ }
                .padding(vertical = 4.dp)
        )

        // The actual dial picker
        TimePicker(
            state = state,
            modifier = Modifier
                .height(100.dp)
                .fillMaxWidth()
        )
    }
}
