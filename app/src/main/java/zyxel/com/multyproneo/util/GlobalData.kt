package zyxel.com.multyproneo.util

import zyxel.com.multyproneo.model.*

/**
 * Created by LouisTien on 2019/5/30.
 */
object GlobalData
{
    var sessionKey = ""
    var cookie = ""
    var currentGatewayIndex = 0
    //var gatewayProfileMutableList = mutableListOf<GatewayProfile>()
    var gatewayList = mutableListOf<GatewayInfo>()
    /*var endDeviceList = mutableListOf<DevicesInfoObject>()
    var ZYXELEndDeviceList = mutableListOf<DevicesInfoObject>()
    var guestEndDeviceList = mutableListOf<DevicesInfoObject>()
    var homeEndDeviceList = mutableListOf<DevicesInfoObject>()*/
    var endDeviceList = mutableListOf<EndDeviceProfile>()
    var ZYXELEndDeviceList = mutableListOf<EndDeviceProfile>()
    var guestEndDeviceList = mutableListOf<EndDeviceProfile>()
    var homeEndDeviceList = mutableListOf<EndDeviceProfile>()
    var gatewayWanInfo = WanInfoProfile()
    var gatewayLanIP = ""
    var guestWiFiStatus = false
    var homeDevAscendingOrder = true
    var guestDevAscendingOrder = true

    fun getCurrentGatewayInfo(): GatewayInfo = gatewayList[currentGatewayIndex]
    fun getDeviceIP(): String = getCurrentGatewayInfo().IP
    fun getDevicePort(): String = getCurrentGatewayInfo().SupportedApiVersion[0].HttpsPort.toString()
    fun getProtocol(): String = getCurrentGatewayInfo().SupportedApiVersion[0].Protocol
    fun getAPIVersion(): String = getCurrentGatewayInfo().SupportedApiVersion[0].LoginURI.substring(0, getCurrentGatewayInfo().SupportedApiVersion[0].LoginURI.lastIndexOf("/"))
    fun getAPIPath(): String = "${GlobalData.getProtocol()}://${GlobalData.getDeviceIP()}:${GlobalData.getDevicePort()}${GlobalData.getAPIVersion()}"
    fun getConnectDeviceCount(): Int = endDeviceList.size - (ZYXELEndDeviceList.size - 1)
    fun getTotalDeviceCount(): Int = homeEndDeviceList.size + guestEndDeviceList.size

    fun getActivatedDeviceCount(): Int
    {
        /*var count = 0
        for(item in homeEndDeviceList)
        {
            if(item.Active) count++
        }

        for(item in guestEndDeviceList)
        {
            if(item.Active) count++
        }
        return count*/
        return 0
    }

    fun sortHomeDeviceList() = if(homeDevAscendingOrder) sortHomeDevAscendingOrder() else sortHomeDevDescendingOrder()

    private fun sortHomeDevAscendingOrder()
    {
        homeDevAscendingOrder = true
        homeEndDeviceList.sortBy { it.Name }
    }

    private fun sortHomeDevDescendingOrder()
    {
        homeDevAscendingOrder = false
        homeEndDeviceList.sortByDescending { it.Name }
    }

    fun sortGuestDeviceList() = if(guestDevAscendingOrder) sortGuestDevAscendingOrder() else sortGuestDevDescendingOrder()

    private fun sortGuestDevAscendingOrder()
    {
        guestDevAscendingOrder = true
        guestEndDeviceList.sortBy { it.Name }
    }

    private fun sortGuestDevDescendingOrder()
    {
        guestDevAscendingOrder = false
        guestEndDeviceList.sortByDescending { it.Name }
    }
}