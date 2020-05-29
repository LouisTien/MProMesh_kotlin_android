package zyxel.com.multyproneo.fragment.cloud

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.fragment_cloud_home.*
import org.jetbrains.anko.support.v4.runOnUiThread
import zyxel.com.multyproneo.R
import zyxel.com.multyproneo.adapter.cloud.CloudZYXELEndDeviceItemAdapter
import zyxel.com.multyproneo.api.cloud.*
import zyxel.com.multyproneo.dialog.GatewayStatusDialog
import zyxel.com.multyproneo.dialog.MeshDeviceStatusDialog
import zyxel.com.multyproneo.dialog.OtherMeshNetworksDialog
import zyxel.com.multyproneo.event.GlobalBus
import zyxel.com.multyproneo.event.HomeEvent
import zyxel.com.multyproneo.event.MainEvent
import zyxel.com.multyproneo.model.*
import zyxel.com.multyproneo.model.cloud.TUTKAllDeviceInfo
import zyxel.com.multyproneo.util.AppConfig
import zyxel.com.multyproneo.util.GlobalData
import zyxel.com.multyproneo.util.LogUtil

class CloudHomeFragment : Fragment()
{
    private val TAG = javaClass.simpleName
    private lateinit var adapter: CloudZYXELEndDeviceItemAdapter
    private lateinit var meshDevicePlacementStatusDisposable: Disposable
    private lateinit var getCloudInfoCompleteDisposable: Disposable

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
    {
        return inflater.inflate(R.layout.fragment_cloud_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)

        GlobalData.notiMac = ""
        GlobalData.notiUid = ""

        cloud_home_mesh_device_list_swipe.setOnRefreshListener{
            GlobalBus.publish(MainEvent.StartGetCloudDeviceInfoTask(AppConfig.LoadingStyle.STY_ONLY_BG))
        }

        meshDevicePlacementStatusDisposable = GlobalBus.listen(HomeEvent.MeshDevicePlacementStatus::class.java).subscribe{
            MeshDeviceStatusDialog(activity!!, it.isHomePage).show()
        }

        getCloudInfoCompleteDisposable = GlobalBus.listen(HomeEvent.GetCloudDeviceInfoComplete::class.java).subscribe{ updateUI() }

        setClickListener()

        Glide.with(activity!!).load(R.drawable.img_locationdefault).apply(RequestOptions.circleCropTransform()).into(cloud_home_site_pic_image)

        GlobalBus.publish(MainEvent.StartGetCloudDeviceInfoTask(AppConfig.LoadingStyle.STY_NORMAL))
    }

    override fun onResume()
    {
        super.onResume()
        GlobalBus.publish(MainEvent.SetCloudHomeIconFocus())
        GlobalBus.publish(MainEvent.ShowCloudBottomToolbar())
    }

    override fun onPause()
    {
        super.onPause()
    }

    override fun onDestroyView()
    {
        super.onDestroyView()
        if(!meshDevicePlacementStatusDisposable.isDisposed) meshDevicePlacementStatusDisposable.dispose()
        if(!getCloudInfoCompleteDisposable.isDisposed) getCloudInfoCompleteDisposable.dispose()
    }

    private val clickListener = View.OnClickListener{ view ->
        when(view)
        {
            cloud_home_wifi_router_image -> GatewayStatusDialog(activity!!).show()

            cloud_home_wifi_router_area_relative ->
            {
                val bundle = Bundle().apply{
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
                GlobalBus.publish(MainEvent.SwitchToFrag(CloudZYXELEndDeviceDetailFragment().apply{ arguments = bundle }))
            }

            cloud_home_guest_wifi_switch ->
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
                GlobalBus.publish(MainEvent.SwitchToFrag(CloudLoadingTransitionFragment().apply{ arguments = bundle }))
            }

            cloud_home_mesh_devices_add_image -> GlobalBus.publish(MainEvent.SwitchToFrag(CloudAddMeshFragment()))

            cloud_home_site_pic_image ->
            {
                val otherMeshListInfo = TUTKAllDeviceInfo()
                for(item in GlobalData.cloudGatewayListInfo.data)
                {
                    if(item.udid != GlobalData.currentUID)
                        otherMeshListInfo.data.add(item)
                }
                OtherMeshNetworksDialog(activity!!, GlobalData.getCurrentGatewayInfo().UserDefineName, otherMeshListInfo).show()
            }

            cloud_home_connect_device_frame -> GlobalBus.publish(MainEvent.EnterCloudDevicesPage())

            cloud_home_guest_wifi_frame -> GlobalBus.publish(MainEvent.EnterCloudWiFiSettingsPage())

            cloud_home_site_refresh_image -> GlobalBus.publish(MainEvent.StartGetCloudDeviceInfoTask(AppConfig.LoadingStyle.STY_NORMAL))
        }
    }

    private fun setClickListener()
    {
        cloud_home_guest_wifi_switch.setOnClickListener(clickListener)
        cloud_home_connect_device_frame.setOnClickListener(clickListener)
        cloud_home_guest_wifi_frame.setOnClickListener(clickListener)
        cloud_home_mesh_devices_add_image.setOnClickListener(clickListener)
        cloud_home_site_pic_image.setOnClickListener(clickListener)
        cloud_home_wifi_router_image.setOnClickListener(clickListener)
        cloud_home_wifi_router_area_relative.setOnClickListener(clickListener)
        cloud_home_site_refresh_image.setOnClickListener(clickListener)
    }

    private fun updateUI()
    {
        GlobalBus.publish(MainEvent.HideLoading())

        if(GlobalData.currentFrag != TAG) return

        if(!isVisible) return

        LogUtil.d(TAG, "updateUI()")

        runOnUiThread{

            cloud_home_title_text.visibility = View.INVISIBLE
            cloud_home_title_image.visibility = View.INVISIBLE
            with(GlobalData.customerLogo)
            {
                when
                {
                    equals("HTC", ignoreCase = true) ->
                    {
                        cloud_home_title_image.setImageResource(R.drawable.htc_logo)
                        cloud_home_title_image.visibility = View.VISIBLE
                    }

                    equals("METRONET", ignoreCase = true) ->
                    {
                        cloud_home_title_image.setImageResource(R.drawable.metronet_logo)
                        cloud_home_title_image.visibility = View.VISIBLE
                    }

                    equals("CCI", ignoreCase = true) ->
                    {
                        cloud_home_title_image.setImageResource(R.drawable.cci_logo)
                        cloud_home_title_image.visibility = View.VISIBLE
                    }

                    equals("CBT", ignoreCase = true) ->
                    {
                        cloud_home_title_image.setImageResource(R.drawable.cbt_logo)
                        cloud_home_title_image.visibility = View.VISIBLE
                    }

                    equals("EPB", ignoreCase = true) ->
                    {
                        cloud_home_title_image.setImageResource(R.drawable.cpb_logo)
                        cloud_home_title_image.visibility = View.VISIBLE
                    }

                    equals("GOLDENWEST", ignoreCase = true) ->
                    {
                        cloud_home_title_image.setImageResource(R.drawable.golden_west_logo)
                        cloud_home_title_image.visibility = View.VISIBLE
                    }

                    else ->
                    {
                        cloud_home_title_text.visibility = View.VISIBLE
                        cloud_home_title_image.visibility = View.INVISIBLE
                    }
                }
            }

            cloud_home_mesh_device_list_swipe.setRefreshing(false)
            cloud_home_wifi_router_name_text.text = GlobalData.getCurrentGatewayInfo().UserDefineName
            cloud_home_connect_device_count_text.text = GlobalData.getActivatedDeviceCount().toString()

            adapter = CloudZYXELEndDeviceItemAdapter(
                    GlobalData.ZYXELEndDeviceList,
                    GlobalData.getCurrentGatewayInfo(),
                    GlobalData.gatewayWanInfo)
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
                    cloud_home_guest_wifi_switch.setImageResource(R.drawable.switch_off)
                }
            }
        }
    }

    private fun setGuestWiFi24GEnableTask()
    {
        val params = ",\"Enable\":${!GlobalData.guestWiFiStatus}"

        P2PWiFiSettingApi.SetGuestWiFi24GInfo()
                .setRequestPageName(TAG)
                .setRequestPayload(params)
                .setResponseListener(object: TUTKP2PResponseCallback()
                {
                    override fun onSuccess(responseStr: String)
                    {
                        setGuestWiFi5GEnableTask()
                    }
                }).execute()
    }

    private fun setGuestWiFi5GEnableTask()
    {
        val params = ",\"Enable\":${!GlobalData.guestWiFiStatus}"

        P2PWiFiSettingApi.SetGuestWiFi5GInfo()
                .setRequestPageName(TAG)
                .setRequestPayload(params)
                .setResponseListener(object: TUTKP2PResponseCallback()
                {
                    override fun onSuccess(responseStr: String)
                    {

                    }
                }).execute()
    }
}