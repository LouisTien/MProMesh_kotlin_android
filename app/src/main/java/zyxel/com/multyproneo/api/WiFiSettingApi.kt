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
}