package zyxel.com.multyproneo.database.room

import androidx.room.Entity
import zyxel.com.multyproneo.util.AppConfig

@Entity(tableName = AppConfig.TABLE_CLIENT_LIST_NAME, primaryKeys = ["mac", "deviceMac"])
data class DatabaseClientListEntity
(
        var mac: String = "N/A", //mac in TABLE_SITE_INFO_NAME table
        var deviceMac: String = "N/A",
        var deviceName: String = "N/A",
        var reserveOne: String = "N/A",
        var reserveTwo: String = "N/A",
        var reserveThree: String = "N/A"
)
