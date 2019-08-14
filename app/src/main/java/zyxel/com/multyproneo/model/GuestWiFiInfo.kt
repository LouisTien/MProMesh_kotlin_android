package zyxel.com.multyproneo.model

/**
 * Created by LouisTien on 2019/8/14.
 */
data class GuestWiFiInfo
(
    val Object: GuestWiFiInfoObject = GuestWiFiInfoObject(),
    val oper_status: String = "N/A",
    val requested_path: String = "N/A"
)

data class GuestWiFiInfoObject
(
    val Enable: Boolean = false,
    val LowerLayers: String = "N/A",
    val SSID: String = "N/A",
    val Status: String = "N/A",
    val X_ZYXEL_MainSSID: Boolean = false
)