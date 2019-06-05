package zyxel.com.multyproneo.model

/**
 * Created by LouisTien on 2019/6/4.
 */
data class EndDeviceProfile
(
        var MAC: String = "N/A",
        var Name:String = "N/A",
        var UserDefineName: String = "N/A",
        var Active: String = "N/A",
        var Blocking: String = "N/A",
        var IPAddress: String = "N/A",
        var ConnectionType: String = "N/A",
        var CapabilityType: String = "N/A",
        var SoftwareVersion: String = "N/A",
        var HostType: Int = 0,
        var L2AutoConfigEnable: Int = 0,
        var L2WifiStatus: Int = 0,
        var L2WifiSyncStatus: Int = 0,
        var SignalStrength: Int = 0,
        var PhyRate: Int = 0,
        var L2ControllableMask: Int = 0,
        var Neighbor: String = "N/A",
        var DLRate: Int = 0,
        var ULRate: Int = 0,
        var GuestGroup: Int = 0,
        var WiFiAutoConfigApprove: Int = 0,
        var Manufacturer: String = "N/A",
        var Rssi: Int = 0,
        var Band: Int = 0,
        var LinkRate24G: Int = 0,
        var LinkRate5G: Int = 0,
        var Channel24G: Int = 0,
        var Channel5G: Int = 0,
        var DeviceMode: String = "N/A",
        var NewDeviceFlag: Int = 0,
        var ResetDeviceFlag: Int = 0,
        var InternetAccess: String = "N/A",
        var RssiRealFlag: Int = 0,
        var RssiValue: String = "N/A",
        var dhcpLeaseTime: String = "N/A",
        var ParentalControlBlocking: String = "N/A"
){}