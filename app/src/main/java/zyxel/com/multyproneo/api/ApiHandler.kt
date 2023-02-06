package zyxel.com.multyproneo.api

import com.google.gson.Gson
import org.json.JSONException
import org.json.JSONObject
import zyxel.com.multyproneo.event.ApiEvent
import zyxel.com.multyproneo.event.GlobalBus
import zyxel.com.multyproneo.model.*
import zyxel.com.multyproneo.tool.CommonTool
import zyxel.com.multyproneo.util.FeatureConfig
import zyxel.com.multyproneo.util.GlobalData
import zyxel.com.multyproneo.util.LogUtil
import kotlin.concurrent.thread

class ApiHandler
{
    private val TAG = "ApiHandler"
    private val apiMap = mutableMapOf<API_REF, () -> Unit>()
    private var event = API_RES_EVENT.API_RES_EVENT_HOME_API_ONCE
    private var apiList: ArrayList<API_REF> = arrayListOf()
    private var apiCount = 0

    enum class API_RES_EVENT
    {
        API_RES_EVENT_FEATURE_LIST,
        API_RES_EVENT_HOME_API_ONCE,
        API_RES_EVENT_HOME_API_REGULAR,
        API_RES_EVENT_DEVICES_API_REGULAR,
        API_RES_EVENT_GATEWAY_EDIT,
        API_RES_EVENT_EXTENDER_EDIT,
        API_RES_EVENT_END_DEVICE_EDIT,
        API_RES_EVENT_PARENTAL_CONTROL,
        API_RES_EVENT_PARENTAL_CONTROL_ADD_SELECT_DEVICE,
        API_RES_EVENT_PARENTAL_CONTROL_EDIT_SELECT_DEVICE
    }

    enum class API_REF
    {
        API_GET_SYSTEM_INFO,
        API_GET_CHANGE_ICON_NAME,
        API_GET_DEVICE_INFO,
        API_GET_WAN_INFO,
        API_GET_GUEST_WIFI_ENABLE,
        API_GET_FSECURE_INFO,
        API_GET_HOSTNAME_REPLACE_INFO,
        API_GET_INTERNET_BLOCK_INFO,
        API_GET_APP_UI_CUSTOM_INFO,
        API_GET_PARENTAL_CONTROL_INFO,
        API_GET_GATEWAY_SYSTEM_DATE,
        API_CHECK_IN_USE_SELECT_DEVICE,
        API_GET_REMOTE_MANAGEMENT,
    }

    init
    {
        LogUtil.d(TAG, "init")

        apiMap[API_REF.API_GET_SYSTEM_INFO] = {
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
            LogUtil.d(TAG,"getChangeIconNameInfoTask()")
            DevicesApi.GetChangeIconNameInfo()
                    .setRequestPageName(TAG)
                    .setResponseListener(object: Commander.ResponseListener()
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
            LogUtil.d(TAG,"getDeviceInfoTask()")
            DevicesApi.GetDevicesInfo()
                    .setRequestPageName(TAG)
                    .setResponseListener(object: Commander.ResponseListener()
                    {
                        override fun onSuccess(responseStr: String)
                        {
                            try
                            {
                                val devicesInfo = Gson().fromJson(responseStr, DevicesInfo::class.javaObjectType)
                                LogUtil.d(TAG,"devicesInfo:$devicesInfo")

                                val newEndDeviceList = mutableListOf<DevicesInfoObject>()
                                val newHomeEndDeviceList = mutableListOf<DevicesInfoObject>()
                                val newZYXELEndDeviceList = mutableListOf<DevicesInfoObject>()
                                val newGuestEndDeviceList = mutableListOf<DevicesInfoObject>()

                                GlobalData.alreadyGetGatewayInfoLocalBase = true

                                /*newZYXELEndDeviceList.add(
                                        DevicesInfoObject
                                        (
                                                Active = true,
                                                HostName = GlobalData.getCurrentGatewayInfo().getName(),
                                                IPAddress = GlobalData.getCurrentGatewayInfo().IP,
                                                PhysAddress = GlobalData.getCurrentGatewayInfo().MAC,
                                                X_ZYXEL_CapabilityType = "L2Device",
                                                X_ZYXEL_ConnectionType = "WiFi",
                                                X_ZYXEL_HostType = GlobalData.getCurrentGatewayInfo().DeviceMode,
                                                X_ZYXEL_SoftwareVersion = GlobalData.getCurrentGatewayInfo().SoftwareVersion
                                        )
                                )*/
                                //layer 2
                                newZYXELEndDeviceList.add(
                                    DevicesInfoObject
                                    (
                                            Active = true,
                                            HostName = "TEST-01",
                                            IPAddress = "192.168.1.38",
                                            PhysAddress = "f0:87:56:8c:f8:78",
                                            X_ZYXEL_CapabilityType = "L2Device",
                                            X_ZYXEL_ConnectionType = "WiFi",
                                            X_ZYXEL_HostType = GlobalData.getCurrentGatewayInfo().DeviceMode,
                                            X_ZYXEL_SoftwareVersion = GlobalData.getCurrentGatewayInfo().SoftwareVersion,
                                            X_ZYXEL_Neighbor = "gateway",
                                            X_ZYXEL_RSSI_STAT = "Weak"
                                    )
                            )

//                                newZYXELEndDeviceList.add(
//                                    DevicesInfoObject
//                                        (
//                                        Active = true,
//                                        HostName = "TEST-02",
//                                        IPAddress = "192.168.1.39",
//                                        PhysAddress = "f0:87:56:8c:f8:79",
//                                        X_ZYXEL_CapabilityType = "L2Device",
//                                        X_ZYXEL_ConnectionType = "WiFi",
//                                        X_ZYXEL_HostType = GlobalData.getCurrentGatewayInfo().DeviceMode,
//                                        X_ZYXEL_SoftwareVersion = GlobalData.getCurrentGatewayInfo().SoftwareVersion,
//                                        X_ZYXEL_Neighbor = "gateway",
//                                        X_ZYXEL_RSSI_STAT = "Weak"
//                                    )
//                                )
//
//                                newZYXELEndDeviceList.add(
//                                    DevicesInfoObject
//                                        (
//                                        Active = true,
//                                        HostName = "TEST-03",
//                                        IPAddress = "192.168.1.40",
//                                        PhysAddress = "f0:87:56:8c:f8:80",
//                                        X_ZYXEL_CapabilityType = "L2Device",
//                                        X_ZYXEL_ConnectionType = "WiFi",
//                                        X_ZYXEL_HostType = GlobalData.getCurrentGatewayInfo().DeviceMode,
//                                        X_ZYXEL_SoftwareVersion = GlobalData.getCurrentGatewayInfo().SoftwareVersion,
//                                        X_ZYXEL_Neighbor = "gateway",
//                                        X_ZYXEL_RSSI_STAT = "Close"
//                                    )
//                                )

                                //layer 3
                                newZYXELEndDeviceList.add(
                                    DevicesInfoObject
                                        (
                                        Active = true,
                                        HostName = "TEST-04",
                                        IPAddress = "192.168.1.41",
                                        PhysAddress = "f0:87:56:8c:f8:81",
                                        X_ZYXEL_CapabilityType = "L2Device",
                                        X_ZYXEL_ConnectionType = "WiFi",
                                        X_ZYXEL_HostType = GlobalData.getCurrentGatewayInfo().DeviceMode,
                                        X_ZYXEL_SoftwareVersion = GlobalData.getCurrentGatewayInfo().SoftwareVersion,
//                                        X_ZYXEL_Neighbor = "f0:87:56:8c:f8:76",
                                        X_ZYXEL_Neighbor = "f0:87:56:8c:f8:80",
                                        X_ZYXEL_RSSI_STAT = "Close"
                                    )
                                )

                                newZYXELEndDeviceList.add(
                                    DevicesInfoObject
                                        (
                                        Active = true,
                                        HostName = "TEST-05",
                                        IPAddress = "192.168.1.42",
                                        PhysAddress = "f0:87:56:8c:f8:82",
                                        X_ZYXEL_CapabilityType = "L2Device",
                                        X_ZYXEL_ConnectionType = "WiFi",
                                        X_ZYXEL_HostType = GlobalData.getCurrentGatewayInfo().DeviceMode,
                                        X_ZYXEL_SoftwareVersion = GlobalData.getCurrentGatewayInfo().SoftwareVersion,
                                        X_ZYXEL_Neighbor = "f0:87:56:8c:f8:78",
//                                        X_ZYXEL_Neighbor = "f0:87:56:8c:f8:80",
                                        X_ZYXEL_RSSI_STAT = "Weak"
                                    )
                                )

                                newZYXELEndDeviceList.add(
                                    DevicesInfoObject
                                        (
                                        Active = true,
                                        HostName = "TEST-06",
                                        IPAddress = "192.168.1.43",
                                        PhysAddress = "f0:87:56:8c:f8:83",
                                        X_ZYXEL_CapabilityType = "L2Device",
                                        X_ZYXEL_ConnectionType = "WiFi",
                                        X_ZYXEL_HostType = GlobalData.getCurrentGatewayInfo().DeviceMode,
                                        X_ZYXEL_SoftwareVersion = GlobalData.getCurrentGatewayInfo().SoftwareVersion,
                                        X_ZYXEL_Neighbor = "f0:87:56:8c:f8:76",
//                                        X_ZYXEL_Neighbor = "f0:87:56:8c:f8:80",
                                        X_ZYXEL_RSSI_STAT = "No"
                                    )
                                )

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
            LogUtil.d(TAG,"getWanInfoTask()")
            GatewayApi.GetWanInfo()
                    .setRequestPageName(TAG)
                    .setResponseListener(object: Commander.ResponseListener()
                    {
                        override fun onSuccess(responseStr: String)
                        {
                            try
                            {
                                val wanInfo = Gson().fromJson(responseStr, WanInfo::class.javaObjectType)
                                LogUtil.d(TAG,"wanInfo:$wanInfo")
                                GlobalData.gatewayWanInfo = wanInfo.copy()
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
            LogUtil.d(TAG,"getGuestWiFiEnableTask()")
            WiFiSettingApi.GetGuestWiFi24GInfo()
                    .setRequestPageName(TAG)
                    .setResponseListener(object: Commander.ResponseListener()
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
            LogUtil.d(TAG,"getFSecureInfoTask()")
            GatewayApi.GetFSecureInfo()
                    .setRequestPageName(TAG)
                    .setResponseListener(object: Commander.ResponseListener()
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
            LogUtil.d(TAG,"getHostNameReplaceInfoTask()")
            GatewayApi.GetHostNameReplaceInfo()
                    .setRequestPageName(TAG)
                    .setResponseListener(object: Commander.ResponseListener()
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

        apiMap[API_REF.API_GET_INTERNET_BLOCK_INFO] = {
            LogUtil.d(TAG,"getInternetBlockingInfoTask()")
            GatewayApi.GetInternetBlockingInfo()
                    .setRequestPageName(TAG)
                    .setResponseListener(object: Commander.ResponseListener()
                    {
                        override fun onSuccess(responseStr: String)
                        {
                            try
                            {
                                val internetBlockingInfo = Gson().fromJson(responseStr, InternetBlockingInfo::class.javaObjectType)
                                LogUtil.d(TAG,"internetBlockingInfo:$internetBlockingInfo")
                                FeatureConfig.internetBlockingStatus = internetBlockingInfo.Object.Enable
                                executeNextAPI()
                            }
                            catch(e: JSONException)
                            {
                                e.printStackTrace()
                            }
                        }
                    }).execute()
        }

        apiMap[API_REF.API_GET_APP_UI_CUSTOM_INFO] = {
            LogUtil.d(TAG,"getAPPUICustomInfoTask()")
            GatewayApi.GetAPPUICustom()
                    .setRequestPageName(TAG)
                    .setResponseListener(object: Commander.ResponseListener()
                    {
                        override fun onSuccess(responseStr: String)
                        {
                            try
                            {
                                FeatureConfig.FeatureInfo = Gson().fromJson(responseStr, AppUICustomInfo::class.javaObjectType)
                                FeatureConfig.FSecureStatus = FeatureConfig.FeatureInfo.APPUICustomList.F_Secure_New
                                LogUtil.d(TAG,"appUICustomInfo:${FeatureConfig.FeatureInfo}")
                                executeNextAPI()
                            }
                            catch(e: JSONException)
                            {
                                e.printStackTrace()
                            }
                        }
                    }).execute()
        }

        apiMap[API_REF.API_GET_PARENTAL_CONTROL_INFO] = {
            LogUtil.d(TAG,"getParentalControlInfo()")
            ParentalControlApi.GetInfo()
                    .setRequestPageName(TAG)
                    .setResponseListener(object: Commander.ResponseListener()
                    {
                        override fun onSuccess(responseStr: String)
                        {
                            try
                            {
                                GlobalData.parentalControlProfileFirstEmptyIndex = 0
                                val parentalControlInfo = Gson().fromJson(responseStr, ParentalControlInfo::class.javaObjectType)

                                GlobalData.parentalControlMasterSwitch = parentalControlInfo.Object.Enable

                                val newParentalControlInfoProfileList = mutableListOf<ParentalControlInfoProfile>()

                                var index = 1
                                for(item in parentalControlInfo.Object.Profile)
                                {
                                    item.index = index

                                    if( (item.Name == "N/A") || (item.Name == "") )
                                    {
                                        if(GlobalData.parentalControlProfileFirstEmptyIndex == 0)
                                            GlobalData.parentalControlProfileFirstEmptyIndex = index

                                        index++
                                        continue
                                    }

                                    newParentalControlInfoProfileList.add(item)
                                    index++
                                }

                                if(GlobalData.parentalControlProfileFirstEmptyIndex == 0)
                                    GlobalData.parentalControlProfileFirstEmptyIndex = index

                                GlobalData.parentalControlInfoProfileList = newParentalControlInfoProfileList.toMutableList()

                                LogUtil.d(TAG, "Profile List: ${GlobalData.parentalControlInfoProfileList}")

                                LogUtil.d(TAG, "=============================")
                                for(subItem in GlobalData.parentalControlInfoProfileList)
                                    LogUtil.d(TAG, "Profile: [${subItem.index}] ${subItem.Name}")
                                LogUtil.d(TAG, "First Empty Profile: [${GlobalData.parentalControlProfileFirstEmptyIndex}]")
                                LogUtil.d(TAG, "=============================")

                                executeNextAPI()
                            }
                            catch(e: JSONException)
                            {
                                e.printStackTrace()
                            }
                        }
                    }).execute()
        }

        apiMap[API_REF.API_GET_GATEWAY_SYSTEM_DATE] = {
            LogUtil.d(TAG,"getGatewaySystemDate()")
            GatewayApi.GetSystemDate()
                    .setRequestPageName(TAG)
                    .setResponseListener(object: Commander.ResponseListener()
                    {
                        override fun onSuccess(responseStr: String)
                        {
                            try
                            {
                                val data = JSONObject(responseStr)
                                val date = data.getJSONObject("Object").getString("localTime")
                                //Tue Jun  8 07:23:09 DST 2021
                                LogUtil.d(TAG,"System Date:$date")

                                /*var str = "Wed May  5 08:52:36 DST 2021"
                                str = str.replace(" DST", "")
                                val sdf = SimpleDateFormat("EEE MMM dd HH:mm:ss yyyy", Locale.ENGLISH)
                                LogUtil.e(TAG,"date:${sdf.parse(str)}")*/

                                val dateArray = date.split(" ").toTypedArray()
                                var index = 1
                                dateArray.forEach {
                                    when(index)
                                    {
                                        1 -> GlobalData.gatewaySystemDate.week = it
                                        2 -> GlobalData.gatewaySystemDate.month = it
                                        3 -> GlobalData.gatewaySystemDate.day = it.toIntOrNull()?:0
                                        4 ->
                                        {
                                            val timeArray = it.split(":")
                                            if(timeArray.size >= 3)
                                            {
                                                GlobalData.gatewaySystemDate.hour = timeArray[0].toIntOrNull()?:0
                                                GlobalData.gatewaySystemDate.min = timeArray[1].toIntOrNull()?:0
                                                GlobalData.gatewaySystemDate.sec = timeArray[2].toIntOrNull()?:0
                                            }
                                        }
                                    }
                                    index += 1
                                }
                                LogUtil.d(TAG,"Gateway System Date:${GlobalData.gatewaySystemDate}")

                                executeNextAPI()
                            }
                            catch(e: JSONException)
                            {
                                e.printStackTrace()
                            }
                        }
                    }).execute()
        }

        apiMap[API_REF.API_CHECK_IN_USE_SELECT_DEVICE] = {
            LogUtil.d(TAG,"checkInUseSelectedDevice()")

            thread {
                for(device in GlobalData.homeEndDeviceList)
                {
                    for(profile in GlobalData.parentalControlInfoProfileList)
                    {
                        for(mac in profile.GetMACAddressList())
                        {
                            if(device.PhysAddress.toLowerCase() == mac.toLowerCase())
                            {
                                device.ParentalControlInUse = true
                                device.InParentalControlProfileName = profile.Name
                                device.ParentalControlBlock = CommonTool.checkScheduleBlock(profile)
                            }
                        }
                    }
                }

                for(device in GlobalData.homeEndDeviceList)
                {
                    for(info in GlobalData.parentalControlSelectedDeviceList)
                    {
                        if(device.PhysAddress.toLowerCase() == info.PhysAddress.toLowerCase())
                            device.ParentalControlSelect = true
                    }
                }

                executeNextAPI()
            }
        }

        apiMap[API_REF.API_GET_REMOTE_MANAGEMENT] = {
            LogUtil.d(TAG,"getRemoteManagement()")
            GatewayApi.GetRemoteManagement()
                    .setRequestPageName(TAG)
                    .setResponseListener(object: Commander.ResponseListener()
                    {
                        override fun onSuccess(responseStr: String)
                        {
                            try
                            {
                                val remoteManagement = Gson().fromJson(responseStr, RemoteManagement::class.javaObjectType)
                                LogUtil.d(TAG,"remoteManagement:${remoteManagement}")
                                FeatureConfig.remoteManagements = remoteManagement.Object
                                executeNextAPI()
                            }
                            catch(e: JSONException)
                            {
                                e.printStackTrace()
                            }
                        }
                    }).execute()
        }
    }

    private fun executeNextAPI()
    {
        LogUtil.d(TAG,"current api index:$apiCount")
        LogUtil.d(TAG,"total api size:${this.apiList.size}")

        if(apiCount >= this.apiList.size)
            GlobalBus.publish(ApiEvent.ApiExecuteComplete(this.event))
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