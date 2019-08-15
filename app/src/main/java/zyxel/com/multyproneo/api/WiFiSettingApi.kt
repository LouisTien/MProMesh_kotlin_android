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
            val getWiFiSettingInfoURL = "${GlobalData.getAPIPath()}/TR181/Value/Device.WiFi.?first_level_only=false"
            val request = Request.Builder()
                    .addHeader("Cookie", GlobalData.cookie)
                    .url(getWiFiSettingInfoURL)
                    .build()
            return request
        }
    }

    class GetMeshInfo : Commander()
    {
        override fun composeRequest(): Request
        {
            val getMeshInfoURL = "${GlobalData.getAPIPath()}/TR181/Value/Device.X_ZYXEL_EXT.EasyMesh."
            val request = Request.Builder()
                    .addHeader("Cookie", GlobalData.cookie)
                    .url(getMeshInfoURL)
                    .build()
            return request
        }
    }

    class SetWiFi24GInfo : Commander()
    {
        override fun composeRequest(): Request
        {
            val setWiFi24GInfoURL = "${GlobalData.getAPIPath()}/TR181/Value/Device.WiFi.SSID.1.?sessionkey=${GlobalData.sessionKey}"
            val requestParam = RequestBody.create(JSON, getParams().toString())
            val request = Request.Builder()
                    .addHeader("Cookie", GlobalData.cookie)
                    .url(setWiFi24GInfoURL)
                    .put(requestParam)
                    .build()
            return request
        }
    }

    class SetWiFi24GPwd : Commander()
    {
        override fun composeRequest(): Request
        {
            val setWiFi24GPwdURL = "${GlobalData.getAPIPath()}/TR181/Value/Device.WiFi.AccessPoint.1.Security.?sessionkey=${GlobalData.sessionKey}"
            val requestParam = RequestBody.create(JSON, getParams().toString())
            val request = Request.Builder()
                    .addHeader("Cookie", GlobalData.cookie)
                    .url(setWiFi24GPwdURL)
                    .put(requestParam)
                    .build()
            return request
        }
    }

    class SetWiFi5GInfo : Commander()
    {
        override fun composeRequest(): Request
        {
            val setWiFi5GInfoURL = "${GlobalData.getAPIPath()}/TR181/Value/Device.WiFi.SSID.5.?sessionkey=${GlobalData.sessionKey}"
            val requestParam = RequestBody.create(JSON, getParams().toString())
            val request = Request.Builder()
                    .addHeader("Cookie", GlobalData.cookie)
                    .url(setWiFi5GInfoURL)
                    .put(requestParam)
                    .build()
            return request
        }
    }

    class SetWiFi5GPwd : Commander()
    {
        override fun composeRequest(): Request
        {
            val setWiFi5GPwdURL = "${GlobalData.getAPIPath()}/TR181/Value/Device.WiFi.AccessPoint.5.Security.?sessionkey=${GlobalData.sessionKey}"
            val requestParam = RequestBody.create(JSON, getParams().toString())
            val request = Request.Builder()
                    .addHeader("Cookie", GlobalData.cookie)
                    .url(setWiFi5GPwdURL)
                    .put(requestParam)
                    .build()
            return request
        }
    }

    class SetGuestWiFi24GInfo : Commander()
    {
        override fun composeRequest(): Request
        {
            val setGuestWiFi24GInfoURL = "${GlobalData.getAPIPath()}/TR181/Value/Device.WiFi.SSID.2.?sessionkey=${GlobalData.sessionKey}"
            val requestParam = RequestBody.create(JSON, getParams().toString())
            val request = Request.Builder()
                    .addHeader("Cookie", GlobalData.cookie)
                    .url(setGuestWiFi24GInfoURL)
                    .put(requestParam)
                    .build()
            return request
        }
    }

    class SetGuestWiFi24GPwd : Commander()
    {
        override fun composeRequest(): Request
        {
            val setGuestWiFi24GPwdURL = "${GlobalData.getAPIPath()}/TR181/Value/Device.WiFi.AccessPoint.2.Security.?sessionkey=${GlobalData.sessionKey}"
            val requestParam = RequestBody.create(JSON, getParams().toString())
            val request = Request.Builder()
                    .addHeader("Cookie", GlobalData.cookie)
                    .url(setGuestWiFi24GPwdURL)
                    .put(requestParam)
                    .build()
            return request
        }
    }

    class SetGuestWiFi5GInfo : Commander()
    {
        override fun composeRequest(): Request
        {
            val setGuestWiFi5GInfoURL = "${GlobalData.getAPIPath()}/TR181/Value/Device.WiFi.SSID.6.?sessionkey=${GlobalData.sessionKey}"
            val requestParam = RequestBody.create(JSON, getParams().toString())
            val request = Request.Builder()
                    .addHeader("Cookie", GlobalData.cookie)
                    .url(setGuestWiFi5GInfoURL)
                    .put(requestParam)
                    .build()
            return request
        }
    }

    class SetGuestWiFi5GPwd : Commander()
    {
        override fun composeRequest(): Request
        {
            val setGuestWiFi5GPwdURL = "${GlobalData.getAPIPath()}/TR181/Value/Device.WiFi.AccessPoint.6.Security.?sessionkey=${GlobalData.sessionKey}"
            val requestParam = RequestBody.create(JSON, getParams().toString())
            val request = Request.Builder()
                    .addHeader("Cookie", GlobalData.cookie)
                    .url(setGuestWiFi5GPwdURL)
                    .put(requestParam)
                    .build()
            return request
        }
    }

    class GetGuestWiFi24GInfo : Commander()
    {
        override fun composeRequest(): Request
        {
            val getGuestWiFi24GInfoURL = "${GlobalData.getAPIPath()}/TR181/Value/Device.WiFi.SSID.2."
            val request = Request.Builder()
                    .addHeader("Cookie", GlobalData.cookie)
                    .url(getGuestWiFi24GInfoURL)
                    .build()
            return request
        }
    }
}