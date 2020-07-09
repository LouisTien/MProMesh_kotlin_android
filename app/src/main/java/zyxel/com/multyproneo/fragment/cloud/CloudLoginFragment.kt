package zyxel.com.multyproneo.fragment.cloud

import android.graphics.Bitmap
import android.net.Uri
import android.net.http.SslError
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.*
import androidx.fragment.app.Fragment
import com.google.gson.Gson
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.fragment_cloud_login.*
import org.jetbrains.anko.support.v4.alert
import org.json.JSONException
import org.json.JSONObject
import zyxel.com.multyproneo.BuildConfig
import zyxel.com.multyproneo.R
import zyxel.com.multyproneo.api.cloud.TUTKCommander
import zyxel.com.multyproneo.api.cloud.AMDMApi
import zyxel.com.multyproneo.dialog.MessageDialog
import zyxel.com.multyproneo.event.DialogEvent
import zyxel.com.multyproneo.event.GlobalBus
import zyxel.com.multyproneo.event.MainEvent
import zyxel.com.multyproneo.model.cloud.*
import zyxel.com.multyproneo.util.AppConfig
import zyxel.com.multyproneo.util.GlobalData
import zyxel.com.multyproneo.util.LogUtil
import zyxel.com.multyproneo.util.SharedPreferencesUtil
import java.util.HashMap
import android.content.Intent
import android.webkit.WebView
import android.webkit.WebChromeClient



class CloudLoginFragment : Fragment()
{
    private val TAG = javaClass.simpleName
    private lateinit var tokenInfo: TUTKTokenInfo
    private lateinit var userInfo: TUTKUserInfo
    private lateinit var specificDeviceInfo: TUTKSpecificDeviceInfo
    private lateinit var addDeviceInfo: TUTKAddDeviceInfo
    private lateinit var updateDeviceInfo: TUTKUpdateDeviceInfo
    private lateinit var msgDialogResponse: Disposable
    private var isInSetupFlow = true
    private var needLoginWhenFinal = false
    private var isDialogShowed = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
    {
        return inflater.inflate(R.layout.fragment_cloud_login, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)

        with(arguments)
        {
            this?.getBoolean("isInSetupFlow", true)?.let{ isInSetupFlow = it }
            this?.getBoolean("needLoginWhenFinal", false)?.let{ needLoginWhenFinal = it }
        }

        msgDialogResponse = GlobalBus.listen(DialogEvent.OnPositiveBtn::class.java).subscribe{ updateUI() }

        cloud_login_relative.visibility = View.INVISIBLE

        GlobalBus.publish(MainEvent.ShowLoading())

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
        if(!msgDialogResponse.isDisposed) msgDialogResponse.dispose()
    }

    private fun updateUI()
    {
        when(isInSetupFlow)
        {
            true ->
            {
                cloud_login_title_text.text = getString(R.string.cloud_login_connect_title)

                when(GlobalData.registeredCloud)
                {
                    true -> cloud_login_description_text.text = getString(R.string.cloud_login_connect_description_registered)
                    false -> cloud_login_description_text.text = getString(R.string.cloud_login_connect_description)
                }
            }

            false ->
            {
                cloud_login_title_text.text = getString(R.string.cloud_login_login_title)
                cloud_login_description_text.text = getString(R.string.cloud_login_login_description)
            }
        }

        cloud_login_relative.visibility = View.VISIBLE
    }

    private fun initWebView()
    {
        cloud_login_WebView.webViewClient = LoginWebViewClient()
        cloud_login_WebView.settings.setAppCacheEnabled(false)
        cloud_login_WebView.settings.cacheMode = WebSettings.LOAD_NO_CACHE
        cloud_login_WebView.settings.javaScriptEnabled = true
        cloud_login_WebView.settings.useWideViewPort = true
        cloud_login_WebView.settings.setSupportZoom(false)
        cloud_login_WebView.settings.javaScriptCanOpenWindowsAutomatically = false
        cloud_login_WebView.settings.saveFormData = false
        //https://am1.tutk.com/auth/authorize/?response_type=code&state=1234&client_id=vXudLCmYSwonVSetUPZrfiVDOjL5kmv2NQaUDmRG&client_secret=8Xj9bgS9IY7JspwEttChbyoHAEJrp05E6oGW6kqx4OsrITwYFxUae5x4wloWPYYoGC8XdQZoVlKm0clXdeyjgLpBkO08rt0yINEUZC35tkqjwR2oVMD3uCiOr9Z5rboz
        cloud_login_WebView.loadUrl("${BuildConfig.TUTK_AM_SITE}/auth/authorize/?response_type=code&state=${BuildConfig.TUTK_AM_STATE}&client_id=${BuildConfig.TUTK_AM_CLIENT_ID}&client_secret=${BuildConfig.TUTK_AM_CLIENT_SECRET}")
        //cloud_login_WebView.addJavascriptInterface(this, "onSubmitListener")
        cloud_login_WebView.clearCache(true)
        cloud_login_WebView.clearHistory()
        cloud_login_WebView.settings.setSupportMultipleWindows(true)
        cloud_login_WebView.webChromeClient = object : WebChromeClient()
        {
            override fun onCreateWindow(view: WebView, dialog: Boolean, userGesture: Boolean, resultMsg: android.os.Message): Boolean
            {
                val result = view.hitTestResult
                val data = result.extra
                val context = view.context
                val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(data))
                context.startActivity(browserIntent)
                return false
            }
        }
        CookieManager.getInstance().removeAllCookies(null)
    }

    private inner class LoginWebViewClient : WebViewClient()
    {
        override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?)
        {
            super.onPageStarted(view, url, favicon)
            LogUtil.d(TAG, "onPageStarted : $url")
        }

        override fun onPageFinished(view: WebView?, url: String?)
        {
            super.onPageFinished(view, url)

            //Production
            //https://am1.tutk.com/oauth/callback/?state=1234&code=7RZ2aTWquqWisnHQbsyMxNtE6rlnNN

            //Beta
            //https://test-us-am1-zyxel.kalayservice.com/oauth/callback/?code=DlPkcDHqXJtR8KnVBKJUGoAGIl58J0&state=1234

            LogUtil.d(TAG, "onPageFinished : $url")
            GlobalBus.publish(MainEvent.HideLoading())

            if(url!!.contains("accounts/login"))
            {
                when(isInSetupFlow)
                {
                    true ->
                    {
                        when(GlobalData.registeredCloud && !isDialogShowed)
                        {
                            true ->
                            {
                                MessageDialog(
                                        activity!!,
                                        getString(R.string.settings_login_cloud_title),
                                        getString(R.string.settings_login_cloud_msg),
                                        arrayOf(getString(R.string.setup_connect_controller_format_error_dialog_confirm)),
                                        AppConfig.DialogAction.ACT_NONE
                                ).show()

                                isDialogShowed = true
                            }

                            false -> updateUI()
                        }
                    }

                    false -> updateUI()
                }
            }

            if(url.contains("oauth/callback"))
            {
                val codeToken = Uri.parse(url).getQueryParameter("code")
                LogUtil.d(TAG, "get code token : $codeToken")

                var refreshToken by SharedPreferencesUtil(activity!!, AppConfig.SHAREDPREF_TUTK_REFRESH_TOKEN_KEY, "")
                var accessToken by SharedPreferencesUtil(activity!!, AppConfig.SHAREDPREF_TUTK_ACCESS_TOKEN_KEY, "")

                //Get Token
                val header = HashMap<String, Any>()
                header["authorization"] = "Basic ${BuildConfig.TUTK_DM_AUTHORIZATION}"
                header["content-type"] = "application/x-www-form-urlencoded"

                val body = HashMap<String, Any>()
                body["grant_type"] = "authorization_code"
                body["code"] = codeToken?:""

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
                                    {
                                        val bundle = Bundle().apply{
                                            putBoolean("needLoginWhenFinal", needLoginWhenFinal)
                                        }
                                        GlobalBus.publish(MainEvent.SwitchToFrag(SetupFinalizingYourHomeNetwork().apply{ arguments = bundle }))
                                    }
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
        header["authorization"] = "Basic ${BuildConfig.TUTK_DM_AUTHORIZATION}"
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
                            LogUtil.pd(TAG,"refreshTokenInfo:$tokenInfo")
                            refreshToken = tokenInfo.refresh_token
                            accessToken = tokenInfo.access_token
                            GlobalData.tokenType = tokenInfo.token_type
                            LogUtil.pd(TAG, "refreshToken:$refreshToken")
                            LogUtil.pd(TAG, "accessToken:$accessToken")
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
        val accessToken by SharedPreferencesUtil(activity!!, AppConfig.SHAREDPREF_TUTK_ACCESS_TOKEN_KEY, "")

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

        val accessToken by SharedPreferencesUtil(activity!!, AppConfig.SHAREDPREF_TUTK_ACCESS_TOKEN_KEY, "")

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
        val accessToken by SharedPreferencesUtil(activity!!, AppConfig.SHAREDPREF_TUTK_ACCESS_TOKEN_KEY, "")

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
        val accessToken by SharedPreferencesUtil(activity!!, AppConfig.SHAREDPREF_TUTK_ACCESS_TOKEN_KEY, "")

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
        val accessToken by SharedPreferencesUtil(activity!!, AppConfig.SHAREDPREF_TUTK_ACCESS_TOKEN_KEY, "")

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
        val accessToken by SharedPreferencesUtil(activity!!, AppConfig.SHAREDPREF_TUTK_ACCESS_TOKEN_KEY, "")

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