package zyxel.com.multyproneo.api.cloud

import android.os.Bundle
import okhttp3.*
import org.json.JSONObject
import zyxel.com.multyproneo.event.GlobalBus
import zyxel.com.multyproneo.event.MainEvent
import zyxel.com.multyproneo.fragment.cloud.CloudLoginFragment
import zyxel.com.multyproneo.fragment.cloud.SetupConnectTroubleshootingFragment
import zyxel.com.multyproneo.util.AppConfig
import zyxel.com.multyproneo.util.LogUtil
import java.io.IOException
import java.util.concurrent.TimeUnit

abstract class TUTKCommander
{
    private val TAG = javaClass.simpleName
    private var headers = Headers.Builder()
    private var formBody = FormBody.Builder()
    private lateinit var responseListener: ResponseListener
    private lateinit var request: Request
    private lateinit var params: JSONObject
    private lateinit var paramStr: String
    private var requestPageName = ""

    companion object
    {
        private val client = OkHttpClient().newBuilder()
                .connectTimeout(60, TimeUnit.SECONDS)
                .readTimeout(300, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .connectionPool(ConnectionPool( 7,60*5, TimeUnit.SECONDS))
                .build()
    }

    abstract fun composeRequest(): Request

    abstract class ResponseListener
    {
        abstract fun onSuccess(responseStr: String)

        open fun onFail(code: Int, msg: String, ctxName: String, act: AppConfig.HTTPErrorAction)
        {
            LogUtil.e("Commander","[onFail]code:$code")
            LogUtil.e("Commander","[onFail]msg:$msg")
            LogUtil.e("Commander","[onFail]ctxName:$ctxName")
            LogUtil.e("Commander","[onFail]act:$act")

            when(act)
            {
                AppConfig.HTTPErrorAction.ERR_ACT_GOTO_LOGIN ->
                {
                    val bundle = Bundle().apply{
                        putBoolean("isInSetupFlow", false)
                    }
                    GlobalBus.publish(MainEvent.SwitchToFrag(CloudLoginFragment().apply{ arguments = bundle }))
                }
                else -> { gotoTroubleShooting() }
            }
        }

        open fun onConnectFail(msg: String, ctxName: String)
        {
            LogUtil.e("Commander","[onConnectFail]msg:$msg")
            LogUtil.e("Commander","[onConnectFail]ctxName:$ctxName")

            gotoTroubleShooting()
        }

        private fun gotoTroubleShooting()
        {
            val bundle = Bundle().apply{
                putSerializable("pageMode", AppConfig.TroubleshootingPage.PAGE_CANNOT_CONNECT_TO_CLOUD)
            }

            GlobalBus.publish(MainEvent.SwitchToFrag(SetupConnectTroubleshootingFragment().apply{ arguments = bundle }))
        }
    }

    fun getHeaders(): Headers.Builder
    {
        return headers
    }

    fun setHeaders(headerMap: Map<String, Any>): TUTKCommander
    {
        for(key in headerMap.keys)
            headers.add(key, headerMap[key].toString())
        return this
    }

    fun getFormBody(): FormBody.Builder
    {
        return formBody
    }

    fun setFormBody(bodyMap: Map<String, Any>): TUTKCommander
    {
        for(key in bodyMap.keys)
            formBody.add(key, bodyMap[key].toString())
        return this
    }

    fun getParams(): JSONObject
    {
        return params
    }

    fun setParams(jasonObject: JSONObject): TUTKCommander
    {
        params = jasonObject
        return this
    }

    fun getParamsStr(): String
    {
        return paramStr
    }

    fun setParams(str: String): TUTKCommander
    {
        paramStr = str
        return this
    }

    fun getRequestPageName(): String
    {
        return requestPageName
    }

    fun setRequestPageName(requestPageName: String): TUTKCommander
    {
        this.requestPageName = requestPageName
        return this
    }

    fun setResponseListener(listener: ResponseListener): TUTKCommander
    {
        responseListener = listener
        return this
    }

    fun execute(): TUTKCommander
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
                responseListener.onConnectFail("${e.message}", getRequestPageName())
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
                    //GlobalBus.publish(MainEvent.HideLoading())
                    responseListener.onSuccess(responseStr)
                }
                else
                {
                    GlobalBus.publish(MainEvent.HideLoading())

                    if(call.request().url().toString().contains("refresh_token"))
                        responseListener.onFail(responseCode, responseStr, getRequestPageName(), AppConfig.HTTPErrorAction.ERR_ACT_GOTO_LOGIN)
                    else
                        responseListener.onFail(responseCode, responseStr, getRequestPageName(), AppConfig.HTTPErrorAction.ERR_ACT_GOTO_RESTART)
                }
            }
        })
        return this
    }
}