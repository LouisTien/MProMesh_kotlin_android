package zyxel.com.multyproneo.api

import okhttp3.MediaType
import okhttp3.Request
import okhttp3.RequestBody
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
            val loginURL = "${GlobalData.getAPIPath()}/UserLogin"
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
            val logoutURL = "${GlobalData.getAPIPath()}/UserLogout?sessionkey=${GlobalData.sessionKey}"
            val requestParam = RequestBody.create(JSON, getParams().toString())
            val request = Request.Builder()
                    .addHeader("Cookie", GlobalData.cookie)
                    .url(logoutURL)
                    .post(requestParam)
                    .build()
            return request
        }
    }
}