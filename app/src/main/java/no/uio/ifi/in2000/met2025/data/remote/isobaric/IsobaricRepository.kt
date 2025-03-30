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

                // Read the reftime (reference time) variable - assuming it's stored as a string or a datetime
                val refTimeVariable = netcdfFile.findVariable("reftime")
                val refTime = refTimeVariable?.let {
                    // You may need to parse the value depending on the format it is stored in
                    it.read().getObject(0) as? String
                } ?: throw Exception("Missing or invalid reftime variable")

                // Read the time variable (representing time steps from reftime) - assumed scalar or 1D array
                val timeVariable = netcdfFile.findVariable("time")
                val time = timeVariable?.let {
                    // If it's a scalar value (e.g., one time step), just read it
                    it.read().getObject(0) as? Float
                } ?: throw Exception("Missing or invalid time variable")

                // Retrieve the 4D variables (dimensions: [isobaric, time, lat, lon])
                val temperatureVar =
                    netcdfFile.findVariable("Temperature_isobaric")?.read() as? ArrayFloat.D4
                val uWindVar = netcdfFile.findVariable("u-component_of_wind_isobaric")
                    ?.read() as? ArrayFloat.D4
                val vWindVar = netcdfFile.findVariable("v-component_of_wind_isobaric")
                    ?.read() as? ArrayFloat.D4

                if (temperatureVar == null || uWindVar == null || vWindVar == null) {
                    println("Missing temperature or wind data")
                    return emptyMap()
                }

                // Loop over latitudes and longitudes to build the data map
                for (latIdx in latitudes.indices) {
                    for (lonIdx in longitudes.indices) {
                        val lat = latitudes[latIdx]
                        val lon = longitudes[lonIdx]

                        val isobaricMap = mutableMapOf<Float, IsobaricData>()
                        for (levelIdx in isobaricLevels.indices) {
                            val level = isobaricLevels[levelIdx]

                            // Loop over the time steps
                            // Access data using the time index
                            val temperature = temperatureVar.get(levelIdx, 0, latIdx, lonIdx)
                            val uWind = uWindVar.get(levelIdx, 0, latIdx, lonIdx)
                            val vWind = vWindVar.get(levelIdx, 0, latIdx, lonIdx)

                            // Store the data in the map for each lat, lon, and isobaric level
                            isobaricMap[level] = IsobaricData(temperature, uWind, vWind)
                        }
                        gribDataMap[Pair(lat, lon)] = isobaricMap
                    }
                }
            }
            // Delete the temporary file
            tempFile.delete()
        } catch (e: Exception) {
            println("Error processing grib file: ${e.message}")
        }
        return gribDataMap
    }
}