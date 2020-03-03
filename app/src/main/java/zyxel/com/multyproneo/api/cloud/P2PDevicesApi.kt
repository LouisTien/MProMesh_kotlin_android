package zyxel.com.multyproneo.api.cloud

import zyxel.com.multyproneo.util.AppConfig

object P2PDevicesApi
{
    class GetDevicesInfo : TUTKP2PCommander()
    {
        override fun requestURL(): String
        {
            return "{\"URI\":\"/TR181/Value/Device.Hosts.Host.\"}"
        }

        override fun method(): AppConfig.TUTKP2PMethod
        {
            return AppConfig.TUTKP2PMethod.MTD_GET
        }
    }
}