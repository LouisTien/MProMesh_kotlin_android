package zyxel.com.multyproneo.fragment.cloud

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.wifi.ScanResult
import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.RotateAnimation
import com.google.gson.Gson
import kotlinx.android.synthetic.main.fragment_wifi_signal_meter.*
import org.jetbrains.anko.support.v4.runOnUiThread
import org.json.JSONException
import org.json.JSONObject
import zyxel.com.multyproneo.R
import zyxel.com.multyproneo.api.cloud.P2PDevicesApi
import zyxel.com.multyproneo.api.cloud.P2PGatewayApi
import zyxel.com.multyproneo.api.cloud.TUTKP2PResponseCallback
import zyxel.com.multyproneo.event.GlobalBus
import zyxel.com.multyproneo.event.MainEvent
import zyxel.com.multyproneo.model.DevicesInfo
import zyxel.com.multyproneo.util.GlobalData
import zyxel.com.multyproneo.util.LogUtil
import java.net.NetworkInterface
import java.util.*
import kotlin.concurrent.schedule

class CloudWiFiSignalMeterFragment : Fragment()
{
    private val TAG = javaClass.simpleName
    private val wifiReceiver = WifiReceiver()
    private lateinit var wifiManager: WifiManager
    private lateinit var connectionInfo: WifiInfo
    private lateinit var scanResultList: List<ScanResult>
    private var getRssiTimer = Timer()
    private var c2gRssi = 0
    private var frequency = 0
    private var finalDegree = 0.0f
    private val maxValue = -40f
    private val minValue = -100f
    private val startDegree = -90f
    private val endDegree = 90f
    private var connectedModel = ""
    private var mobileDeviceIndex = 0
    private var isStarted = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
    {
        return inflater.inflate(R.layout.fragment_wifi_signal_meter, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)
        wifiManager = activity?.applicationContext?.getSystemService(Context.WIFI_SERVICE) as WifiManager
        wifiManager.startScan()

        wifi_signal_start_button.setOnClickListener{
            if(wifi_signal_start_button.text == resources.getString(R.string.diagnostic_wifi_signal_start))
            {
                wifi_signal_start_button.text = resources.getString(R.string.diagnostic_wifi_signal_stop)
                wifi_signal_info_strength_text.text = ""
                startRegularUpdateRssi()
            }
            else
            {
                wifi_signal_start_button.text = resources.getString(R.string.diagnostic_wifi_signal_start)
                stopRegularUpdateRssi()
            }
        }

        GlobalBus.publish(MainEvent.ShowLoading())
    }

    override fun onResume()
    {
        super.onResume()
        activity?.applicationContext?.registerReceiver(wifiReceiver, IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION))
    }

    override fun onPause()
    {
        super.onPause()
        getRssiTimer.cancel()
        activity?.applicationContext?.unregisterReceiver(wifiReceiver)
    }

    override fun onDestroyView()
    {
        super.onDestroyView()
    }

    private fun startRegularUpdateRssi()
    {
        isStarted = true
        getRssiTimer = Timer()
        getRssiTimer.schedule(1000, 5000){ getRssiTask() }
    }

    private fun stopRegularUpdateRssi()
    {
        isStarted = false
        getRssiTimer.cancel()
        setDefaultPointerStatus()
    }

    private fun setDefaultPointerStatus()
    {
        if(!isVisible) return

        runOnUiThread{
            wifi_signal_info_strength_text.text = ""
            wifi_signal_info_strength_unit_text.visibility = View.INVISIBLE
            animatePointer(wifi_signal_pointer_view, maxValue, minValue, -70f, startDegree, endDegree)
        }
    }

    private fun updateWiFiInfoUI()
    {
        if(!isVisible) return

        runOnUiThread{
            for(i in scanResultList.indices)
            {
                if(scanResultList[i].BSSID.equals(connectionInfo.bssid, ignoreCase = true))
                {
                    c2gRssi = scanResultList[i].level
                    frequency = scanResultList[i].frequency
                    wifi_signal_info_band_text.text = if(frequency > 5000) "5GHz" else "2.4GHz"
                }
            }

            wifi_signal_info_name_text.text = connectionInfo.ssid.substring(1, connectionInfo.ssid.length - 1)
            wifi_signal_info_connect_text.text = if(connectedModel == "") GlobalData.getCurrentGatewayInfo().getName() else connectedModel
        }
    }

    private fun updateSignalValue(value: Float)
    {
        runOnUiThread{
            wifi_signal_info_strength_text.text = value.toString()
            wifi_signal_info_strength_unit_text.visibility = View.VISIBLE

            var newValue = value

            if(newValue < -100f)
                newValue = -100f

            if(newValue > -40f)
                newValue = -40f

            animatePointer(wifi_signal_pointer_view, maxValue, minValue, newValue, startDegree, endDegree)
        }
    }

    private fun animatePointer(view: View, max: Float, min: Float, value: Float, startDegree: Float, endDegree: Float)
    {
        val targetDegree = ((value - min) / (max - min)) * (endDegree - startDegree) + startDegree
        val anim = RotateAnimation(finalDegree, targetDegree,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 1f - 0.09f)
        anim.duration = 500
        anim.fillAfter = true
        view.startAnimation(anim)
        finalDegree = targetDegree
    }

    private fun getMobileDeviceMacAddress(): String
    {
        try
        {
            val all = Collections.list(NetworkInterface.getNetworkInterfaces())
            for(nif in all)
            {
                if(!nif.name.equals("wlan0", ignoreCase = true))
                    continue

                val macBytes = nif.hardwareAddress ?: return ""

                val res1 = StringBuilder()
                for(b in macBytes)
                {
                    var singleByte = Integer.toHexString(b.toInt() and 0xFF)
                    if(singleByte.length == 1)
                        singleByte = "0$singleByte"

                    res1.append("$singleByte:")
                }

                if(res1.isNotEmpty())
                    res1.deleteCharAt(res1.length - 1)

                return res1.toString()
            }
        }
        catch(ex: Exception)
        {

        }

        return "02:00:00:00:00:00"
    }

    private inner class WifiReceiver : BroadcastReceiver()
    {
        override fun onReceive(context: Context?, intent: Intent?)
        {
            scanResultList = wifiManager.scanResults
            if(scanResultList.isEmpty())
            {
                GlobalBus.publish(MainEvent.HideLoading())
                GlobalBus.publish(MainEvent.EnterSearchGatewayPage())
            }
            else
                getConnectedWiFiInfoTask()
        }
    }

    private fun getConnectedWiFiInfoTask()
    {
        LogUtil.d(TAG,"getConnectedWiFiInfoTask()")
        var neighborMAC = ""
        var devicesInfo: DevicesInfo
        val connManager = activity?.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI)
        if(networkInfo.isConnected)
        {
            val wifiManager = activity?.applicationContext?.getSystemService(Context.WIFI_SERVICE) as WifiManager
            connectionInfo = wifiManager.connectionInfo
            LogUtil.d(TAG, "Connected Info BSSID = ${connectionInfo.bssid}")

            P2PDevicesApi.GetDevicesInfo()
                    .setRequestPageName(TAG)
                    .setResponseListener(object: TUTKP2PResponseCallback()
                    {
                        override fun onSuccess(responseStr: String)
                        {
                            try
                            {
                                devicesInfo = Gson().fromJson(responseStr, DevicesInfo::class.javaObjectType)
                                LogUtil.d(TAG,"devicesInfo:$devicesInfo")

                                for(i in devicesInfo.Object.indices)
                                {
                                    if(devicesInfo.Object[i].HostName == "N/A")
                                        continue

                                    if( (devicesInfo.Object[i].PhysAddress == getMobileDeviceMacAddress()) && (devicesInfo.Object[i].X_ZYXEL_Neighbor != "") )
                                    {
                                        neighborMAC = devicesInfo.Object[i].X_ZYXEL_Neighbor
                                        mobileDeviceIndex = devicesInfo.Object[i].IndexFromFW
                                        LogUtil.e(TAG,"[1]mobileDeviceIndex:$mobileDeviceIndex")

                                        for(itemX in devicesInfo.Object)
                                        {
                                            if( (itemX.X_ZYXEL_CapabilityType == "L2Device") && (itemX.PhysAddress == neighborMAC) )
                                            {
                                                connectedModel = itemX.getName()
                                                LogUtil.d(TAG, "mobile device connected to $connectedModel")
                                            }
                                        }
                                    }
                                }

                                GlobalBus.publish(MainEvent.HideLoading())
                                updateWiFiInfoUI()
                            }
                            catch(e: JSONException)
                            {
                                e.printStackTrace()
                                GlobalBus.publish(MainEvent.HideLoading())
                                GlobalBus.publish(MainEvent.EnterSearchGatewayPage())
                            }
                        }
                    }).execute()
        }
    }

    private fun getRssiTask()
    {
        LogUtil.d(TAG,"getRssiTask()")
        var mobileMAC = ""
        var c2gRssi = 0

        mobileMAC = getMobileDeviceMacAddress()
        if( (mobileMAC != "") && (mobileMAC != "02:00:00:00:00:00") )
        {
            c2gRssi = 0

            LogUtil.e(TAG,"[2]mobileDeviceIndex:$mobileDeviceIndex")
            P2PGatewayApi.GetRssiInfo(mobileDeviceIndex)
                    .setRequestPageName(TAG)
                    .setResponseListener(object: TUTKP2PResponseCallback()
                    {
                        override fun onSuccess(responseStr: String)
                        {
                            LogUtil.d(TAG, "GetRssiInfo = $responseStr")
                            try
                            {
                                val data = JSONObject(responseStr)
                                c2gRssi = data.getJSONObject("Object").getInt("X_ZYXEL_RSSI")
                                LogUtil.d(TAG,"c2gRssi:$c2gRssi")

                                if(isStarted)
                                    updateSignalValue(c2gRssi.toFloat())
                            }
                            catch(e: JSONException)
                            {
                                e.printStackTrace()
                            }
                        }
                    }).execute()
        }
    }
}