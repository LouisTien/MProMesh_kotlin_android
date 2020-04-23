package zyxel.com.multyproneo.api.cloud

import zyxel.com.multyproneo.util.AppConfig

object P2PGatewayApi
{
    class GetSystemInfo : TUTKP2PCommander()
    {
        override fun requestURL(): String
        {
            return "{\"URI\":\"${AppConfig.API_SYSTEM_INFO}\"}"
        }

        override fun method(): AppConfig.TUTKP2PMethod
        {
            return AppConfig.TUTKP2PMethod.MTD_GET
        }
    }

    class GetFWVersionInfo : TUTKP2PCommander()
    {
        override fun requestURL(): String
        {
            return "{\"URI\":\"${AppConfig.API_DEVICE_INFO}\"}"
        }

        override fun method(): AppConfig.TUTKP2PMethod
        {
            return AppConfig.TUTKP2PMethod.MTD_GET
        }
    }

    class GetLanIPInfo : TUTKP2PCommander()
    {
        override fun requestURL(): String
        {
            return "{\"URI\":\"${AppConfig.API_IP_INTERFACE_INFO}\"}"
        }

        override fun method(): AppConfig.TUTKP2PMethod
        {
            return AppConfig.TUTKP2PMethod.MTD_GET
        }
    }

    class GetWanInfo : TUTKP2PCommander()
    {
        override fun requestURL(): String
        {
            return "{\"URI\":\"${AppConfig.API_WAN_INFO}\"}"
        }

        override fun method(): AppConfig.TUTKP2PMethod
        {
            return AppConfig.TUTKP2PMethod.MTD_GET
        }
    }

    class GetFSecureInfo : TUTKP2PCommander()
    {
        override fun requestURL(): String
        {
            return "{\"URI\":\"${AppConfig.API_FSECURE_INFO}\"}"
        }

        override fun method(): AppConfig.TUTKP2PMethod
        {
            return AppConfig.TUTKP2PMethod.MTD_GET
        }
    }

    class GetHostNameReplaceInfo : TUTKP2PCommander()
    {
        override fun requestURL(): String
        {
            return "{\"URI\":\"${AppConfig.API_HOST_NAME_REPLACE_INFO}\"}"
        }

        override fun method(): AppConfig.TUTKP2PMethod
        {
            return AppConfig.TUTKP2PMethod.MTD_GET
        }
    }

    class GetInternetBlockingInfo : TUTKP2PCommander()
    {
        override fun requestURL(): String
        {
            return "{\"URI\":\"${AppConfig.API_INTERNET_BLOCKING_INFO}\"}"
        }

        override fun method(): AppConfig.TUTKP2PMethod
        {
            return AppConfig.TUTKP2PMethod.MTD_GET
        }
    }

    class SetSystemInfo : TUTKP2PCommander()
    {
        override fun requestURL(): String
        {
            return "{\"URI\":\"${AppConfig.API_SYSTEM_INFO}\"${getRequestPayload()}}"
        }

        override fun method(): AppConfig.TUTKP2PMethod
        {
            return AppConfig.TUTKP2PMethod.MTD_PUT
        }
    }

    class GatewayReboot : TUTKP2PCommander()
    {
        override fun requestURL(): String
        {
            return "{\"URI\":\"${AppConfig.API_REBOOT}\"}"
        }

        override fun method(): AppConfig.TUTKP2PMethod
        {
            return AppConfig.TUTKP2PMethod.MTD_POST
        }
    }

    class GetRssiInfo(val index: Int = 0) : TUTKP2PCommander()
    {
        override fun requestURL(): String
        {
            return "{\"URI\":\"${AppConfig.API_DEVICE_HOST_INFO}$index.\"}"
        }

        override fun method(): AppConfig.TUTKP2PMethod
        {
            return AppConfig.TUTKP2PMethod.MTD_GET
        }
    }

    class VerifyCloudAgent : TUTKP2PCommander()
    {
        override fun requestURL(): String
        {
            return "{\"URI\":\"${AppConfig.API_CLOUD_AUTH}\"${getRequestPayload()}}"
        }

        override fun method(): AppConfig.TUTKP2PMethod
        {
            return AppConfig.TUTKP2PMethod.MTD_POST
        }
    }
}