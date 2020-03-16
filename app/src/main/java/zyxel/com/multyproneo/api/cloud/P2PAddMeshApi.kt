package zyxel.com.multyproneo.api.cloud

import zyxel.com.multyproneo.util.AppConfig

object P2PAddMeshApi
{
    class StartPairing : TUTKP2PCommander()
    {
        override fun requestURL(): String
        {
            return "{\"URI\":\"${AppConfig.API_MESH}\"${getRequestPayload()}}"
        }

        override fun method(): AppConfig.TUTKP2PMethod
        {
            return AppConfig.TUTKP2PMethod.MTD_PUT
        }
    }

    class GetWPSStatus : TUTKP2PCommander()
    {
        override fun requestURL(): String
        {
            return "{\"URI\":\"${AppConfig.API_MESH}\"}"
        }

        override fun method(): AppConfig.TUTKP2PMethod
        {
            return AppConfig.TUTKP2PMethod.MTD_GET
        }
    }
}