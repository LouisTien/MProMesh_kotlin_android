package zyxel.com.multyproneo.database.room

import androidx.room.*
import zyxel.com.multyproneo.util.AppConfig

@Dao
interface DatabaseSiteInfoDao
{
    @Query("select * from " + AppConfig.TABLE_SITE_INFO_NAME)
    fun getAll(): List<DatabaseSiteInfoEntity>

    @Query("select * from " + AppConfig.TABLE_SITE_INFO_NAME + " where mac LIKE :mac LIMIT 1")
    fun queryByMac(mac: String): DatabaseSiteInfoEntity

    @Query("select * from " + AppConfig.TABLE_SITE_INFO_NAME + " where uid LIKE :uid LIMIT 1")
    fun queryByUid(uid: String): DatabaseSiteInfoEntity

    @Query("select * from " + AppConfig.TABLE_SITE_INFO_NAME + " where backup LIKE :value")
    fun queryByBackup(value: Boolean): List<DatabaseSiteInfoEntity>

    @Query("select * from " + AppConfig.TABLE_SITE_INFO_NAME + " where notification LIKE :value")
    fun queryByNoti(value: Boolean): List<DatabaseSiteInfoEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(item: DatabaseSiteInfoEntity): Long

    @Update
    fun update(item: DatabaseSiteInfoEntity): Int

    @Delete
    fun delete(item: DatabaseSiteInfoEntity): Int
}