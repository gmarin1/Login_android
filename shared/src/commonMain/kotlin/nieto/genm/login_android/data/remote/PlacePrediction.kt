package nieto.genm.login_android.data.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class NominatimResult(
    @SerialName("display_name") val descripcion: String = "",
    @SerialName("place_id") val placeId: Long = 0L,
    @SerialName("lat") val latitud: String = "0.0",
    @SerialName("lon") val longitud: String = "0.0",
    @SerialName("address") val address: NominatimAddress? = null
)

@Serializable
data class NominatimAddress(
    @SerialName("road") val calle: String? = null,
    @SerialName("house_number") val numero: String? = null,
    @SerialName("suburb") val colonia: String? = null,
    @SerialName("neighbourhood") val fraccionamiento: String? = null,
    @SerialName("quarter") val barrio: String? = null,
    @SerialName("city") val ciudad: String? = null,
    @SerialName("municipality") val municipio: String? = null,
    @SerialName("county") val delegacion: String? = null,
    @SerialName("town") val pueblo: String? = null,
    @SerialName("postcode") val codigoPostal: String? = null,
    @SerialName("state") val estado: String? = null
)

@Serializable
data class PlacePrediction(
    val descripcion: String,
    val placeId: String,
    val calle: String,
    val numero: String,
    val colonia: String,
    val municipio: String,
    val codigoPostal: String,
    val latitud: Double = 0.0,
    val longitud: Double = 0.0
)