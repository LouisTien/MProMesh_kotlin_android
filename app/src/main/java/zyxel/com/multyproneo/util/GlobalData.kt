package zyxel.com.multyproneo.util

import zyxel.com.multyproneo.model.EndDeviceProfile
import zyxel.com.multyproneo.model.GatewayProfile
import zyxel.com.multyproneo.model.WanInfoProfile

/**
 * Created by LouisTien on 2019/5/30.
 */
class GlobalData
{

    companion object
    {
        var currentGatewayIndex = 0
        var gatewayProfileMutableList = mutableListOf<GatewayProfile>()
        var endDeviceList = mutableListOf<EndDeviceProfile>()
        var ZYXELEndDeviceList = mutableListOf<EndDeviceProfile>()
        var guestEndDeviceList = mutableListOf<EndDeviceProfile>()
        var homeEndDeviceList = mutableListOf<EndDeviceProfile>()
        var gatewayWanInfo = WanInfoProfile()
        var gatewayLanIP = ""
        var guestWiFiStatus = false

        fun getCurrentGatewayInfo(): GatewayProfile = gatewayProfileMutableList.get(currentGatewayIndex)
        fun getConnectDeviceCount(): Int = endDeviceList.size - (ZYXELEndDeviceList.size - 1)
    }
}