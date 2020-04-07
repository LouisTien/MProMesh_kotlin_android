package zyxel.com.multyproneo.fragment.cloud

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.gson.Gson
import kotlinx.android.synthetic.main.fragment_setup_finalizing_your_home_network.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.support.v4.runOnUiThread
import org.json.JSONException
import org.json.JSONObject
import zyxel.com.multyproneo.R
import zyxel.com.multyproneo.api.*
import zyxel.com.multyproneo.api.cloud.AMDMApi
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
import zyxel.com.multyproneo.util.*
import java.util.HashMap

class SetupFinalizingYourHomeNetwork : Fragment()
{
    private val TAG = javaClass.simpleName
    private lateinit var addDeviceInfo: TUTKAddDeviceInfo
    private lateinit var wanInfo: WanInfo
    private lateinit var devicesInfo: DevicesInfo
    private lateinit var WiFiSettingInfoSet: WiFiSettingInfo
    private lateinit var cloudAgentInfo: CloudAgentInfo
    private lateinit var userInfo: TUTKUserInfo
    private lateinit var loginInfo: LoginInfo
    private lateinit var db: DatabaseCloudUtil
    private var newHomeEndDeviceList = mutableListOf<DevicesInfoObject>()
    private var WiFiName = ""
    private var WiFiPwd = ""
    private var needLoginWhenFinal = false

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

        db = DatabaseCloudUtil.getInstance(activity!!)!!

        runOnUiThread{
            setup_finalizing_network_content_animation_view.setAnimation("FinalizingNetwork_1_oldJson.json")
            setup_finalizing_network_content_animation_view.playAnimation()
        }

        Thread.sleep(2000)

        if(needLoginWhenFinal)
            login()
        else
            getWanInfoTask()
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
                            getWanInfoTask()
                        }
                        catch(e: JSONException)
                        {
                            e.printStackTrace()
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

                            getDeviceInfoTask()
                        }
                        catch(e: JSONException)
                        {
                            GlobalBus.publish(MainEvent.HideLoading())
                            e.printStackTrace()
                        }
                    }
                }).execute()
    }

    private fun getDeviceInfoTask()
    {
        LogUtil.d(TAG,"getDeviceInfoTask()")

        DevicesApi.GetDevicesInfo()
                .setRequestPageName(TAG)
                .setIsUsingInCloudFlow(true)
                .setResponseListener(object: Commander.ResponseListener()
                {
                    override fun onSuccess(responseStr: String)
                    {
                        try
                        {
                            devicesInfo = Gson().fromJson(responseStr, DevicesInfo::class.javaObjectType)
                            LogUtil.d(TAG,"devicesInfo:$devicesInfo")

                            var index = 1
                            for(item in devicesInfo.Object)
                            {
                                item.IndexFromFW = index

                                if( (item.HostName == "N/A") || (item.HostName == "") )
                                {
                                    index++
                                    continue
                                }

                                if(item.X_ZYXEL_CapabilityType != "L2Device" && item.X_ZYXEL_Conn_Guest != 1)
                                    newHomeEndDeviceList.add(item)

                                LogUtil.d(TAG,"update devicesInfo:$item")

                                index++
                            }

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
                        Thread.sleep(3000)
                        getIOTCLoginStatus()
                    }
                }).execute()
    }

    private fun getIOTCLoginStatus()
    {
        LogUtil.d(TAG,"getIOTCLoginStatus()")
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
                                addDevice()
                            else
                                gotoTroubleShooting()
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
        params.put("displayName", GlobalData.getCurrentGatewayInfo().ModelName)
        params.put("credential", GlobalData.getCurrentGatewayInfo().MAC)
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
                            addToDB()
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
                    true
            )

            db.getSiteInfoDao().insert(siteInfo)

            for(item in newHomeEndDeviceList)
            {
                val clientInfo = DatabaseClientListEntity(
                        GlobalData.getCurrentGatewayInfo().MAC,
                        item.PhysAddress,
                        item.HostName
                )

                db.getClientListDao().insert(clientInfo)
            }

            setLogoutTask()
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

    private fun gotoTroubleShooting()
    {
        val bundle = Bundle().apply{
            putSerializable("pageMode", AppConfig.TroubleshootingPage.PAGE_CLOUD_API_ERROR)
        }

        GlobalBus.publish(MainEvent.SwitchToFrag(SetupConnectTroubleshootingFragment().apply{ arguments = bundle }))
    }
}