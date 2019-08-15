package zyxel.com.multyproneo.model

/**
 * Created by LouisTien on 2019/8/13.
 */
data class ChangeIconNameInfo
(
    val Object: List<ChangeIconNameInfoObject> = listOf(ChangeIconNameInfoObject()),
    val oper_status: String = "N/A",
    val requested_path: String = "N/A"
)

data class ChangeIconNameInfoObject
(
    val HostName: String = "N/A",
    val MacAddress: String = "N/A",
    val DeviceIcon: String = "N/A",
    val Internet_Blocking_Enable: Boolean = false,
    val BrowsingProtection: Boolean = false,
    val TrackingProtection: Boolean = false,
    val IOTProtection: Boolean = false
)