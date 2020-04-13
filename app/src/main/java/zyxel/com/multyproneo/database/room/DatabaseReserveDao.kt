package zyxel.com.multyproneo.database.room

import androidx.room.*
import zyxel.com.multyproneo.util.AppConfig

@Dao
interface DatabaseReserveDao
{
    @Query("select * from " + AppConfig.TABLE_RESERVE_NAME)
    fun getAll(): List<DatabaseReserveEntity>

    @Query("select * from " + AppConfig.TABLE_RESERVE_NAME + " where reserveOne LIKE :reserveOne LIMIT 1")
    fun queryByReserveOne(reserveOne: String): DatabaseReserveEntity

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(item: DatabaseReserveEntity): Long

    @Update
    fun update(item: DatabaseReserveEntity): Int

    @Delete
    fun delete(item: DatabaseReserveEntity): Int
}