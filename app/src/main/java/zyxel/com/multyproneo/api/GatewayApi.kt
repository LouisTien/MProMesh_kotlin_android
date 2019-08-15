package zyxel.com.multyproneo.api

import okhttp3.MediaType
import okhttp3.Request
import okhttp3.RequestBody
import zyxel.com.multyproneo.util.GlobalData

/**
 * Created by LouisTien on 2019/8/14.
 */
object GatewayApi
{
    private val JSON = MediaType.parse("application/json; charset=utf-8")

    class GetWanInfo : Commander()
    {
        override fun composeRequest(): Request
        {
            val getWanInfoURL = "${GlobalData.getAPIPath()}/TR181/Value/Device.DeviceInfo.WanInfo."
            val request = Request.Builder()
                    .addHeader("Cookie", GlobalData.cookie)
                    .url(getWanInfoURL)
                    .build()
            return request
        }
    }

    class GetFSecureInfo : Commander()
    {
        override fun composeRequest(): Request
        {
            val getFSecureInfoURL = "${GlobalData.getAPIPath()}/TR181/Value/Device.X_ZYXEL_License."
            val request = Request.Builder()
                    .addHeader("Cookie", GlobalData.cookie)
                    .url(getFSecureInfoURL)
                    .build()
            return request
        }
    }

    class Reboot : Commander()
    {
        override fun composeRequest(): Request
        {
            val rebootURL = "${GlobalData.getAPIPath()}/Reboot?sessionkey=${GlobalData.sessionKey}"
            val requestParam = RequestBody.create(JSON, getParams().toString())
            val request = Request.Builder()
                    .addHeader("Cookie", GlobalData.cookie)
                    .url(rebootURL)
                    .post(requestParam)
                    .build()
            return request
        }
    }
}