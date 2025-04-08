package no.uio.ifi.in2000.met2025.data.remote.isobaric

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import no.uio.ifi.in2000.met2025.data.local.database.GribData
import no.uio.ifi.in2000.met2025.data.local.database.GribDataDAO
import no.uio.ifi.in2000.met2025.data.local.database.GribUpdated
import no.uio.ifi.in2000.met2025.data.local.database.GribUpdatedDAO
import no.uio.ifi.in2000.met2025.data.models.AvailabilityData
import no.uio.ifi.in2000.met2025.data.models.GribDataMap
import no.uio.ifi.in2000.met2025.data.models.GribVectors
import no.uio.ifi.in2000.met2025.data.models.IsobaricAvailabilityResponse
import no.uio.ifi.in2000.met2025.data.models.StructuredAvailability
import no.uio.ifi.in2000.met2025.domain.helpers.RoundFloatToXDecimalsDouble
import ucar.ma2.ArrayFloat
import ucar.nc2.NetcdfFiles
import java.io.File
import java.time.Instant
import javax.inject.Inject

//TODO: Inject GribDAO

class IsobaricRepository @Inject constructor(
    private val isobaricDataSource: IsobaricDataSource,
    private val gribDAO: GribDataDAO,
    private val updatedDAO: GribUpdatedDAO
) {
    suspend fun getIsobaricGribData(timeSlot: Instant): Result<GribDataMap> {
        //holds a common updated timestamp and individual time stamps and uri's for each dataset
        val availableData = getAvailabilityData()
        println("IsobaricGribDataCalled")
        if (availableData != null) {
            //returns the uri and time stamp for the dataset where timeSlot is valid
            val data = availableData.findClosestBefore(timeSlot)
            val time = data?.time
            val byteArray: ByteArray
            if (!isGribUpToDate(availableData)) {
                gribDAO.clearAll()
                updatedDAO.delete()
                time?.let { updatedDAO.insert(GribUpdated(it.toString())) }
                val isobaricData: Result<ByteArray> =
                    isobaricDataSource.fetchIsobaricGribData(data!!.uri)
                byteArray = isobaricData.fold(
                    onSuccess = { it },
                    onFailure = { return returnErrorAndPrint("Error fetching grib data") }
                )
            } else {
                if (gribDAO.getByTimestamp(time.toString()) != null) {
                    val isobaricData = gribDAO.getByTimestamp(time.toString())!!
                    byteArray = isobaricData.data
                } else {
                    val isobaricData: Result<ByteArray> =
                        isobaricDataSource.fetchIsobaricGribData(data!!.uri)
                    byteArray = isobaricData.fold(
                        onSuccess = { it },
                        onFailure = { return returnErrorAndPrint("Error fetching grib data") }
                    )
                }
            }
            val gribData = GribData(time.toString(), byteArray)
            gribDAO.insert(gribData)
            val res = parseGribData(byteArray, time.toString())
            println("IsobaricGribDataReturned")
            return res
        } else {
            return returnErrorAndPrint("Availability data is null")
        }
    }

    private suspend fun parseGribData(byteArray: ByteArray, time: String): Result<GribDataMap> {
        Mutex().withLock {
            val dataMap = mutableMapOf<Pair<Double, Double>, MutableMap<Int, GribVectors>>()

            try {
                println("trycatch started")
                val tempFile = withContext(Dispatchers.IO) {
                    File.createTempFile("isobaric", ".grib2")
                }.apply {
                    writeBytes(byteArray)
                }
                println("Tempfile created")

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

                    val isobaricLevels =
                        (netcdfFile.findVariable("isobaric")?.read() as? ArrayFloat.D1)
                            ?.let { array ->
                                (0 until array.size).map { idx -> array.get(idx.toInt()) }
                            }

                    if (latitudes == null || longitudes == null || isobaricLevels == null) {
                        val errorMsg = "Missing lat/lon/isobaric data"
                        println(errorMsg)
                        return Result.failure((Exception(errorMsg)))
                    }
                    println("lat/lon/isobaric data found")

                    // Henter ut 4d arrayene for variablene på gitte punkter.
                    val temperatureVar = netcdfFile.findVariable("Temperature_isobaric")?.read() as? ArrayFloat.D4
                    val uWindVar = netcdfFile.findVariable("u-component_of_wind_isobaric")
                        ?.read() as? ArrayFloat.D4
                    val vWindVar = netcdfFile.findVariable("v-component_of_wind_isobaric")
                        ?.read() as? ArrayFloat.D4

                    if (uWindVar == null || vWindVar == null || temperatureVar == null) { //
                        return returnErrorAndPrint("Missing temperature or wind data")
                    }
                    println("temperature/wind data found")

                    //test prints for å forstå datasettet
                    //println("Temperature shape: ${temperatureVar.shape.contentToString()}")
                    println("uWind shape: ${uWindVar.shape.contentToString()}")
                    println("vWind shape: ${vWindVar.shape.contentToString()}")
                    println("temperature shape: ${temperatureVar.shape.contentToString()}")
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
                                    val temperature = temperatureVar.get(0, levelIdx, latIdx, lonIdx)
                                    val uWind = uWindVar.get(0, levelIdx, latIdx, lonIdx)
                                    val vWind = vWindVar.get(0, levelIdx, latIdx, lonIdx)

                                    isobaricMap[level.toInt()] = GribVectors(temperature, uWind, vWind)
                                } catch (e: IndexOutOfBoundsException) {
                                    println("Index error: levelIdx=$levelIdx, latIdx=$latIdx, lonIdx=$lonIdx")
                                }
                            }

                            dataMap[Pair( // ikkje bruk +, for den returnerer eit nytt map og muterer ikkje det gamle :)))
                                RoundFloatToXDecimalsDouble(lat, 2),
                                RoundFloatToXDecimalsDouble(lon - 360, 2)
                            )] = isobaricMap
                        }
                    }
                }
                // Sletter tempFilen siden dataen er lagt i map
                tempFile.delete()
            } catch (e: Exception) {
                println("Error processing GRIB file: ${e.message}")
            }
            val gribDataMap = GribDataMap(time, dataMap)
            return Result.success(gribDataMap)
        }
    }

    private suspend fun getAvailabilityData(): StructuredAvailability?{
        val response = isobaricDataSource.fetchAvailabilityData()
        val data = response.getOrNull() ?: return null
        println("Availability data fetched")
        return restructureAvailabilityResponse(data)
    }

    suspend fun isGribDataAvailable(time: Instant): Boolean {
        val availableData = getAvailabilityData()
        return availableData?.findClosestBefore(time) != null
    }

    private suspend fun isGribUpToDate(availResponse: StructuredAvailability): Boolean {
        return availResponse.updated.toString() == updatedDAO.getUpdated()
    }

    private fun restructureAvailabilityResponse(
        availResponse: IsobaricAvailabilityResponse
    ): StructuredAvailability {
        val updatedInstant = Instant.parse(availResponse.entries.first().updated)

        val availData = availResponse.entries.map { entry ->
            AvailabilityData(entry.params.area, Instant.parse(entry.params.time), entry.uri)
        }

        return StructuredAvailability(updatedInstant, availData)
    }

    private fun StructuredAvailability.findClosestBefore(targetTime: Instant): AvailabilityData? {
        val data = this.availData
            .filter { it.time <= targetTime } //kryssa fingrane for at <= ikkje ødelegge alt
            .maxByOrNull { it.time }
        return data
    }

    private fun <T> returnErrorAndPrint(message: String): Result<T> {
        println(message)
        return Result.failure(Exception(message))
    }
}

