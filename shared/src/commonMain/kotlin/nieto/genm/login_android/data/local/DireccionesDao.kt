package nieto.genm.login_android.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
@Dao
interface DireccionesDao {

    @Query("SELECT * FROM direcciones WHERE userId = :userId ORDER BY id DESC")
    fun obtenerPorUsuario(userId: Long): Flow<List<DireccionesEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(direccion: DireccionesEntity)

    @Query("DELETE FROM direcciones WHERE id = :direccionId AND userId = :userId")
    suspend fun eliminar(direccionId: Long, userId: Long)
}