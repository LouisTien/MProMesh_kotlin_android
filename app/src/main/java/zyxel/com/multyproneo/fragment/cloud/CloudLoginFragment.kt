package zyxel.com.multyproneo.fragment.cloud

import android.graphics.Bitmap
import android.net.http.SslError
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.*
import com.google.gson.Gson
import kotlinx.android.synthetic.main.fragment_cloud_login.*
import org.jetbrains.anko.support.v4.alert
import org.json.JSONException
import org.json.JSONObject
import zyxel.com.multyproneo.R
import zyxel.com.multyproneo.api.cloud.TUTKCommander
import zyxel.com.multyproneo.api.cloud.AMDMApi
import zyxel.com.multyproneo.event.GlobalBus
import zyxel.com.multyproneo.event.MainEvent
import zyxel.com.multyproneo.model.cloud.*
import zyxel.com.multyproneo.util.AppConfig
import zyxel.com.multyproneo.util.GlobalData
import zyxel.com.multyproneo.util.LogUtil
import zyxel.com.multyproneo.util.SharedPreferencesUtil
import java.util.HashMap

class CloudLoginFragment : Fragment()
{
    private val TAG = javaClass.simpleName
    private val CODE_TOKEN_PARAM = "&code="
    private lateinit var tokenInfo: TUTKTokenInfo
    private lateinit var userInfo: TUTKUserInfo
    private lateinit var specificDeviceInfo: TUTKSpecificDeviceInfo
    private lateinit var addDeviceInfo: TUTKAddDeviceInfo
    private lateinit var updateDeviceInfo: TUTKUpdateDeviceInfo
    private var isInSetupFlow = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
    {
        return inflater.inflate(R.layout.fragment_cloud_login, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)

        with(arguments)
        {
            this?.getBoolean("isInSetupFlow")?.let{ isInSetupFlow = it }
        }

        initWebView()
    }

    override fun onResume()
    {
        super.onResume()
        GlobalBus.publish(MainEvent.HideBottomToolbar())
    }

    override fun onPause()
    {
        super.onPause()
    }

    override fun onDestroyView()
    {
        super.onDestroyView()
    }

    private fun initWebView()
    {
        cloudLoginWebView.webViewClient = LoginWebViewClient()
        cloudLoginWebView.settings.setAppCacheEnabled(false)
        cloudLoginWebView.settings.cacheMode = WebSettings.LOAD_NO_CACHE
        cloudLoginWebView.settings.javaScriptEnabled = true
        cloudLoginWebView.settings.useWideViewPort = true
        cloudLoginWebView.settings.setSupportZoom(false)
        cloudLoginWebView.settings.javaScriptCanOpenWindowsAutomatically = false
        cloudLoginWebView.settings.saveFormData = false
        //https://am1.tutk.com/auth/authorize/?response_type=code&state=1234&client_id=vXudLCmYSwonVSetUPZrfiVDOjL5kmv2NQaUDmRG&client_secret=8Xj9bgS9IY7JspwEttChbyoHAEJrp05E6oGW6kqx4OsrITwYFxUae5x4wloWPYYoGC8XdQZoVlKm0clXdeyjgLpBkO08rt0yINEUZC35tkqjwR2oVMD3uCiOr9Z5rboz
        cloudLoginWebView.loadUrl("${AppConfig.TUTK_AM_SITE}/auth/authorize/?response_type=code&state=${AppConfig.TUTK_AM_STATE}&client_id=${AppConfig.TUTK_AM_CLIENT_ID}&client_secret=${AppConfig.TUTK_AM_CLIENT_SECRET}")
        //cloudLoginWebView.addJavascriptInterface(this, "onSubmitListener")
        cloudLoginWebView.clearCache(true)
        cloudLoginWebView.clearHistory()
        CookieManager.getInstance().removeAllCookies(null)
    }

    private inner class LoginWebViewClient : WebViewClient()
    {
        override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?)
        {
            super.onPageStarted(view, url, favicon)
            LogUtil.d(TAG, "onPageStarted : $url")
            GlobalBus.publish(MainEvent.ShowLoading())
        }

        override fun onPageFinished(view: WebView?, url: String?)
        {
            super.onPageFinished(view, url)
            //https://am1.tutk.com/oauth/callback/?state=1234&code=7RZ2aTWquqWisnHQbsyMxNtE6rlnNN
            LogUtil.d(TAG, "onPageFinished : $url")
            GlobalBus.publish(MainEvent.HideLoading())

            if(url!!.contains("oauth/callback"))
            {
                val codeToken = url.substring(url.indexOf(CODE_TOKEN_PARAM) + CODE_TOKEN_PARAM.length)
                LogUtil.d(TAG, "get code token : $codeToken")

                var refreshToken by SharedPreferencesUtil(activity!!, AppConfig.SHAREDPREF_TUTK_REFRESH_TOKEN_KEY, "")
                var accessToken by SharedPreferencesUtil(activity!!, AppConfig.SHAREDPREF_TUTK_ACCESS_TOKEN_KEY, "")

                //Get Token
                val header = HashMap<String, Any>()
                header["authorization"] = "Basic ${AppConfig.TUTK_DM_AUTHORIZATION}"
                header["content-type"] = "application/x-www-form-urlencoded"

                val body = HashMap<String, Any>()
                body["grant_type"] = "authorization_code"
                body["code"] = codeToken

                AMDMApi.GetToken()
                        .setRequestPageName(TAG)
                        .setHeaders(header)
                        .setFormBody(body)
                        .setResponseListener(object: TUTKCommander.ResponseListener()
                        {
                            override fun onSuccess(responseStr: String)
                            {
                                try
                                {
                                    tokenInfo = Gson().fromJson(responseStr, TUTKTokenInfo::class.javaObjectType)
                                    LogUtil.d(TAG,"getTokenInfo:$tokenInfo")
                                    refreshToken = tokenInfo.refresh_token
                                    accessToken = tokenInfo.access_token
                                    GlobalData.tokenType = tokenInfo.token_type
                                    LogUtil.d(TAG, "refreshToken:$refreshToken")
                                    LogUtil.d(TAG, "accessToken:$accessToken")

                                    if(isInSetupFlow)
                                        GlobalBus.publish(MainEvent.SwitchToFrag(SetupFinalizingYourHomeNetwork()))
                                    else
                                        GlobalBus.publish(MainEvent.GetCloudInfo())
                                }
                                catch(e: JSONException)
                                {
                                    e.printStackTrace()

                                    GlobalBus.publish(MainEvent.HideLoading())
                                }
                            }
                        }).execute()
            }

            view!!.clearHistory()
            view.clearCache(true)
        }

        override fun onReceivedError(view: WebView?, errorCode: Int, description: String?, failingUrl: String?)
        {
            super.onReceivedError(view, errorCode, description, failingUrl)
            LogUtil.e(TAG, "onReceivedError -> errorCode : $errorCode, failingUrl : $failingUrl")
            GlobalBus.publish(MainEvent.HideLoading())
            GlobalBus.publish(MainEvent.ShowToast(getString(R.string.cloud_login_no_internet), TAG))
        }

        override fun onReceivedError(view: WebView?, request: WebResourceRequest?, error: WebResourceError?)
        {
            super.onReceivedError(view, request, error)
            LogUtil.e(TAG, "onReceivedError -> error : $error, request : $request")
            GlobalBus.publish(MainEvent.HideLoading())
            GlobalBus.publish(MainEvent.ShowToast(getString(R.string.cloud_login_no_internet), TAG))
        }

        override fun onReceivedSslError(view: WebView?, handler: SslErrorHandler?, error: SslError?)
        {
            super.onReceivedSslError(view, handler, error)
            LogUtil.e(TAG, "onReceivedSslError -> error : $error")
            GlobalBus.publish(MainEvent.HideLoading())
            alert(getString(R.string.cloud_login_no_ssl_error))
            {
                positiveButton("continue") { handler!!.proceed()}
                negativeButton("cancel") { handler!!.cancel()}
            }.show()
        }
    }

    private fun refreshToken()
    {
        var refreshToken by SharedPreferencesUtil(activity!!, AppConfig.SHAREDPREF_TUTK_REFRESH_TOKEN_KEY, "")
        var accessToken by SharedPreferencesUtil(activity!!, AppConfig.SHAREDPREF_TUTK_ACCESS_TOKEN_KEY, "")

        val header = HashMap<String, Any>()
        header["authorization"] = "Basic ${AppConfig.TUTK_DM_AUTHORIZATION}"
        header["content-type"] = "application/x-www-form-urlencoded"

        val body = HashMap<String, Any>()
        body["grant_type"] = "refresh_token"
        body["refresh_token"] = refreshToken

        AMDMApi.RefreshToken()
                .setRequestPageName(TAG)
                .setHeaders(header)
                .setFormBody(body)
                .setResponseListener(object: TUTKCommander.ResponseListener()
                {
                    override fun onSuccess(responseStr: String)
                    {
                        try
                        {
                            tokenInfo = Gson().fromJson(responseStr, TUTKTokenInfo::class.javaObjectType)
                            LogUtil.d(TAG,"refreshTokenInfo:$tokenInfo")
                            refreshToken = tokenInfo.refresh_token
                            accessToken = tokenInfo.access_token
                            GlobalData.tokenType = tokenInfo.token_type
                            LogUtil.d(TAG, "refreshToken:$refreshToken")
                            LogUtil.d(TAG, "accessToken:$accessToken")
                        }
                        catch(e: JSONException)
                        {
                            e.printStackTrace()

                            GlobalBus.publish(MainEvent.HideLoading())
                        }
                    }
                }).execute()
    }

    private fun getUserInfo()
    {
        var accessToken by SharedPreferencesUtil(activity!!, AppConfig.SHAREDPREF_TUTK_ACCESS_TOKEN_KEY, "")

        val header = HashMap<String, Any>()
        header["authorization"] = "${GlobalData.tokenType} $accessToken"

        AMDMApi.GetUserInfo()
                .setRequestPageName(TAG)
                .setHeaders(header)
                .setResponseListener(object: TUTKCommander.ResponseListener()
                {
                    override fun onSuccess(responseStr: String)
                    {
                        try
                        {
                            userInfo = Gson().fromJson(responseStr, TUTKUserInfo::class.javaObjectType)
                            LogUtil.d(TAG,"userInfo:$userInfo")
                            GlobalData.currentEmail = userInfo.email
                            getAllDevice()
                        }
                        catch(e: JSONException)
                        {
                            e.printStackTrace()

                            GlobalBus.publish(MainEvent.HideLoading())
                        }
                    }
                }).execute()
    }

    private fun getAllDevice()
    {
        LogUtil.d(TAG,"getAllDevice()")

        var accessToken by SharedPreferencesUtil(activity!!, AppConfig.SHAREDPREF_TUTK_ACCESS_TOKEN_KEY, "")

        val header = HashMap<String, Any>()
        header["authorization"] = "${GlobalData.tokenType} $accessToken"

        AMDMApi.GetAllDevice()
                .setRequestPageName(TAG)
                .setHeaders(header)
                .setResponseListener(object: TUTKCommander.ResponseListener()
                {
                    override fun onSuccess(responseStr: String)
                    {
                        try
                        {
                            GlobalData.cloudGatewayListInfo = Gson().fromJson(responseStr, TUTKAllDeviceInfo::class.javaObjectType)
                            LogUtil.d(TAG,"allDeviceInfo:${GlobalData.cloudGatewayListInfo}")
                            GlobalBus.publish(MainEvent.SwitchToFrag(CloudGatewayListFragment()))
                        }
                        catch(e: JSONException)
                        {
                            e.printStackTrace()

                            GlobalBus.publish(MainEvent.HideLoading())
                        }
                    }
                }).execute()
    }

    private fun getSpecificDevice()
    {
        var accessToken by SharedPreferencesUtil(activity!!, AppConfig.SHAREDPREF_TUTK_ACCESS_TOKEN_KEY, "")

        val header = HashMap<String, Any>()
        header["authorization"] = "${GlobalData.tokenType} $accessToken"

        AMDMApi.GetSpecificDevice("EBKUAX3MUD7M9G6GU1CJ")
                .setRequestPageName(TAG)
                .setHeaders(header)
                .setResponseListener(object: TUTKCommander.ResponseListener()
                {
                    override fun onSuccess(responseStr: String)
                    {
                        try
                        {
                            specificDeviceInfo = Gson().fromJson(responseStr, TUTKSpecificDeviceInfo::class.javaObjectType)
                            LogUtil.d(TAG,"specificDeviceInfo:$specificDeviceInfo")
                        }
                        catch(e: JSONException)
                        {
                            e.printStackTrace()

                            GlobalBus.publish(MainEvent.HideLoading())
                        }
                    }
                }).execute()
    }

    private fun addDevice()
    {
        var accessToken by SharedPreferencesUtil(activity!!, AppConfig.SHAREDPREF_TUTK_ACCESS_TOKEN_KEY, "")

        val header = HashMap<String, Any>()
        header["authorization"] = "${GlobalData.tokenType} $accessToken"

        val params = JSONObject()
        params.put("udid", "77KA952WU5RMUH6GY123")
        params.put("fwVer", "V5.17(ABUP.0)b2_20200311")
        params.put("displayName", "EX3510-Test")
        params.put("credential", "00:00:00:00:00")
        LogUtil.d(TAG,"addDevice param:$params")

        AMDMApi.AddDevice()
                .setRequestPageName(TAG)
                .setHeaders(header)
                .setParams(params)
                .setResponseListener(object: TUTKCommander.ResponseListener()
                {
                    override fun onSuccess(responseStr: String)
                    {
                        try
                        {
                            addDeviceInfo = Gson().fromJson(responseStr, TUTKAddDeviceInfo::class.javaObjectType)
                            LogUtil.d(TAG,"addDeviceInfo:$addDeviceInfo")
                        }
                        catch(e: JSONException)
                        {
                            e.printStackTrace()

                            GlobalBus.publish(MainEvent.HideLoading())
                        }
                    }
                }).execute()
    }


    private fun updateDevice()
    {
        var accessToken by SharedPreferencesUtil(activity!!, AppConfig.SHAREDPREF_TUTK_ACCESS_TOKEN_KEY, "")

        val header = HashMap<String, Any>()
        header["authorization"] = "${GlobalData.tokenType} $accessToken"

        val params = JSONObject()
        params.put("fwVer", "v3.0.9")
        params.put("displayName", "AmyHome")
        params.put("credential", "xxxxxxxxxxxxx")
        LogUtil.d(TAG,"updateDevice param:$params")

        AMDMApi.UpdateDevice("kkkkkkkkkkkkkkkkkkkk")
                .setRequestPageName(TAG)
                .setHeaders(header)
                .setParams(params)
                .setResponseListener(object: TUTKCommander.ResponseListener()
                {
                    override fun onSuccess(responseStr: String)
                    {
                        try
                        {
                            updateDeviceInfo = Gson().fromJson(responseStr, TUTKUpdateDeviceInfo::class.javaObjectType)
                            LogUtil.d(TAG,"updateDeviceInfo:$updateDeviceInfo")
                        }
                        catch(e: JSONException)
                        {
                            e.printStackTrace()

                            GlobalBus.publish(MainEvent.HideLoading())
                        }
                    }
                }).execute()
    }

    private fun deleteDevice()
    {
        var accessToken by SharedPreferencesUtil(activity!!, AppConfig.SHAREDPREF_TUTK_ACCESS_TOKEN_KEY, "")

        val header = HashMap<String, Any>()
        header["authorization"] = "${GlobalData.tokenType} $accessToken"

        AMDMApi.DeleteDevice("kkkkkkkkkkkkkkkkkkkk")
                .setRequestPageName(TAG)
                .setHeaders(header)
                .setResponseListener(object: TUTKCommander.ResponseListener()
                {
                    override fun onSuccess(responseStr: String)
                    {
                        LogUtil.d(TAG,"deleteDevice:$responseStr")
                        getAllDevice()
                    }
                }).execute()
    }
}