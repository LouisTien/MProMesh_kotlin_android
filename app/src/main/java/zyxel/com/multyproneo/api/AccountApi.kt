package zyxel.com.multyproneo.api

import okhttp3.MediaType
import okhttp3.Request
import okhttp3.RequestBody
import zyxel.com.multyproneo.util.AppConfig
import zyxel.com.multyproneo.util.GlobalData

/**
 * Created by LouisTien on 2019/7/16.
 */
object AccountApi
{
    private val JSON = MediaType.parse("application/json; charset=utf-8")

    class Login : Commander()
    {
        override fun composeRequest(): Request
        {
            val loginURL = "${AppConfig.RESTfulProtocol}://${GlobalData.deviceIP}:${GlobalData.devicePort}${AppConfig.RESTfulVersion}/UserLogin"
            val requestParam = RequestBody.create(JSON, getParams().toString())
            val request = Request.Builder()
                    //.headers(getHeaders().build())
                    .url(loginURL)
                    .post(requestParam)
                    .build()
            return request
        }
    }

    class Logout : Commander()
    {
        override fun composeRequest(): Request
        {
            val logoutURL = "${AppConfig.RESTfulProtocol}://${GlobalData.deviceIP}:${GlobalData.devicePort}${AppConfig.RESTfulVersion}/UserLogout?sessionkey=${GlobalData.sessionkey}"
            val requestParam = RequestBody.create(JSON, getParams().toString())
            val request = Request.Builder()
                    .url(logoutURL)
                    .post(requestParam)
                    .build()
            return request
        }
    }
}