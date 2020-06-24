package zyxel.com.multyproneo.fragment.cloud

import android.os.Bundle
import android.os.CountDownTimer
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.gson.Gson
import kotlinx.android.synthetic.main.fragment_setup_finalizing_your_home_network.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.support.v4.runOnUiThread
import org.json.JSONException
import org.json.JSONObject
import zyxel.com.multyproneo.R
import zyxel.com.multyproneo.api.*
import zyxel.com.multyproneo.api.cloud.AMDMApi
import zyxel.com.multyproneo.api.cloud.NotificationApi
import zyxel.com.multyproneo.api.cloud.TUTKCommander
import zyxel.com.multyproneo.database.room.DatabaseClientListEntity
import zyxel.com.multyproneo.database.room.DatabaseSiteInfoEntity
import zyxel.com.multyproneo.event.GlobalBus
import zyxel.com.multyproneo.event.MainEvent
import zyxel.com.multyproneo.model.*
import zyxel.com.multyproneo.model.cloud.CloudAgentInfo
import zyxel.com.multyproneo.model.cloud.TUTKAddDeviceInfo
import zyxel.com.multyproneo.model.cloud.TUTKAllDeviceInfo
import zyxel.com.multyproneo.model.cloud.TUTKUserInfo
import zyxel.com.multyproneo.tool.CryptTool
import zyxel.com.multyproneo.util.*
import java.util.HashMap

class SetupFinalizingYourHomeNetwork : Fragment()
{
    private val TAG = javaClass.simpleName
    private lateinit var addDeviceInfo: TUTKAddDeviceInfo
    private lateinit var wanInfo: WanInfo
    private lateinit var changeIconNameInfo: ChangeIconNameInfo
    private lateinit var WiFiSettingInfoSet: WiFiSettingInfo
    private lateinit var cloudAgentInfo: CloudAgentInfo
    private lateinit var userInfo: TUTKUserInfo
    private lateinit var loginInfo: LoginInfo
    private lateinit var db: DatabaseCloudUtil
    private lateinit var countDownTimerWanInfo: CountDownTimer
    private lateinit var countDownTimerIOTCStatus: CountDownTimer
    private var changeIconNameList = mutableListOf<ChangeIconNameInfoObject>()
    private var WiFiName = ""
    private var WiFiPwd = ""
    private var needLoginWhenFinal = false
    private var getIOTCStatusCount = 0
    private val IOTCRetryTimes = 5

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
    {
        return inflater.inflate(R.layout.fragment_setup_finalizing_your_home_network, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)

        with(arguments)
        {
            this?.getBoolean("needLoginWhenFinal", false)?.let{ needLoginWhenFinal = it }
        }

        countDownTimerWanInfo = object : CountDownTimer((AppConfig.waitForLoginTime * 1000).toLong(), 1000)
        {
            override fun onTick(millisUntilFinished: Long) {}
            override fun onFinish() = getSystemInfoTask()
        }

        countDownTimerIOTCStatus = object : CountDownTimer((AppConfig.waitForGetIOTCLoginStatusTime * 1000).toLong(), 1000)
        {
            override fun onTick(millisUntilFinished: Long) {}
            override fun onFinish() = getIOTCLoginStatus()
        }

        db = DatabaseCloudUtil.getInstance(activity!!)!!

        runOnUiThread{
            setup_finalizing_network_content_animation_view.setAnimation("FinalizingNetwork_1_oldJson.json")
            setup_finalizing_network_content_animation_view.playAnimation()
        }

        Thread.sleep(2000)

        when(needLoginWhenFinal)
        {
            true ->
            {
                when(AppConfig.SNLogin)
                {
                    true -> SNlogin()
                    false -> login()
                }

                countDownTimerWanInfo.start()
            }

            false -> getSystemInfoTask()
        }
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
        countDownTimerWanInfo.cancel()
        countDownTimerIOTCStatus.cancel()
    }

    private fun login()
    {
        LogUtil.d(TAG,"login()")

        val params = JSONObject()
        params.put("username", GlobalData.getCurrentGatewayInfo().UserName)
        params.put("password", GlobalData.getCurrentGatewayInfo().Password)
        LogUtil.d(TAG,"login param:$params")
        AccountApi.Login()
                .setRequestPageName(TAG)
                .setParams(params)
                .setIsUsingInCloudFlow(true)
                .setResponseListener(object: Commander.ResponseListener()
                {
                    override fun onSuccess(responseStr: String)
                    {
                        try
                        {
                            loginInfo = Gson().fromJson(responseStr, LoginInfo::class.javaObjectType)
                            LogUtil.d(TAG,"loginInfo:$loginInfo")
                            GlobalData.sessionKey = loginInfo.sessionkey
                        }
                        catch(e: JSONException)
                        {
                            e.printStackTrace()
                        }
                    }
                }).execute()
    }

    private fun SNlogin()
    {
        LogUtil.d(TAG,"SNlogin()")

        val iv = CryptTool.getRandomString(16)
        val encryptedSN = CryptTool.EncryptAES(
                iv.toByteArray(charset("UTF-8")),
                CryptTool.KeyAESDefault.toByteArray(charset("UTF-8")),
                GlobalData.getCurrentGatewayInfo().SerialNumber.toByteArray(charset("UTF-8")))

        val params = JSONObject()
        params.put("serialnumber", encryptedSN)
        params.put("iv", iv)
        LogUtil.d(TAG,"login param:$params")
        AccountApi.SNLogin()
                .setRequestPageName(TAG)
                .setParams(params)
                .setIsUsingInCloudFlow(true)
                .setResponseListener(object: Commander.ResponseListener()
                {
                    override fun onSuccess(responseStr: String)
                    {
                        try
                        {
                            loginInfo = Gson().fromJson(responseStr, LoginInfo::class.javaObjectType)
                            LogUtil.d(TAG,"loginInfo:$loginInfo")
                            GlobalData.sessionKey = loginInfo.sessionkey
                        }
                        catch(e: JSONException)
                        {
                            e.printStackTrace()
                        }
                    }
                }).execute()
    }

    private fun getSystemInfoTask()
    {
        LogUtil.d(TAG,"getSystemInfoTask()")
        GatewayApi.GetSystemInfo()
                .setRequestPageName(TAG)
                .setResponseListener(object: Commander.ResponseListener()
                {
                    override fun onSuccess(responseStr: String)
                    {
                        try
                        {
                            val data = JSONObject(responseStr)
                            var name = data.getJSONObject("Object").getString("HostName")
                            LogUtil.d(TAG,"HostName:$name")
                            GlobalData.getCurrentGatewayInfo().UserDefineName = name
                            getWanInfoTask()
                        }
                        catch(e: JSONException)
                        {
                            e.printStackTrace()

                            GlobalBus.publish(MainEvent.HideLoading())
                        }
                    }
                }).execute()
    }

    private fun getWanInfoTask()
    {
        LogUtil.d(TAG,"getWanInfoTask()")

        GatewayApi.GetWanInfo()
                .setRequestPageName(TAG)
                .setIsUsingInCloudFlow(true)
                .setResponseListener(object: Commander.ResponseListener()
                {
                    override fun onSuccess(responseStr: String)
                    {
                        try
                        {
                            wanInfo = Gson().fromJson(responseStr, WanInfo::class.javaObjectType)
                            LogUtil.d(TAG,"wanInfo:$wanInfo")
                            GlobalData.gatewayWanInfo = wanInfo.copy()
                            GlobalData.getCurrentGatewayInfo().MAC = wanInfo.Object.MAC
                            getWiFiSettingInfoTask()
                        }
                        catch(e: JSONException)
                        {
                            e.printStackTrace()

                            GlobalBus.publish(MainEvent.HideLoading())
                        }
                    }
                }).execute()
    }

    private fun getWiFiSettingInfoTask()
    {
        LogUtil.d(TAG,"getWiFiSettingInfoTask()")

        WiFiSettingApi.GetWiFiSettingInfo()
                .setRequestPageName(TAG)
                .setIsUsingInCloudFlow(true)
                .setResponseListener(object: Commander.ResponseListener()
                {
                    override fun onSuccess(responseStr: String)
                    {
                        try
                        {
                            WiFiSettingInfoSet = Gson().fromJson(responseStr, WiFiSettingInfo::class.javaObjectType)
                            LogUtil.d(TAG,"wiFiSettingInfo:$WiFiSettingInfoSet")

                            WiFiName = WiFiSettingInfoSet.Object.SSID[0].SSID
                            WiFiPwd = WiFiSettingInfoSet.Object.AccessPoint[0].Security.KeyPassphrase

                            getChangeIconNameInfoTask()
                        }
                        catch(e: JSONException)
                        {
                            GlobalBus.publish(MainEvent.HideLoading())
                            e.printStackTrace()
                        }
                    }
                }).execute()
    }

    private fun getChangeIconNameInfoTask()
    {
        LogUtil.d(TAG,"getChangeIconNameInfoTask()")

        DevicesApi.GetChangeIconNameInfo()
                .setRequestPageName(TAG)
                .setResponseListener(object: Commander.ResponseListener()
                {
                    override fun onSuccess(responseStr: String)
                    {
                        try
                        {
                            changeIconNameInfo = Gson().fromJson(responseStr, ChangeIconNameInfo::class.javaObjectType)
                            LogUtil.d(TAG,"changeIconNameInfo:$changeIconNameInfo")
                            changeIconNameList = changeIconNameInfo.Object.toMutableList()
                            startRDTServer()
                        }
                        catch(e: JSONException)
                        {
                            e.printStackTrace()

                            GlobalBus.publish(MainEvent.HideLoading())
                        }
                    }
                }).execute()
    }

    private fun startRDTServer()
    {
        val params = JSONObject()
        params.put("Enable", true)
        params.put("GenCredential", true)
        params.put("PushNotification", true)
        LogUtil.d(TAG,"startRDTServer param:$params")

        GatewayApi.ControlCloudAgent()
                .setRequestPageName(TAG)
                .setParams(params)
                .setIsUsingInCloudFlow(true)
                .setResponseListener(object: Commander.ResponseListener()
                {
                    override fun onSuccess(responseStr: String)
                    {
                        cloudAgentInfo = Gson().fromJson(responseStr, CloudAgentInfo::class.javaObjectType)
                        LogUtil.d(TAG,"startRDTServer:$cloudAgentInfo")
                        GlobalData.sessionKey = cloudAgentInfo.sessionkey
                        GlobalData.currentCredential = cloudAgentInfo.Object.Credential
                        countDownTimerIOTCStatus.start()
                    }
                }).execute()
    }

    private fun getIOTCLoginStatus()
    {
        LogUtil.d(TAG,"getIOTCLoginStatus()")

        getIOTCStatusCount++

        GatewayApi.GetCloudAgentInfo()
                .setRequestPageName(TAG)
                .setIsUsingInCloudFlow(true)
                .setResponseListener(object: Commander.ResponseListener()
                {
                    override fun onSuccess(responseStr: String)
                    {
                        try
                        {
                            cloudAgentInfo = Gson().fromJson(responseStr, CloudAgentInfo::class.javaObjectType)
                            LogUtil.d(TAG,"getIOTCLoginStatus:$cloudAgentInfo")

                            if(cloudAgentInfo.Object.Status.contains("success", ignoreCase = true))
                            {
                                GlobalData.currentCredential = cloudAgentInfo.Object.Credential
                                addDevice()
                            }
                            else
                            {
                                if(getIOTCStatusCount <= IOTCRetryTimes)
                                    countDownTimerIOTCStatus.start()
                                else
                                    gotoTroubleShooting()
                            }
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
        LogUtil.d(TAG,"addDevice()")

        val accessToken by SharedPreferencesUtil(activity!!, AppConfig.SHAREDPREF_TUTK_ACCESS_TOKEN_KEY, "")

        val header = HashMap<String, Any>()
        header["authorization"] = "${GlobalData.tokenType} $accessToken"

        val params = JSONObject()
        params.put("udid", GlobalData.currentUID)
        params.put("fwVer", GlobalData.getCurrentGatewayInfo().SoftwareVersion)
        //params.put("displayName", GlobalData.getCurrentGatewayInfo().ModelName)
        params.put("displayName", GlobalData.getCurrentGatewayInfo().UserDefineName)
        params.put("credential", GlobalData.currentCredential)
        LogUtil.d(TAG,"addDevice param:$params")

        LogUtil.e(TAG,"UserDefineName:${GlobalData.getCurrentGatewayInfo().UserDefineName}")
        LogUtil.e(TAG,"ModelName:${GlobalData.getCurrentGatewayInfo().ModelName}")

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
                            registerNoti()
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

                            runOnUiThread{
                                setup_finalizing_network_content_animation_view.setAnimation("FinalizingNetwork_2_oldJson.json")
                                setup_finalizing_network_content_animation_view.playAnimation()
                            }

                            Thread.sleep(5500)

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

    private fun registerNoti()
    {
        LogUtil.d(TAG,"registerNoti()")

        val phoneUdid = Settings.System.getString(activity!!.contentResolver, Settings.Secure.ANDROID_ID)
        val notificationToken by SharedPreferencesUtil(activity!!, AppConfig.SHAREDPREF_NOTIFICATION_TOKEN, "")

        val header = HashMap<String, Any>()
        val body = HashMap<String, Any>()
        body["cmd"] = "client"
        body["os"] = "android"
        body["appid"] = AppConfig.NOTI_BUNDLE_ID
        body["udid"] = phoneUdid
        body["token"] = notificationToken
        body["lang"] = "enUS"
        body["dev"] = 0

        NotificationApi.Common(activity!!)
                .setRequestPageName(TAG)
                .setHeaders(header)
                .setFormBody(body)
                .setResponseListener(object: TUTKCommander.ResponseListener()
                {
                    override fun onSuccess(responseStr: String)
                    {
                        LogUtil.d(TAG,"NotificationApi Register:$responseStr")
                        mappingNoti()
                    }
                }).execute()
    }

    private fun mappingNoti()
    {
        LogUtil.d(TAG,"mappingNoti()")

        val phoneUdid = Settings.System.getString(activity!!.contentResolver, Settings.Secure.ANDROID_ID)

        val header = HashMap<String, Any>()
        val body = HashMap<String, Any>()
        body["cmd"] = "mapping"
        body["os"] = "android"
        body["appid"] = AppConfig.NOTI_BUNDLE_ID
        body["uid"] = GlobalData.currentUID
        body["udid"] = phoneUdid
        body["format"] = AppConfig.NOTI_FORMAT
        body["interval"] = AppConfig.NOTI_INTERVAL

        NotificationApi.Common(activity!!)
                .setRequestPageName(TAG)
                .setHeaders(header)
                .setFormBody(body)
                .setResponseListener(object: TUTKCommander.ResponseListener()
                {
                    override fun onSuccess(responseStr: String)
                    {
                        LogUtil.d(TAG,"NotificationApi Mapping:$responseStr")
                        addToDB()
                    }
                }).execute()
    }

    private fun addToDB()
    {
        doAsync{

            val siteInfo = DatabaseSiteInfoEntity(
                    GlobalData.getCurrentGatewayInfo().MAC,
                    GlobalData.currentUID,
                    GlobalData.getCurrentGatewayInfo().ModelName,
                    "N/A",
                    WiFiName,
                    WiFiPwd,
                    true,
                    true
            )

            db.getSiteInfoDao().insert(siteInfo)

            for(item in changeIconNameList)
            {
                val clientInfo = DatabaseClientListEntity(
                        GlobalData.getCurrentGatewayInfo().MAC,
                        item.MacAddress,
                        item.HostName
                )

                db.getClientListDao().insert(clientInfo)
            }

            if(AppConfig.SNLogin) setSNLogoutTask() else setLogoutTask()
        }
    }

    private fun setLogoutTask()
    {
        val params = JSONObject()
        AccountApi.Logout()
                .setRequestPageName(TAG)
                .setParams(params)
                .setIsUsingInCloudFlow(true)
                .setResponseListener(object: Commander.ResponseListener()
                {
                    override fun onSuccess(responseStr: String)
                    {
                        getUserInfo()
                    }
                }).execute()
    }

    private fun setSNLogoutTask()
    {
        val params = JSONObject()
        AccountApi.SNLogout()
                .setRequestPageName(TAG)
                .setParams(params)
                .setIsUsingInCloudFlow(true)
                .setResponseListener(object: Commander.ResponseListener()
                {
                    override fun onSuccess(responseStr: String)
                    {
                        getUserInfo()
                    }
                }).execute()
    }

    private fun gotoTroubleShooting()
    {
        val bundle = Bundle().apply{
            putSerializable("pageMode", AppConfig.TroubleshootingPage.PAGE_CLOUD_API_ERROR)
        }

        GlobalBus.publish(MainEvent.SwitchToFrag(SetupConnectTroubleshootingFragment().apply{ arguments = bundle }))
    }
}