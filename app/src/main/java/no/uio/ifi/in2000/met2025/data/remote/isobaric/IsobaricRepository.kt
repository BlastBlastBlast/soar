package no.uio.ifi.in2000.met2025.data.remote.isobaric

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import no.uio.ifi.in2000.met2025.data.models.GribDataMap
import no.uio.ifi.in2000.met2025.data.models.IsobaricData
import ucar.ma2.ArrayFloat
import ucar.nc2.NetcdfFiles
import java.io.File
import javax.inject.Inject

class IsobaricRepository @Inject constructor(
    private val isobaricDataSource: IsobaricDataSource
) {
    suspend fun getCurrentIsobaricGribData(): GribDataMap {
        val gribDataMap = mutableMapOf<Pair<Float, Float>, MutableMap<Float, IsobaricData>>()

        try {
            val isobaricData: Result<ByteArray> = isobaricDataSource.fetchCurrentIsobaricGribData()
            val byteArray = isobaricData.getOrNull() ?: return emptyMap()

            // Create temporary file on the IO dispatcher
            val tempFile = withContext(Dispatchers.IO) {
                File.createTempFile("isobaric", ".grib2")
            }.apply {
                writeBytes(byteArray)
            }

            // Open the GRIB file
            NetcdfFiles.open(tempFile.absolutePath).use { netcdfFile ->
                // Read latitude variable (assumed 1D)
                val latitudes = (netcdfFile.findVariable("lat")?.read() as? ArrayFloat.D1)
                    ?.let { array ->
                        (0 until array.size).map { idx -> array.get(idx.toInt()) }
                    }
                // Read longitude variable (assumed 1D)
                val longitudes = (netcdfFile.findVariable("lon")?.read() as? ArrayFloat.D1)
                    ?.let { array ->
                        (0 until array.size).map { idx -> array.get(idx.toInt()) }
                    }
                // Read isobaric levels variable (assumed 1D)
                val isobaricLevels = (netcdfFile.findVariable("isobaric")?.read() as? ArrayFloat.D1)
                    ?.let { array ->
                        (0 until array.size).map { idx -> array.get(idx.toInt()) }
                    }

                if (latitudes == null || longitudes == null || isobaricLevels == null) {
                    println("Missing lat/lon/isobaric data")
                    return emptyMap()
                }

                // Retrieve the 4D variables (dimensions: [isobaric, time, lat, lon])
                val temperatureVar = netcdfFile.findVariable("Temperature_isobaric")?.read() as? ArrayFloat.D4
                val uWindVar = netcdfFile.findVariable("u-component_of_wind_isobaric")?.read() as? ArrayFloat.D4
                val vWindVar = netcdfFile.findVariable("v-component_of_wind_isobaric")?.read() as? ArrayFloat.D4

                if (temperatureVar == null || uWindVar == null || vWindVar == null) {
                    println("Missing temperature or wind data")
                    return emptyMap()
                }

                // Extract the timestamp from the first available entry
                val firstTempValue = arrayOf("025-03-31T09:00:00Z")
                val firstTimeIndex = 0  // Assuming all entries use the same timestamp

                println("Temperature shape: ${temperatureVar.shape.contentToString()}")
                println("uWind shape: ${uWindVar.shape.contentToString()}")
                println("vWind shape: ${vWindVar.shape.contentToString()}")
                println("Latitudes size: ${latitudes.size}, Longitudes size: ${longitudes.size}")
                println("Isobaric levels size: ${isobaricLevels.size}")

                // Loop over latitudes and longitudes to build the data map
                for (latIdx in latitudes.indices) {
                    for (lonIdx in longitudes.indices) {
                        val lat = latitudes[latIdx]
                        val lon = longitudes[lonIdx]

                        val isobaricMap = mutableMapOf<Float, IsobaricData>()

                        val levelIdx = 0  // Fixing the isobaric level to the first dimension (since shape[0] = 1)

                        val temperature = temperatureVar.get(levelIdx, firstTimeIndex, latIdx, lonIdx)
                        val uWind = uWindVar.get(levelIdx, firstTimeIndex, latIdx, lonIdx)
                        val vWind = vWindVar.get(levelIdx, firstTimeIndex, latIdx, lonIdx)

                        isobaricMap[isobaricLevels.first()] = IsobaricData(temperature, uWind, vWind)

                        gribDataMap[Pair(lat, lon)] = isobaricMap
                    }
                }
            }
            // Delete the temporary file
            tempFile.delete()
        } catch (e: Exception) {
            println("Error processing GRIB file: ${e.message}")
        }
        return gribDataMap
    }
}