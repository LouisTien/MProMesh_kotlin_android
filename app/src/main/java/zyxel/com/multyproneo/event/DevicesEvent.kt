package zyxel.com.multyproneo.event

/**
 * Created by LouisTien on 2019/6/5.
 */
class DevicesEvent
{
    class GetDeviceInfoComplete()
    class GetCloudDeviceInfoComplete()
    class MeshDevicePlacementStatus(var isHomePage: Boolean)
    class ShowTips()
}