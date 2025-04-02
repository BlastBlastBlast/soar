package no.uio.ifi.in2000.met2025.ui.screens.home.maps

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class LocationViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    companion object {
        private const val COORDINATES_KEY = "coordinates"
    }

    private val _coordinates = MutableStateFlow(
        savedStateHandle.get<Pair<Double, Double>>(COORDINATES_KEY) ?: Pair(59.942, 10.726)
    )
    val coordinates: StateFlow<Pair<Double, Double>> = _coordinates.asStateFlow()

    fun updateCoordinates(lat: Double, lon: Double) {
        val newCoordinates = Pair(lat, lon)
        _coordinates.value = newCoordinates
        savedStateHandle[COORDINATES_KEY] = newCoordinates // Persist the state
    }

    fun getCurrentCoordinates(): Pair<Double, Double> = _coordinates.value
}

