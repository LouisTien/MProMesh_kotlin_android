package zyxel.com.multyproneo.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.fragment_cloud_home.*
import org.jetbrains.anko.support.v4.runOnUiThread
import org.json.JSONException
import org.json.JSONObject
import zyxel.com.multyproneo.R
import zyxel.com.multyproneo.adapter.cloud.CloudZYXELEndDeviceItemAdapter
import zyxel.com.multyproneo.api.AccountApi
import zyxel.com.multyproneo.api.ApiHandler
import zyxel.com.multyproneo.api.Commander
import zyxel.com.multyproneo.api.WiFiSettingApi
import zyxel.com.multyproneo.dialog.GatewayStatusDialog
import zyxel.com.multyproneo.dialog.MeshDeviceStatusDialog
import zyxel.com.multyproneo.event.ApiEvent
import zyxel.com.multyproneo.event.GlobalBus
import zyxel.com.multyproneo.event.HomeEvent
import zyxel.com.multyproneo.event.MainEvent
import zyxel.com.multyproneo.model.DevicesInfoObject
import zyxel.com.multyproneo.util.AppConfig
import zyxel.com.multyproneo.util.FeatureConfig
import zyxel.com.multyproneo.util.GlobalData
import zyxel.com.multyproneo.util.LogUtil
import java.util.*
import kotlin.concurrent.schedule

/**
 * Created by LouisTien on 2019/6/4.
 */
class HomeFragment : Fragment()
{
    private val TAG = "HomeFragment"
    private lateinit var meshDevicePlacementStatusDisposable: Disposable
    private lateinit var getInfoCompleteDisposable: Disposable
    private lateinit var stopRegularTaskDisposable: Disposable
    private lateinit var adapter: CloudZYXELEndDeviceItemAdapter
    private var apiTimer = Timer()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
    {
        return inflater.inflate(R.layout.fragment_cloud_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)

        GlobalData.currentFrag = TAG

        meshDevicePlacementStatusDisposable = GlobalBus.listen(HomeEvent.MeshDevicePlacementStatus::class.java).subscribe {
            MeshDeviceStatusDialog(activity!!, it.isHomePage, false).show()
        }

        getInfoCompleteDisposable = GlobalBus.listen(ApiEvent.ApiExecuteComplete::class.java).subscribe {
            when(it.event)
            {
                ApiHandler.API_RES_EVENT.API_RES_EVENT_FEATURE_LIST -> executeOnceApi()
                ApiHandler.API_RES_EVENT.API_RES_EVENT_HOME_API_ONCE -> executeRegularApi()
                ApiHandler.API_RES_EVENT.API_RES_EVENT_HOME_API_REGULAR -> updateUI()
                else -> { }
            }
        }

        stopRegularTaskDisposable = GlobalBus.listen(ApiEvent.StopRegularTask::class.java).subscribe{
            GlobalBus.publish(MainEvent.HideLoading())
            stopGetDeviceInfo()
        }

        cloud_home_mesh_device_list_swipe.setOnRefreshListener {
            GlobalBus.publish(MainEvent.ShowLoadingOnlyGrayBG())
            stopGetDeviceInfo()
            executeRegularApi()
        }

        setClickListener()

        cloud_home_title_item_area_relative.visibility = View.GONE
        home_title_item_area_relative.visibility = View.VISIBLE

    }

    override fun onResume()
    {
        super.onResume()
        GlobalBus.publish(MainEvent.SetHomeIconFocus())
        GlobalBus.publish(MainEvent.ShowBottomToolbar())
        startGetDeviceInfo()
    }

    override fun onPause()
    {
        super.onPause()
        stopGetDeviceInfo()
    }

    override fun onDestroyView()
    {
        super.onDestroyView()
        if(!meshDevicePlacementStatusDisposable.isDisposed) meshDevicePlacementStatusDisposable.dispose()
        if(!getInfoCompleteDisposable.isDisposed) getInfoCompleteDisposable.dispose()
        if(!stopRegularTaskDisposable.isDisposed) stopRegularTaskDisposable.dispose()
    }

    private val clickListener = View.OnClickListener { view ->
        when(view)
        {
            cloud_home_wifi_router_image -> GatewayStatusDialog(activity!!).show()

            cloud_home_wifi_router_area_relative ->
            {
                val bundle = Bundle().apply {
                    putBoolean("GatewayMode", true)
                    putSerializable("GatewayInfo", GlobalData.getCurrentGatewayInfo())
                    putSerializable("WanInfo", GlobalData.gatewayWanInfo)
                    putSerializable("DevicesInfo", DevicesInfoObject
                    (
                            Active = true,
                            HostName = GlobalData.getCurrentGatewayInfo().getName(),
                            IPAddress = GlobalData.getCurrentGatewayInfo().IP,
                            X_ZYXEL_CapabilityType = "L2Device",
                            X_ZYXEL_ConnectionType = "WiFi",
                            X_ZYXEL_HostType = GlobalData.getCurrentGatewayInfo().DeviceMode,
                            X_ZYXEL_SoftwareVersion = GlobalData.getCurrentGatewayInfo().SoftwareVersion
                    ))
                }
                GlobalBus.publish(MainEvent.SwitchToFrag(ZYXELEndDeviceDetailFragment().apply { arguments = bundle }))
            }

            cloud_home_guest_wifi_switch ->
            {
                setGuestWiFi24GEnableTask()

                val bundle = Bundle().apply {
                    putString("Title", getString(R.string.loading_transition_update_wifi_settings))
                    putInt("LoadingSecond", AppConfig.WiFiSettingTime)
                    putSerializable("DesPage", AppConfig.LoadingGoToPage.FRAG_SEARCH)
                    putBoolean("IsCloud", false)
                }
                GlobalBus.publish(MainEvent.SwitchToFrag(LoadingTransitionProgressFragment().apply { arguments = bundle }))
            }

            home_site_refresh_image ->
            {
                GlobalBus.publish(MainEvent.ShowLoading())
                stopGetDeviceInfo()
                executeRegularApi()
            }

            home_site_topology_image -> {
                val bundle = Bundle().apply {
                    putBoolean("isGateway", true)
                }

                GlobalBus.publish(MainEvent.SwitchToFrag(MeshTopologyFragment().apply {
                    arguments = bundle
                }))
            }

            cloud_home_connect_device_frame -> GlobalBus.publish(MainEvent.EnterDevicesPage())

            cloud_home_guest_wifi_frame -> GlobalBus.publish(MainEvent.EnterWiFiSettingsPage())

            cloud_home_mesh_devices_add_image ->
            {
                when(FeatureConfig.FeatureInfo.APPUICustomList.Add_Mesh_WiFi)
                {
                    false -> GlobalBus.publish(MainEvent.SwitchToFrag(AddMeshCableInfoFragment()))
                    else -> GlobalBus.publish(MainEvent.SwitchToFrag(AddMeshFragment()))
                }
            }

        }
    }

    private fun setClickListener()
    {
        cloud_home_guest_wifi_switch.setOnClickListener(clickListener)
        cloud_home_connect_device_frame.setOnClickListener(clickListener)
        cloud_home_guest_wifi_frame.setOnClickListener(clickListener)
        cloud_home_mesh_devices_add_image.setOnClickListener(clickListener)
        cloud_home_wifi_router_image.setOnClickListener(clickListener)
        cloud_home_wifi_router_area_relative.setOnClickListener(clickListener)
        home_site_refresh_image.setOnClickListener(clickListener)
        home_site_topology_image.setOnClickListener(clickListener)
    }

    private fun updateUI()
    {
        if(GlobalData.currentFrag != TAG) return

        if(!isVisible) return

        LogUtil.d(TAG, "updateUI()")

        runOnUiThread {

            GlobalBus.publish(MainEvent.HideLoading())

            cloud_home_mesh_device_list_swipe.setRefreshing(false)

            cloud_home_wifi_router_name_text.text = GlobalData.getCurrentGatewayInfo().UserDefineName

            home_mesh_status_content_text.text = getString(R.string.home_mesh_down)
            for(item in GlobalData.ZYXELEndDeviceList)
            {
                if(item.Active)
                    home_mesh_status_content_text.text = getString(R.string.home_mesh_up)
            }

            cloud_home_connect_device_count_text.text = GlobalData.getActivatedDeviceCount().toString()

            adapter = CloudZYXELEndDeviceItemAdapter(
                    GlobalData.ZYXELEndDeviceList,
                    GlobalData.getCurrentGatewayInfo(),
                    GlobalData.gatewayWanInfo,
                    false)
            cloud_home_mesh_device_list.adapter = adapter

            cloud_home_wifi_router_image.visibility = View.VISIBLE
            if(GlobalData.gatewayWanInfo.Object.Status == "Enable")
                cloud_home_wifi_router_image.setImageResource(R.drawable.icon_device_has)
            else
                cloud_home_wifi_router_image.setImageResource(R.drawable.icon_device_no)

            when(GlobalData.guestWiFiStatus)
            {
                true ->
                {
                    cloud_home_guest_wifi_status_text.text = getString(R.string.home_guest_wifi_status_on)
                    cloud_home_guest_wifi_switch.setImageResource(R.drawable.switch_on)
                }

                else ->
                {
                    cloud_home_guest_wifi_status_text.text = getString(R.string.home_guest_wifi_status_off)
                    cloud_home_guest_wifi_switch.setImageResource(R.drawable.switch_off_2)
                }
            }
            cloud_home_guest_wifi_status_text.isSelected = true

            home_mesh_status_area_relative.visibility =
                    if(FeatureConfig.FeatureInfo.APPUICustomList.Home_MESH_status)
                        View.VISIBLE
                    else
                        View.GONE

            cloud_home_guest_wifi_frame.visibility =
                    if(FeatureConfig.FeatureInfo.APPUICustomList.Guest_WiFi)
                        View.VISIBLE
                    else
                        View.INVISIBLE
        }
    }

    private fun setGuestWiFi24GEnableTask()
    {
        val params = JSONObject()
        params.put("Enable", !GlobalData.guestWiFiStatus)
        LogUtil.d(TAG, "setGuestWiFi24GEnableTask param:$params")

        WiFiSettingApi.SetGuestWiFi24GInfo()
                .setRequestPageName(TAG)
                .setParams(params)
                .setResponseListener(object : Commander.ResponseListener()
                {
                    override fun onSuccess(responseStr: String)
                    {
                        try
                        {
                            val data = JSONObject(responseStr)
                            val sessionkey = data.get("sessionkey").toString()
                            GlobalData.loginInfo.sessionkey = sessionkey
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
        LogUtil.d(TAG, "setGuestWiFi5GEnableTask param:$params")

        WiFiSettingApi.SetGuestWiFi5GInfo()
                .setRequestPageName(TAG)
                .setParams(params)
                .setResponseListener(object : Commander.ResponseListener()
                {
                    override fun onSuccess(responseStr: String)
                    {
                        try
                        {
                            val data = JSONObject(responseStr)
                            val sessionkey = data.get("sessionkey").toString()
                            GlobalData.loginInfo.sessionkey = sessionkey
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
                .setResponseListener(object : Commander.ResponseListener()
                {
                    override fun onSuccess(responseStr: String)
                    {

                    }
                }).execute()
    }

    private fun startGetDeviceInfo()
    {
        if(GlobalData.alreadyGetGatewayInfoLocalBase)
            updateUI()
        else
            GlobalBus.publish(MainEvent.ShowLoading())

        if(GlobalData.isSupportAPPUICustomization())
        {
            ApiHandler().execute(
                    ApiHandler.API_RES_EVENT.API_RES_EVENT_FEATURE_LIST,
                    arrayListOf(ApiHandler.API_REF.API_GET_APP_UI_CUSTOM_INFO)
            )
        }
        else
            executeOnceApi()
    }

    private fun stopGetDeviceInfo()
    {
        apiTimer.cancel()
    }

    private fun executeOnceApi()
    {
        val apiList = arrayListOf<ApiHandler.API_REF>()

        if(FeatureConfig.FeatureInfo.APPUICustomList.Parental_Control)
            GlobalBus.publish(MainEvent.ShowParentalControlIcon())

        if(FeatureConfig.FeatureInfo.APPUICustomList.Guest_WiFi)
            apiList.add(ApiHandler.API_REF.API_GET_GUEST_WIFI_ENABLE)

        if(!FeatureConfig.FeatureInfo.APPUICustomList.F_Secure_New
            && FeatureConfig.FeatureInfo.APPUICustomList.F_Secure)
            apiList.add(ApiHandler.API_REF.API_GET_FSECURE_INFO)

        if(FeatureConfig.FeatureInfo.APPUICustomList.Host_Name_Replace)
            apiList.add(ApiHandler.API_REF.API_GET_HOSTNAME_REPLACE_INFO)

        if(FeatureConfig.FeatureInfo.APPUICustomList.Internet_Blocking)
            apiList.add(ApiHandler.API_REF.API_GET_INTERNET_BLOCK_INFO)

        if(FeatureConfig.FeatureInfo.APPUICustomList.GW_LAN_IP_PORT)
            apiList.add(ApiHandler.API_REF.API_GET_REMOTE_MANAGEMENT)

        ApiHandler().execute(
                ApiHandler.API_RES_EVENT.API_RES_EVENT_HOME_API_ONCE,
                apiList
        )
    }

    private fun executeRegularApi()
    {
        val apiList = arrayListOf(
                ApiHandler.API_REF.API_GET_SYSTEM_INFO,
                ApiHandler.API_REF.API_GET_CHANGE_ICON_NAME,
                ApiHandler.API_REF.API_GET_DEVICE_INFO,
                ApiHandler.API_REF.API_GET_WAN_INFO
        )

        if(FeatureConfig.FeatureInfo.APPUICustomList.Guest_WiFi)
            apiList.add(ApiHandler.API_REF.API_GET_GUEST_WIFI_ENABLE)

        apiTimer = Timer()
        apiTimer.schedule(0, (AppConfig.endDeviceListUpdateTime * 1000).toLong())
        {
            ApiHandler().execute(
                    ApiHandler.API_RES_EVENT.API_RES_EVENT_HOME_API_REGULAR,
                    apiList
            )
        }
    }
}