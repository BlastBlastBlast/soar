package no.uio.ifi.in2000.met2025.ui.screens.amtosphericwind

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import no.uio.ifi.in2000.met2025.data.models.IsobaricData
import no.uio.ifi.in2000.met2025.data.remote.isobaric.IsobaricRepository
import no.uio.ifi.in2000.met2025.domain.WeatherModel
import javax.inject.Inject

@HiltViewModel
class AtmosphericWindViewModel @Inject constructor(
    private val weatherModel: WeatherModel
) : ViewModel() {

    sealed class AtmosphericWindUiState {
        data object Idle : AtmosphericWindUiState()
        data object Loading : AtmosphericWindUiState()
        data class Success(val isobaricData: IsobaricData) : AtmosphericWindUiState()
        data class Error(val message: String) : AtmosphericWindUiState()
    }

    private val _uiState = MutableStateFlow<AtmosphericWindUiState>(AtmosphericWindUiState.Idle)
    val uiState: StateFlow<AtmosphericWindUiState> = _uiState

    fun loadIsobaricData(lat: Double, lon: Double) {
        viewModelScope.launch {
            _uiState.value = AtmosphericWindUiState.Loading
            /*_uiState.value = weatherModel.getCurrentIsobaricData()
                .fold(
                    onSuccess = { data ->
                        AtmosphericWindUiState.Success(data)
                    },
                    onFailure = { throwable ->
                        AtmosphericWindUiState.Error(throwable.message ?: "Ukjent feil")
                    }
                )*/
        }
    }
}