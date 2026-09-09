package nieto.genm.login_android.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.headers
import io.ktor.client.request.parameter
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

object PlacesService {
    private val client = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
                prettyPrint = true
            })
        }
    }

    private const val BASE_URL = "https://nominatim.openstreetmap.org/search"
    private const val USER_AGENT = "LoginAndroidApp/1.0"
    suspend fun buscarColonias(
        query: String,
        municipio: String = "",
        cp: String = ""
    ): List<PlacePrediction> {
        if (query.length < 3) return emptyList()

        return try {
            val response: List<NominatimResult> = client.get(BASE_URL) {
                val queryCompleta = listOf(query, municipio, cp)
                    .filter { it.isNotBlank() }
                    .joinToString(", ")

                parameter("q", queryCompleta)
                parameter("format", "json")
                parameter("addressdetails", "1")
                parameter("countrycodes", "mx")
                parameter("limit", "5")

                contentType(ContentType.Application.Json)
                headers {
                    append("User-Agent", USER_AGENT)
                    append("Accept", "application/json")
                }
            }.body()
            response.map { it.toPlacePrediction() }
        } catch (e: Exception) {
            println("NOMINATIM ERROR COLONIAS: ${e.message}")
            emptyList()
        }
    }
    suspend fun buscarPorCodigoPostal(cp: String): List<PlacePrediction> {
        if (cp.length != 5) return emptyList()

        return try {
            val response: List<NominatimResult> = client.get(BASE_URL) {
                parameter("postalcode", cp)
                parameter("format", "json")
                parameter("addressdetails", "1")
                parameter("countrycodes", "mx")
                parameter("limit", "10")

                contentType(ContentType.Application.Json)
                headers {
                    append("User-Agent", USER_AGENT)
                    append("Accept", "application/json")
                }
            }.body()
            response.map { it.toPlacePrediction() }
        } catch (e: Exception) {
            println("NOMINATIM ERROR CP: ${e.message}")
            emptyList()
        }
    }
    suspend fun buscarMunicipios(query: String): List<PlacePrediction> {
        if (query.length < 3) return emptyList()

        return try {
            val response: List<NominatimResult> = client.get(BASE_URL) {
                parameter("q", query)
                parameter("format", "json")
                parameter("addressdetails", "1")
                parameter("countrycodes", "mx")
                parameter("limit", "5")

                contentType(ContentType.Application.Json)
                headers {
                    append("User-Agent", USER_AGENT)
                    append("Accept", "application/json")
                }
            }.body()

            response.map { it.toPlacePrediction() }
        } catch (e: Exception) {
            println("NOMINATIM ERROR MUNICIPIOS: ${e.message}")
            emptyList()
        }
    }
    suspend fun buscarCalles(
        calleQuery: String,
        colonia: String,
        municipio: String = "",
        cp: String
    ): List<PlacePrediction> {
        if (calleQuery.length < 3) return emptyList()

        return try {
            val response: List<NominatimResult> = client.get(BASE_URL) {
                val streetParam = listOf(calleQuery, colonia)
                    .filter { it.isNotBlank() }
                    .joinToString(", ")
                parameter("street", streetParam)
                if (municipio.isNotBlank()) parameter("county", municipio)
                if (cp.isNotBlank()) parameter("postalcode", cp)
                parameter("format", "json")
                parameter("addressdetails", "1")
                parameter("countrycodes", "mx")
                parameter("limit", "5")

                contentType(ContentType.Application.Json)
                headers {
                    append("User-Agent", USER_AGENT)
                    append("Accept", "application/json")
                }
            }.body()

            response.map { it.toPlacePrediction() }
        } catch (e: Exception) {
            println("NOMINATIM ERROR CALLES: ${e.message}")
            emptyList()
        }
    }
    suspend fun obtenerCoordenadasFinales(
        calle: String,
        numero: String,
        colonia: String,
        municipio: String = "",
        cp: String
    ): Pair<Double, Double> {
        return try {
            val direccionTexto = listOf("$calle $numero", colonia, municipio, cp, "Mexico")
                .filter { it.isNotBlank() }
                .joinToString(", ")

            val response: List<NominatimResult> = client.get(BASE_URL) {
                parameter("q", direccionTexto)
                parameter("countrycodes", "mx")
                parameter("format", "json")
                parameter("limit", "1")

                headers { append("User-Agent", USER_AGENT) }
            }.body()

            val coincidencia = response.firstOrNull()
            if (coincidencia != null) {
                Pair(
                    coincidencia.latitud.toDoubleOrNull() ?: 0.0,
                    coincidencia.longitud.toDoubleOrNull() ?: 0.0
                )
            } else {
                val fallback = buscarColonias(colonia.ifEmpty { cp }).firstOrNull()
                Pair(fallback?.latitud ?: 0.0, fallback?.longitud ?: 0.0)
            }
        } catch (e: Exception) {
            Pair(0.0, 0.0)
        }
    }

    suspend fun buscarDirecciones(query: String): List<PlacePrediction> {
        if (query.length < 3) return emptyList()

        return try {
            val response: List<NominatimResult> = client.get(BASE_URL) {
                parameter("q", query)
                parameter("format", "json")
                parameter("addressdetails", "1")
                parameter("countrycodes", "mx")
                parameter("limit", "5")

                contentType(ContentType.Application.Json)
                headers {
                    append("User-Agent", USER_AGENT)
                    append("Accept", "application/json")
                }
            }.body()

            response.map { it.toPlacePrediction() }
        } catch (e: Exception) {
            emptyList()
        }
    }
    private fun NominatimResult.toPlacePrediction(): PlacePrediction {
        val addr = this.address

        val coloniaResuelta = addr?.colonia
            ?: addr?.fraccionamiento
            ?: addr?.barrio
            ?: ""

        val municipioResuelto = addr?.municipio
            ?: addr?.delegacion
            ?: addr?.ciudad
            ?: addr?.pueblo
            ?: ""

        return PlacePrediction(
            descripcion = this.descripcion,
            placeId = this.placeId.toString(),
            calle = addr?.calle ?: "",
            numero = addr?.numero ?: "",
            colonia = coloniaResuelta,
            municipio = municipioResuelto,
            codigoPostal = addr?.codigoPostal ?: "",
            latitud = this.latitud.toDoubleOrNull() ?: 0.0,
            longitud = this.longitud.toDoubleOrNull() ?: 0.0
        )
    }
}