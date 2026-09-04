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

    suspend fun buscarDirecciones(query: String): List<PlacePrediction> {
        if (query.length < 3) return emptyList()

        return try {
            val response: List<NominatimResult> = client.get("https://nominatim.openstreetmap.org/search") {
                parameter("q", query)
                parameter("format", "json")
                parameter("addressdetails", "1")
                parameter("countrycodes", "mx")
                parameter("limit", "5")

                contentType(ContentType.Application.Json)
                headers {
                    append("User-Agent", "LoginAndroidApp/1.0")
                    append("Accept", "application/json")
                }
            }.body()

            response.map {
                PlacePrediction(
                    descripcion = it.descripcion,
                    placeId = it.placeId.toString(),
                    latitud = it.latitud.toDoubleOrNull() ?: 0.0,
                    longitud = it.longitud.toDoubleOrNull() ?: 0.0
                )
            }
        } catch (e: Exception) {
            println("NOMINATIM ERROR: ${e.message}")
            emptyList()
        }
    }
}