package zyxel.com.multyproneo.database.room

import androidx.room.Entity
import androidx.room.PrimaryKey
import zyxel.com.multyproneo.util.AppConfig

@Entity(tableName = AppConfig.TABLE_RESERVE_NAME)
data class DatabaseReserveEntity
(
    var reserveOne: String = "N/A",
    var reserveTwo: String = "N/A",
    var reserveThree: String = "N/A",
    var reserveFour: Int = 0,
    var reserveFive: Boolean = false
)
{
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0
}