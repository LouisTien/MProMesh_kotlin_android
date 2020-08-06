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
            val loginURL = "${GlobalData.getAPIPath()}${AppConfig.API_LOGIN}"
            val requestParam = RequestBody.create(JSON, getParams().toString())
            return Request.Builder()
                    //.headers(getHeaders().build())
                    .url(loginURL)
                    .post(requestParam)
                    .build()
        }
    }

    class Logout : Commander()
    {
        override fun composeRequest(): Request
        {
            val logoutURL = "${GlobalData.getAPIPath()}${AppConfig.API_LOGOUT}?sessionkey=${GlobalData.loginInfo.sessionkey}"
            val requestParam = RequestBody.create(JSON, getParams().toString())
            return Request.Builder()
                    .addHeader("Cookie", GlobalData.cookie)
                    .url(logoutURL)
                    .post(requestParam)
                    .build()
        }
    }

    class SNLogin : Commander()
    {
        override fun composeRequest(): Request
        {
            val loginURL = "${GlobalData.getAPIPath()}${AppConfig.API_SNLOGIN}"
            val requestParam = RequestBody.create(JSON, getParams().toString())
            return Request.Builder()
                    .url(loginURL)
                    .post(requestParam)
                    .build()
        }
    }

    class SNLogout : Commander()
    {
        override fun composeRequest(): Request
        {
            val logoutURL = "${GlobalData.getAPIPath()}${AppConfig.API_SNLOGOUT}?sessionkey=${GlobalData.loginInfo.sessionkey}"
            val requestParam = RequestBody.create(JSON, getParams().toString())
            return Request.Builder()
                    .addHeader("Cookie", GlobalData.cookie)
                    .url(logoutURL)
                    .post(requestParam)
                    .build()
        }
    }
}