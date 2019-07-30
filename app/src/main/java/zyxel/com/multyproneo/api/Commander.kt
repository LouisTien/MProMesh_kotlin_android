package zyxel.com.multyproneo.api

import com.google.gson.Gson
import okhttp3.*
import org.json.JSONObject
import zyxel.com.multyproneo.event.GlobalBus
import zyxel.com.multyproneo.event.MainEvent
import zyxel.com.multyproneo.model.HttpErrorInfo
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
            .hostnameVerifier(TrustHostnameVerifier)
            .build()
    //private var mCtx: Context
    private var headers = Headers.Builder()
    private lateinit var responseListener: ResponseListener
    private lateinit var request: Request
    private lateinit var params: JSONObject
    private lateinit var paramStr: String
    private lateinit var errorInfo: HttpErrorInfo
    private var requestPageName = ""
    private var showLoading = false

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

        open fun onFail(code: Int, msg: String, ctxName: String)
        {
            LogUtil.e("Commander","[onFail]code:$code")
            LogUtil.e("Commander","[onFail]msg:$msg")
            LogUtil.e("Commander","[onFail]ctxName:$ctxName")

            GlobalBus.publish(MainEvent.ShowToast(msg, ctxName))
            GlobalBus.publish(MainEvent.EnterSearchGatewayPage())
        }

        open fun onConnectFail(msg: String, ctxName: String)
        {
            LogUtil.e("Commander","[onConnectFail]msg:$msg")
            LogUtil.e("Commander","[onConnectFail]ctxName:$ctxName")

            GlobalBus.publish(MainEvent.ShowToast(msg, ctxName))
            GlobalBus.publish(MainEvent.EnterSearchGatewayPage())
        }
    }

    private fun createSSLSocketFactory(): SSLSocketFactory?
    {
        var ssfFactory: SSLSocketFactory? = null
        try
        {
            val sc = SSLContext.getInstance("TLS")
            sc.init(null, arrayOf<TrustManager>(TrustCerts), SecureRandom())
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

    fun showLoading(loading: Boolean): Commander
    {
        showLoading = loading
        return this
    }

    fun isShowLoading(): Boolean
    {
        return showLoading
    }

    fun execute(): Commander
    {
        if(showLoading)
            GlobalBus.publish(MainEvent.ShowLoading())

        request = composeRequest()
        LogUtil.d(TAG, request.toString())
        val call = client.newCall(request)
        call.enqueue(object: Callback
        {
            override fun onFailure(call: Call, e: IOException)
            {
                LogUtil.e(TAG,"[onFailure]")
                GlobalBus.publish(MainEvent.HideLoading())
                responseListener.onConnectFail("${e.message}", getRequestPageName())
            }

            override fun onResponse(call: Call, response: Response)
            {
                LogUtil.d(TAG,"[onResponse]")
                var responseCode = response.code()
                var responseStr = response.body()!!.string()
                LogUtil.d(TAG, "onResponse code = $responseCode")
                LogUtil.d(TAG, "onResponse body = $responseStr")
                if(response.isSuccessful)
                {
                    //val data = JSONObject(responseStr)
                    //val result = data.get("oper_status").toString()

                    if(showLoading)
                        GlobalBus.publish(MainEvent.HideLoading())

                    responseListener.onSuccess(responseStr)
                }
                else
                {
                    GlobalBus.publish(MainEvent.HideLoading())
                    errorInfo = Gson().fromJson(responseStr, HttpErrorInfo::class.java)
                    responseListener.onFail(responseCode, errorInfo.oper_status, getRequestPageName())
                }
            }
        })
        return this
    }
}