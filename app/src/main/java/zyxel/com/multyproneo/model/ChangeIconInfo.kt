package zyxel.com.multyproneo.model

/**
 * Created by LouisTien on 2019/8/13.
 */
data class ChangeIconInfo
(
    val Object: List<ChangeIconInfoObject> = listOf(ChangeIconInfoObject()),
    val oper_status: String = "N/A",
    val requested_path: String = "N/A"
)

data class ChangeIconInfoObject
(
    val HostName: String = "N/A",
    val MacAddress: String = "N/A",
    val DeviceIcon: String = "N/A",
    val Internet_Blocking_Enable: Int = 0,
    val BrowsingProtection: Int = 0,
    val TrackingProtection: Int = 0,
    val IOTProtection: Int = 0
)