package zyxel.com.multyproneo.api

import okhttp3.MediaType
import okhttp3.Request
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
}