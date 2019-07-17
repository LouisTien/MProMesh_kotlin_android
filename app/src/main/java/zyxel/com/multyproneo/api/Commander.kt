package zyxel.com.multyproneo.api

import okhttp3.*
import org.json.JSONObject
import zyxel.com.multyproneo.util.LogUtil
import java.io.IOException
import java.security.SecureRandom
import java.util.concurrent.TimeUnit
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSocketFactory
import javax.net.ssl.TrustManager

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
            .sslSocketFactory(createSSLSocketFactory())
            .hostnameVerifier(TrustAllHostnameVerifier)
            .build()
    //private var mCtx: Context
    private var headers = Headers.Builder()
    private lateinit var responseListener: ResponseListener
    private lateinit var request: Request
    private lateinit var params: JSONObject
    private lateinit var paramStr: String
    private var requestPageName = ""

    abstract fun composeRequest(): Request

    /*constructor(context: Context)
    {
        mCtx = context
        headers = Headers.Builder()
        headers.add("Content-Type", "application/json")
    }*/

    abstract class ResponseListener
    {
        abstract fun onSuccess(responseStr: String)
        fun onFail(msg: String, ctxName: String)
        {
            LogUtil.e("Commander","[onFail]msg:$msg")
            LogUtil.e("Commander","[onFail]ctxName:$ctxName")
        }
    }

    private fun createSSLSocketFactory(): SSLSocketFactory?
    {
        var ssfFactory: SSLSocketFactory? = null
        try
        {
            val sc = SSLContext.getInstance("TLS")
            sc.init(null, arrayOf<TrustManager>(TrustAllCerts), SecureRandom())
            ssfFactory = sc.socketFactory
        }
        catch(e: Exception)
        {

        }
        return ssfFactory
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
                responseListener.onFail("${e.message}", getRequestPageName())
            }

            override fun onResponse(call: Call, response: Response)
            {
                LogUtil.d(TAG, "onResponse = ${response.code()}")
                if(response.isSuccessful)
                    responseListener.onSuccess(response.body()!!.string())
                else
                {
                    LogUtil.e(TAG,"onResponse error = response.code:${response.code()}, response.body():${response.body()!!.string()}")
                    responseListener.onFail(response.body()!!.string(), getRequestPageName())
                }
            }
        })
        return this
    }
}