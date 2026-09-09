package nieto.genm.login_android.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "direcciones")
data class DireccionesEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: Long,
    val calle: String,
    val numero: String,
    val colonia: String,
    val municipio: String = "",
    val codigoPostal: String,
    val latitud: Double = 0.0,
    val longitud: Double = 0.0
)