package zyxel.com.multyproneo.fragment

import android.content.Context
import android.net.ConnectivityManager
import android.net.wifi.WifiConfiguration
import android.net.wifi.WifiManager
import android.os.AsyncTask
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.gson.Gson
import org.json.JSONException
import zyxel.com.multyproneo.R
import zyxel.com.multyproneo.event.GlobalBus
import zyxel.com.multyproneo.event.MainEvent
import zyxel.com.multyproneo.model.GatewayInfo
import zyxel.com.multyproneo.socketconnect.IResponseListener
import zyxel.com.multyproneo.socketconnect.SocketController
import zyxel.com.multyproneo.util.AppConfig
import zyxel.com.multyproneo.util.GlobalData
import zyxel.com.multyproneo.util.LogUtil

class SetupConnectingControllerFragment : Fragment(), IResponseListener
{
    private val TAG = javaClass.simpleName
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

        with(arguments)
        {
            this?.getBoolean("needConnectFlow")?.let{ needConnectFlow = it }
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

                LogUtil.d(TAG, "findingDeviceInfo:${findingDeviceInfo}")

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

                if(!exist)
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
            GlobalData.gatewayList = gatewayList.toMutableList()//copy list to global data
            //GlobalBus.publish(MainEvent.SwitchToFrag(GatewayListFragment()))
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
        retryTimes = 6
        runSearchTask()
    }

    private fun runSearchTask()
    {
        retryTimes++
        GlobalData.gatewayList.clear()
        SocketController(responseListener).deviceScan()
    }

    private fun gotoCannotConnectControllerTroubleshootingPage()
    {
        val bundle = Bundle().apply{
            putSerializable("pageMode", AppConfig.TroubleshootingPage.PAGE_CONNOT_CONNECT_CONTROLLER)
            putBoolean("needConnectFlowForBack", needConnectFlow)
        }

        GlobalBus.publish(MainEvent.SwitchToFrag(SetupConnectTroubleshootingFragment().apply{ arguments = bundle }))
    }

    inner class WiFiConfigTask(var mWifiConfiguration: WifiConfiguration) : AsyncTask<String, Int, Boolean>()
    {
        private var isRunning = true
        private var connectStatus = false
        private var count = 0

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

            if(wifiManager.configuredNetworks != null)
            {
                for(configuration in wifiManager.configuredNetworks)
                {
                    val newSSID = configuration.SSID
                    if(mWifiConfiguration.SSID == newSSID)
                    {
                        LogUtil.d(TAG, "set task exist net id = ${configuration.networkId}")
                        wifiManager.removeNetwork(configuration.networkId)
                    }
                }
            }

            if(wifiManager.configuredNetworks != null)
            {
                LogUtil.d(TAG, "new wifi config")
                val netId = wifiManager.addNetwork(mWifiConfiguration)
                wifiManager.disconnect()
                wifiManager.enableNetwork(netId, true)
                wifiManager.reconnect()

                while(isRunning)
                {
                    count++
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
            LogUtil.d(TAG, "network info = " + mWifi.isConnected)
            LogUtil.d(TAG, "network info connecting = " + mWifi.isConnectedOrConnecting)

            if(mWifi.isConnected)
            {
                val activeWifiInfo = wifiManager.connectionInfo
                if(activeWifiInfo != null)
                {
                    isRunning = false
                    connectStatus = true
                }
                else
                {
                    isRunning = (count != 15)
                    connectStatus = false
                }
            }
            else
            {
                isRunning = (count != 15)
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