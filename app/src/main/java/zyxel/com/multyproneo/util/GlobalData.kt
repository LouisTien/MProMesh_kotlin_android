package zyxel.com.multyproneo.util

import zyxel.com.multyproneo.model.GatewayProfile

/**
 * Created by LouisTien on 2019/5/30.
 */
class GlobalData
{

    companion object
    {
        var currentGatewayIndex = 0
        var gatewayProfileArrayList = arrayListOf<GatewayProfile>()

        fun getCurrentGatewayInfo(): GatewayProfile
        {
            return gatewayProfileArrayList.get(currentGatewayIndex)
        }
    }
}