package nieto.genm.login_android.data.local

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase

actual class DatabaseBuilder(private val context: Context) {
    actual fun build(): RoomDatabase.Builder<AppDB> {
        val appContext = context.applicationContext
        val dbFile = appContext.getDatabasePath("direcciones.db")
        return Room.databaseBuilder<AppDB>(
            context = appContext,
            name = dbFile.absolutePath
        )
    }
}