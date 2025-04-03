package no.uio.ifi.in2000.met2025.ui.screens.atmosphericwind

import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.runtime.Composable
import no.uio.ifi.in2000.met2025.data.models.IsobaricData
import no.uio.ifi.in2000.met2025.data.models.IsobaricDataItem
import no.uio.ifi.in2000.met2025.data.models.IsobaricDataValues

// Mock data
val mockIsobaricDataItems = listOf(
    IsobaricDataItem(
        time = "2025-03-15T17:00:00Z",
        valuesAtLayer = mapOf(
            100 to IsobaricDataValues(altitude = 16000.0, windSpeed = 15.0, windFromDirection = 270.0),
            150 to IsobaricDataValues(altitude = 14000.0, windSpeed = 14.0, windFromDirection = 280.0),
            200 to IsobaricDataValues(altitude = 12000.0, windSpeed = 13.0, windFromDirection = 290.0),
            225 to IsobaricDataValues(altitude = 11500.0, windSpeed = 12.0, windFromDirection = 300.0),
            250 to IsobaricDataValues(altitude = 11000.0, windSpeed = 11.0, windFromDirection = 310.0),
            275 to IsobaricDataValues(altitude = 10500.0, windSpeed = 10.0, windFromDirection = 320.0),
            300 to IsobaricDataValues(altitude = 10000.0, windSpeed = 9.0, windFromDirection = 330.0),
            350 to IsobaricDataValues(altitude = 9000.0, windSpeed = 8.0, windFromDirection = 340.0),
            400 to IsobaricDataValues(altitude = 8000.0, windSpeed = 7.0, windFromDirection = 350.0),
            450 to IsobaricDataValues(altitude = 7000.0, windSpeed = 6.0, windFromDirection = 360.0),
            500 to IsobaricDataValues(altitude = 5500.0, windSpeed = 5.0, windFromDirection = 370.0),
            600 to IsobaricDataValues(altitude = 4000.0, windSpeed = 4.0, windFromDirection = 380.0),
            700 to IsobaricDataValues(altitude = 3000.0, windSpeed = 4.0, windFromDirection = 380.0),
            750 to IsobaricDataValues(altitude = 2500.0, windSpeed = 4.0, windFromDirection = 380.0),
            850 to IsobaricDataValues(altitude = 1500.0, windSpeed = 4.0, windFromDirection = 380.0),
            1013 to IsobaricDataValues(altitude = 0.0, windSpeed = 4.0, windFromDirection = 380.0)
        )
    ),
    IsobaricDataItem(
        time = "2025-03-15T20:00:00Z",
        valuesAtLayer = mapOf(
            100 to IsobaricDataValues(altitude = 16000.0, windSpeed = 14.0, windFromDirection = 260.0),
            150 to IsobaricDataValues(altitude = 14000.0, windSpeed = 13.0, windFromDirection = 270.0),
            200 to IsobaricDataValues(altitude = 12000.0, windSpeed = 12.0, windFromDirection = 280.0),
            225 to IsobaricDataValues(altitude = 11500.0, windSpeed = 11.0, windFromDirection = 290.0),
            250 to IsobaricDataValues(altitude = 11000.0, windSpeed = 10.0, windFromDirection = 300.0),
            275 to IsobaricDataValues(altitude = 10500.0, windSpeed = 9.0, windFromDirection = 310.0),
            300 to IsobaricDataValues(altitude = 10000.0, windSpeed = 8.0, windFromDirection = 320.0),
            350 to IsobaricDataValues(altitude = 9000.0, windSpeed = 7.0, windFromDirection = 330.0),
            400 to IsobaricDataValues(altitude = 8000.0, windSpeed = 6.0, windFromDirection = 340.0),
            450 to IsobaricDataValues(altitude = 7000.0, windSpeed = 5.0, windFromDirection = 350.0),
            500 to IsobaricDataValues(altitude = 5500.0, windSpeed = 4.0, windFromDirection = 360.0),
            600 to IsobaricDataValues(altitude = 4000.0, windSpeed = 3.0, windFromDirection = 370.0),
            700 to IsobaricDataValues(altitude = 3000.0, windSpeed = 2.0, windFromDirection = 380.0),
            750 to IsobaricDataValues(altitude = 2500.0, windSpeed = 4.0, windFromDirection = 380.0),
            850 to IsobaricDataValues(altitude = 1500.0, windSpeed = 4.0, windFromDirection = 380.0),
            1013 to IsobaricDataValues(altitude = 0.0, windSpeed = 4.0, windFromDirection = 380.0)
        )
    )
)

val mockUiStateSuccess = AtmosphericWindViewModel.AtmosphericWindUiState.Success(
    isobaricData = IsobaricData("2021-10-01", mockIsobaricDataItems)
)

@Preview(showBackground = true)
@Composable
fun AtmosphericWindScreenPreview() {
    AtmosphericWindScreenMock(uiState = mockUiStateSuccess, onLoadData = { _, _ -> })
}

@Composable
fun AtmosphericWindScreenMock(
    uiState: AtmosphericWindViewModel.AtmosphericWindUiState,
    onLoadData: (Double, Double) -> Unit
) {
    ScreenContent(windUiState = uiState, coordinates = Pair(59.942, 10.726), onLoadData = onLoadData)
}
