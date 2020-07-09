package zyxel.com.multyproneo.util

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import zyxel.com.multyproneo.database.room.*

@Database(entities = [DatabaseSiteInfoEntity::class, DatabaseClientListEntity::class, DatabaseReserveEntity::class], version = 1)
abstract class DatabaseCloudUtil : RoomDatabase()
{
    abstract fun getSiteInfoDao(): DatabaseSiteInfoDao
    abstract fun getClientListDao(): DatabaseClientListDao
    abstract fun getReserveDao(): DatabaseReserveDao

    companion object
    {
        fun getInstance(context: Context): DatabaseCloudUtil?
        {
            return Room.databaseBuilder(
                    context,
                    DatabaseCloudUtil::class.java,
                    AppConfig.DATABASE_NAME
            )
                    .build()
        }
    }
}