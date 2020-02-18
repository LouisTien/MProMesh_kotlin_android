package zyxel.com.multyproneo.database.room

import androidx.room.Entity
import androidx.room.PrimaryKey
import zyxel.com.multyproneo.util.AppConfig

@Entity(tableName = AppConfig.TABLE_SITE_INFO_NAME)
data class DatabaseSiteInfoEntity
(
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0,
    var mac: String = "",
    var siteName: String = "",
    var sitePicPath: String = "",
    var wifiSSID: String = "",
    var wifiPWD: String = "",
    var backup: Boolean = false, //true: save wifiSSID, wifiPWD in this table, save deviceMac, deviceName in TABLE_CLIENT_LIST_NAME table
    var reserveOne: String = "",
    var reserveTwo: String = "",
    var reserveThree: String = ""
)