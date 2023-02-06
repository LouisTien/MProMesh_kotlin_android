package zyxel.com.multyproneo.util

import zyxel.com.multyproneo.TreeNode
import zyxel.com.multyproneo.model.*
import zyxel.com.multyproneo.model.cloud.TUTKAllDeviceInfo
import java.io.File

/**
 * Created by LouisTien on 2019/5/30.
 */
object GlobalData
{
    var cookie = ""
    var currentFrag = ""
    var currentGatewayIndex = 0
    var diagnosticCurrentFrag = ""
    var gatewayList = mutableListOf<GatewayInfo>()
    var endDeviceList = mutableListOf<DevicesInfoObject>()
    var ZYXELEndDeviceList = mutableListOf<DevicesInfoObject>()
    var guestEndDeviceList = mutableListOf<DevicesInfoObject>()
    var homeEndDeviceList = mutableListOf<DevicesInfoObject>()
    var layer2EndDeviceList = mutableListOf<TreeNode<DevicesInfoObject>>()
    var layer3EndDeviceList = mutableListOf<TreeNode<DevicesInfoObject>>()
    var layer4EndDeviceList = mutableListOf<TreeNode<DevicesInfoObject>>()
    var changeIconNameList = mutableListOf<ChangeIconNameInfoObject>()
    var parentalControlInfoProfileList = mutableListOf<ParentalControlInfoProfile>()
    var parentalControlSelectedDeviceList = mutableListOf<DevicesInfoObject>()
    var gatewayWanInfo = WanInfo()
    var guestWiFiStatus = false
    var homeDevAscendingOrder = true
    var guestDevAscendingOrder = true
    var scanSSID = ""
    var scanPWD = ""
    var scanAccount = ""
    var scanAccountPWD = ""
    var notiUid = ""
    var notiMac = ""
    var customerLogo = ""
    var logFileDeliver = false
    var registeredCloud = false
    var L2DeviceNumber = 0
    var alreadyGetGatewayInfoLocalBase = false
    var loginInfo = LoginInfo()
    var parentalControlMasterSwitch: Boolean = false
    var parentalControlProfilePicDir: File? = null
    var parentalControlProfileFirstEmptyIndex = 0
    var gatewaySystemDate = GatewaySystemDate()

    //TUTK
    var tokenType = "Bearer"
    var currentUID = ""
    var currentEmail = ""
    var currentDisplayName = ""
    var currentCredential = ""
    var cloudGatewayListInfo = TUTKAllDeviceInfo()

    //Model
    var AX7501_B1 = "AX7501-B1"
    var DX3300_T1 = "DX3300-T1"
    var DX3301_T0 = "DX3301-T0"
    var EMG3525_T50B = "EMG3525-T50B"
    var EMG3525_T50C = "EMG3525-T50C"
    var EMG5523_T50B = "EMG5523-T50B"
    var EMG5723_T50K = "EMG5723-T50K"
    var EX3200_T0 = "EX3200-T0"
    var EX3300_T1 = "EX3300-T1"
    var EX3301_T0 = "EX3301-T0"
    var EX5510_B0 = "EX5510-B0"
    var EX5600_T0 = "EX5600-T0"
    var EX5601_T0 = "EX5601-T0"
    var EX7501_B0 = "EX7501-B0"
    var PX5111_T0 = "PX5111-T0"
    var PX5501_B1 = "PX5501-B1"
    var VMG3625_T50B = "VMG3625-T50B"
    var VMG3625_T50C = "VMG3625-T50C"
    var VMG3927_T50K = "VMG3927-T50K"
    var VMG8623_T50B = "VMG8623-T50B"
    var VMG8825_T50K = "VMG8825-T50K"
    var WAP6807 = "WAP6807"
    var WX3100_T0 = "WX3100-T0"
    var WX5600_T0 = "WX5600-T0"


    fun getCurrentGatewayInfo(): GatewayInfo = gatewayList[currentGatewayIndex]
    fun getDeviceIP(): String = getCurrentGatewayInfo().IP
    fun getDevicePort(): String = getCurrentGatewayInfo().SupportedApiVersion[0].HttpsPort.toString()
    fun getProtocol(): String = getCurrentGatewayInfo().SupportedApiVersion[0].Protocol
    fun getAPIVersion(): String = getCurrentGatewayInfo().SupportedApiVersion[0].LoginURI.substring(0, getCurrentGatewayInfo().SupportedApiVersion[0].LoginURI.lastIndexOf("/"))
    fun getAPIPath(): String = "${getProtocol()}://${getDeviceIP()}:${getDevicePort()}${getAPIVersion()}"
    fun getTotalDeviceCount(): Int = homeEndDeviceList.size + guestEndDeviceList.size

    fun getActivatedDeviceCount(): Int
    {
        var count = 0
        for(item in homeEndDeviceList)
        {
            if(item.Active) count++
        }

        for(item in guestEndDeviceList)
        {
            if(item.Active) count++
        }
        return count
    }

    fun sortHomeDeviceList() = if(homeDevAscendingOrder) sortHomeDevAscendingOrder() else sortHomeDevDescendingOrder()

    private fun sortHomeDevAscendingOrder()
    {
        homeDevAscendingOrder = true
        homeEndDeviceList.sortBy{ it.getName().toUpperCase() }
    }

    private fun sortHomeDevDescendingOrder()
    {
        homeDevAscendingOrder = false
        homeEndDeviceList.sortByDescending{ it.getName().toUpperCase() }
    }

    fun sortGuestDeviceList() = if(guestDevAscendingOrder) sortGuestDevAscendingOrder() else sortGuestDevDescendingOrder()

    private fun sortGuestDevAscendingOrder()
    {
        guestDevAscendingOrder = true
        guestEndDeviceList.sortBy{ it.getName().toUpperCase() }
    }

    private fun sortGuestDevDescendingOrder()
    {
        guestDevAscendingOrder = false
        guestEndDeviceList.sortByDescending{ it.getName().toUpperCase() }
    }

    fun isSupportMultiObjects(): Boolean
    {
        for(i in loginInfo.MethodList)
        {
            if(i.contains("MultiObjects"))
                return true
        }

        return false
    }

    fun isSupportAPPUICustomization(): Boolean
    {
        for(i in loginInfo.MethodList)
        {
            if(i.contains("APPUICustomization"))
                return true
        }

        return false
    }
}