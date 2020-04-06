package zyxel.com.multyproneo.fragment

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.gson.Gson
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.fragment_home.*
import org.jetbrains.anko.support.v4.runOnUiThread
import org.json.JSONException
import org.json.JSONObject
import zyxel.com.multyproneo.R
import zyxel.com.multyproneo.adapter.ZYXELEndDeviceItemAdapter
import zyxel.com.multyproneo.api.AccountApi
import zyxel.com.multyproneo.api.Commander
import zyxel.com.multyproneo.api.GatewayApi
import zyxel.com.multyproneo.api.WiFiSettingApi
import zyxel.com.multyproneo.dialog.InternetStatusDialog
import zyxel.com.multyproneo.event.GlobalBus
import zyxel.com.multyproneo.event.HomeEvent
import zyxel.com.multyproneo.event.MainEvent
import zyxel.com.multyproneo.model.cloud.CloudAgentInfo
import zyxel.com.multyproneo.util.AppConfig
import zyxel.com.multyproneo.util.GlobalData
import zyxel.com.multyproneo.util.LogUtil

/**
 * Created by LouisTien on 2019/6/4.
 */
class HomeFragment : Fragment()
{
    private val TAG = javaClass.simpleName
    private lateinit var getInfoCompleteDisposable: Disposable
    private lateinit var internetStatusHelper: InternetStatusDialog
    private lateinit var adapter: ZYXELEndDeviceItemAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
    {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)
        getInfoCompleteDisposable = GlobalBus.listen(HomeEvent.GetDeviceInfoComplete::class.java).subscribe{ updateUI() }
        home_device_list_swipe.setOnRefreshListener{
            GlobalBus.publish(MainEvent.ShowLoadingOnlyGrayBG())
            GlobalBus.publish(MainEvent.StopGetDeviceInfoTask())
            GlobalBus.publish(MainEvent.StartGetDeviceInfoTask())
        }
        setClickListener()
    }

    override fun onResume()
    {
        super.onResume()
        GlobalBus.publish(MainEvent.SetHomeIconFocus())
        GlobalBus.publish(MainEvent.ShowBottomToolbar())
        if(GlobalData.ZYXELEndDeviceList.size > 0) updateUI()
        GlobalBus.publish(MainEvent.StartGetDeviceInfoTask())
    }

    override fun onPause()
    {
        super.onPause()
        GlobalBus.publish(MainEvent.StopGetDeviceInfoTask())
    }

    override fun onDestroyView()
    {
        super.onDestroyView()
        if(!getInfoCompleteDisposable.isDisposed) getInfoCompleteDisposable.dispose()
    }

    private val clickListener = View.OnClickListener{ view ->
        when(view)
        {
            home_internet_status_help_image ->
            {
                internetStatusHelper = InternetStatusDialog(activity!!)
                internetStatusHelper.show()
                //startRDTServer()
                //getIOTCLoginStatus()
            }

            home_guest_wifi_switch ->
            {
                setGuestWiFi24GEnableTask()

                val bundle = Bundle().apply{
                    putString("Title", "")
                    putString("Description", getString(R.string.loading_transition_please_wait))
                    putString("Sec_Description", getString(R.string.loading_transition_update_wifi_settings))
                    putInt("LoadingSecond", AppConfig.WiFiSettingTime)
                    putSerializable("Anim", AppConfig.LoadingAnimation.ANIM_REBOOT)
                    putSerializable("DesPage", AppConfig.LoadingGoToPage.FRAG_SEARCH)
                    putBoolean("ShowCountDownTimer", false)
                }
                GlobalBus.publish(MainEvent.SwitchToFrag(LoadingTransitionFragment().apply{ arguments = bundle }))
            }

            home_connect_device_frame -> {GlobalBus.publish(MainEvent.EnterDevicesPage())}
            home_guest_wifi_frame -> {GlobalBus.publish(MainEvent.EnterWiFiSettingsPage())}
            home_add_mesh_image, home_add_mesh_text -> {GlobalBus.publish(MainEvent.SwitchToFrag(AddMeshFragment()))}
        }
    }

    private fun setClickListener()
    {
        home_internet_status_help_image.setOnClickListener(clickListener)
        home_guest_wifi_switch.setOnClickListener(clickListener)
        home_connect_device_frame.setOnClickListener(clickListener)
        home_guest_wifi_frame.setOnClickListener(clickListener)
        home_add_mesh_image.setOnClickListener(clickListener)
        home_add_mesh_text.setOnClickListener(clickListener)
    }

    private fun updateUI()
    {
        if(GlobalData.currentFrag != TAG) return

        if(!isVisible) return

        LogUtil.d(TAG, "updateUI()")

        runOnUiThread{
            GlobalBus.publish(MainEvent.HideLoading())
            home_device_list_swipe.setRefreshing(false)

            home_connect_device_count_text.text = GlobalData.getActivatedDeviceCount().toString()
            adapter = ZYXELEndDeviceItemAdapter(
                    GlobalData.ZYXELEndDeviceList,
                    GlobalData.getCurrentGatewayInfo(),
                    GlobalData.gatewayWanInfo)
            home_device_list.adapter = adapter

            home_internet_status_content_text.text = getString(if(GlobalData.gatewayWanInfo.Object.Status == "Enable") R.string.home_online else R.string.home_offline)

            when(GlobalData.guestWiFiStatus)
            {
                true ->
                {
                    home_guest_wifi_status_text.text = getString(R.string.home_guest_wifi_status_on)
                    home_guest_wifi_switch.setImageResource(R.drawable.switch_on)
                }

                else ->
                {
                    home_guest_wifi_status_text.text = getString(R.string.home_guest_wifi_status_off)
                    home_guest_wifi_switch.setImageResource(R.drawable.switch_off)
                }
            }
        }
    }

    private fun setGuestWiFi24GEnableTask()
    {
        val params = JSONObject()
        params.put("Enable", !GlobalData.guestWiFiStatus)
        LogUtil.d(TAG,"setGuestWiFi24GEnableTask param:$params")

        WiFiSettingApi.SetGuestWiFi24GInfo()
                .setRequestPageName(TAG)
                .setParams(params)
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

                        setGuestWiFi5GEnableTask()
                    }
                }).execute()
    }

    private fun setGuestWiFi5GEnableTask()
    {
        val params = JSONObject()
        params.put("Enable", !GlobalData.guestWiFiStatus)
        LogUtil.d(TAG,"setGuestWiFi5GEnableTask param:$params")

        WiFiSettingApi.SetGuestWiFi5GInfo()
                .setRequestPageName(TAG)
                .setParams(params)
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
                .setResponseListener(object: Commander.ResponseListener()
                {
                    override fun onSuccess(responseStr: String)
                    {

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
                        LogUtil.d(TAG,"responseStr:$responseStr")
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
                            val cloudAgentInfo = Gson().fromJson(responseStr, CloudAgentInfo::class.javaObjectType)
                            LogUtil.d(TAG,"getIOTCLoginStatus:$cloudAgentInfo")
                        }
                        catch(e: JSONException)
                        {
                            e.printStackTrace()

                            GlobalBus.publish(MainEvent.HideLoading())
                        }
                    }
                }).execute()
    }
}