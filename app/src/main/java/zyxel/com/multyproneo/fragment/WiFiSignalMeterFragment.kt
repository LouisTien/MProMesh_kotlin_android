package zyxel.com.multyproneo.fragment

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.wifi.ScanResult
import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.RotateAnimation
import androidx.fragment.app.Fragment
import com.google.gson.Gson
import kotlinx.android.synthetic.main.fragment_wifi_signal_meter.*
import org.jetbrains.anko.support.v4.runOnUiThread
import org.json.JSONException
import org.json.JSONObject
import zyxel.com.multyproneo.R
import zyxel.com.multyproneo.api.Commander
import zyxel.com.multyproneo.api.DevicesApi
import zyxel.com.multyproneo.api.GatewayApi
import zyxel.com.multyproneo.event.GlobalBus
import zyxel.com.multyproneo.event.MainEvent
import zyxel.com.multyproneo.model.DevicesInfo
import zyxel.com.multyproneo.tool.SpecialCharacterHandler
import zyxel.com.multyproneo.util.GlobalData
import zyxel.com.multyproneo.util.LogUtil
import java.net.NetworkInterface
import java.util.*
import kotlin.concurrent.schedule

/**
 * Created by LouisTien on 2019/6/26.
 */
class WiFiSignalMeterFragment : Fragment()
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
            if(wifi_signal_start_text.text == resources.getString(R.string.common_start))
            {
                wifi_signal_start_text.text = resources.getString(R.string.diagnostic_wifi_signal_stop)
                wifi_signal_info_strength_text.text = ""
                startRegularUpdateRssi()
            }
            else
            {
                wifi_signal_start_text.text = resources.getString(R.string.common_start)
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
        if(!checkCurrentPage()) return

        runOnUiThread{
            wifi_signal_info_strength_text.text = ""
            wifi_signal_info_strength_unit_text.visibility = View.INVISIBLE
            animatePointer(wifi_signal_pointer_view, maxValue, minValue, -70f, startDegree, endDegree)
        }
    }

    private fun updateWiFiInfoUI()
    {
        if(!checkCurrentPage()) return

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
        if(!checkCurrentPage()) return

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

    private fun checkCurrentPage(): Boolean
    {
        if(GlobalData.currentFrag != "DiagnosticFragment") return false
        if(GlobalData.diagnosticCurrentFrag != "WiFiSignalMeterFragment") return false
        if(!isVisible) return false
        return true
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
        var devicesInfo: DevicesInfo
        val connManager = activity?.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI)
        if(networkInfo.isConnected)
        {
            val wifiManager = activity?.applicationContext?.getSystemService(Context.WIFI_SERVICE) as WifiManager
            connectionInfo = wifiManager.connectionInfo
            LogUtil.d(TAG, "Connected Info BSSID = ${connectionInfo.bssid}")

            DevicesApi.GetDevicesInfo()
                    .setRequestPageName(TAG)
                    .setResponseListener(object: Commander.ResponseListener()
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
                                        mobileDeviceIndex = i + 1

                                        for(itemX in devicesInfo.Object)
                                        {
                                            if((itemX.X_ZYXEL_CapabilityType == "L2Device"))
                                            {
                                                //if mac = 00:11:22:33:44:50, check the mac 00:11:22:33:44:5 to compare for work around of FW bug#130534

                                                var neighborSubMAC = ""
                                                var deviceSubMAC = ""

                                                if(devicesInfo.Object[i].X_ZYXEL_Neighbor.contains(":")
                                                    && ( (devicesInfo.Object[i].X_ZYXEL_Neighbor.lastIndexOf(":") + 2) == (devicesInfo.Object[i].X_ZYXEL_Neighbor.length - 1)) )
                                                {
                                                    neighborSubMAC = devicesInfo.Object[i].X_ZYXEL_Neighbor.substring(0, devicesInfo.Object[i].X_ZYXEL_Neighbor.lastIndexOf(":") + 2)
                                                    LogUtil.d(TAG,"neighborSubMAC:$neighborSubMAC")
                                                }

                                                if(itemX.PhysAddress.contains(":")
                                                    && ( (itemX.PhysAddress.lastIndexOf(":") + 2) == (itemX.PhysAddress.length - 1)) )
                                                {
                                                    deviceSubMAC = itemX.PhysAddress.substring(0, itemX.PhysAddress.lastIndexOf(":") + 2)
                                                    LogUtil.d(TAG,"deviceSubMAC:$deviceSubMAC")
                                                }

                                                if(neighborSubMAC != "" && deviceSubMAC != "")
                                                {
                                                    if(neighborSubMAC.equals(deviceSubMAC, ignoreCase = true))
                                                        connectedModel = SpecialCharacterHandler.checkEmptyTextValue(itemX.getName())
                                                }
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

            GatewayApi.GetRssiInfo(mobileDeviceIndex)
                    .setRequestPageName(TAG)
                    .setResponseListener(object: Commander.ResponseListener()
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