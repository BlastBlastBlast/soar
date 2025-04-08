package no.uio.ifi.in2000.met2025.ui.screens.atmosphericwind

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import no.uio.ifi.in2000.met2025.data.local.database.LaunchSite
import no.uio.ifi.in2000.met2025.data.local.database.LaunchSiteDAO
//import no.uio.ifi.in2000.met2025.data.models.IsobaricData
import no.uio.ifi.in2000.met2025.data.models.IsobaricData
import no.uio.ifi.in2000.met2025.domain.WeatherModel
import java.time.Duration
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import javax.inject.Inject

@HiltViewModel
class AtmosphericWindViewModel @Inject constructor(
    private val weatherModel: WeatherModel,
    private val launchSiteDAO: LaunchSiteDAO
) : ViewModel() {

    sealed class AtmosphericWindUiState {
        data object Idle : AtmosphericWindUiState()
        data object Loading : AtmosphericWindUiState()
        data class Success(val isobaricData: IsobaricData) : AtmosphericWindUiState()
        data class Error(val message: String) : AtmosphericWindUiState()
    }

    private val _isobaricData = MutableStateFlow<Map<Instant, AtmosphericWindUiState>>(emptyMap())
    val isobaricData: StateFlow<Map<Instant, AtmosphericWindUiState>> = _isobaricData

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

//    fun loadAllAvailableIsobaricData(lat: Double, lon: Double) {
//        (1..<8).forEach { index ->
//            val currentTime = Instant.now()
//            val currentHour = LocalDateTime.ofInstant(currentTime, ZoneId.systemDefault()).truncatedTo(
//                ChronoUnit.HOURS)
//            val nextDivisibleHour = generateSequence(currentHour) { it.plusHours(1) }
//                .first { it.hour % 3 == 0 }
//            val validTime = nextDivisibleHour.atZone(ZoneId.systemDefault()).toInstant()
//            val itemTime = validTime.plus(Duration.ofHours(index.toLong() * 3))
//
//            Mutex().withLock {
//                loadIsobaricData(lat, lon, itemTime)
//            }
//        }
//    }

//    fun loadAllAvailableIsobaricDataInOrder(lat: Double, lon: Double) {
//        viewModelScope.launch {
//            val currentTime = Instant.now()
//            val currentHour = LocalDateTime.ofInstant(currentTime, ZoneId.systemDefault()).truncatedTo(ChronoUnit.HOURS)
//            val nextDivisibleHour = generateSequence(currentHour) { it.plusHours(1) }
//                .first { it.hour % 3 == 0 }
//            var validTime = nextDivisibleHour.atZone(ZoneId.systemDefault()).toInstant()
//
//            repeat(8) {
//                Mutex().withLock {
//                    updateIsobaricData(lat, lon, validTime)
//                    validTime = validTime.plus(Duration.ofHours(3))
//                }
//            }
//        }
//    }

    fun loadIsobaricData(lat: Double, lon: Double, time: Instant) {
        viewModelScope.launch {
            //Mutex().withLock {
                updateIsobaricData(lat, lon, time)
            //}
        }
    }

    private suspend fun updateIsobaricData(
        lat: Double,
        lon: Double,
        time: Instant
    ) {
        observeTempSite()
        val currentItem = isobaricData.value[time]
        _isobaricData.value += (time to AtmosphericWindUiState.Loading)
        _isobaricData.value += (
                time to weatherModel.getCurrentIsobaricData(lat, lon, time).fold(
                    onFailure = { throwable ->
                        if (currentItem !is AtmosphericWindUiState.Success)
                            AtmosphericWindUiState.Error(
                                throwable.message ?: "Ukjent feil"
                            )
                        else
                            AtmosphericWindUiState.Success(currentItem.isobaricData) //TODO: add something on screen to show that this value is outdated
                        },
                    onSuccess = { data ->
                        AtmosphericWindUiState.Success(data)
                    }
                )
                )
    }
}