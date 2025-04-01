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

            val tempFile = withContext(Dispatchers.IO) {
                File.createTempFile("isobaric", ".grib2")
            }.apply {
                writeBytes(byteArray)
            }

            NetcdfFiles.open(tempFile.absolutePath).use { netcdfFile ->
                // Read latitude variable
                val latitudes = (netcdfFile.findVariable("lat")?.read() as? ArrayFloat.D1)
                    ?.let { array ->
                        (0 until array.size).map { idx -> array.get(idx.toInt()) }
                    }
                // Read longitude variable
                val longitudes = (netcdfFile.findVariable("lon")?.read() as? ArrayFloat.D1)
                    ?.let { array ->
                        (0 until array.size).map { idx -> array.get(idx.toInt()) }
                    }
                // Read isobaric levels variable
                val isobaricLevels = (netcdfFile.findVariable("isobaric")?.read() as? ArrayFloat.D1)
                    ?.let { array ->
                        (0 until array.size).map { idx -> array.get(idx.toInt()) }
                    }

                if (latitudes == null || longitudes == null || isobaricLevels == null) {
                    println("Missing lat/lon/isobaric data")
                    return emptyMap()
                }

                val reftime = netcdfFile.findVariable("reftime")?.read()
                val reftimeValues = (reftime as? ArrayFloat.D1)?.let { array ->
                    (0 until array.size).map { idx -> array.get(idx.toInt()) }
                }

                val time = netcdfFile.findVariable("time")?.read()
                val timeValues = (time as? ArrayFloat.D1)?.let { array ->
                    (0 until array.size).map { idx -> array.get(idx.toInt()) }
                }

                println("Reference time values: $reftimeValues")
                println("Time values: $timeValues")

                // Retrieve the 4D variables (dimensions: [isobaric, time, lat, lon])
                val temperatureVar = netcdfFile.findVariable("Temperature_isobaric")?.read() as? ArrayFloat.D4
                val uWindVar = netcdfFile.findVariable("u-component_of_wind_isobaric")?.read() as? ArrayFloat.D4
                val vWindVar = netcdfFile.findVariable("v-component_of_wind_isobaric")?.read() as? ArrayFloat.D4

                if (temperatureVar == null || uWindVar == null || vWindVar == null) {
                    println("Missing temperature or wind data")
                    return emptyMap()
                }

                // Extract the timestamp from the first available entry
                //TODO: FIX THIS SHIT
                val firstTempValue = arrayOf("025-03-31T09:00:00Z")
                val firstTimeIndex = 0

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

                        for (levelIdx in isobaricLevels.indices) {  // Loop through all isobaric levels
                            val level = isobaricLevels[levelIdx]  // Get the pressure level

                            try {
                                val temperature = temperatureVar.get(firstTimeIndex, levelIdx, latIdx, lonIdx)
                                val uWind = uWindVar.get(firstTimeIndex, levelIdx, latIdx, lonIdx)
                                val vWind = vWindVar.get(firstTimeIndex, levelIdx, latIdx, lonIdx)

                                isobaricMap[level] = IsobaricData(temperature, uWind, vWind)
                            } catch (e: IndexOutOfBoundsException) {
                                println("Index error: levelIdx=$levelIdx, latIdx=$latIdx, lonIdx=$lonIdx")
                            }
                        }

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