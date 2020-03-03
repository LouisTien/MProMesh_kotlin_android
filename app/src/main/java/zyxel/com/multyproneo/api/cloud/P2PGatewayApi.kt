package zyxel.com.multyproneo.api.cloud

import zyxel.com.multyproneo.util.AppConfig

object P2PGatewayApi
{
    class GetSystemInfo : TUTKP2PCommander()
    {
        override fun requestURL(): String
        {
            return "{\"URI\":\"/TR181/Value/Device.X_ZYXEL_System_Info.\"}"
        }

        override fun method(): AppConfig.TUTKP2PMethod
        {
            return AppConfig.TUTKP2PMethod.MTD_GET
        }
    }
}