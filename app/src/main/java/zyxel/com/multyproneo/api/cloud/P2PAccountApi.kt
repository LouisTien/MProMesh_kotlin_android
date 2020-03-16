package zyxel.com.multyproneo.api.cloud

import zyxel.com.multyproneo.util.AppConfig

object P2PAccountApi
{
    class Logout : TUTKP2PCommander()
    {
        override fun requestURL(): String
        {
            return "{\"URI\":\"${AppConfig.API_LOGOUT}\"}"
        }

        override fun method(): AppConfig.TUTKP2PMethod
        {
            return AppConfig.TUTKP2PMethod.MTD_POST
        }
    }
}