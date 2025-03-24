// Kotlin
package no.uio.ifi.in2000.met2025.ui.screens.home

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class HomeScreenViewModel @Inject constructor() : ViewModel() {

    //TODO: Implement ui states and functions for the home screen
    //TODO: Implement the geocoding function
    //TODO: implement event handling for the home screen

    // Default coordinates (Ole Johan Dahl's hus at Oslo University)
    private val _coordinates = MutableStateFlow(Pair(59.942, 10.726))
    val coordinates: StateFlow<Pair<Double, Double>> = _coordinates

    fun updateCoordinates(lat: Double, lon: Double) {
        _coordinates.value = Pair(lat, lon)
    }

    // TODO: geocoding -> fetch coordinates from address
    // Dummy geocoding function. Replace with a real geocoding service if needed.
    fun geocodeAddress(address: String): Pair<Double, Double>? {
        return if (address.contains("NYC", ignoreCase = true)) {
            Pair(40.7128, -74.0060)
        } else {
            null
        }
    }
}