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
            val getWiFiSettingInfoURL = "${AppConfig.RESTfulProtocol}://${GlobalData.deviceIP}:${GlobalData.devicePort}${AppConfig.RESTfulVersion}/TR181/Value/Device.WiFi.?first_level_only=false"
            val request = Request.Builder()
                    .url(getWiFiSettingInfoURL)
                    .build()
            return request
        }
    }

    class SetWiFi24GInfo : Commander()
    {
        override fun composeRequest(): Request
        {
            val setWiFi24GInfoURL = "${AppConfig.RESTfulProtocol}://${GlobalData.deviceIP}:${GlobalData.devicePort}${AppConfig.RESTfulVersion}/TR181/Value/Device.WiFi.SSID.1.?sessionkey=${GlobalData.sessionkey}"
            val request = Request.Builder()
                    .url(setWiFi24GInfoURL)
                    .put(RequestBody.create(JSON, getParams().toString()))
                    .build()
            return request
        }
    }

    class SetWiFi24GPwd : Commander()
    {
        override fun composeRequest(): Request
        {
            val setWiFi24GPwdURL = "${AppConfig.RESTfulProtocol}://${GlobalData.deviceIP}:${GlobalData.devicePort}${AppConfig.RESTfulVersion}/TR181/Value/Device.WiFi.AccessPoint.1.Security.?sessionkey=${GlobalData.sessionkey}"
            val request = Request.Builder()
                    .url(setWiFi24GPwdURL)
                    .put(RequestBody.create(JSON, getParams().toString()))
                    .build()
            return request
        }
    }

    class SetWiFi5GInfo : Commander()
    {
        override fun composeRequest(): Request
        {
            val setWiFi5GInfoURL = "${AppConfig.RESTfulProtocol}://${GlobalData.deviceIP}:${GlobalData.devicePort}${AppConfig.RESTfulVersion}/TR181/Value/Device.WiFi.SSID.5.?sessionkey=${GlobalData.sessionkey}"
            val request = Request.Builder()
                    .url(setWiFi5GInfoURL)
                    .put(RequestBody.create(JSON, getParams().toString()))
                    .build()
            return request
        }
    }

    class SetWiFi5GPwd : Commander()
    {
        override fun composeRequest(): Request
        {
            val setWiFi5GPwdURL = "${AppConfig.RESTfulProtocol}://${GlobalData.deviceIP}:${GlobalData.devicePort}${AppConfig.RESTfulVersion}/TR181/Value/Device.WiFi.AccessPoint.5.Security.?sessionkey=${GlobalData.sessionkey}"
            val request = Request.Builder()
                    .url(setWiFi5GPwdURL)
                    .put(RequestBody.create(JSON, getParams().toString()))
                    .build()
            return request
        }
    }

    class SetGuestWiFi24GInfo : Commander()
    {
        override fun composeRequest(): Request
        {
            val setGuestWiFi24GInfoURL = "${AppConfig.RESTfulProtocol}://${GlobalData.deviceIP}:${GlobalData.devicePort}${AppConfig.RESTfulVersion}/TR181/Value/Device.WiFi.SSID.2.?sessionkey=${GlobalData.sessionkey}"
            val request = Request.Builder()
                    .url(setGuestWiFi24GInfoURL)
                    .put(RequestBody.create(JSON, getParams().toString()))
                    .build()
            return request
        }
    }

    class SetGuestWiFi24GPwd : Commander()
    {
        override fun composeRequest(): Request
        {
            val setGuestWiFi24GPwdURL = "${AppConfig.RESTfulProtocol}://${GlobalData.deviceIP}:${GlobalData.devicePort}${AppConfig.RESTfulVersion}/TR181/Value/Device.WiFi.AccessPoint.2.Security.?sessionkey=${GlobalData.sessionkey}"
            val request = Request.Builder()
                    .url(setGuestWiFi24GPwdURL)
                    .put(RequestBody.create(JSON, getParams().toString()))
                    .build()
            return request
        }
    }

    class SetGuestWiFi5GInfo : Commander()
    {
        override fun composeRequest(): Request
        {
            val setGuestWiFi5GInfoURL = "${AppConfig.RESTfulProtocol}://${GlobalData.deviceIP}:${GlobalData.devicePort}${AppConfig.RESTfulVersion}/TR181/Value/Device.WiFi.SSID.6.?sessionkey=${GlobalData.sessionkey}"
            val request = Request.Builder()
                    .url(setGuestWiFi5GInfoURL)
                    .put(RequestBody.create(JSON, getParams().toString()))
                    .build()
            return request
        }
    }

    class SetGuestWiFi5GPwd : Commander()
    {
        override fun composeRequest(): Request
        {
            val setGuestWiFi5GPwdURL = "${AppConfig.RESTfulProtocol}://${GlobalData.deviceIP}:${GlobalData.devicePort}${AppConfig.RESTfulVersion}/TR181/Value/Device.WiFi.AccessPoint.6.Security.?sessionkey=${GlobalData.sessionkey}"
            val request = Request.Builder()
                    .url(setGuestWiFi5GPwdURL)
                    .put(RequestBody.create(JSON, getParams().toString()))
                    .build()
            return request
        }
    }
}