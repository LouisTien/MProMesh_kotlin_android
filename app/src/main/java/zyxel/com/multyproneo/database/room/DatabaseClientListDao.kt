package zyxel.com.multyproneo.database.room

import android.arch.persistence.room.*
import zyxel.com.multyproneo.util.AppConfig

@Dao
interface DatabaseClientListDao
{
    @Query("select * from " + AppConfig.TABLE_CLIENT_LIST_NAME)
    fun getAll(): List<DatabaseClientListEntity>

    @Query("select * from " + AppConfig.TABLE_CLIENT_LIST_NAME + " where mac LIKE :mac")
    fun queryByMac(mac: String): List<DatabaseClientListEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(item: DatabaseClientListEntity): Long

    @Update
    fun update(item: DatabaseClientListEntity): Int

    @Delete
    fun delete(item: DatabaseClientListEntity): Int
}