package no.uio.ifi.in2000.met2025.data.remote.isobaric

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import no.uio.ifi.in2000.met2025.data.local.database.GribData
import no.uio.ifi.in2000.met2025.data.local.database.GribDataDAO
import no.uio.ifi.in2000.met2025.data.local.database.GribUpdated
import no.uio.ifi.in2000.met2025.data.local.database.GribUpdatedDAO
import no.uio.ifi.in2000.met2025.data.models.grib.AvailabilityData
import no.uio.ifi.in2000.met2025.data.models.grib.GribDataMap
import no.uio.ifi.in2000.met2025.data.models.grib.GribDataResult
import no.uio.ifi.in2000.met2025.data.models.grib.GribParsingResult
import no.uio.ifi.in2000.met2025.data.models.grib.GribVectors
import no.uio.ifi.in2000.met2025.data.models.grib.GribAvailabilityResponse
import no.uio.ifi.in2000.met2025.data.models.grib.StructuredAvailability
import no.uio.ifi.in2000.met2025.domain.helpers.roundFloatToXDecimalsDouble
import ucar.ma2.ArrayFloat
import ucar.nc2.NetcdfFiles
import java.io.File
import java.time.Duration
import java.time.Instant
import javax.inject.Inject

/**
 * IsobaricRepository
 *
 * Orchestrates fetching of isobaric GRIB datasets from the remote API,
 * caching and persisting raw bytes locally, and parsing them into
 * structured maps of meteorological vectors by location and pressure level.
 *
 * Special notes:
 * - Stores each GRIB payload in local database for offline reuse.
 * - Tracks the last update timestamp to decide whether to fetch fresh data.
 * - Uses an in-memory map to avoid reparsing recently accessed datasets.
 */

class IsobaricRepository @Inject constructor(
    private val isobaricDataSource: IsobaricDataSource,
    private val gribDAO: GribDataDAO,
    private val updatedDAO: GribUpdatedDAO
) {
    //cache
    private val gribMaps: MutableMap<String, GribDataMap> = mutableMapOf()

    /**
     * Retrieves the GRIB map for the closest dataset at or before timeSlot.
     * Handles availability lookup, cache invalidation, database persistence,
     * remote fetching, and parsing.
     */
    suspend fun getIsobaricGribData(timeSlot: Instant): GribDataResult {
        val availableData = getAvailabilityData()

        if (availableData != null) {

            // Pick the entry valid for the requested time
            val data = availableData.findClosestBefore(timeSlot)
                ?: return GribDataResult.AvailabilityError

            val time = data.time
            // Return in-memory if already parsed
            if (time.toString() in gribMaps.keys) {return GribDataResult.Success(gribMaps[time.toString()]!!)}

            val byteArray: ByteArray
            // Decide whether to fetch fresh or load from DB based on last update
            if (!isGribUpToDate(availableData)) {
                //If new data is available, delete outdated data.
                gribDAO.clearAll()
                updatedDAO.delete()

                //Store new update time.
                time.let { updatedDAO.insert(GribUpdated(it.toString(), availableData.latest.toString()))}

                val isobaricData: Result<ByteArray> = isobaricDataSource.fetchIsobaricGribData(data.uri)
                byteArray = isobaricData.fold(
                    onSuccess = { it },
                    onFailure = { return GribDataResult.FetchingError }
                )
                //Store in database
                val gribData = GribData(time.toString(), byteArray)
                gribDAO.insert(gribData)

            } else {
                //Try to load from database.
                if (gribDAO.findByTimestamp(time.toString()) != null) {
                    val isobaricData = gribDAO.findByTimestamp(time.toString())!!
                    byteArray = isobaricData.data

                //If not in database, fetch fresh data.
                } else {
                    val isobaricData: Result<ByteArray> = isobaricDataSource.fetchIsobaricGribData(data.uri)
                    byteArray = isobaricData.fold(
                        onSuccess = { it },
                        onFailure = { return GribDataResult.FetchingError }
                    )
                    //Store in database
                    val gribData = GribData(time.toString(), byteArray)
                    gribDAO.insert(gribData)
                }
            }



            val res = parseGribData(byteArray, time.toString())
            when (res){
                is GribParsingResult.Error -> return GribDataResult.FetchingError
                is GribParsingResult.Success -> gribMaps[time.toString()] = res.gribDataMap
            }
            return GribDataResult.Success(res.gribDataMap)
        } else {
            /*
             * Handle case with no availability data: try rounding to nearest 3h slot
             * since the grib API follows this format 99% of the time.
             */
            val roundedTime = timeSlot.roundToNearest3Hour()
            if (roundedTime in gribMaps.keys) {return GribDataResult.Success(gribMaps[roundedTime]!!)}
            val databaseCheck = gribDAO.findByTimestamp(roundedTime)
            if (databaseCheck != null) {
                val byteArray = databaseCheck.data
                when (val res = parseGribData(byteArray, roundedTime)){
                    is GribParsingResult.Error -> return GribDataResult.ParsingError
                    is GribParsingResult.Success -> {
                        gribMaps[roundedTime] = res.gribDataMap
                        return GribDataResult.Success(res.gribDataMap)
                    }
                }
            }
            return GribDataResult.FetchingError
        }
    }

    /**
     * Parses raw GRIB byte content into a GribDataMap.
     * Writes to a temp file for NetCDF library consumption, reads variables,
     * and builds a nested map of (lat,lon) → (pressure hPa → vectors).
     */
    private suspend fun parseGribData(byteArray: ByteArray, time: String): GribParsingResult {
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

                    /*
                    Extract the variables that are stored in normal 1D arrays
                    These are required to extract data from 4D arrays later
                    */
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
                        return GribParsingResult.Error
                    }

                    // Read the 4D variables for temperature and wind components
                    val temperatureVar = netcdfFile.findVariable("Temperature_isobaric")?.read() as? ArrayFloat.D4
                    val uWindVar = netcdfFile.findVariable("u-component_of_wind_isobaric")
                        ?.read() as? ArrayFloat.D4
                    val vWindVar = netcdfFile.findVariable("v-component_of_wind_isobaric")
                        ?.read() as? ArrayFloat.D4

                    if (uWindVar == null || vWindVar == null || temperatureVar == null) { //
                        return GribParsingResult.Error
                    }
                    println("temperature/wind data found")

                    /*
                    Old print statements for debugging, keeping them here in case they're ever needed
                    //println("Temperature shape: ${temperatureVar.shape.contentToString()}")
                    println("uWind shape: ${uWindVar.shape.contentToString()}")
                    println("vWind shape: ${vWindVar.shape.contentToString()}")
                    println("temperature shape: ${temperatureVar.shape.contentToString()}")
                    println("Latitudes size: ${latitudes.size}, Longitudes size: ${longitudes.size}")
                    println("Isobaric levels size: ${isobaricLevels.size}")
                    println("Isobaric levels from NetCDF: ${isobaricLevels.joinToString()}")
                     */

                    // Build nested map over lat, lon, and pressure levels
                    for (latIdx in latitudes.indices) {
                        for (lonIdx in longitudes.indices) {
                            val lat = latitudes[latIdx]
                            val lon = longitudes[lonIdx]

                            val isobaricMap = mutableMapOf<Int, GribVectors>()

                            for (levelIdx in isobaricLevels.indices) {
                                val level = isobaricLevels[levelIdx] / 100  //Convert from Pa to hPa

                                try {
                                    /*
                                    Accessing the 4D array: [time, level, lat, lon]
                                    Accessing is done by referencing the index of the variable arrays.
                                    Our Grib2 datasets have a single time step, so it's
                                    always the value at index 0 of the time array.
                                    */
                                    val temperature = temperatureVar.get(0, levelIdx, latIdx, lonIdx)
                                    val uWind = uWindVar.get(0, levelIdx, latIdx, lonIdx)
                                    val vWind = vWindVar.get(0, levelIdx, latIdx, lonIdx)

                                    isobaricMap[level.toInt()] = GribVectors(temperature, uWind, vWind)
                                } catch (e: IndexOutOfBoundsException) {
                                    println("Index error: levelIdx=$levelIdx, latIdx=$latIdx, lonIdx=$lonIdx")
                                    return GribParsingResult.Error
                                }
                            }

                            dataMap[Pair(
                                roundFloatToXDecimalsDouble(lat, 2),
                                roundFloatToXDecimalsDouble(lon - 360, 2)
                            )] = isobaricMap
                        }
                    }
                }
                tempFile.delete()
            } catch (e: Exception) {
                println("Error processing GRIB file: ${e.message}")
                return GribParsingResult.Error
            }
            //Return successful parsing result
            return GribParsingResult.Success(gribDataMap = GribDataMap(time, dataMap))
        }
    }

    /**
     * Retrieves and restructures availability metadata from the API.
     */
    private suspend fun getAvailabilityData(): StructuredAvailability?{
        val response = isobaricDataSource.fetchAvailabilityData()
        val data = response.getOrNull() ?: return null

        return restructureAvailabilityResponse(data)
    }

    /**
     * Checks whether GRIB data on disk is up to date with the latest availability timestamp.
     */
    private suspend fun isGribUpToDate(availResponse: StructuredAvailability): Boolean {
        return availResponse.updated.toString() == updatedDAO.findUpdated()
    }

    /**
     * Converts raw API availability response to internal StructuredAvailability.
     */
    private fun restructureAvailabilityResponse(
        availResponse: GribAvailabilityResponse
    ): StructuredAvailability {
        val updatedInstant = Instant.parse(availResponse.entries.first().updated)

        val availData = availResponse.entries.map { entry ->
            AvailabilityData(entry.params.area, Instant.parse(entry.params.time), entry.uri)
        }

        val latest = availData.maxOf { it.time }

        return StructuredAvailability(updatedInstant, latest, availData)
    }

    /**
     * Retrieves the latest timeslot with available grib data from the API.
     * If API call fails, check database for last entry.
     * If no data is available, returns null.
     * Clears outdated data if needed, and updates the GribUpdated table accordingly.
     */
    suspend fun getLatestAvailableGrib(): Instant? {
        val availData = getAvailabilityData()
        val data: Instant
        if (availData == null) {
            data = Instant.parse(updatedDAO.findLatest()) ?: return null
        } else {
            data = availData.latest
        }

        if (data < Instant.now()) return null

        if (availData != null && availData.updated.toString() != updatedDAO.findUpdated()) {
            updatedDAO.delete()
            gribDAO.clearAll()
            updatedDAO.insert(GribUpdated(availData.updated.toString(), availData.latest.toString()))
        }
        return data
    }

    /**
     * Finds the availability entry closest before the target time,
     * within a ~3-hour window.
     */
    private fun StructuredAvailability.findClosestBefore(targetTime: Instant): AvailabilityData? {
        val data = this.availData
            .filter { it.time <= targetTime } //kryssa fingrane for at <= ikkje ødelegge alt
            .maxByOrNull { it.time }
        if (data != null) {
            if (Duration.between(targetTime, data.time).abs() > Duration.ofSeconds(10799))
                return null
        }
        return data
    }

    fun getLatestAvailableGribFlow(): Flow<Instant?> {
        return updatedDAO
            .findLatestFlow()
            .map { isoString ->
                isoString
                    ?.let { runCatching { Instant.parse(it) }.getOrNull() }
            }
    }

    /**
     * Rounds an Instant down to the nearest 3-hour boundary.
     */
    private fun Instant.roundToNearest3Hour(): String {
        val rounded = (epochSecond / 3600 / 3) * 3 * 3600
        return Instant.ofEpochSecond(rounded).toString()
    }
}

