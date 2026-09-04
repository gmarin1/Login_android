package nieto.genm.login_android.data.local

import androidx.room.RoomDatabase

expect class DatabaseBuilder {
    fun build(): RoomDatabase.Builder<AppDB>
}

fun getRoomDatabase(builder: DatabaseBuilder): AppDB {
    return builder
        .build()
        .build()
}