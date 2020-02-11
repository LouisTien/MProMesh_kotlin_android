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
import zyxel.com.multyproneo.R
import zyxel.com.multyproneo.event.GlobalBus
import zyxel.com.multyproneo.event.MainEvent
import zyxel.com.multyproneo.util.GlobalData
import zyxel.com.multyproneo.util.LogUtil

class SetupConnectingControllerFragment : Fragment()
{
    private val TAG = javaClass.simpleName

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
    {
        return inflater.inflate(R.layout.fragment_setup_connecting_controller, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)

        val mWifiConfiguration = WifiConfiguration()
        mWifiConfiguration.SSID = String.format("\"%s\"", GlobalData.scanSSID);
        mWifiConfiguration.preSharedKey = String.format("\"%s\"", GlobalData.scanPWD)
        WiFiConfigTask(mWifiConfiguration).execute()
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

    inner class WiFiConfigTask(var mWifiConfiguration: WifiConfiguration) : AsyncTask<String, Int, Boolean>()
    {
        private var isRunning = true
        private var connectStatus = false
        private var count = 0

        override fun onPreExecute()
        {
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
            super.onPostExecute(result)

            LogUtil.d(TAG, "connect status = $result")
        }
    }
}