package nieto.genm.login_android.data.local

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
@Database(entities = [DireccionesEntity::class], version = 1)
@ConstructedBy(AppDBConstructor::class)
abstract class AppDB : RoomDatabase() {
    abstract fun direccionDao(): DireccionesDao
}

expect object AppDBConstructor : RoomDatabaseConstructor<AppDB>{
    override fun initialize(): AppDB
}