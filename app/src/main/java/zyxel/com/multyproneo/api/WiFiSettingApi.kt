package zyxel.com.multyproneo.api

import okhttp3.MediaType
import okhttp3.Request
import okhttp3.RequestBody
import zyxel.com.multyproneo.util.AppConfig
import zyxel.com.multyproneo.util.GlobalData

/**
 * Created by LouisTien on 2019/7/29.
 */
object WiFiSettingApi
{
    private val JSON = MediaType.parse("application/json; charset=utf-8")

    class GetWiFiSettingInfo : Commander()
    {
        override fun composeRequest(): Request
        {
            val getWiFiSettingInfoURL = "${GlobalData.getAPIPath()}${AppConfig.API_WIFI_SETTING_INFO}"
            return Request.Builder()
                    .addHeader("Cookie", GlobalData.cookie)
                    .url(getWiFiSettingInfoURL)
                    .build()
        }
    }

    class GetMeshInfo : Commander()
    {
        override fun composeRequest(): Request
        {
            val getMeshInfoURL = "${GlobalData.getAPIPath()}${AppConfig.API_MESH_INFO}"
            return Request.Builder()
                    .addHeader("Cookie", GlobalData.cookie)
                    .url(getMeshInfoURL)
                    .build()
        }
    }

    class SetWiFi24GInfo : Commander()
    {
        override fun composeRequest(): Request
        {
            val setWiFi24GInfoURL = "${GlobalData.getAPIPath()}${AppConfig.API_WIFI_24G_INFO}?sessionkey=${GlobalData.sessionKey}"
            val requestParam = RequestBody.create(JSON, getParams().toString())
            return Request.Builder()
                    .addHeader("Cookie", GlobalData.cookie)
                    .url(setWiFi24GInfoURL)
                    .put(requestParam)
                    .build()
        }
    }

    class SetWiFi24GPwd : Commander()
    {
        override fun composeRequest(): Request
        {
            val setWiFi24GPwdURL = "${GlobalData.getAPIPath()}${AppConfig.API_WIFI_24G_PWD}?sessionkey=${GlobalData.sessionKey}"
            val requestParam = RequestBody.create(JSON, getParams().toString())
            return Request.Builder()
                    .addHeader("Cookie", GlobalData.cookie)
                    .url(setWiFi24GPwdURL)
                    .put(requestParam)
                    .build()
        }
    }

    class SetWiFi5GInfo : Commander()
    {
        override fun composeRequest(): Request
        {
            val setWiFi5GInfoURL = "${GlobalData.getAPIPath()}${AppConfig.API_WIFI_5G_INFO}?sessionkey=${GlobalData.sessionKey}"
            val requestParam = RequestBody.create(JSON, getParams().toString())
            return Request.Builder()
                    .addHeader("Cookie", GlobalData.cookie)
                    .url(setWiFi5GInfoURL)
                    .put(requestParam)
                    .build()
        }
    }

    class SetWiFi5GPwd : Commander()
    {
        override fun composeRequest(): Request
        {
            val setWiFi5GPwdURL = "${GlobalData.getAPIPath()}${AppConfig.API_WIFI_5G_PWD}?sessionkey=${GlobalData.sessionKey}"
            val requestParam = RequestBody.create(JSON, getParams().toString())
            return Request.Builder()
                    .addHeader("Cookie", GlobalData.cookie)
                    .url(setWiFi5GPwdURL)
                    .put(requestParam)
                    .build()
        }
    }

    class SetGuestWiFi24GInfo : Commander()
    {
        override fun composeRequest(): Request
        {
            val setGuestWiFi24GInfoURL = "${GlobalData.getAPIPath()}${AppConfig.API_GUEST_WIFI_24G_INFO}?sessionkey=${GlobalData.sessionKey}"
            val requestParam = RequestBody.create(JSON, getParams().toString())
            return Request.Builder()
                    .addHeader("Cookie", GlobalData.cookie)
                    .url(setGuestWiFi24GInfoURL)
                    .put(requestParam)
                    .build()
        }
    }

    class SetGuestWiFi24GPwd : Commander()
    {
        override fun composeRequest(): Request
        {
            val setGuestWiFi24GPwdURL = "${GlobalData.getAPIPath()}${AppConfig.API_GUEST_WIFI_24G_PWD}?sessionkey=${GlobalData.sessionKey}"
            val requestParam = RequestBody.create(JSON, getParams().toString())
            return Request.Builder()
                    .addHeader("Cookie", GlobalData.cookie)
                    .url(setGuestWiFi24GPwdURL)
                    .put(requestParam)
                    .build()
        }
    }

    class SetGuestWiFi5GInfo : Commander()
    {
        override fun composeRequest(): Request
        {
            val setGuestWiFi5GInfoURL = "${GlobalData.getAPIPath()}${AppConfig.API_GUEST_WIFI_5G_INFO}?sessionkey=${GlobalData.sessionKey}"
            val requestParam = RequestBody.create(JSON, getParams().toString())
            return Request.Builder()
                    .addHeader("Cookie", GlobalData.cookie)
                    .url(setGuestWiFi5GInfoURL)
                    .put(requestParam)
                    .build()
        }
    }

    class SetGuestWiFi5GPwd : Commander()
    {
        override fun composeRequest(): Request
        {
            val setGuestWiFi5GPwdURL = "${GlobalData.getAPIPath()}${AppConfig.API_GUEST_WIFI_5G_PWD}?sessionkey=${GlobalData.sessionKey}"
            val requestParam = RequestBody.create(JSON, getParams().toString())
            return Request.Builder()
                    .addHeader("Cookie", GlobalData.cookie)
                    .url(setGuestWiFi5GPwdURL)
                    .put(requestParam)
                    .build()
        }
    }

    class GetGuestWiFi24GInfo : Commander()
    {
        override fun composeRequest(): Request
        {
            val getGuestWiFi24GInfoURL = "${GlobalData.getAPIPath()}${AppConfig.API_GUEST_WIFI_24G_INFO}"
            return Request.Builder()
                    .addHeader("Cookie", GlobalData.cookie)
                    .url(getGuestWiFi24GInfoURL)
                    .build()
        }
    }
}