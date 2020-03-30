package zyxel.com.multyproneo.api

import android.os.Bundle
import com.google.gson.Gson
import okhttp3.*
import org.json.JSONObject
import zyxel.com.multyproneo.event.GlobalBus
import zyxel.com.multyproneo.event.MainEvent
import zyxel.com.multyproneo.fragment.cloud.SetupConnectTroubleshootingFragment
import zyxel.com.multyproneo.model.HttpErrorInfo
import zyxel.com.multyproneo.util.AppConfig
import zyxel.com.multyproneo.util.FeatureConfig
import zyxel.com.multyproneo.util.GlobalData
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
    /*private val client = OkHttpClient().newBuilder()
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(300, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .connectionPool(ConnectionPool( 7,60*5, TimeUnit.SECONDS))
            .sslSocketFactory(createSSLSocketFactory())
            .hostnameVerifier(TrustHostnameVerifier)
            .build()*/
    //private var mCtx: Context
    private var headers = Headers.Builder()
    private var formBody = FormBody.Builder()
    private lateinit var responseListener: ResponseListener
    private lateinit var request: Request
    private lateinit var params: JSONObject
    private lateinit var paramStr: String
    private lateinit var errorInfo: HttpErrorInfo
    private var requestPageName = ""
    private var cloudUsing = false

    companion object
    {
        private val client = OkHttpClient().newBuilder()
                .connectTimeout(60, TimeUnit.SECONDS)
                .readTimeout(300, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .connectionPool(ConnectionPool( 7,60*5, TimeUnit.SECONDS))
                .sslSocketFactory(createSSLSocketFactory())
                .hostnameVerifier(TrustHostnameVerifier)
                .build()

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
    }

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

        open fun onFail(code: Int, msg: String, ctxName: String, isCloudUsing: Boolean)
        {
            LogUtil.e("Commander","[onFail]code:$code")
            LogUtil.e("Commander","[onFail]msg:$msg")
            LogUtil.e("Commander","[onFail]ctxName:$ctxName")

            if(isCloudUsing)
                gotoTroubleShooting()
            else
            {
                stopAllRegularTask()
                GlobalBus.publish(MainEvent.ShowErrorMsgDialog(msg, ctxName))
            }
        }

        open fun onConnectFail(msg: String, ctxName: String, isCloudUsing: Boolean)
        {
            LogUtil.e("Commander","[onConnectFail]msg:$msg")
            LogUtil.e("Commander","[onConnectFail]ctxName:$ctxName")

            var specialMsg = msg
            if( (msg.contains("failed to connect to")) or (msg.contains("Failed to connect to")) )
                specialMsg = "Server is disconnect."

            if(isCloudUsing)
                gotoTroubleShooting()
            else
            {
                stopAllRegularTask()
                GlobalBus.publish(MainEvent.ShowErrorMsgDialog(specialMsg, ctxName))
            }
        }

        private fun stopAllRegularTask()
        {
            GlobalBus.publish(MainEvent.StopGetDeviceInfoTask())
            GlobalBus.publish(MainEvent.StopGetWPSStatusTask())
            GlobalBus.publish(MainEvent.StopGetSpeedTestStatusTask())
        }

        private fun gotoTroubleShooting()
        {
            val bundle = Bundle().apply{
                putSerializable("pageMode", AppConfig.TroubleshootingPage.PAGE_CLOUD_API_ERROR)
            }

            GlobalBus.publish(MainEvent.SwitchToFrag(SetupConnectTroubleshootingFragment().apply{ arguments = bundle }))
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

    fun getFormBody(): FormBody.Builder
    {
        return formBody
    }

    fun setFormBody(bodyMap: Map<String, Any>): Commander
    {
        for(key in bodyMap.keys)
            formBody.add(key, bodyMap[key].toString())
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

    fun setIsUsingInCloudFlow(value: Boolean): Commander
    {
        cloudUsing = value
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
                LogUtil.e(TAG,"[onFailure]")
                GlobalBus.publish(MainEvent.HideLoading())
                responseListener.onConnectFail("${e.message}", getRequestPageName(), cloudUsing)
            }

            override fun onResponse(call: Call, response: Response)
            {
                LogUtil.d(TAG,"[onResponse]")
                var responseCode = response.code()
                var responseStr = response.body()!!.string()
                LogUtil.d(TAG, "onResponse code = $responseCode")
                if(responseStr.length > 4000)
                {
                    for(i in responseStr.indices step 4000)
                    {
                        if(i + 4000 < responseStr.length)
                            LogUtil.d(TAG, "onResponse body (count) = ${responseStr.substring(i, i + 4000)}")
                        else
                            LogUtil.d(TAG, "onResponse body (end) = ${responseStr.substring(i, responseStr.length)}")
                    }
                }
                else
                    LogUtil.d(TAG, "onResponse body = $responseStr")

                if(response.isSuccessful)
                {
                    val data = JSONObject(responseStr)
                    val result = data.get("oper_status").toString()
                    if(result.equals("Success", ignoreCase = false))
                    {
                        responseListener.onSuccess(responseStr)

                        if(call.request().url().toString().contains("UserLogin"))
                        {
                            val cookies = response.headers().values("Set-Cookie")
                            val cookie = cookies[0].substring(0, cookies.get(0).indexOf(";"))
                            GlobalData.cookie = cookie
                            LogUtil.d(TAG,"cookie:$cookie")
                        }
                    }
                    else
                    {
                        if(call.request().url().toString().contains("Device.X_ZYXEL_EXT.InternetBlocking"))
                        {
                            responseStr = "{\n" +
                                          "\"requested_path\": \"Device.X_ZYXEL_EXT.InternetBlocking.\",\n" +
                                          "\"oper_status\": \"Success\",\n" +
                                          "\"Object\": {\n" +
                                          "\"Enable\": ${FeatureConfig.internetBlockingStatus}\n" +
                                          "}\n" +
                                          "}"

                            responseListener.onSuccess(responseStr)
                        }
                        else if(call.request().url().toString().contains("Device.X_ZYXEL_TUTK_CloudAgent."))
                        {
                            responseStr = "{}"
                            responseListener.onSuccess(responseStr)
                        }
                        else
                        {
                            GlobalBus.publish(MainEvent.HideLoading())
                            responseListener.onFail(responseCode, result, getRequestPageName(), cloudUsing)
                        }
                    }
                }
                else
                {
                    GlobalBus.publish(MainEvent.HideLoading())
                    errorInfo = Gson().fromJson(responseStr, HttpErrorInfo::class.javaObjectType)
                    responseListener.onFail(responseCode, errorInfo.oper_status, getRequestPageName(), cloudUsing)
                }
            }
        })
        return this
    }
}