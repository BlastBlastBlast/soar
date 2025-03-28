package no.uio.ifi.in2000.met2025.ui.maps

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class LocationViewModel @Inject constructor() : ViewModel() {

    private val _coordinates = MutableStateFlow(Pair(59.942, 10.726)) // Default Coordinates
    val coordinates: StateFlow<Pair<Double, Double>> = _coordinates.asStateFlow()

    fun updateCoordinates(lat: Double, lon: Double) {
        _coordinates.value = Pair(lat, lon)
    }

    fun getCurrentCoordinates(): Pair<Double, Double> {
        return _coordinates.value
    }
}
