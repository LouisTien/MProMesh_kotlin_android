package zyxel.com.multyproneo.api

import okhttp3.MediaType
import okhttp3.Request
import okhttp3.RequestBody
import zyxel.com.multyproneo.util.AppConfig
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
            val startPairingURL = "${GlobalData.getAPIPath()}${AppConfig.API_MESH}?sessionkey=${GlobalData.sessionKey}"
            val requestParam = RequestBody.create(JSON, getParams().toString())
            return Request.Builder()
                    .addHeader("Cookie", GlobalData.cookie)
                    .url(startPairingURL)
                    .put(requestParam)
                    .build()
        }
    }

    class GetWPSStatus : Commander()
    {
        override fun composeRequest(): Request
        {
            val getWPSStatusURL = "${GlobalData.getAPIPath()}${AppConfig.API_MESH}"
            return Request.Builder()
                    .addHeader("Cookie", GlobalData.cookie)
                    .url(getWPSStatusURL)
                    .build()
        }
    }
}