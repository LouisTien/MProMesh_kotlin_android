package zyxel.com.multyproneo.model

/**
 * Created by LouisTien on 2019/7/29.
 */
data class WiFiSettingInfo
(
    val Object: WiFiSettingObject = WiFiSettingObject(),
    val oper_status: String = "N/A",
    val requested_path: String = "N/A"
)

data class WiFiSettingObject
(
    val AccessPoint: List<AccessPoint> = listOf(AccessPoint()),
    val AccessPointNumberOfEntries: Int = 0,
    val Radio: List<Radio> = listOf(Radio()),
    val RadioNumberOfEntries: Int = 0,
    val SSID: List<SSID> = listOf(SSID()),
    val SSIDNumberOfEntries: Int = 0,
    val X_ZYXEL_ApSteering: XZYXELApSteering = XZYXELApSteering(),
    val X_ZYXEL_OneSSID: XZYXELOneSSID = XZYXELOneSSID()
)

data class XZYXELOneSSID
(
    val Enable: Boolean = false
)

data class Radio
(
    val Enable: Boolean = false,
    val OperatingFrequencyBand: String = "N/A",
    val OperatingStandards: String = "N/A",
    val Stats: Stats = Stats(),
    val Status: String = "N/A",
    val SupportedStandards: String = "N/A"
)

data class Stats
(
    val BytesReceived: Long = 0,
    val BytesSent: Long = 0,
    val DiscardPacketsReceived: Long = 0,
    val DiscardPacketsSent: Long = 0,
    val ErrorsReceived: Long = 0,
    val ErrorsSent: Long = 0,
    val PacketsReceived: Long = 0,
    val PacketsSent: Long = 0,
    val X_ZYXEL_Rate: String = "N/A"
)

data class AccessPoint
(
    val AssociatedDevice: List<Any> = listOf(Any()),
    val AssociatedDeviceNumberOfEntries: Int = 0,
    val IsolationEnable: Boolean = false,
    val MaxAssociatedDevices: Int = 0,
    val SSIDReference: String = "N/A",
    val Security: Security = Security()
)

data class Security
(
    val KeyPassphrase: String = "N/A",
    val ModeEnabled: String = "N/A",
    val ModesSupported: String = "N/A",
    val PreSharedKey: String = "N/A",
    val X_ZYXEL_AutoGenPSK: Boolean = false
)

data class SSID
(
    val Enable: Boolean = false,
    val LowerLayers: String = "N/A",
    val SSID: String = "N/A",
    val Status: String = "N/A",
    val X_ZYXEL_GuestAPs: String = "N/A",
    val X_ZYXEL_MainSSID: Boolean = false
)

data class XZYXELApSteering
(
    val ClearSteeringStatus: Int = 0,
    val DisableSteering: Boolean = false,
    val Enable: Boolean = false,
    val Idle_Pkt_Threshold: Int = 0,
    val LoadBalancingInterval: Int = 0,
    val LogLevel: Int = 0,
    val ProhibitAfterSteering: Int = 0,
    val Prohibit_Steering_OverWDS_AP: Boolean = false,
    val RE_Threshold_Adjustment: Int = 0,
    val STA_Num_Difference_Threshold: Int = 0,
    val ScanOtherAPsInterval: Int = 0,
    val SteeringTime: Int = 0,
    val WiFi_24G_NormalToBad_Threshold: Int = 0,
    val WiFi_24G_NormalToGood_Threshold: Int = 0,
    val WiFi_24G_Upgrade_Threshold: Int = 0,
    val WiFi_5G_NormalToBad_Threshold: Int = 0,
    val WiFi_5G_NormalToGood_Threshold: Int = 0
)