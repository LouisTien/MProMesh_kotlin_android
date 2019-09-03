package zyxel.com.multyproneo.api

import okhttp3.MediaType
import okhttp3.Request
import okhttp3.RequestBody
import zyxel.com.multyproneo.util.GlobalData

/**
 * Created by LouisTien on 2019/9/3.
 */
object AddMeshApi
{
    private val JSON = MediaType.parse("application/json; charset=utf-8")

    class StartPairing : Commander()
    {
        override fun composeRequest(): Request
        {
            val startPairingURL = "${GlobalData.getAPIPath()}/TR181/Value/Device.WiFi.AccessPoint.5.WPS.?sessionkey=${GlobalData.sessionKey}"
            val requestParam = RequestBody.create(AddMeshApi.JSON, getParams().toString())
            val request = Request.Builder()
                    .addHeader("Cookie", GlobalData.cookie)
                    .url(startPairingURL)
                    .put(requestParam)
                    .build()
            return request
        }
    }

    class GetWPSStatus : Commander()
    {
        override fun composeRequest(): Request
        {
            val getWPSStatusURL = "${GlobalData.getAPIPath()}/TR181/Value/Device.WiFi.AccessPoint.5.WPS."
            val request = Request.Builder()
                    .addHeader("Cookie", GlobalData.cookie)
                    .url(getWPSStatusURL)
                    .build()
            return request
        }
    }
}