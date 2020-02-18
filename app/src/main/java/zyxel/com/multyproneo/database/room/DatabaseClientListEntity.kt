package zyxel.com.multyproneo.database.room

import androidx.room.Entity
import androidx.room.PrimaryKey
import zyxel.com.multyproneo.util.AppConfig

@Entity(tableName = AppConfig.TABLE_CLIENT_LIST_NAME)
data class DatabaseClientListEntity
(
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0,
    var mac: String = "",
    var deviceMac: String = "",
    var deviceName: String = "",
    var reserveOne: String = "",
    var reserveTwo: String = "",
    var reserveThree: String = ""
)