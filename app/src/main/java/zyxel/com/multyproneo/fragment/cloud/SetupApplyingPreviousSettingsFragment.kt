package zyxel.com.multyproneo.fragment.cloud

import android.os.Bundle
import android.os.CountDownTimer
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_applying_previous_settings.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.support.v4.runOnUiThread
import org.jetbrains.anko.uiThread
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import zyxel.com.multyproneo.R
import zyxel.com.multyproneo.api.AccountApi
import zyxel.com.multyproneo.api.Commander
import zyxel.com.multyproneo.api.DevicesApi
import zyxel.com.multyproneo.api.WiFiSettingApi
import zyxel.com.multyproneo.database.room.DatabaseClientListEntity
import zyxel.com.multyproneo.database.room.DatabaseSiteInfoEntity
import zyxel.com.multyproneo.event.GlobalBus
import zyxel.com.multyproneo.event.MainEvent
import zyxel.com.multyproneo.util.AppConfig
import zyxel.com.multyproneo.util.DatabaseCloudUtil
import zyxel.com.multyproneo.util.GlobalData
import zyxel.com.multyproneo.util.LogUtil

class SetupApplyingPreviousSettingsFragment : Fragment()
{
    private val TAG = javaClass.simpleName
    private var mac = ""
    private lateinit var db: DatabaseCloudUtil
    private lateinit var siteInfo: DatabaseSiteInfoEntity
    private lateinit var clientList: List<DatabaseClientListEntity>
    private lateinit var countDownTimer: CountDownTimer

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
    {
        return inflater.inflate(R.layout.fragment_applying_previous_settings, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)
        with(arguments){ this?.getString("MAC")?.let{ mac = it } }

        countDownTimer = object : CountDownTimer((AppConfig.WiFiSettingTime * 1000).toLong(), 1000)
        {
            override fun onTick(millisUntilFinished: Long) {}
            override fun onFinish() = finishAction()
        }

        db = DatabaseCloudUtil.getInstance(activity!!)!!
        getDataFromDB()

        runOnUiThread{
            setup_apply_previous_settings_content_animation_view.setAnimation("ApplyingWiFiSettings_oldJson.json")
            setup_apply_previous_settings_content_animation_view.playAnimation()
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
        countDownTimer.cancel()
    }

    private fun getDataFromDB()
    {
        doAsync{
            siteInfo = db.getSiteInfoDao().queryByMac(mac)
            clientList = db.getClientListDao().queryByMac(mac)

            uiThread{
                countDownTimer.start()
                setLocalDeviceNames()
            }
        }
    }

    private fun setLocalDeviceNames()
    {
        val params = JSONObject()
        val paramsArray = JSONArray()

        for(i in 0 until clientList.size)
        {
            val paramsSub = JSONObject()
            paramsSub.put("HostName", clientList[i].deviceName)
            paramsSub.put("MacAddress", clientList[i].deviceMac)
            paramsSub.put("Internet_Blocking_Enable", false)
            paramsArray.put(paramsSub)
        }

        params.put("multiObject", paramsArray)
        LogUtil.d(TAG,"setLocalDeviceNames param:$params")

        DevicesApi.SetChangeIconNameInfo()
                .setRequestPageName(TAG)
                .setParams(params)
                .setIsUsingInCloudFlow(true)
                .setResponseListener(object: Commander.ResponseListener()
                {
                    override fun onSuccess(responseStr: String)
                    {
                        try
                        {
                            val data = JSONObject(responseStr)
                            val sessionkey = data.get("sessionkey").toString()
                            GlobalData.sessionKey = sessionkey
                            setWiFi24GSSIDTask()
                        }
                        catch(e: JSONException)
                        {
                            e.printStackTrace()
                            GlobalBus.publish(MainEvent.HideLoading())
                        }
                    }
                }).execute()
    }

    private fun setWiFi24GSSIDTask()
    {
        val params = JSONObject()
        params.put("SSID", siteInfo.wifiSSID)
        LogUtil.d(TAG,"setWiFi24GSSIDTask param:$params")

        WiFiSettingApi.SetWiFi24GInfo()
                .setRequestPageName(TAG)
                .setParams(params)
                .setIsUsingInCloudFlow(true)
                .setResponseListener(object: Commander.ResponseListener()
                {
                    override fun onSuccess(responseStr: String)
                    {
                        try
                        {
                            val data = JSONObject(responseStr)
                            val sessionkey = data.get("sessionkey").toString()
                            GlobalData.sessionKey = sessionkey
                        }
                        catch(e: JSONException)
                        {
                            e.printStackTrace()
                        }

                        setWiFi24GPwdTask()
                    }
                }).execute()
    }

    private fun setWiFi24GPwdTask()
    {
        val params = JSONObject()
        params.put("ModeEnabled", "WPA2-Personal")
        params.put("KeyPassphrase", siteInfo.wifiPWD)
        params.put("X_ZYXEL_AutoGenPSK", false)
        LogUtil.d(TAG,"setWiFi24GPwdTask param:$params")

        WiFiSettingApi.SetWiFi24GPwd()
                .setRequestPageName(TAG)
                .setParams(params)
                .setIsUsingInCloudFlow(true)
                .setResponseListener(object: Commander.ResponseListener()
                {
                    override fun onSuccess(responseStr: String)
                    {
                        try
                        {
                            val data = JSONObject(responseStr)
                            val sessionkey = data.get("sessionkey").toString()
                            GlobalData.sessionKey = sessionkey
                        }
                        catch(e: JSONException)
                        {
                            e.printStackTrace()
                        }

                        setWiFi5GSSIDTask()
                    }
                }).execute()
    }

    private fun setWiFi5GSSIDTask()
    {
        val params = JSONObject()
        params.put("SSID", siteInfo.wifiSSID)
        LogUtil.d(TAG,"setWiFi5GSSIDTask param:$params")

        WiFiSettingApi.SetWiFi5GInfo()
                .setRequestPageName(TAG)
                .setParams(params)
                .setIsUsingInCloudFlow(true)
                .setResponseListener(object: Commander.ResponseListener()
                {
                    override fun onSuccess(responseStr: String)
                    {
                        try
                        {
                            val data = JSONObject(responseStr)
                            val sessionkey = data.get("sessionkey").toString()
                            GlobalData.sessionKey = sessionkey
                        }
                        catch(e: JSONException)
                        {
                            e.printStackTrace()
                        }

                        setWiFi5GPwdTask()
                    }
                }).execute()
    }

    private fun setWiFi5GPwdTask()
    {
        val params = JSONObject()
        params.put("ModeEnabled", "WPA2-Personal")
        params.put("KeyPassphrase", siteInfo.wifiPWD)
        params.put("X_ZYXEL_AutoGenPSK", false)
        LogUtil.d(TAG,"setWiFi5GPwdTask param:$params")

        WiFiSettingApi.SetWiFi5GPwd()
                .setRequestPageName(TAG)
                .setParams(params)
                .setIsUsingInCloudFlow(true)
                .setResponseListener(object: Commander.ResponseListener()
                {
                    override fun onSuccess(responseStr: String)
                    {
                        try
                        {
                            val data = JSONObject(responseStr)
                            val sessionkey = data.get("sessionkey").toString()
                            GlobalData.sessionKey = sessionkey
                        }
                        catch(e: JSONException)
                        {
                            e.printStackTrace()
                        }

                        setLogoutTask()
                    }
                }).execute()
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

                    }
                }).execute()
    }

    private fun finishAction()
    {
        val bundle = Bundle().apply{
            putString("MAC", mac)
            putBoolean("needLoginWhenFinal", true)
        }
        GlobalBus.publish(MainEvent.SwitchToFrag(SetupReconnectRouterPreviousSettingsFragment().apply{ arguments = bundle }))
    }
}