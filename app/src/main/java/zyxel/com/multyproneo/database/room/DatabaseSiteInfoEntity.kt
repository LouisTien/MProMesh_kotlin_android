package zyxel.com.multyproneo.database.room

import androidx.room.Entity
import androidx.room.PrimaryKey
import zyxel.com.multyproneo.util.AppConfig

@Entity(tableName = AppConfig.TABLE_SITE_INFO_NAME)
data class DatabaseSiteInfoEntity
(
    @PrimaryKey
    var mac: String = "N/A",
    var siteName: String = "N/A",
    var sitePicPath: String = "N/A",
    var wifiSSID: String = "N/A",
    var wifiPWD: String = "N/A",
    var backup: Boolean = false, //true: save wifiSSID, wifiPWD in this table, save deviceMac, deviceName in TABLE_CLIENT_LIST_NAME table
    var reserveOne: String = "N/A",
    var reserveTwo: String = "N/A",
    var reserveThree: String = "N/A"
)