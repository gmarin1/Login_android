package nieto.genm.login_android.data.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class NominatimResult(
    @SerialName("display_name") val descripcion: String = "",
    @SerialName("place_id") val placeId: Long = 0L,
    @SerialName("lat") val latitud: String = "0.0",
    @SerialName("lon") val longitud: String = "0.0"
)

@Serializable
data class PlacePrediction(
    val descripcion: String,
    val placeId: String,
    val latitud: Double = 0.0,
    val longitud: Double = 0.0
)