package zyxel.com.multyproneo.event

import zyxel.com.multyproneo.model.cloud.AllDeviceInfo

/**
 * Created by LouisTien on 2019/5/30.
 */
class GatewayListEvent
{
    class OnDeviceSelected(var index: Int)
    class OnDeviceDelete(var index: Int)
    class ConfirmDeviceDelete(var info: AllDeviceInfo, var backup: Boolean)
}