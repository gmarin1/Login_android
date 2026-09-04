package nieto.genm.login_android.data.local

import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSUserDomainMask

actual class DatabaseBuilder {
    @OptIn(ExperimentalForeignApi::class)
    actual fun build(): RoomDatabase.Builder<AppDB> {
        val documentDirectory = NSFileManager.defaultManager.URLForDirectory(
            directory = NSDocumentDirectory,
            inDomain = NSUserDomainMask,
            appropriateForURL = null,
            create = false,
            error = null
        )
        val dbFilePath = requireNotNull(documentDirectory?.path) + "/direcciones.db"

        return Room.databaseBuilder<AppDB>(
            name = dbFilePath,
            factory = { AppDBConstructor.initialize() }
        )
    }
}