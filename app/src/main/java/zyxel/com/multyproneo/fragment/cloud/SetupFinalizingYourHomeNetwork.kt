package zyxel.com.multyproneo.fragment.cloud

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.gson.Gson
import org.jetbrains.anko.doAsync
import org.json.JSONException
import org.json.JSONObject
import zyxel.com.multyproneo.R
import zyxel.com.multyproneo.api.Commander
import zyxel.com.multyproneo.api.DevicesApi
import zyxel.com.multyproneo.api.WiFiSettingApi
import zyxel.com.multyproneo.api.cloud.AMDMApi
import zyxel.com.multyproneo.api.cloud.TUTKCommander
import zyxel.com.multyproneo.database.room.DatabaseClientListEntity
import zyxel.com.multyproneo.database.room.DatabaseSiteInfoEntity
import zyxel.com.multyproneo.event.GlobalBus
import zyxel.com.multyproneo.event.MainEvent
import zyxel.com.multyproneo.model.DevicesInfo
import zyxel.com.multyproneo.model.DevicesInfoObject
import zyxel.com.multyproneo.model.WiFiSettingInfo
import zyxel.com.multyproneo.model.cloud.TUTKAddDeviceInfo
import zyxel.com.multyproneo.model.cloud.TUTKAllDeviceInfo
import zyxel.com.multyproneo.util.*
import java.util.HashMap

class SetupFinalizingYourHomeNetwork : Fragment()
{
    private val TAG = javaClass.simpleName
    private lateinit var addDeviceInfo: TUTKAddDeviceInfo
    private lateinit var devicesInfo: DevicesInfo
    private lateinit var WiFiSettingInfoSet: WiFiSettingInfo
    private lateinit var db: DatabaseCloudUtil
    private var newHomeEndDeviceList = mutableListOf<DevicesInfoObject>()
    private var WiFiName = ""
    private var WiFiPwd = ""

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
    {
        return inflater.inflate(R.layout.fragment_setup_finalizing_your_home_network, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)
        db = DatabaseCloudUtil.getInstance(activity!!)!!
        getWiFiSettingInfoTask()
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

                            addDevice()
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

        var accessToken by SharedPreferencesUtil(activity!!, AppConfig.SHAREDPREF_TUTK_ACCESS_TOKEN_KEY, "")

        val header = HashMap<String, Any>()
        header["authorization"] = "${GlobalData.tokenType} $accessToken"

        val params = JSONObject()
        params.put("udid", GlobalData.currentUID)
        params.put("fwVer", GlobalData.getCurrentGatewayInfo().SoftwareVersion)
        params.put("displayName", GlobalData.getCurrentGatewayInfo().ModelName)
        params.put("credential", "")
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

        var accessToken by SharedPreferencesUtil(activity!!, AppConfig.SHAREDPREF_TUTK_ACCESS_TOKEN_KEY, "")

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

    private fun addToDB()
    {
        doAsync{

            var siteInfo = DatabaseSiteInfoEntity(
                    GlobalData.getCurrentGatewayInfo().MAC,
                    GlobalData.getCurrentGatewayInfo().ModelName,
                    "N/A",
                    WiFiName,
                    WiFiPwd,
                    true
            )

            db.getSiteInfoDao().insert(siteInfo)

            for(item in newHomeEndDeviceList)
            {
                var clientInfo = DatabaseClientListEntity(
                        GlobalData.getCurrentGatewayInfo().MAC,
                        item.PhysAddress,
                        item.HostName
                )

                db.getClientListDao().insert(clientInfo)
            }

            getAllDevice()
        }
    }
}