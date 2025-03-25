

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import no.uio.ifi.in2000.met2025.data.remote.Isobaric.IsobaricDataSource
import ucar.nc2.NetcdfFiles
import ucar.nc2.grib.grib2.Grib2DataReader
import ucar.nc2.grib.grib2.Grib2Record
import java.io.ByteArrayInputStream
import java.io.File
import javax.inject.Inject


class IsobaricgribRepository @Inject constructor(
    private val isobaricDataSource: IsobaricDataSource
) {

    suspend fun getCurrentIsobaricgribData() {

        try {

            val isobaricData: Result<ByteArray> = isobaricDataSource.fetchCurrentIsobaricgribData()
            //bruker fold til å konvertere fra result set til et bytearray
            val byteArray = isobaricData.fold(
                onSuccess = { byteArray ->
                    byteArray
                },
                onFailure = { exception ->
                    println("Failed to fetch the data: ${exception.message}")
                    ByteArray(0)
                }
            )

            val byteArrayInputStream = ByteArrayInputStream(byteArray)
            try {
                val grib2Record = Grib2DataReader().getData(byteArrayInputStream)
            } catch (e: Exception) {
            }

            // Loop through the records and process them
            while (grib2Record != null) {
                // Process the GRIB2 record (you can extract PDS or data here)
                val pds = grib2Record.pds
                val productDefinition = grib2Record.productDefinition

                println("GRIB2 Record:")
                println("  Discipline: ${pds.discipline}")
                println("  Parameter: ${pds.parameterName}")
                println("  Time: ${pds.timeRange}")
                println("  Data: ${productDefinition.data}") // Process the actual data accordingly

                // Read the next record
                grib2Record = grib2Input.readGrib2Record()
            }

            // Close the Grib2Input stream
            grib2Input.close()

            /*
            val tempFile = withContext(Dispatchers.IO) {
                File.createTempFile("isobaric", ".grib2")
            }.apply {
                writeBytes(byteArray)
            }


            NetcdfFiles.open(tempFile.absolutePath).use { netcdfFile ->
                val variableNames = netcdfFile.variables.map {it.fullName}
                println("Available Variables: $variableNames")
            }
            */

        } catch (e: Exception) {
            println("Error processing grib file")
        }

    }
}

class Grib2Factory {

    companion object {
        /**
         * Reads all GRIB2 records from the given byte array.
         */
        fun readAllGrib2Records(byteArray: ByteArray): List<Grib2Record> {
            val records = mutableListOf<Grib2Record>()
            val inputStream: InputStream = ByteArrayInputStream(byteArray)

            try {
                // Using Grib2RecordReader to read records
                val reader = Grib2RecordReader(inputStream)

                // Read each record in the GRIB2 data
                var record: Grib2Record? = reader.readRecord()
                while (record != null) {
                    records.add(record)
                    record = reader.readRecord()  // Continue reading next record
                }

                reader.close()  // Close reader when done
            } catch (e: Exception) {
                println("Error reading GRIB2 records: ${e.message}")
            }

            return records
        }
    }
}