package zyxel.com.multyproneo.fragment.cloud

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.gson.Gson
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.fragment_cloud_home.*
import org.jetbrains.anko.support.v4.runOnUiThread
import org.json.JSONException
import org.json.JSONObject
import zyxel.com.multyproneo.R
import zyxel.com.multyproneo.adapter.cloud.CloudZYXELEndDeviceItemAdapter
import zyxel.com.multyproneo.api.cloud.*
import zyxel.com.multyproneo.dialog.GatewayStatusDialog
import zyxel.com.multyproneo.dialog.MeshDeviceStatusDialog
import zyxel.com.multyproneo.event.GlobalBus
import zyxel.com.multyproneo.event.HomeEvent
import zyxel.com.multyproneo.event.MainEvent
import zyxel.com.multyproneo.model.*
import zyxel.com.multyproneo.util.AppConfig
import zyxel.com.multyproneo.util.FeatureConfig
import zyxel.com.multyproneo.util.GlobalData
import zyxel.com.multyproneo.util.LogUtil

class CloudHomeFragment : Fragment()
{
    private val TAG = javaClass.simpleName
    private lateinit var changeIconNameInfo: ChangeIconNameInfo
    private lateinit var devicesInfo: DevicesInfo
    private lateinit var wanInfo: WanInfo
    private lateinit var guestWiFiInfo: GuestWiFiInfo
    private lateinit var fSecureInfo: FSecureInfo
    private lateinit var hostNameReplaceInfo: HostNameReplaceInfo
    private lateinit var gateDetailInfo: GatewayDetailInfo
    private lateinit var ipInterfaceInfo: IPInterfaceInfo
    private lateinit var adapter: CloudZYXELEndDeviceItemAdapter
    private lateinit var meshDevicePlacementStatusDisposable: Disposable

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
    {
        return inflater.inflate(R.layout.fragment_cloud_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)

        cloud_home_mesh_device_list_swipe.setOnRefreshListener{
            GlobalBus.publish(MainEvent.ShowLoadingOnlyGrayBG())
            startGetAllNeedDeviceInfoTask()
        }

        meshDevicePlacementStatusDisposable = GlobalBus.listen(HomeEvent.MeshDevicePlacementStatus::class.java).subscribe{
            MeshDeviceStatusDialog(activity!!).show()
        }

        setClickListener()

        Glide.with(activity!!).load(R.drawable.img_locationdefault).apply(RequestOptions.circleCropTransform()).into(cloud_home_site_pic_image)

        startGetAllNeedDeviceInfoTask()
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
    }

    private fun updateUI()
    {
        if(GlobalData.currentFrag != TAG) return

        if(!isVisible) return

        LogUtil.d(TAG, "updateUI()")

        runOnUiThread{
            cloud_home_mesh_device_list_swipe.setRefreshing(false)
            cloud_home_wifi_router_name_text.text = GlobalData.getCurrentGatewayInfo().UserDefineName
            cloud_home_connect_device_count_text.text = GlobalData.getActivatedDeviceCount().toString()

            adapter = CloudZYXELEndDeviceItemAdapter(
                    GlobalData.ZYXELEndDeviceList,
                    GlobalData.getCurrentGatewayInfo(),
                    GlobalData.gatewayWanInfo)
            cloud_home_mesh_device_list.adapter = adapter

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

            GlobalBus.publish(MainEvent.HideLoading())
        }
    }

    private fun startGetAllNeedDeviceInfoTask()
    {
        GlobalBus.publish(MainEvent.ShowLoading())

        var gatewayList = mutableListOf<GatewayInfo>()
        var findingDeviceInfo = GatewayInfo()
        gatewayList.add(findingDeviceInfo)

        GlobalData.currentGatewayIndex = 0
        GlobalData.gatewayList = gatewayList.toMutableList()

        getSystemInfoTask()
    }

    private fun getSystemInfoTask()
    {
        LogUtil.d(TAG,"getSystemInfoTask()")
        P2PGatewayApi.GetSystemInfo()
                .setRequestPageName(TAG)
                .setResponseListener(object: TUTKP2PResponseCallback()
                {
                    override fun onSuccess(responseStr: String)
                    {
                        try
                        {
                            val data = JSONObject(responseStr)
                            var name = data.getJSONObject("Object").getString("HostName")
                            LogUtil.d(TAG,"HostName:$name")
                            GlobalData.getCurrentGatewayInfo().UserDefineName = name
                            getFWVersionInfoTask()
                        }
                        catch(e: JSONException)
                        {
                            e.printStackTrace()

                            GlobalBus.publish(MainEvent.HideLoading())
                        }
                    }
                }).execute()
    }

    private fun getFWVersionInfoTask()
    {
        LogUtil.d(TAG,"getFWVersionInfoTask()")
        P2PGatewayApi.GetFWVersionInfo()
                .setRequestPageName(TAG)
                .setResponseListener(object: TUTKP2PResponseCallback()
                {
                    override fun onSuccess(responseStr: String)
                    {
                        try
                        {
                            gateDetailInfo = Gson().fromJson(responseStr, GatewayDetailInfo::class.javaObjectType)
                            LogUtil.d(TAG,"gateDetailInfo:$gateDetailInfo")
                            GlobalData.getCurrentGatewayInfo().SoftwareVersion = gateDetailInfo.Object.SoftwareVersion
                            getLanIPInfoTask()
                        }
                        catch(e: JSONException)
                        {
                            e.printStackTrace()

                            GlobalBus.publish(MainEvent.HideLoading())
                        }
                    }
                }).execute()
    }

    private fun getLanIPInfoTask()
    {
        LogUtil.d(TAG,"getLanIPInfoTask()")
        P2PGatewayApi.GetLanIPInfo()
                .setRequestPageName(TAG)
                .setResponseListener(object: TUTKP2PResponseCallback()
                {
                    override fun onSuccess(responseStr: String)
                    {
                        try
                        {
                            ipInterfaceInfo = Gson().fromJson(responseStr, IPInterfaceInfo::class.javaObjectType)
                            LogUtil.d(TAG,"ipInterfaceInfo:$ipInterfaceInfo")
                            for(item in ipInterfaceInfo.Object)
                            {
                                if( (item.X_ZYXEL_IfName == "br0") && (item.IPv4Address.isNotEmpty()) )
                                {
                                    GlobalData.getCurrentGatewayInfo().IP = item.IPv4Address[0].IPAddress
                                    break
                                }
                            }
                            getChangeIconNameInfoTask()
                        }
                        catch(e: JSONException)
                        {
                            e.printStackTrace()

                            GlobalBus.publish(MainEvent.HideLoading())
                        }
                    }
                }).execute()
    }

    private fun getChangeIconNameInfoTask()
    {
        LogUtil.d(TAG,"getChangeIconNameInfoTask()")
        P2PDevicesApi.GetChangeIconNameInfo()
                .setRequestPageName(TAG)
                .setResponseListener(object: TUTKP2PResponseCallback()
                {
                    override fun onSuccess(responseStr: String)
                    {
                        try
                        {
                            changeIconNameInfo = Gson().fromJson(responseStr, ChangeIconNameInfo::class.javaObjectType)
                            LogUtil.d(TAG,"changeIconNameInfo:$changeIconNameInfo")
                            GlobalData.changeIconNameList = changeIconNameInfo.Object.toMutableList()
                            getDeviceInfoTask()
                        }
                        catch(e: JSONException)
                        {
                            e.printStackTrace()

                            GlobalBus.publish(MainEvent.HideLoading())
                        }
                    }
                }).execute()
    }

    private fun getDeviceInfoTask()
    {
        LogUtil.d(TAG,"getDeviceInfoTask()")
        P2PDevicesApi.GetDevicesInfo()
                .setRequestPageName(TAG)
                .setResponseListener(object: TUTKP2PResponseCallback()
                {
                    override fun onSuccess(responseStr: String)
                    {
                        try
                        {
                            /*val responseStr2 =
                                    "{\n" +
                                            "       \"requested_path\": \"Device.Hosts.Host.\",\n" +
                                            "       \"oper_status\": \"Success\",\n" +
                                            "       \"Object\": [\n" +
                                            "         {\n" +
                                            "           \"PhysAddress\": \"\",\n" +
                                            "           \"IPAddress\": \"\",\n" +
                                            "           \"IPAddress6\": \"\",\n" +
                                            "           \"IPLinkLocalAddress6\": \"\",\n" +
                                            "           \"AddressSource\": \"\",\n" +
                                            "           \"DHCPClient\": \"\",\n" +
                                            "           \"LeaseTimeRemaining\": 0,\n" +
                                            "           \"AssociatedDevice\": \"\",\n" +
                                            "           \"Layer1Interface\": \"\",\n" +
                                            "           \"Layer3Interface\": \"\",\n" +
                                            "           \"VendorClassID\": \"\",\n" +
                                            "           \"ClientID\": \"\",\n" +
                                            "           \"UserClassID\": \"\",\n" +
                                            "           \"HostName\": \"\",\n" +
                                            "           \"Active\": \"\",\n" +
                                            "           \"X_ZYXEL_DeleteLease\": \"\",\n" +
                                            "           \"X_ZYXEL_ConnectionType\": \"\",\n" +
                                            "           \"X_ZYXEL_ConnectedAP\": \"\",\n" +
                                            "           \"X_ZYXEL_HostType\": \"\",\n" +
                                            "           \"X_ZYXEL_CapabilityType\": \"\",\n" +
                                            "           \"X_ZYXEL_PhyRate\": 0,\n" +
                                            "           \"X_ZYXEL_WiFiStatus\": \"\",\n" +
                                            "           \"X_ZYXEL_SignalStrength\": 0,\n" +
                                            "           \"X_ZYXEL_SNR\": 0,\n" +
                                            "           \"X_ZYXEL_RSSI\": 0,\n" +
                                            "           \"X_ZYXEL_SoftwareVersion\": \"\",\n" +
                                            "           \"X_ZYXEL_Address6Source\": \"\",\n" +
                                            "           \"X_ZYXEL_DHCP6Client\": \"\",\n" +
                                            "           \"X_ZYXEL_BytesSent\": 0,\n" +
                                            "           \"X_ZYXEL_BytesReceived\": 0,\n" +
                                            "           \"ClientDuid\": \"\",\n" +
                                            "           \"ExpireTime\": \"\",\n" +
                                            "           \"X_ZYXEL_Neighbor\": \"\",\n" +
                                            "           \"X_ZYXEL_Conn_Guest\": 0,\n" +
                                            "           \"X_ZYXEL_Band\": 0,\n" +
                                            "           \"X_ZYXEL_Channel_24G\": 0,\n" +
                                            "           \"X_ZYXEL_Channel_5G\": 0,\n" +
                                            "           \"X_ZYXEL_DHCPLeaseTime\": 0,\n" +
                                            "           \"X_ZYXEL_RSSI_STAT\": \"\"\n" +
                                            "         },\n" +
                                            "{\n" +
                                            "           \"PhysAddress\": \"b8:d5:26:4d:85:cb\",\n" +
                                            "           \"IPAddress\": \"192.168.1.245\",\n" +
                                            "           \"IPAddress6\": \"\",\n" +
                                            "           \"IPLinkLocalAddress6\": \"fe80::bad5:26ff:fe4d:85cb\",\n" +
                                            "           \"AddressSource\": \"DHCP\",\n" +
                                            "           \"DHCPClient\": \"\",\n" +
                                            "           \"LeaseTimeRemaining\": 67506,\n" +
                                            "           \"AssociatedDevice\": \"\",\n" +
                                            "           \"Layer1Interface\": \"Device.Ethernet.Interface.4\",\n" +
                                            "           \"Layer3Interface\": \"Device.IP.Interface.1\",\n" +
                                            "           \"VendorClassID\": \"*\",\n" +
                                            "           \"ClientID\": \"\",\n" +
                                            "           \"UserClassID\": \"\",\n" +
                                            "           \"HostName\": \"WX3310-B0_28850\",\n" +
                                            "           \"Active\": true,\n" +
                                            "           \"X_ZYXEL_DeleteLease\": false,\n" +
                                            "           \"X_ZYXEL_ConnectionType\": \"Ethernet\",\n" +
                                            "           \"X_ZYXEL_ConnectedAP\": \"\",\n" +
                                            "           \"X_ZYXEL_HostType\": \"AP\",\n" +
                                            "           \"X_ZYXEL_CapabilityType\": \"L2Device\",\n" +
                                            "           \"X_ZYXEL_PhyRate\": 1000,\n" +
                                            "           \"X_ZYXEL_WiFiStatus\": true,\n" +
                                            "           \"X_ZYXEL_SignalStrength\": 0,\n" +
                                            "           \"X_ZYXEL_SNR\": 0,\n" +
                                            "           \"X_ZYXEL_RSSI\": 0,\n" +
                                            "           \"X_ZYXEL_SoftwareVersion\": \"V1.00(ABSF.0)b3\",\n" +
                                            "           \"X_ZYXEL_Address6Source\": \"\",\n" +
                                            "           \"X_ZYXEL_DHCP6Client\": \"\",\n" +
                                            "           \"X_ZYXEL_BytesSent\": 0,\n" +
                                            "           \"X_ZYXEL_BytesReceived\": 0,\n" +
                                            "           \"ClientDuid\": \"\",\n" +
                                            "           \"ExpireTime\": \"\",\n" +
                                            "           \"X_ZYXEL_DHCPLeaseTime\": 1584073586,\n" +
                                            "           \"X_ZYXEL_RSSI_STAT\": \"Too Close\",\n" +
                                            "           \"X_ZYXEL_Neighbor\": \"\",\n" +
                                            "           \"X_ZYXEL_Conn_Guest\": 0,\n" +
                                            "           \"X_ZYXEL_Band\": 0,\n" +
                                            "           \"X_ZYXEL_Channel_24G\": 0,\n" +
                                            "           \"X_ZYXEL_Channel_5G\": 0\n" +
                                            "         },\n" +
                                            "         {\n" +
                                            "           \"PhysAddress\": \"40:4e:36:b3:fd:15\",\n" +
                                            "           \"IPAddress\": \"192.168.1.36\",\n" +
                                            "           \"IPAddress6\": \"\",\n" +
                                            "           \"IPLinkLocalAddress6\": \"fe80::424e:36ff:feb3:fd15\",\n" +
                                            "           \"AddressSource\": \"DHCP\",\n" +
                                            "           \"DHCPClient\": \"\",\n" +
                                            "           \"LeaseTimeRemaining\": 86035,\n" +
                                            "           \"AssociatedDevice\": \"WiFi.AccessPoint.5.AssociatedDevice.1\",\n" +
                                            "           \"Layer1Interface\": \"\",\n" +
                                            "           \"Layer3Interface\": \"Device.IP.Interface.1\",\n" +
                                            "           \"VendorClassID\": \"android-dhcp-9\",\n" +
                                            "           \"ClientID\": \"\",\n" +
                                            "           \"UserClassID\": \"\",\n" +
                                            "           \"HostName\": \"android-e5784d2ba14b34c5\",\n" +
                                            "           \"Active\": true,\n" +
                                            "           \"X_ZYXEL_DeleteLease\": false,\n" +
                                            "           \"X_ZYXEL_ConnectionType\": \"Wi-Fi\",\n" +
                                            "           \"X_ZYXEL_ConnectedAP\": \"02:16:77:01:00:02\",\n" +
                                            "           \"X_ZYXEL_HostType\": \"Desktop\",\n" +
                                            "           \"X_ZYXEL_CapabilityType\": \"Client\",\n" +
                                            "           \"X_ZYXEL_PhyRate\": 761,\n" +
                                            "           \"X_ZYXEL_WiFiStatus\": false,\n" +
                                            "           \"X_ZYXEL_SignalStrength\": 5,\n" +
                                            "           \"X_ZYXEL_SNR\": 50,\n" +
                                            "           \"X_ZYXEL_RSSI\": -34,\n" +
                                            "           \"X_ZYXEL_SoftwareVersion\": \"\",\n" +
                                            "           \"X_ZYXEL_Address6Source\": \"\",\n" +
                                            "           \"X_ZYXEL_DHCP6Client\": \"\",\n" +
                                            "           \"X_ZYXEL_BytesSent\": 271,\n" +
                                            "           \"X_ZYXEL_BytesReceived\": 701,\n" +
                                            "           \"ClientDuid\": \"\",\n" +
                                            "           \"ExpireTime\": \"\",\n" +
                                            "           \"X_ZYXEL_DHCPLeaseTime\": 1584341644,\n" +
                                            "           \"X_ZYXEL_Neighbor\": \"02:16:77:01:00:02\",\n" +
                                            "           \"X_ZYXEL_Conn_Guest\": 0,\n" +
                                            "           \"X_ZYXEL_Band\": 2,\n" +
                                            "           \"X_ZYXEL_Channel_24G\": 0,\n" +
                                            "           \"X_ZYXEL_Channel_5G\": 149,\n" +
                                            "           \"X_ZYXEL_RSSI_STAT\": \"Too Close\"\n" +
                                            "         }\n" +
                                            "       ]\n" +
                                            "     }\n"*/

                            devicesInfo = Gson().fromJson(responseStr, DevicesInfo::class.javaObjectType)
                            LogUtil.d(TAG,"devicesInfo:$devicesInfo")

                            val newEndDeviceList = mutableListOf<DevicesInfoObject>()
                            val newHomeEndDeviceList = mutableListOf<DevicesInfoObject>()
                            val newZYXELEndDeviceList = mutableListOf<DevicesInfoObject>()
                            val newGuestEndDeviceList = mutableListOf<DevicesInfoObject>()

                            /*newZYXELEndDeviceList.add(
                                    DevicesInfoObject
                                    (
                                            Active = true,
                                            HostName = GlobalData.getCurrentGatewayInfo().getName(),
                                            IPAddress = GlobalData.getCurrentGatewayInfo().IP,
                                            X_ZYXEL_CapabilityType = "L2Device",
                                            X_ZYXEL_ConnectionType = "WiFi",
                                            X_ZYXEL_HostType = GlobalData.getCurrentGatewayInfo().DeviceMode,
                                            X_ZYXEL_SoftwareVersion = GlobalData.getCurrentGatewayInfo().SoftwareVersion
                                    )
                            )*/

                            var index = 1
                            for(item in devicesInfo.Object)
                            {
                                item.IndexFromFW = index

                                if( (item.HostName == "N/A") || (item.HostName == "") )
                                {
                                    index++
                                    continue
                                }

                                for(itemCin in GlobalData.changeIconNameList)
                                {
                                    if(item.PhysAddress == itemCin.MacAddress)
                                    {
                                        item.UserDefineName = itemCin.HostName
                                        item.Internet_Blocking_Enable = itemCin.Internet_Blocking_Enable
                                    }
                                }

                                if(item.X_ZYXEL_CapabilityType == "L2Device")
                                    newZYXELEndDeviceList.add(item)
                                else
                                {
                                    if(item.X_ZYXEL_Conn_Guest == 1)
                                        newGuestEndDeviceList.add(item)
                                    else
                                        newHomeEndDeviceList.add(item)
                                }

                                newEndDeviceList.add(item)

                                LogUtil.d(TAG,"update devicesInfo:$item")

                                index++
                            }

                            GlobalData.endDeviceList = newEndDeviceList.toMutableList()
                            GlobalData.homeEndDeviceList = newHomeEndDeviceList.toMutableList()
                            GlobalData.ZYXELEndDeviceList = newZYXELEndDeviceList.toMutableList()
                            GlobalData.guestEndDeviceList = newGuestEndDeviceList.toMutableList()

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
        P2PGatewayApi.GetWanInfo()
                .setRequestPageName(TAG)
                .setResponseListener(object: TUTKP2PResponseCallback()
                {
                    override fun onSuccess(responseStr: String)
                    {
                        try
                        {
                            wanInfo = Gson().fromJson(responseStr, WanInfo::class.javaObjectType)
                            LogUtil.d(TAG,"wanInfo:$wanInfo")
                            GlobalData.gatewayWanInfo = wanInfo.copy()
                            getGuestWiFiEnableTask()
                        }
                        catch(e: JSONException)
                        {
                            e.printStackTrace()

                            GlobalBus.publish(MainEvent.HideLoading())
                        }
                    }
                }).execute()
    }

    private fun getGuestWiFiEnableTask()
    {
        LogUtil.d(TAG,"getGuestWiFiEnableTask()")
        P2PWiFiSettingApi.GetGuestWiFi24GInfo()
                .setRequestPageName(TAG)
                .setResponseListener(object: TUTKP2PResponseCallback()
                {
                    override fun onSuccess(responseStr: String)
                    {
                        try
                        {
                            guestWiFiInfo = Gson().fromJson(responseStr, GuestWiFiInfo::class.javaObjectType)
                            LogUtil.d(TAG,"guestWiFiInfo:$guestWiFiInfo")
                            GlobalData.guestWiFiStatus = guestWiFiInfo.Object.Enable
                            getFSecureInfoTask()
                        }
                        catch(e: JSONException)
                        {
                            e.printStackTrace()

                            GlobalBus.publish(MainEvent.HideLoading())
                        }
                    }
                }).execute()

    }

    private fun getFSecureInfoTask()
    {
        LogUtil.d(TAG,"getFSecureInfoTask()")
        P2PGatewayApi.GetFSecureInfo()
                .setRequestPageName(TAG)
                .setResponseListener(object: TUTKP2PResponseCallback()
                {
                    override fun onSuccess(responseStr: String)
                    {
                        try
                        {
                            fSecureInfo = Gson().fromJson(responseStr, FSecureInfo::class.javaObjectType)
                            LogUtil.d(TAG,"fSecureInfo:$fSecureInfo")
                            FeatureConfig.FSecureStatus = fSecureInfo.Object.Cyber_Security_FSC
                            getHostNameReplaceInfoTask()
                        }
                        catch(e: JSONException)
                        {
                            e.printStackTrace()

                            GlobalBus.publish(MainEvent.HideLoading())
                        }
                    }
                }).execute()
    }

    private fun getHostNameReplaceInfoTask()
    {
        LogUtil.d(TAG,"getHostNameReplaceInfoTask()")
        P2PGatewayApi.GetHostNameReplaceInfo()
                .setRequestPageName(TAG)
                .setResponseListener(object: TUTKP2PResponseCallback()
                {
                    override fun onSuccess(responseStr: String)
                    {
                        try
                        {
                            hostNameReplaceInfo = Gson().fromJson(responseStr, HostNameReplaceInfo::class.javaObjectType)
                            LogUtil.d(TAG,"hostNameReplaceInfo:$hostNameReplaceInfo")
                            FeatureConfig.hostNameReplaceStatus = hostNameReplaceInfo.Object.Enable
                            updateUI()
                        }
                        catch(e: JSONException)
                        {
                            e.printStackTrace()

                            GlobalBus.publish(MainEvent.HideLoading())
                        }
                    }
                }).execute()
    }

    private fun setGuestWiFi24GEnableTask()
    {
        val value = if(GlobalData.guestWiFiStatus) "false" else "true"
        val params = ",\"Enable\":\"$value\""

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
        val value = if(GlobalData.guestWiFiStatus) "false" else "true"
        val params = ",\"Enable\":\"$value\""

        P2PWiFiSettingApi.SetGuestWiFi5GInfo()
                .setRequestPageName(TAG)
                .setRequestPayload(params)
                .setResponseListener(object: TUTKP2PResponseCallback()
                {
                    override fun onSuccess(responseStr: String)
                    {
                        setLogoutTask()
                    }
                }).execute()
    }

    private fun setLogoutTask()
    {
        P2PAccountApi.Logout()
                .setRequestPageName(TAG)
                .setResponseListener(object: TUTKP2PResponseCallback()
                {
                    override fun onSuccess(responseStr: String)
                    {

                    }
                }).execute()
    }
}