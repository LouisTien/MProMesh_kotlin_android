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
        var homeDevAscendingOrder = true
        var guestDevAscendingOrder = true

        fun getCurrentGatewayInfo(): GatewayProfile = gatewayProfileMutableList[currentGatewayIndex]
        fun getConnectDeviceCount(): Int = endDeviceList.size - (ZYXELEndDeviceList.size - 1)
        fun getTotalDeviceCount(): Int = homeEndDeviceList.size + guestEndDeviceList.size

        fun getActivatedDeviceCount(): Int
        {
            var count = 0
            for(item in homeEndDeviceList)
            {
                if(item.Active.equals("Connect", ignoreCase = true)) count++
            }

            for(item in guestEndDeviceList)
            {
                if(item.Active.equals("Connect", ignoreCase = true)) count++
            }
            return count
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
}