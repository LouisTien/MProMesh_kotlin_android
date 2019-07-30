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
    private val GetWiFiSettingInfoURL = "${AppConfig.RESTfulProtocol}://${GlobalData.deviceIP}:${GlobalData.devicePort}${AppConfig.RESTfulVersion}/TR181/Value/Device.WiFi.?first_level_only=false"
    private val SetWiFi24GInfoURL = "${AppConfig.RESTfulProtocol}://${GlobalData.deviceIP}:${GlobalData.devicePort}${AppConfig.RESTfulVersion}/TR181/Value/Device.WiFi.SSID.1."
    private val SetWiFi24GPwdURL = "${AppConfig.RESTfulProtocol}://${GlobalData.deviceIP}:${GlobalData.devicePort}${AppConfig.RESTfulVersion}/TR181/Value/Device.WiFi.AccessPoint.1.Security."
    private val SetWiFi5GInfoURL = "${AppConfig.RESTfulProtocol}://${GlobalData.deviceIP}:${GlobalData.devicePort}${AppConfig.RESTfulVersion}/TR181/Value/Device.WiFi.SSID.5."
    private val SetWiFi5GPwdURL = "${AppConfig.RESTfulProtocol}://${GlobalData.deviceIP}:${GlobalData.devicePort}${AppConfig.RESTfulVersion}/TR181/Value/Device.WiFi.AccessPoint.5.Security."
    private val SetGuestWiFi24GInfoURL = "${AppConfig.RESTfulProtocol}://${GlobalData.deviceIP}:${GlobalData.devicePort}${AppConfig.RESTfulVersion}/TR181/Value/Device.WiFi.SSID.2."
    private val SetGuestWiFi24GPwdURL = "${AppConfig.RESTfulProtocol}://${GlobalData.deviceIP}:${GlobalData.devicePort}${AppConfig.RESTfulVersion}/TR181/Value/Device.WiFi.AccessPoint.2.Security."
    private val SetGuestWiFi5GInfoURL = "${AppConfig.RESTfulProtocol}://${GlobalData.deviceIP}:${GlobalData.devicePort}${AppConfig.RESTfulVersion}/TR181/Value/Device.WiFi.SSID.6."
    private val SetGuestWiFi5GPwdURL = "${AppConfig.RESTfulProtocol}://${GlobalData.deviceIP}:${GlobalData.devicePort}${AppConfig.RESTfulVersion}/TR181/Value/Device.WiFi.AccessPoint.6.Security."

    private val JSON = MediaType.parse("application/json; charset=utf-8")

    class GetWiFiSettingInfo : Commander()
    {
        override fun composeRequest(): Request
        {
            val request = Request.Builder()
                    .url(GetWiFiSettingInfoURL)
                    .build()
            return request
        }
    }

    class SetWiFi24GInfo : Commander()
    {
        override fun composeRequest(): Request
        {
            val request = Request.Builder()
                    .url(SetWiFi24GInfoURL)
                    .put(RequestBody.create(JSON, getParams().toString()))
                    .build()
            return request
        }
    }

    class SetWiFi24GPwd : Commander()
    {
        override fun composeRequest(): Request
        {
            val request = Request.Builder()
                    .url(SetWiFi24GPwdURL)
                    .put(RequestBody.create(JSON, getParams().toString()))
                    .build()
            return request
        }
    }

    class SetWiFi5GInfo : Commander()
    {
        override fun composeRequest(): Request
        {
            val request = Request.Builder()
                    .url(SetWiFi5GInfoURL)
                    .put(RequestBody.create(JSON, getParams().toString()))
                    .build()
            return request
        }
    }

    class SetWiFi5GPwd : Commander()
    {
        override fun composeRequest(): Request
        {
            val request = Request.Builder()
                    .url(SetWiFi5GPwdURL)
                    .put(RequestBody.create(JSON, getParams().toString()))
                    .build()
            return request
        }
    }

    class SetGuestWiFi24GInfo : Commander()
    {
        override fun composeRequest(): Request
        {
            val request = Request.Builder()
                    .url(SetGuestWiFi24GInfoURL)
                    .put(RequestBody.create(JSON, getParams().toString()))
                    .build()
            return request
        }
    }

    class SetGuestWiFi24GPwd : Commander()
    {
        override fun composeRequest(): Request
        {
            val request = Request.Builder()
                    .url(SetGuestWiFi24GPwdURL)
                    .put(RequestBody.create(JSON, getParams().toString()))
                    .build()
            return request
        }
    }

    class SetGuestWiFi5GInfo : Commander()
    {
        override fun composeRequest(): Request
        {
            val request = Request.Builder()
                    .url(SetGuestWiFi5GInfoURL)
                    .put(RequestBody.create(JSON, getParams().toString()))
                    .build()
            return request
        }
    }

    class SetGuestWiFi5GPwd : Commander()
    {
        override fun composeRequest(): Request
        {
            val request = Request.Builder()
                    .url(SetGuestWiFi5GPwdURL)
                    .put(RequestBody.create(JSON, getParams().toString()))
                    .build()
            return request
        }
    }
}