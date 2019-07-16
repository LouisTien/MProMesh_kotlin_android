package zyxel.com.multyproneo.api

import android.content.Context
import okhttp3.MediaType
import okhttp3.Request
import okhttp3.RequestBody
import org.json.JSONObject
import zyxel.com.multyproneo.util.AppConfig
import zyxel.com.multyproneo.util.LogUtil

/**
 * Created by LouisTien on 2019/7/16.
 */
object LoginOutApi
{
    private val TAG = javaClass.simpleName
    val JSON = MediaType.parse("application/json; charset=utf-8")

    class Login(context: Context, var userName: String = "", var password: String = "") : Commander(context)
    {
        private var api = ""
        override fun composeRequest(): Request
        {
            api = AppConfig.DeviceIP + AppConfig.DevicePort + "/api/v1/UserLogin"

            val params = JSONObject()
            params.put("username", userName)
            params.put("password", password)
            LogUtil.d(TAG,"login param:${params.toString()}")
            val requestParam = RequestBody.create(JSON, params.toString())

            val request = Request.Builder()
                    .url(api)
                    .headers(getHeaders().build())
                    .post(requestParam)
                    .build()
            return request
        }
    }
}