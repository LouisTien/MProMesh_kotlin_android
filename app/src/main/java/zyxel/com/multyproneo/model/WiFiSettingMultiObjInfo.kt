package zyxel.com.multyproneo.model

data class WiFiSettingMultiObjInfo
(
    val Object: List<WiFiSettingMultiObjInfoObject> = listOf(WiFiSettingMultiObjInfoObject()),
    val oper_status: String = "N/A",
    val requested_path: String = "N/A",
    val sessionkey: String = "N/A"
)

data class WiFiSettingMultiObjInfoObject
(
    val Enable: Boolean = false,
    val KeyPassphrase: String = "N/A",
    val LowerLayers: String = "N/A",
    val ModeEnabled: String = "N/A",
    val ModesSupported: String = "N/A",
    val PreSharedKey: String = "N/A",
    val SSID: String = "N/A",
    val Status: String = "N/A",
    val X_ZYXEL_AutoGenPSK: Boolean = false,
    val X_ZYXEL_MainSSID: Boolean = false,
    val oper_status: String = "N/A",
    val requested_path: String = "N/A"
)