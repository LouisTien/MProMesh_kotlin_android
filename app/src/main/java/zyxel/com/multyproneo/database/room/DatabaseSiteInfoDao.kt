package zyxel.com.multyproneo.database.room

import androidx.room.*
import zyxel.com.multyproneo.util.AppConfig

@Dao
interface DatabaseSiteInfoDao
{
    @Query("select * from " + AppConfig.TABLE_SITE_INFO_NAME)
    fun getAll(): List<DatabaseSiteInfoEntity>

    @Query("select * from " + AppConfig.TABLE_SITE_INFO_NAME + " where id LIKE :id LIMIT 1")
    fun queryById(id: Long): DatabaseSiteInfoEntity

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(item: DatabaseSiteInfoEntity): Long

    @Update
    fun update(item: DatabaseSiteInfoEntity): Int

    @Delete
    fun delete(item: DatabaseSiteInfoEntity): Int
}