package no.uio.ifi.in2000.met2025.data.remote.isobaric

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import no.uio.ifi.in2000.met2025.data.models.GribDataMap
import no.uio.ifi.in2000.met2025.data.models.GribVectors
import ucar.ma2.ArrayFloat
import ucar.nc2.NetcdfFiles
import java.io.File
import java.math.BigDecimal
import java.math.RoundingMode
import javax.inject.Inject

class IsobaricRepository @Inject constructor(
    private val isobaricDataSource: IsobaricDataSource
) {
    suspend fun getCurrentIsobaricGribData(): GribDataMap {
        val gribDataMap = mutableMapOf<Pair<Double, Double>, MutableMap<Int, GribVectors>>()

        try {
            val isobaricData: Result<ByteArray> = isobaricDataSource.fetchCurrentIsobaricGribData()
            val byteArray = isobaricData.getOrNull() ?: return emptyMap()

            val tempFile = withContext(Dispatchers.IO) {
                File.createTempFile("isobaric", ".grib2")
            }.apply {
                writeBytes(byteArray)
            }

            NetcdfFiles.open(tempFile.absolutePath).use { netcdfFile ->
                //Henter ut variablene som ligger i vanlige arrayer.
                //Disse trengs for å hente ut data fra 4d arrayene
                val latitudes = (netcdfFile.findVariable("lat")?.read() as? ArrayFloat.D1)
                    ?.let { array ->
                        (0 until array.size).map { idx -> array.get(idx.toInt()) }
                    }

                val longitudes = (netcdfFile.findVariable("lon")?.read() as? ArrayFloat.D1)
                    ?.let { array ->
                        (0 until array.size).map { idx -> array.get(idx.toInt()) }
                    }

                val isobaricLevels = (netcdfFile.findVariable("isobaric")?.read() as? ArrayFloat.D1)
                    ?.let { array ->
                        (0 until array.size).map { idx -> array.get(idx.toInt()) }
                    }

                if (latitudes == null || longitudes == null || isobaricLevels == null) {
                    println("Missing lat/lon/isobaric data")
                    return emptyMap()
                }

                // Henter ut 4d arrayene for variablene på gitte punkter.
                //val temperatureVar = netcdfFile.findVariable("Temperature_isobaric")?.read() as? ArrayFloat.D4
                val uWindVar = netcdfFile.findVariable("u-component_of_wind_isobaric")?.read() as? ArrayFloat.D4
                val vWindVar = netcdfFile.findVariable("v-component_of_wind_isobaric")?.read() as? ArrayFloat.D4

                if (uWindVar == null || vWindVar == null) { //temperatureVar == null
                    println("Missing temperature or wind data")
                    return emptyMap()
                }

                //test prints for å forstå datasettet
                //println("Temperature shape: ${temperatureVar.shape.contentToString()}")
                println("uWind shape: ${uWindVar.shape.contentToString()}")
                println("vWind shape: ${vWindVar.shape.contentToString()}")
                println("Latitudes size: ${latitudes.size}, Longitudes size: ${longitudes.size}")
                println("Isobaric levels size: ${isobaricLevels.size}")
                println("Isobaric levels from NetCDF: ${isobaricLevels.joinToString()}")

                // Nøsted løkke over kombinasjonen latitude, longitude og isobaric level
                for (latIdx in latitudes.indices) {
                    for (lonIdx in longitudes.indices) {
                        val lat = latitudes[latIdx]
                        val lon = longitudes[lonIdx]

                        val isobaricMap = mutableMapOf<Int, GribVectors>()

                        for (levelIdx in isobaricLevels.indices) {
                            val level = isobaricLevels[levelIdx] / 100  //Convert from Pa to hPa

                            try {
                                //val temperature = temperatureVar.get(0, levelIdx, latIdx, lonIdx)
                                val uWind = uWindVar.get(0, levelIdx, latIdx, lonIdx)
                                val vWind = vWindVar.get(0, levelIdx, latIdx, lonIdx)

                                isobaricMap[level.toInt()] = GribVectors(uWind, vWind)
                            } catch (e: IndexOutOfBoundsException) {
                                println("Index error: levelIdx=$levelIdx, latIdx=$latIdx, lonIdx=$lonIdx")
                            }
                        }

                        gribDataMap[Pair(
                            BigDecimal(lat.toDouble()).setScale(2, RoundingMode.HALF_UP).toDouble(),
                            (BigDecimal((lon -360).toDouble()).setScale(2, RoundingMode.HALF_UP).toDouble())
                        )] = isobaricMap
                    }
                }
            }
            // Sletter tempFilen siden dataen er lagt i map
            tempFile.delete()
        } catch (e: Exception) {
            println("Error processing GRIB file: ${e.message}")
        }
        return gribDataMap
    }
}