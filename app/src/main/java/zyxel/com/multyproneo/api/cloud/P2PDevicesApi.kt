package zyxel.com.multyproneo.api.cloud

import zyxel.com.multyproneo.util.AppConfig

object P2PDevicesApi
{
    class GetDevicesInfo : TUTKP2PCommander()
    {
        override fun requestURL(): String
        {
            return "{\"URI\":\"${AppConfig.API_DEVICE_HOST_INFO}\"}"
        }

        override fun method(): AppConfig.TUTKP2PMethod
        {
            return AppConfig.TUTKP2PMethod.MTD_GET
        }
    }

    class GetChangeIconNameInfo : TUTKP2PCommander()
    {
        override fun requestURL(): String
        {
            return "{\"URI\":\"${AppConfig.API_CHANGE_ICON_NAME_INFO}\"}"
        }

        override fun method(): AppConfig.TUTKP2PMethod
        {
            return AppConfig.TUTKP2PMethod.MTD_GET
        }
    }

    class SetChangeIconNameInfo : TUTKP2PCommander()
    {
        override fun requestURL(): String
        {
            return "{\"URI\":\"${AppConfig.API_CHANGE_ICON_NAME_INFO}\"${getRequestPayload()}}"
        }

        override fun method(): AppConfig.TUTKP2PMethod
        {
            return AppConfig.TUTKP2PMethod.MTD_POST
        }
    }

    class SetChangeIconNameInfoByIndex(val index: Int = 0) : TUTKP2PCommander()
    {
        override fun requestURL(): String
        {
            return "{\"URI\":\"${AppConfig.API_CHANGE_ICON_NAME_INFO}$index\"${getRequestPayload()}}"
        }

        override fun method(): AppConfig.TUTKP2PMethod
        {
            return AppConfig.TUTKP2PMethod.MTD_POST
        }
    }

    class EndDeviceReboot(val index: Int = 0) : TUTKP2PCommander()
    {
        override fun requestURL(): String
        {
            return "{\"URI\":\"${AppConfig.API_DEVICE_HOST_INFO}$index.X_ZYXEL_EXT.\"${getRequestPayload()}}"
        }

        override fun method(): AppConfig.TUTKP2PMethod
        {
            return AppConfig.TUTKP2PMethod.MTD_POST
        }
    }
}