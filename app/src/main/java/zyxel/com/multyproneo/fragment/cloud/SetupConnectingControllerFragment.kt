package zyxel.com.multyproneo.fragment.cloud

import android.content.Context
import android.net.ConnectivityManager
import android.net.wifi.WifiConfiguration
import android.net.wifi.WifiManager
import android.os.AsyncTask
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.gson.Gson
import kotlinx.android.synthetic.main.fragment_setup_connecting_controller.*
import org.jetbrains.anko.support.v4.runOnUiThread
import org.json.JSONException
import org.json.JSONObject
import zyxel.com.multyproneo.R
import zyxel.com.multyproneo.api.AccountApi
import zyxel.com.multyproneo.api.Commander
import zyxel.com.multyproneo.event.GlobalBus
import zyxel.com.multyproneo.event.MainEvent
import zyxel.com.multyproneo.fragment.LoginFragment
import zyxel.com.multyproneo.model.GatewayInfo
import zyxel.com.multyproneo.model.LoginInfo
import zyxel.com.multyproneo.socketconnect.IResponseListener
import zyxel.com.multyproneo.socketconnect.SocketController
import zyxel.com.multyproneo.util.AppConfig
import zyxel.com.multyproneo.util.GlobalData
import zyxel.com.multyproneo.util.LogUtil

class SetupConnectingControllerFragment : Fragment(), IResponseListener
{
    private val TAG = "SetupConnectingControllerFragment"
    private lateinit var findingDeviceInfo: GatewayInfo
    private var gatewayList = mutableListOf<GatewayInfo>()
    private val responseListener = this
    private var retryTimes = 0
    private var needConnectFlow = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
    {
        return inflater.inflate(R.layout.fragment_setup_connecting_controller, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)

        GlobalData.currentFrag = TAG
        GlobalData.registeredCloud = false

        with(arguments)
        {
            this?.getBoolean("needConnectFlow", false)?.let{ needConnectFlow = it }
        }

        when(needConnectFlow)
        {
            true ->
            {
                val mWifiConfiguration = WifiConfiguration()
                mWifiConfiguration.SSID = String.format("\"%s\"", GlobalData.scanSSID);
                mWifiConfiguration.preSharedKey = String.format("\"%s\"", GlobalData.scanPWD)
                WiFiConfigTask(mWifiConfiguration).execute()
            }

            false -> startFindDevice()
        }

        runOnUiThread{
            setup_connecting_controller_content_animation_view.setAnimation("ConnectToWiFiRouter_1_oldJson.json")
            setup_connecting_controller_content_animation_view.playAnimation()
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
    }

    override fun responseReceived(ip: String, data: String)
    {
        LogUtil.d(TAG,"responseReceived: ip = $ip, data = $data")
        /*if(data.contains("ApiName") && data.contains("SupportedApiVersion"))
        {
            val data = JSONObject(data)
            val ApiName = data.get("ApiName").toString()
            LogUtil.d(TAG, "ApiName:$ApiName")
            val ModelName = data.get("ModelName").toString()
            LogUtil.d(TAG, "ModelName:$ModelName")
            val SoftwareVersion = data.get("SoftwareVersion").toString()
            LogUtil.d(TAG, "SoftwareVersion:$SoftwareVersion")
            val DeviceMode = data.get("DeviceMode").toString()
            LogUtil.d(TAG, "DeviceMode:$DeviceMode")
            val SupportedApiVersion = data.getJSONArray("SupportedApiVersion")
            val subdata = JSONObject(SupportedApiVersion[0].toString())
            LogUtil.d(TAG, "subdata:$subdata")
            val ApiVersion = subdata.get("ApiVersion").toString()
            LogUtil.d(TAG, "ApiVersion:$ApiVersion")
            val LoginURL = subdata.get("LoginURL").toString()
            LogUtil.d(TAG, "LoginURL:$LoginURL")
        }*/

        if(data.contains("ApiName") && data.contains("SupportedApiVersion"))
        {
            try
            {
                findingDeviceInfo = Gson().fromJson(data, GatewayInfo::class.javaObjectType)
                findingDeviceInfo.IP = ip

                /*userDefineName = DatabaseUtil.getInstance(activity!!)?.getDeviceUserDefineNameFromDB(findingDeviceInfo.MAC)!!
                LogUtil.d(TAG, "userDefineName from DB:$userDefineName")

                if(userDefineName == "")
                    findingDeviceInfo.UserDefineName = findingDeviceInfo.ModelName
                else
                    findingDeviceInfo.UserDefineName = userDefineName*/

                LogUtil.d(TAG, "findingDeviceInfo:$findingDeviceInfo")

                var exist = false
                for(item in gatewayList)
                {
                    if(item.MAC == findingDeviceInfo.MAC)
                    {
                        LogUtil.d(TAG, "already exist, MAC:${findingDeviceInfo.MAC}")
                        exist = true
                        break
                    }
                }

                if(!exist && findingDeviceInfo.ApiName == "ZYXEL RESTful API")
                    gatewayList.add(findingDeviceInfo)
            }
            catch(e: JSONException)
            {
                e.printStackTrace()
            }
        }
    }

    override fun responseReceivedDone()
    {
        LogUtil.d(TAG,"responseReceivedDone")
        if(gatewayList.size > 0)
        {
            runOnUiThread{
                setup_connecting_controller_content_animation_view.setAnimation("ConnectToWiFiRouter_2_oldJson.json")
                setup_connecting_controller_content_animation_view.playAnimation()
            }

            Thread.sleep(2500)

            GlobalData.gatewayList = gatewayList.toMutableList()//copy list to global data
            login()
        }
        else
        {
            if(retryTimes < 5)
                runSearchTask()
            else
                gotoCannotConnectControllerTroubleshootingPage()
        }
    }

    private fun startFindDevice()
    {
        retryTimes = 0
        runSearchTask()
    }

    private fun runSearchTask()
    {
        retryTimes++
        GlobalData.gatewayList.clear()
        SocketController(responseListener).deviceScan()
    }

    private fun login()
    {
        if(needConnectFlow)
        {
            GlobalData.currentGatewayIndex = 0

            val params = JSONObject()
            params.put("username", GlobalData.scanAccount)
            params.put("password", GlobalData.scanAccountPWD)
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
                                GlobalData.loginInfo = Gson().fromJson(responseStr, LoginInfo::class.javaObjectType)
                                LogUtil.d(TAG,"loginInfo:${GlobalData.loginInfo}")
                                GlobalData.gatewayList[0].UserName = GlobalData.scanAccount
                                GlobalData.gatewayList[0].Password = GlobalData.scanAccountPWD
                                GlobalBus.publish(MainEvent.SwitchToFrag(SetupConnectingInternetFragment()))
                            }
                            catch(e: JSONException)
                            {
                                e.printStackTrace()
                            }
                        }

                        override fun onFail(code: Int, msg: String, ctxName: String, isCloudUsing: Boolean)
                        {
                            LogUtil.e(TAG, "[onFail] code = $code")
                            LogUtil.e(TAG, "[onFail] msg = $msg")
                            LogUtil.e(TAG, "[onFail] ctxName = $ctxName")
                            gotoLoginPage()
                        }
                    }).execute()
        }
        else
            gotoLoginPage()
    }

    private fun gotoLoginPage()
    {
        if(GlobalData.gatewayList[0].SupportedCloudAgent)
        {
            val bundle = Bundle().apply{
                putBoolean("needConnectFlowForRetry", needConnectFlow)
            }

            when(AppConfig.SNLogin)
            {
                true -> GlobalBus.publish(MainEvent.SwitchToFrag(SetupSNLoginFragment().apply{ arguments = bundle }))
                else -> GlobalBus.publish(MainEvent.SwitchToFrag(SetupLoginFragment().apply{ arguments = bundle }))
            }
        }
        else
            GlobalBus.publish(MainEvent.SwitchToFrag(LoginFragment()))
    }

    private fun gotoCannotConnectControllerTroubleshootingPage()
    {
        val bundle = Bundle().apply{
            putSerializable("pageMode", AppConfig.TroubleshootingPage.PAGE_CANNOT_CONNECT_CONTROLLER)
            putBoolean("needConnectFlowForRetry", needConnectFlow)
        }

        GlobalBus.publish(MainEvent.SwitchToFrag(SetupConnectTroubleshootingFragment().apply{ arguments = bundle }))
    }

    inner class WiFiConfigTask(var mWifiConfiguration: WifiConfiguration) : AsyncTask<String, Int, Boolean>()
    {
        private var isRunning = true
        private var connectStatus = false
        private var count = 0
        private var networkID = 0

        override fun onPreExecute()
        {
            LogUtil.d(TAG, "[WiFiConfigTask]onPreExecute()")

            super.onPreExecute()

            mWifiConfiguration.allowedProtocols.set(WifiConfiguration.Protocol.RSN)
            mWifiConfiguration.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK)
            mWifiConfiguration.status = WifiConfiguration.Status.ENABLED
            mWifiConfiguration.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP)
            mWifiConfiguration.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP)
            mWifiConfiguration.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK)
            mWifiConfiguration.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP)
            mWifiConfiguration.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP)
            mWifiConfiguration.allowedProtocols.set(WifiConfiguration.Protocol.RSN)
            mWifiConfiguration.allowedProtocols.set(WifiConfiguration.Protocol.WPA)
        }

        override fun doInBackground(vararg params: String?): Boolean
        {
            LogUtil.d(TAG, "[WiFiConfigTask]doInBackground()")

            val wifiManager = activity!!.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
            if(wifiManager.isWifiEnabled)
                LogUtil.d(TAG, "WiFi is already enabled")
            else
            {
                LogUtil.d(TAG, "WiFi is disable")
                wifiManager.isWifiEnabled = true
            }

            var existID = 0
            if(wifiManager.configuredNetworks != null)
            {
                for(configuration in wifiManager.configuredNetworks)
                {
                    LogUtil.d(TAG, "configured = ${configuration.SSID}")

                    val newSSID = configuration.SSID
                    if(mWifiConfiguration.SSID == newSSID)
                    {
                        LogUtil.d(TAG, "set task exist net id = ${configuration.networkId}")
                        val isExistConfiguration = wifiManager.removeNetwork(configuration.networkId)
                        LogUtil.d(TAG, "delete exist net id = $isExistConfiguration")

                        if(!isExistConfiguration)
                            existID = configuration.networkId
                    }
                }
            }

            if(wifiManager.configuredNetworks != null)
            {
                networkID = wifiManager.addNetwork(mWifiConfiguration)
                if(networkID == -1)
                {
                    networkID = existID
                    LogUtil.d(TAG, "set exist wifi network id = $networkID")
                }
                else
                    LogUtil.d(TAG, "set new wifi network id = $networkID")


                wifiManager.disconnect()
                wifiManager.enableNetwork(networkID, true)
                wifiManager.reconnect()

                while(isRunning)
                {
                    count++
                    LogUtil.d(TAG, "wifi connect count = $count")
                    LogUtil.d(TAG, "set wifi network id = $networkID")
                    LogUtil.d(TAG, "set wifi SSID = ${mWifiConfiguration.SSID}")

                    wifiManager.enableNetwork(networkID, true)
                    wifiManager.reconnect()

                    publishProgress(count)
                    try
                    {
                        Thread.sleep(1000)
                    }
                    catch(e: InterruptedException)
                    {
                        e.printStackTrace()
                    }

                }

                return connectStatus
            }
            else
                return false
        }

        override fun onProgressUpdate(vararg values: Int?)
        {
            super.onProgressUpdate(*values)

            val connectivityManager = activity!!.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val wifiManager = activity!!.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
            LogUtil.d(TAG, "extra = " + ConnectivityManager.EXTRA_NO_CONNECTIVITY)
            val mWifi = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI)
            LogUtil.d(TAG, "network info connected = ${mWifi!!.isConnected}")

            if(mWifi.isConnected)
            {
                val activeWifiInfo = wifiManager.connectionInfo
                LogUtil.d(TAG, "network info = $activeWifiInfo")

                if(activeWifiInfo != null)
                {
                    isRunning = false
                    connectStatus = true
                }
                else
                {
                    wifiManager.disconnect()

                    isRunning = (count != AppConfig.waitWiFiConnectionCount)
                    connectStatus = false
                }
            }
            else
            {
                isRunning = (count != AppConfig.waitWiFiConnectionCount)
                connectStatus = false
            }
        }

        override fun onPostExecute(result: Boolean?)
        {
            LogUtil.d(TAG, "[WiFiConfigTask]onPostExecute()")

            super.onPostExecute(result)

            LogUtil.d(TAG, "connect status = $result")

            when(result)
            {
                true -> startFindDevice()

                false -> gotoCannotConnectControllerTroubleshootingPage()
            }

        }
    }
}