package zyxel.com.multyproneo.api.cloud

import android.os.Bundle
import com.google.gson.Gson
import org.json.JSONException
import org.json.JSONObject
import zyxel.com.multyproneo.event.GlobalBus
import zyxel.com.multyproneo.event.MainEvent
import zyxel.com.multyproneo.event.P2PApiEvent
import zyxel.com.multyproneo.fragment.cloud.CloudEndDeviceDetailFragment
import zyxel.com.multyproneo.fragment.cloud.CloudEndDeviceDetailOfflineFragment
import zyxel.com.multyproneo.fragment.cloud.CloudZYXELEndDeviceDetailFragment
import zyxel.com.multyproneo.model.*
import zyxel.com.multyproneo.util.FeatureConfig
import zyxel.com.multyproneo.util.GlobalData
import zyxel.com.multyproneo.util.LogUtil
import kotlin.concurrent.thread

class P2PApiHandler
{
    private val TAG = "P2PApiHandler"
    private val apiMap = mutableMapOf<API_REF, () -> Unit>()
    private var event = API_RES_EVENT.API_RES_EVENT_HOME
    private var apiList: ArrayList<API_REF> = arrayListOf()
    private var apiCount = 0

    enum class API_RES_EVENT
    {
        API_RES_EVENT_HOME,
        API_RES_EVENT_DEVICES,
        API_RES_EVENT_NOTI_ENTER
    }

    enum class API_REF
    {
        API_CHECK_NOTI_FLOW, //click notification message, will enter device detail page by this api. Need to be executed in the last
        API_GET_SYSTEM_INFO, //include init Global data, need to be executed the first
        API_GET_CHANGE_ICON_NAME,
        API_GET_DEVICE_INFO,
        API_GET_WAN_INFO,
        API_GET_GUEST_WIFI_ENABLE,
        API_GET_FSECURE_INFO,
        API_GET_HOSTNAME_REPLACE_INFO,
        API_GET_APP_CUSTOM_INFO,
        API_GET_LAN_IP,
        API_GET_FW_VERSION
    }

    init
    {
        LogUtil.d(TAG, "init")

        apiMap[API_REF.API_GET_SYSTEM_INFO] = {
            LogUtil.d(TAG,"getCloudSystemInfoTask()")

            val gatewayList = mutableListOf<GatewayInfo>()
            val findingDeviceInfo = GatewayInfo()
            gatewayList.add(findingDeviceInfo)

            GlobalData.currentGatewayIndex = 0
            GlobalData.gatewayList = gatewayList.toMutableList()

            P2PGatewayApi.GetSystemInfo()
                    .setRequestPageName(TAG)
                    .setResponseListener(object: TUTKP2PResponseCallback()
                    {
                        override fun onSuccess(responseStr: String)
                        {
                            try
                            {
                                val data = JSONObject(responseStr)
                                val name = data.getJSONObject("Object").getString("HostName")
                                LogUtil.d(TAG,"HostName:$name")
                                GlobalData.getCurrentGatewayInfo().UserDefineName = name
                                executeNextAPI()
                            }
                            catch(e: JSONException)
                            {
                                e.printStackTrace()
                            }
                        }
                    }).execute()
        }

        apiMap[API_REF.API_GET_CHANGE_ICON_NAME] = {
            LogUtil.d(TAG,"getCloudChangeIconNameInfoTask()")
            P2PDevicesApi.GetChangeIconNameInfo()
                    .setRequestPageName(TAG)
                    .setResponseListener(object: TUTKP2PResponseCallback()
                    {
                        override fun onSuccess(responseStr: String)
                        {
                            try
                            {
                                val changeIconNameInfo = Gson().fromJson(responseStr, ChangeIconNameInfo::class.javaObjectType)
                                LogUtil.d(TAG,"changeIconNameInfo:$changeIconNameInfo")
                                GlobalData.changeIconNameList = changeIconNameInfo.Object.toMutableList()
                                executeNextAPI()
                            }
                            catch(e: JSONException)
                            {
                                e.printStackTrace()
                            }
                        }
                    }).execute()
        }

        apiMap[API_REF.API_GET_DEVICE_INFO] = {
            LogUtil.d(TAG,"getCloudDeviceInfoTask()")
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

                                val devicesInfo = Gson().fromJson(responseStr, DevicesInfo::class.javaObjectType)
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

                                executeNextAPI()
                            }
                            catch(e: JSONException)
                            {
                                e.printStackTrace()
                            }
                        }
                    }).execute()
        }

        apiMap[API_REF.API_GET_WAN_INFO] = {
            LogUtil.d(TAG,"getCloudWanInfoTask()")
            P2PGatewayApi.GetWanInfo()
                    .setRequestPageName(TAG)
                    .setResponseListener(object: TUTKP2PResponseCallback()
                    {
                        override fun onSuccess(responseStr: String)
                        {
                            try
                            {
                                val wanInfo = Gson().fromJson(responseStr, WanInfo::class.javaObjectType)
                                LogUtil.d(TAG,"wanInfo:$wanInfo")
                                GlobalData.gatewayWanInfo = wanInfo.copy()
                                GlobalData.getCurrentGatewayInfo().MAC = wanInfo.Object.MAC
                                executeNextAPI()
                            }
                            catch(e: JSONException)
                            {
                                e.printStackTrace()
                            }
                        }
                    }).execute()
        }

        apiMap[API_REF.API_GET_GUEST_WIFI_ENABLE] = {
            LogUtil.d(TAG,"getCloudGuestWiFiEnableTask()")
            P2PWiFiSettingApi.GetGuestWiFi24GInfo()
                    .setRequestPageName(TAG)
                    .setResponseListener(object: TUTKP2PResponseCallback()
                    {
                        override fun onSuccess(responseStr: String)
                        {
                            try
                            {
                                val guestWiFiInfo = Gson().fromJson(responseStr, GuestWiFiInfo::class.javaObjectType)
                                LogUtil.d(TAG,"guestWiFiInfo:$guestWiFiInfo")
                                GlobalData.guestWiFiStatus = guestWiFiInfo.Object.Enable
                                executeNextAPI()
                            }
                            catch(e: JSONException)
                            {
                                e.printStackTrace()
                            }
                        }
                    }).execute()
        }

        apiMap[API_REF.API_GET_FSECURE_INFO] = {
            LogUtil.d(TAG,"getCloudFSecureInfoTask()")
            P2PGatewayApi.GetFSecureInfo()
                    .setRequestPageName(TAG)
                    .setResponseListener(object: TUTKP2PResponseCallback()
                    {
                        override fun onSuccess(responseStr: String)
                        {
                            try
                            {
                                val fSecureInfo = Gson().fromJson(responseStr, FSecureInfo::class.javaObjectType)
                                LogUtil.d(TAG,"fSecureInfo:$fSecureInfo")
                                FeatureConfig.FSecureStatus = fSecureInfo.Object.Cyber_Security_FSC
                                executeNextAPI()
                            }
                            catch(e: JSONException)
                            {
                                e.printStackTrace()
                            }
                        }
                    }).execute()
        }

        apiMap[API_REF.API_GET_HOSTNAME_REPLACE_INFO] = {
            LogUtil.d(TAG,"getCloudHostNameReplaceInfoTask()")
            P2PGatewayApi.GetHostNameReplaceInfo()
                    .setRequestPageName(TAG)
                    .setResponseListener(object: TUTKP2PResponseCallback()
                    {
                        override fun onSuccess(responseStr: String)
                        {
                            try
                            {
                                val hostNameReplaceInfo = Gson().fromJson(responseStr, HostNameReplaceInfo::class.javaObjectType)
                                LogUtil.d(TAG,"hostNameReplaceInfo:$hostNameReplaceInfo")
                                FeatureConfig.hostNameReplaceStatus = hostNameReplaceInfo.Object.Enable
                                executeNextAPI()
                            }
                            catch(e: JSONException)
                            {
                                e.printStackTrace()
                            }
                        }
                    }).execute()
        }

        apiMap[API_REF.API_GET_APP_CUSTOM_INFO] = {
            LogUtil.d(TAG,"getCustomerInfoTask()")
            P2PGatewayApi.GetCustomerInfo()
                    .setRequestPageName(TAG)
                    .setResponseListener(object: TUTKP2PResponseCallback()
                    {
                        override fun onSuccess(responseStr: String)
                        {
                            try
                            {
                                val customerInfo = Gson().fromJson(responseStr, CustomerInfo::class.javaObjectType)
                                LogUtil.d(TAG,"customerInfo:$customerInfo")
                                GlobalData.customerLogo = customerInfo.Object.X_ZYXEL_APP_Customer
                                GlobalData.logFileDeliver = customerInfo.Object.LogFileDeliver
                                executeNextAPI()
                            }
                            catch(e: JSONException)
                            {
                                e.printStackTrace()
                            }
                        }
                    }).execute()
        }

        apiMap[API_REF.API_GET_LAN_IP] = {
            LogUtil.d(TAG,"getCloudLanIPInfoTask()")
            P2PGatewayApi.GetLanIPInfo()
                    .setRequestPageName(TAG)
                    .setResponseListener(object: TUTKP2PResponseCallback()
                    {
                        override fun onSuccess(responseStr: String)
                        {
                            try
                            {
                                val ipInterfaceInfo = Gson().fromJson(responseStr, IPInterfaceInfo::class.javaObjectType)
                                LogUtil.d(TAG,"ipInterfaceInfo:$ipInterfaceInfo")
                                for(item in ipInterfaceInfo.Object)
                                {
                                    if( (item.X_ZYXEL_IfName == "br0") && (item.IPv4Address.isNotEmpty()) )
                                    {
                                        GlobalData.getCurrentGatewayInfo().IP = item.IPv4Address[0].IPAddress
                                        break
                                    }
                                }
                                executeNextAPI()
                            }
                            catch(e: JSONException)
                            {
                                e.printStackTrace()
                            }
                        }
                    }).execute()
        }

        apiMap[API_REF.API_GET_FW_VERSION] = {
            LogUtil.d(TAG,"getCloudFWVersionInfoTask()")
            P2PGatewayApi.GetFWVersionInfo()
                    .setRequestPageName(TAG)
                    .setResponseListener(object: TUTKP2PResponseCallback()
                    {
                        override fun onSuccess(responseStr: String)
                        {
                            try
                            {
                                val gateDetailInfo = Gson().fromJson(responseStr, GatewayDetailInfo::class.javaObjectType)
                                LogUtil.d(TAG,"gateDetailInfo:$gateDetailInfo")
                                GlobalData.getCurrentGatewayInfo().SoftwareVersion = gateDetailInfo.Object.SoftwareVersion
                                GlobalData.getCurrentGatewayInfo().ModelName = gateDetailInfo.Object.ModelName
                                GlobalData.getCurrentGatewayInfo().SerialNumber = gateDetailInfo.Object.SerialNumber
                                executeNextAPI()
                            }
                            catch(e: JSONException)
                            {
                                e.printStackTrace()
                            }
                        }
                    }).execute()
        }

        apiMap[API_REF.API_CHECK_NOTI_FLOW] = {
            thread {
                if(GlobalData.notiUid.isNotEmpty() && GlobalData.notiMac.isNotEmpty())
                {
                    LogUtil.d(TAG,"checkNotiFlow()")

                    GlobalBus.publish(MainEvent.HideLoading())

                    var isZyxelEndDevice = false
                    var isExist = false
                    var deviceInfo = DevicesInfoObject()

                    for(item in GlobalData.ZYXELEndDeviceList)
                    {
                        if(GlobalData.notiMac == item.PhysAddress)
                        {
                            isExist = true
                            isZyxelEndDevice = true
                            deviceInfo = item
                            break
                        }
                    }

                    for(item in GlobalData.homeEndDeviceList)
                    {
                        if(GlobalData.notiMac == item.PhysAddress)
                        {
                            isExist = true
                            isZyxelEndDevice = false
                            deviceInfo = item
                            break
                        }
                    }

                    GlobalData.notiMac = ""
                    GlobalData.notiUid = ""

                    if(isExist)
                    {
                        if(isZyxelEndDevice)
                        {
                            val bundle = Bundle().apply{
                                putBoolean("GatewayMode", false)
                                putSerializable("GatewayInfo", GlobalData.getCurrentGatewayInfo())
                                putSerializable("WanInfo", GlobalData.gatewayWanInfo)
                                putSerializable("DevicesInfo", deviceInfo)
                            }
                            GlobalBus.publish(MainEvent.SwitchToFrag(CloudZYXELEndDeviceDetailFragment().apply{ arguments = bundle }))
                        }
                        else
                        {
                            val bundle = Bundle().apply{
                                putSerializable("DevicesInfo", deviceInfo)
                                putString("Search", "")
                                putBoolean("FromSearch", false)
                            }
                            GlobalBus.publish(MainEvent.SwitchToFrag(CloudEndDeviceDetailFragment().apply{ arguments = bundle }))
                        }
                    }
                    else
                        GlobalBus.publish(MainEvent.SwitchToFrag(CloudEndDeviceDetailOfflineFragment()))
                }
                else
                    executeNextAPI()
            }
        }
    }

    private fun executeNextAPI()
    {
        LogUtil.d(TAG,"current api index:$apiCount")
        LogUtil.d(TAG,"total api size:${this.apiList.size}")

        if(apiCount >= this.apiList.size)
            GlobalBus.publish(P2PApiEvent.ApiExecuteComplete(this.event))
        else
        {
            if(this.apiList.isNotEmpty())
            {
                apiMap[this.apiList[apiCount]]?.let { it() }
                apiCount++
            }
        }
    }

    fun execute(event: API_RES_EVENT, apiList: ArrayList<API_REF>)
    {
        LogUtil.d(TAG, "execute")

        this.event = event
        this.apiList = apiList

        apiCount = 0
        executeNextAPI()
    }
}