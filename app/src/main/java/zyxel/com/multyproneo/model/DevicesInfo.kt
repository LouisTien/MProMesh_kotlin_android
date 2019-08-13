package zyxel.com.multyproneo.model

import java.io.Serializable

/**
 * Created by LouisTien on 2019/8/12.
 */
data class DevicesInfo
(
    val Object: List<DevicesInfoObject> = listOf(DevicesInfoObject()),
    val oper_status: String = "N/A",
    val requested_path: String = "N/A"
)

data class DevicesInfoObject
(
    val Active: Boolean = false,
    val AddressSource: String = "N/A",
    val AssociatedDevice: String = "N/A",
    val ClientDuid: String = "N/A",
    val ClientID: String = "N/A",
    val DHCPClient: String = "N/A",
    val ExpireTime: String = "N/A",
    val HostName: String = "N/A",
    val IPAddress: String = "N/A",
    val IPAddress6: String = "N/A",
    val IPLinkLocalAddress6: String = "N/A",
    val Layer1Interface: String = "N/A",
    val Layer3Interface: String = "N/A",
    val LeaseTimeRemaining: Int = 0,
    val PhysAddress: String = "N/A",
    val UserClassID: String = "N/A",
    val VendorClassID: String = "N/A",
    val X_ZYXEL_Address6Source: String = "N/A",
    val X_ZYXEL_Band: Int = 0,
    val X_ZYXEL_BytesReceived: Int = 0,
    val X_ZYXEL_BytesSent: Int = 0,
    val X_ZYXEL_CapabilityType: String = "N/A",
    val X_ZYXEL_Channel_24G: Int = 0,
    val X_ZYXEL_Channel_5G: Int = 0,
    val X_ZYXEL_Conn_Guest: Int = 0,
    val X_ZYXEL_ConnectedAP: String = "N/A",
    val X_ZYXEL_ConnectionType: String = "N/A",
    val X_ZYXEL_DHCP6Client: String = "N/A",
    val X_ZYXEL_DHCPLeaseTime: Int = 0,
    val X_ZYXEL_DeleteLease: Boolean = false,
    val X_ZYXEL_HostType: String = "N/A",
    val X_ZYXEL_Neighbor: String = "N/A",
    val X_ZYXEL_PhyRate: Int = 0,
    val X_ZYXEL_RSSI: Int = 0,
    val X_ZYXEL_RSSI_STAT: String = "N/A",
    val X_ZYXEL_SNR: Int = 0,
    val X_ZYXEL_SignalStrength: Int = 0,
    val X_ZYXEL_SoftwareVersion: String = "N/A",
    val X_ZYXEL_WiFiStatus: Boolean = false,
    var UserDefineName: String = "N/A", //ChangeIconInfoObject.HostName
    var MacAddress: String = "N/A", //ChangeIconInfoObject.MacAddress
    var Internet_Blocking_Enable: Int = 0 //ChangeIconInfoObject.Internet_Blocking_Enable
) : Serializable
{
    fun getName(): String
    {
        if(UserDefineName == "N/A")
            return HostName
        else
            return UserDefineName
    }
}