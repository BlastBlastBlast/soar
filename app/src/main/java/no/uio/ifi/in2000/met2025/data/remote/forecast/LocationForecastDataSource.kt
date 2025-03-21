package no.uio.ifi.in2000.met2025.data.remote.forecast

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.url
import javax.inject.Inject
import javax.inject.Named
import no.uio.ifi.in2000.met2025.data.models.ForecastDataResponse
import java.math.RoundingMode

class LocationForecastDataSource @Inject constructor(
    private val httpClient: HttpClient //TODO: Spesifiser Json client
) {
    suspend fun getForecastDataResponse(lat: Double, lon: Double): Result<ForecastDataResponse> {
        return try {
            Result.success(httpClient.get {
                url("https://api.met.no/weatherapi/locationforecast/2.0/compact?lat=${lat.toBigDecimal().setScale(2, RoundingMode.HALF_UP).toDouble()}&lon=${lon.toBigDecimal().setScale(2, RoundingMode.HALF_UP).toDouble()}")
            }.body())
        } catch (e: Exception) {
            Result.failure(e)
        }

    }
}

//Viktige regler for bruk av MET Weather API
//Obligatorisk datakilde: Locationforecast på api.met.no for bakkedata.
//Endringer i vilkårene blir kunngjort via e-postlisten.
//
//Identifikasjon er obligatorisk
//Alle forespørsler må inkludere en User-Agent-streng med:
//Applikasjons- eller domenenavn
//Versjonsnummer (valgfritt)
//Kontaktinformasjon (e-post eller lenke til nettsted)
//Manglende identifikasjon kan føre til at tilgang blir blokkert uten varsel.
//Eksempler på gyldig User-Agent:
//
//
//Trafikkbegrensning – Unngå overbelastning
//Optimaliser forespørsler:
//Ikke send unødvendige eller overdrevne forespørsler.
//Bruk caching – Lagre data lokalt og bruk If-Modified-Since for å unngå gjentatte forespørsler.
//Spred forespørslene – Unngå å sende mange forespørsler samtidig (f.eks. eksakt hver time).
//Koordinater må avrundes – Bruk maks 4 desimaler for bredde-/lengdegrad.
//Båndbreddegrenser:
//Maks 20 forespørsler per sekund per applikasjon uten spesiell avtale.
//Store nettsteder eller apper må bruke en caching proxy gateway for å redusere belastningen.
//Brudd kan føre til struping (HTTP 429) eller blokkering.
//
//Lisensiering og kreditering (CC BY 4.0)
//All bruk av åpne data krever korrekt kreditering:
//Oppgi kilde: Meteorologisk institutt / MET Weather API
//Inkluder en lenke til lisensen.
//Angi om dataene er endret.
//Ikke gi inntrykk av at MET støtter din tjeneste.
//
//Personvern
//Utviklere er ansvarlige for brukernes persondata.
//Direkte API-kall fra apper/nettlesere lagrer IP-adresser og geokoordinater i logger.
//Bruk proxy-gateway for å anonymisere brukerne.
//
//Tekniske krav
//Protokoller:
//Kun HTTPS støttes – Vedvarende bruk av HTTP kan føre til blokkering.
//Klienter må støtte gzip-komprimering for å redusere båndbreddebruk.
//API-tilkoblinger:
//Direkte API-kall fra nettlesere/mobilapper bør unngås. Bruk en proxy-server for caching og autentisering.
//CORS støttes ikke fullt ut for avanserte forespørsler.
//
//Tilgangskontroll og misbruk
//Hvis du ikke følger vilkårene:
//Trafikkbegrensning (Throttling): Overbelastning fører til HTTP 429 (Too Many Requests).
//Misbruk: Forsøk på å omgå begrensninger, sende falske User-Agent-strenger eller overbelaste API-et kan føre til permanent blokkering.
