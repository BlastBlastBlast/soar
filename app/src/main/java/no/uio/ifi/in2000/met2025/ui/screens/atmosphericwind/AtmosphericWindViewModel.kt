package no.uio.ifi.in2000.met2025.ui.screens.atmosphericwind

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import no.uio.ifi.in2000.met2025.data.local.Database.LaunchSite
import no.uio.ifi.in2000.met2025.data.local.Database.LaunchSiteDAO
//import no.uio.ifi.in2000.met2025.data.models.IsobaricData
import no.uio.ifi.in2000.met2025.data.models.IsobaricDataItem
import no.uio.ifi.in2000.met2025.data.remote.isobaric.IsobaricRepository
import no.uio.ifi.in2000.met2025.domain.WeatherModel
import java.time.Instant
import javax.inject.Inject

@HiltViewModel
class AtmosphericWindViewModel @Inject constructor(
    private val weatherModel: WeatherModel,
    private val launchSiteDAO: LaunchSiteDAO
) : ViewModel() {

    sealed class AtmosphericWindUiState {
        data object Idle : AtmosphericWindUiState()
        data object Loading : AtmosphericWindUiState()
        data class Success(val isobaricItems: Map<String, IsobaricDataItem>) : AtmosphericWindUiState()
        data class Error(val message: String) : AtmosphericWindUiState()
    }

    private val _uiState = MutableStateFlow<AtmosphericWindUiState>(AtmosphericWindUiState.Idle)
    val uiState: StateFlow<AtmosphericWindUiState> = _uiState

    private val _launchSite = MutableStateFlow<LaunchSite?>(null)
    val launchSite: StateFlow<LaunchSite?> = _launchSite

    init {
        observeTempSite()
    }

    private fun observeTempSite() {
        viewModelScope.launch {
            launchSiteDAO.getTempSite().collect { site ->
                _launchSite.value = site
            }
        }
    }

    fun loadIsobaricData(lat: Double, lon: Double) {
        viewModelScope.launch {
            val currentItems = uiState.value
            _uiState.value = AtmosphericWindUiState.Loading
            _uiState.value = weatherModel.getCurrentIsobaricData(lat, lon, Instant.now())
                .fold(
                    onFailure = { throwable ->
                    if (currentItems !is AtmosphericWindUiState.Success)
                        AtmosphericWindUiState.Error(throwable.message ?: "Ukjent feil")
                    else
                        AtmosphericWindUiState.Success(currentItems.isobaricItems)
                    },
                    onSuccess = { data ->
                        AtmosphericWindUiState.Success(
                            if (currentItems !is AtmosphericWindUiState.Success)
                                mapOf(data.time to data)
                            else
                                currentItems.isobaricItems + (data.time to data)
                        )
                    }
                )
        }
    }
}