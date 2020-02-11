package zyxel.com.multyproneo.util

import zyxel.com.multyproneo.model.*

/**
 * Created by LouisTien on 2019/5/30.
 */
object GlobalData
{
    var sessionKey = ""
    var cookie = ""
    var currentFrag = ""
    var currentGatewayIndex = 0
    var gatewayList = mutableListOf<GatewayInfo>()
    var endDeviceList = mutableListOf<DevicesInfoObject>()
    var ZYXELEndDeviceList = mutableListOf<DevicesInfoObject>()
    var guestEndDeviceList = mutableListOf<DevicesInfoObject>()
    var homeEndDeviceList = mutableListOf<DevicesInfoObject>()
    var changeIconNameList = mutableListOf<ChangeIconNameInfoObject>()
    var gatewayWanInfo = WanInfo()
    var guestWiFiStatus = false
    var homeDevAscendingOrder = true
    var guestDevAscendingOrder = true
    var scanSSID = ""
    var scanPWD = ""
    var scanAccount = ""
    var scanAccountPWD = ""

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
}