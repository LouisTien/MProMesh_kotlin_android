package zyxel.com.multyproneo.api

import okhttp3.MediaType
import okhttp3.Request
import okhttp3.RequestBody
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
            val getDeviceInfoURL = "${GlobalData.getAPIPath()}/TR181/Value/Device.Hosts.Host."
            val request = Request.Builder()
                    .addHeader("Cookie", GlobalData.cookie)
                    .url(getDeviceInfoURL)
                    .build()
            return request
        }
    }

    class GetChangeIconNameInfo : Commander()
    {
        override fun composeRequest(): Request
        {
            val getChangeIconNameInfoURL = "${GlobalData.getAPIPath()}/TR181/Value/Device.X_ZYXEL_Change_Icon_Name."
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
            val setChangeIconNameInfoURL = "${GlobalData.getAPIPath()}/TR181/Value/Device.X_ZYXEL_Change_Icon_Name.?sessionkey=${GlobalData.sessionKey}"
            val requestParam = RequestBody.create(JSON, getParams().toString())
            val request = Request.Builder()
                    .addHeader("Cookie", GlobalData.cookie)
                    .url(setChangeIconNameInfoURL)
                    .post(requestParam)
                    .build()
            return request
        }
    }

    class SetChangeIconNameInfoByIndex(val index: Int = 0) : Commander()
    {
        override fun composeRequest(): Request
        {
            val setChangeIconNameInfoURL = "${GlobalData.getAPIPath()}/TR181/Value/Device.X_ZYXEL_Change_Icon_Name.$index.?sessionkey=${GlobalData.sessionKey}"
            val requestParam = RequestBody.create(JSON, getParams().toString())
            val request = Request.Builder()
                    .addHeader("Cookie", GlobalData.cookie)
                    .url(setChangeIconNameInfoURL)
                    .put(requestParam)
                    .build()
            return request
        }
    }
}