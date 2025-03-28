import no.uio.ifi.in2000.met2025.data.models.IsobaricData

class IsobaricgribRepository {
    suspend fun getIsobaricData(lat: Double, lon: Double): Result<IsobaricData> {
        return Result.success(IsobaricData("2021-10-01", emptyList()))
    }
}