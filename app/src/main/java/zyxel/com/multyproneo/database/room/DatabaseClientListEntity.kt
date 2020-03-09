package zyxel.com.multyproneo.database.room

import androidx.room.Entity
import androidx.room.PrimaryKey
import zyxel.com.multyproneo.util.AppConfig

@Entity(tableName = AppConfig.TABLE_CLIENT_LIST_NAME)
data class DatabaseClientListEntity
(
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0,
    var mac: String = "N/A",
    var deviceMac: String = "N/A",
    var deviceName: String = "N/A",
    var reserveOne: String = "N/A",
    var reserveTwo: String = "N/A",
    var reserveThree: String = "N/A"
)