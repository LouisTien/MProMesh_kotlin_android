package zyxel.com.multyproneo.util

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import zyxel.com.multyproneo.database.room.DatabaseClientListDao
import zyxel.com.multyproneo.database.room.DatabaseClientListEntity
import zyxel.com.multyproneo.database.room.DatabaseSiteInfoDao
import zyxel.com.multyproneo.database.room.DatabaseSiteInfoEntity

@Database(entities = [DatabaseSiteInfoEntity::class, DatabaseClientListEntity::class], version = 1)
abstract class DatabaseCloudUtil : RoomDatabase()
{
    abstract fun getSiteInfoDao(): DatabaseSiteInfoDao
    abstract fun getClientListDao(): DatabaseClientListDao

    companion object
    {
        private var INSTANCE: DatabaseCloudUtil? = null

        fun getInstance(context: Context): DatabaseCloudUtil?
        {
            if(INSTANCE == null)
            {
                synchronized(DatabaseCloudUtil::class)
                {
                    INSTANCE = Room.databaseBuilder(
                            context,
                            DatabaseCloudUtil::class.java,
                            AppConfig.DATABASE_NAME
                    ).build()
                }
            }
            return INSTANCE
        }
    }

    fun destroyInstance()
    {
        INSTANCE = null
    }
}