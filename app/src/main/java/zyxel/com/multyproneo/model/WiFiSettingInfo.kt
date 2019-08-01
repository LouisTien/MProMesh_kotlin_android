package zyxel.com.multyproneo.model

/**
 * Created by LouisTien on 2019/7/29.
 */
data class WiFiSettingInfo
(
    val Object: Object,
    val oper_status: String,
    val requested_path: String
)

data class Object
(
    val AccessPoint: List<AccessPoint>,
    val AccessPointNumberOfEntries: Int,
    val Radio: List<Radio>,
    val RadioNumberOfEntries: Int,
    val SSID: List<SSID>,
    val SSIDNumberOfEntries: Int,
    val X_ZYXEL_ApSteering: XZYXELApSteering,
    val X_ZYXEL_OneSSID: XZYXELOneSSID
)

data class XZYXELOneSSID
(
    val Enable: Boolean
)

data class Radio
(
    val Enable: Boolean,
    val OperatingFrequencyBand: String,
    val OperatingStandards: String,
    val Stats: Stats,
    val Status: String,
    val SupportedStandards: String
)

data class Stats
(
    val BytesReceived: Int,
    val BytesSent: Int,
    val DiscardPacketsReceived: Int,
    val DiscardPacketsSent: Int,
    val ErrorsReceived: Int,
    val ErrorsSent: Int,
    val PacketsReceived: Int,
    val PacketsSent: Int,
    val X_ZYXEL_Rate: String
)

data class AccessPoint
(
    val AssociatedDevice: List<Any>,
    val AssociatedDeviceNumberOfEntries: Int,
    val IsolationEnable: Boolean,
    val MaxAssociatedDevices: Int,
    val SSIDReference: String,
    val Security: Security
)

data class Security
(
    val KeyPassphrase: String,
    val ModeEnabled: String,
    val ModesSupported: String,
    val PreSharedKey: String,
    val X_ZYXEL_AutoGenPSK: Boolean
)

data class SSID
(
    val Enable: Boolean,
    val LowerLayers: String,
    val SSID: String,
    val Status: String,
    val X_ZYXEL_GuestAPs: String,
    val X_ZYXEL_MainSSID: Boolean
)

data class XZYXELApSteering
(
    val ClearSteeringStatus: Int,
    val DisableSteering: Boolean,
    val Enable: Boolean,
    val Idle_Pkt_Threshold: Int,
    val LoadBalancingInterval: Int,
    val LogLevel: Int,
    val ProhibitAfterSteering: Int,
    val Prohibit_Steering_OverWDS_AP: Boolean,
    val RE_Threshold_Adjustment: Int,
    val STA_Num_Difference_Threshold: Int,
    val ScanOtherAPsInterval: Int,
    val SteeringTime: Int,
    val WiFi_24G_NormalToBad_Threshold: Int,
    val WiFi_24G_NormalToGood_Threshold: Int,
    val WiFi_24G_Upgrade_Threshold: Int,
    val WiFi_5G_NormalToBad_Threshold: Int,
    val WiFi_5G_NormalToGood_Threshold: Int
)