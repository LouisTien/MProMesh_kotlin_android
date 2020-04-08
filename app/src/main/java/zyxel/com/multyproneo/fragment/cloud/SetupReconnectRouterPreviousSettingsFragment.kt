package zyxel.com.multyproneo.fragment.cloud

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
import kotlinx.android.synthetic.main.fragment_reconnect_router_previous_settings.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.support.v4.runOnUiThread
import zyxel.com.multyproneo.R
import zyxel.com.multyproneo.database.room.DatabaseSiteInfoEntity
import zyxel.com.multyproneo.event.GlobalBus
import zyxel.com.multyproneo.event.MainEvent
import zyxel.com.multyproneo.util.AppConfig
import zyxel.com.multyproneo.util.DatabaseCloudUtil
import zyxel.com.multyproneo.util.LogUtil

class SetupReconnectRouterPreviousSettingsFragment : Fragment()
{
    private val TAG = javaClass.simpleName
    private var mac = ""
    private var needLoginWhenFinal = false
    private lateinit var db: DatabaseCloudUtil
    private lateinit var siteInfo: DatabaseSiteInfoEntity

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
    {
        return inflater.inflate(R.layout.fragment_reconnect_router_previous_settings, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)

        with(arguments)
        {
            this?.getString("MAC")?.let{ mac = it }
            this?.getBoolean("needLoginWhenFinal", false)?.let{ needLoginWhenFinal = it }
        }

        db = DatabaseCloudUtil.getInstance(activity!!)!!
        getDataFromDB()

        runOnUiThread{
            setup_reconnect_apply_previous_settings_content_animation_view.setAnimation("ConnectToTheInternet_1_oldJson.json")
            setup_reconnect_apply_previous_settings_content_animation_view.playAnimation()
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

    private fun getDataFromDB()
    {
        doAsync{
            siteInfo = db.getSiteInfoDao().queryByMac(mac)
            connectRouterTask()
        }
    }

    private fun connectRouterTask()
    {
        val mWifiConfiguration = WifiConfiguration()
        mWifiConfiguration.SSID = String.format("\"%s\"", siteInfo.wifiSSID)
        mWifiConfiguration.preSharedKey = String.format("\"%s\"", siteInfo.wifiPWD)
        WiFiConfigTask(mWifiConfiguration).execute()
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

            /*if(wifiManager.configuredNetworks != null)
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
                return false*/

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
            }
            else
                connectStatus = false


            when(connectStatus)
            {
                true -> {
                    runOnUiThread{
                        setup_reconnect_apply_previous_settings_content_animation_view.setAnimation("ConnectToTheInternet_2_oldJson.json")
                        setup_reconnect_apply_previous_settings_content_animation_view.playAnimation()
                    }

                    Thread.sleep(2500)

                    val bundle = Bundle().apply{
                        putBoolean("needLoginWhenFinal", needLoginWhenFinal)
                    }

                    GlobalBus.publish(MainEvent.SwitchToFrag(ConnectToCloudFragment().apply{ arguments = bundle }))
                }

                false ->
                {
                    val bundle = Bundle().apply{
                        putSerializable("pageMode", AppConfig.TroubleshootingPage.PAGE_CANNOT_CONNECT_CONTROLLER_PREVIOUS_SET)
                        putString("MAC", mac)
                    }

                    GlobalBus.publish(MainEvent.SwitchToFrag(SetupConnectTroubleshootingFragment().apply{ arguments = bundle }))
                }
            }

            return true
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

        /*override fun onPostExecute(result: Boolean?)
        {
            LogUtil.d(TAG, "[WiFiConfigTask]onPostExecute()")

            super.onPostExecute(result)

            LogUtil.d(TAG, "connect status = $result")

            when(result)
            {
                true -> {
                    runOnUiThread{
                        setup_reconnect_apply_previous_settings_content_animation_view.setAnimation("ConnectToTheInternet_2_oldJson.json")
                        setup_reconnect_apply_previous_settings_content_animation_view.playAnimation()
                    }

                    Thread.sleep(2500)

                    GlobalBus.publish(MainEvent.SwitchToFrag(ConnectToCloudFragment()))
                }

                false ->
                {
                    val bundle = Bundle().apply{
                        putSerializable("pageMode", AppConfig.TroubleshootingPage.PAGE_CANNOT_CONNECT_CONTROLLER_PREVIOUS_SET)
                        putString("MAC", mac)
                    }

                    GlobalBus.publish(MainEvent.SwitchToFrag(SetupConnectTroubleshootingFragment().apply{ arguments = bundle }))
                }
            }
        }*/
    }
}