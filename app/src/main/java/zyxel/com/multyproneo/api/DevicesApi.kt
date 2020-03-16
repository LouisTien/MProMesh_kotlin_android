package zyxel.com.multyproneo.api

import okhttp3.MediaType
import okhttp3.Request
import okhttp3.RequestBody
import zyxel.com.multyproneo.util.AppConfig
import zyxel.com.multyproneo.util.GlobalData

/**
 * Created by LouisTien on 2019/8/12.
 */
object DevicesApi
{
    private val JSON = MediaType.parse("application/json; charset=utf-8")

    class GetDevicesInfo : Commander()
    {
        override fun composeRequest(): Request
        {
            val getDeviceInfoURL = "${GlobalData.getAPIPath()}${AppConfig.API_DEVICE_HOST_INFO}"
            return Request.Builder()
                    .addHeader("Cookie", GlobalData.cookie)
                    .url(getDeviceInfoURL)
                    .build()
        }
    }

    class GetChangeIconNameInfo : Commander()
    {
        override fun composeRequest(): Request
        {
            val getChangeIconNameInfoURL = "${GlobalData.getAPIPath()}${AppConfig.API_CHANGE_ICON_NAME_INFO}"
            val request = Request.Builder()
                    .addHeader("Cookie", GlobalData.cookie)
                    .url(getChangeIconNameInfoURL)
                    .build()
            return request
        }
    }

    class SetChangeIconNameInfo : Commander()
    {
        override fun composeRequest(): Request
        {
            val setChangeIconNameInfoURL = "${GlobalData.getAPIPath()}${AppConfig.API_CHANGE_ICON_NAME_INFO}?sessionkey=${GlobalData.sessionKey}"
            val requestParam = RequestBody.create(JSON, getParams().toString())
            return Request.Builder()
                    .addHeader("Cookie", GlobalData.cookie)
                    .url(setChangeIconNameInfoURL)
                    .post(requestParam)
                    .build()
        }
    }

    class SetChangeIconNameInfoByIndex(val index: Int = 0) : Commander()
    {
        override fun composeRequest(): Request
        {
            val setChangeIconNameInfoURL = "${GlobalData.getAPIPath()}${AppConfig.API_CHANGE_ICON_NAME_INFO}$index.?sessionkey=${GlobalData.sessionKey}"
            val requestParam = RequestBody.create(JSON, getParams().toString())
            return Request.Builder()
                    .addHeader("Cookie", GlobalData.cookie)
                    .url(setChangeIconNameInfoURL)
                    .put(requestParam)
                    .build()
        }
    }
}