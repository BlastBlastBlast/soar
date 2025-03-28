package no.uio.ifi.in2000.met2025.data.remote.isobaric

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ucar.ma2.ArrayFloat
import ucar.nc2.NetcdfFiles
import ucar.nc2.Variable
import java.io.File
import javax.inject.Inject

class IsobaricRepository @Inject constructor(
    private val isobaricDataSource: IsobaricDataSource
) {

    suspend fun getCurrentIsobaricGribData() {
        try {
            val isobaricData: Result<ByteArray> = isobaricDataSource.fetchCurrentIsobaricGribData()
            val byteArray = isobaricData.fold(
                onSuccess = { byteArray ->
                    byteArray
                },
                onFailure = { exception ->
                    println("Failed to fetch the data: ${exception.message}")
                    ByteArray(0)
                }
            )

            val tempFile = withContext(Dispatchers.IO) {
                File.createTempFile("isobaric", ".grib2")
            }.apply {
                writeBytes(byteArray)
            }

            // Open the file as a NetcdfFile
            NetcdfFiles.open(tempFile.absolutePath).use { netcdfFile ->
                // List available variables
                val variableNames = netcdfFile.variables.map { it.fullName }
                println("Available Variables: $variableNames")

                // Example: Accessing data from a specific variable (replace with your desired variable)
                val targetVariableName = "var_0_2_100_L103" // Replace with a real variable name from your file
                val targetVariable: Variable? = netcdfFile.findVariable(targetVariableName)
                if (targetVariable != null) {
                    val data = targetVariable.read() // Read the data, it will be an object of type Array

                    // Check the data type and handle it appropriately
                    when (data) {
                        is ArrayFloat.D4 -> {
                            println("Data is of type ArrayFloat.D4, example of access : ${data.get(0, 0, 0, 0)}")
                        }

                        // Add more cases for other Array types if needed
                        is ucar.ma2.ArrayDouble.D4 -> {
                            println("Data is of type ArrayDouble.D4, example of access : ${data.get(0, 0, 0, 0)}")
                        }
                        is ucar.ma2.ArrayInt.D4 -> {
                            println("Data is of type ArrayInt.D4, example of access : ${data.get(0, 0, 0, 0)}")
                        }
                        //you can add more cases for other data types here
                        else -> {
                            println("Unsupported data type: ${data::class.java.name}")
                            println("Please add a new case in the when condition to support this data type.")
                        }
                    }
                } else {
                    println("Variable '$targetVariableName' not found.")
                }
            }
            //delete the tempfile
            tempFile.delete()
        } catch (e: Exception) {
            println("Error processing grib file: ${e.message}")
        }
    }
}

/*
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
*/