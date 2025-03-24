package no.uio.ifi.in2000.met2025.data.remote.Isobaric

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ucar.nc2.NetcdfFiles
import java.io.File
import javax.inject.Inject


class IsobaricgribRepository @Inject constructor(
    private val isobaricDataSource: IsobaricDataSource
) {

    suspend fun getCurrentIsobaricgribData() {

        try {

            val isobaricData: ByteArray = isobaricDataSource.fetchCurrentIsobaricgribData()

            val tempFile = withContext(Dispatchers.IO) {
                File.createTempFile("isobaric", ".grib2")
            }.apply {
                writeBytes(isobaricData)
            }


            NetcdfFiles.open(tempFile.absolutePath).use { netcdfFile ->
                val variableNames = netcdfFile.variables.map {it.fullName}
                println("Available Variables: $variableNames")
            }

        } catch (e: Exception) {
            println("Error processing grib file")
        }

    }
}