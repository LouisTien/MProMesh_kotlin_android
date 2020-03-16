package zyxel.com.multyproneo.api.cloud

import zyxel.com.multyproneo.util.AppConfig

object P2PWiFiSettingApi
{
    class GetWiFiSettingInfo : TUTKP2PCommander()
    {
        override fun requestURL(): String
        {
            return "{\"URI\":\"${AppConfig.API_WIFI_SETTING_INFO}\"}"
        }

        override fun method(): AppConfig.TUTKP2PMethod
        {
            return AppConfig.TUTKP2PMethod.MTD_GET
        }
    }

    class GetGuestWiFi24GInfo : TUTKP2PCommander()
    {
        override fun requestURL(): String
        {
            return "{\"URI\":\"${AppConfig.API_GUEST_WIFI_24G_INFO}\"}"
        }

        override fun method(): AppConfig.TUTKP2PMethod
        {
            return AppConfig.TUTKP2PMethod.MTD_GET
        }
    }

    class SetGuestWiFi24GInfo : TUTKP2PCommander()
    {
        override fun requestURL(): String
        {
            return "{\"URI\":\"${AppConfig.API_GUEST_WIFI_24G_INFO}\"${getRequestPayload()}}"
        }

        override fun method(): AppConfig.TUTKP2PMethod
        {
            return AppConfig.TUTKP2PMethod.MTD_PUT
        }
    }

    class SetGuestWiFi5GInfo : TUTKP2PCommander()
    {
        override fun requestURL(): String
        {
            return "{\"URI\":\"${AppConfig.API_GUEST_WIFI_5G_INFO}\"${getRequestPayload()}}"
        }

        override fun method(): AppConfig.TUTKP2PMethod
        {
            return AppConfig.TUTKP2PMethod.MTD_PUT
        }
    }
}