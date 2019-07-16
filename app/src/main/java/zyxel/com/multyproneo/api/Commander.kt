package zyxel.com.multyproneo.api

import android.content.Context
import okhttp3.*
import org.json.JSONObject
import zyxel.com.multyproneo.R
import zyxel.com.multyproneo.util.LogUtil
import java.io.IOException
import java.util.concurrent.TimeUnit

/**
 * Created by LouisTien on 2019/7/16.
 */
abstract class Commander
{
    private val TAG = javaClass.simpleName
    private val client = OkHttpClient().newBuilder()
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(300, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .connectionPool(ConnectionPool( 7,60*5, TimeUnit.SECONDS))
            .build()
    private var mCtx: Context
    private var headers: Headers.Builder
    private lateinit var responseListener: ResponseListener
    private lateinit var request: Request
    private lateinit var params: JSONObject
    private lateinit var paramStr: String
    private var requestPageName = ""

    abstract fun composeRequest(): Request

    constructor(context: Context)
    {
        mCtx = context
        headers = Headers.Builder()
        headers.add("Content-Type", "application/json")
    }

    abstract class ResponseListener
    {
        abstract fun onSuccess(responseStr: String)
        fun onFail(msg: String, ctxName: String)
        {
            LogUtil.e("Commander","[onFail]msg:$msg")
            LogUtil.e("Commander","[onFail]ctxName:$ctxName")
        }
    }

    fun getHeaders(): Headers.Builder
    {
        return headers
    }

    fun setHeaders(headerMap: Map<String, Any>): Commander
    {
        for(key in headerMap.keys)
            headers.add(key, headerMap[key].toString())
        return this
    }

    fun getParams(): JSONObject
    {
        return params
    }

    fun setParams(jasonObject: JSONObject): Commander
    {
        params = jasonObject
        return this
    }

    fun getParamsStr(): String
    {
        return paramStr
    }

    fun setParams(str: String): Commander
    {
        paramStr = str
        return this
    }

    fun getRequestPageName(): String
    {
        return requestPageName
    }

    fun setRequestPageName(requestPageName: String): Commander
    {
        this.requestPageName = requestPageName
        return this
    }

    fun setResponseListener(listener: ResponseListener): Commander
    {
        responseListener = listener
        return this
    }

    fun execute(): Commander
    {
        request = composeRequest()
        LogUtil.d(TAG, request.toString())
        val call = client.newCall(request)
        call.enqueue(object: Callback
        {
            override fun onFailure(call: Call, e: IOException)
            {
                responseListener.onFail(mCtx.getString(R.string.message_dialog_not_connect_try_again), getRequestPageName())
            }

            override fun onResponse(call: Call, response: Response)
            {
                if(response.isSuccessful)
                    responseListener.onSuccess(response.body()!!.string())
                else
                {
                    LogUtil.e(TAG,"onResponse error = ${response.code()} : ${response.body()!!.string()}")
                    responseListener.onFail(response.body()!!.string(), getRequestPageName())
                }
            }
        })
        return this
    }
}