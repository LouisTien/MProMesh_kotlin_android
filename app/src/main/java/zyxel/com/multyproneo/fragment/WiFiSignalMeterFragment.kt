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
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.RotateAnimation
import kotlinx.android.synthetic.main.fragment_wifi_signal_meter.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import zyxel.com.multyproneo.R
import zyxel.com.multyproneo.event.GlobalBus
import zyxel.com.multyproneo.event.MainEvent
import java.net.NetworkInterface
import java.util.*
import kotlin.concurrent.schedule

/**
 * Created by LouisTien on 2019/6/26.
 */
class WiFiSignalMeterFragment : Fragment()
{
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
    }

    override fun onDestroyView()
    {
        super.onDestroyView()
        getRssiTimer.cancel()
        activity?.applicationContext?.unregisterReceiver(wifiReceiver)
    }

    private fun startRegularUpdateRssi()
    {
        getRssiTimer = Timer()
        getRssiTimer.schedule(1000, 5000){ getRssiTask() }
    }

    private fun stopRegularUpdateRssi()
    {
        getRssiTimer.cancel()
        setDefaultPointerStatus()
    }

    private fun setDefaultPointerStatus()
    {
        wifi_signal_info_strength_text.text = ""
        wifi_signal_info_strength_unit_text.visibility = View.INVISIBLE
        animatePointer(wifi_signal_pointer_view, maxValue, minValue, -70f, startDegree, endDegree)
    }

    private fun updateSignalValue(value: Float)
    {
        wifi_signal_info_strength_text.text = value.toString()
        wifi_signal_info_strength_unit_text.visibility = View.VISIBLE

        var newValue = value

        if(newValue < -100f)
            newValue = -100f

        if(newValue > -40f)
            newValue = -40f

        animatePointer(wifi_signal_pointer_view, maxValue, minValue, newValue, startDegree, endDegree)
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
        /*var ssid = ""
        var neighborMAC = ""
        var connectedModel = ""
        var endDeviceProfileArrayList: MutableList<EndDeviceProfile>
        var result = false

        doAsync{
            val connManager = activity?.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val networkInfo = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI)
            if(networkInfo.isConnected)
            {
                val wifiManager = activity?.applicationContext?.getSystemService(Context.WIFI_SERVICE) as WifiManager
                connectionInfo = wifiManager.connectionInfo
                ssid = connectionInfo.ssid
                endDeviceProfileArrayList = mutableListOf<EndDeviceProfile>(
                        EndDeviceProfile(
                                UserDefineName = "Access Point",
                                Name = "WAP6804-AP-40619",
                                MAC = "5c:6a:80:e1:33:27",
                                Active = "Connect",
                                Blocking = "Non-Blocking",
                                IPAddress = "192.168.1.151",
                                ConnectionType = "Other",
                                CapabilityType = "L2Device",
                                SoftwareVersion = "1.00(ABKH.6)C0",
                                HostType = 3,
                                L2AutoConfigEnable = 3,
                                L2WifiStatus = 1,
                                Neighbor = "unknown",
                                Manufacturer = "unknown",
                                Channel24G = 11,
                                Channel5G = 157,
                                DeviceMode = "AP",
                                NewDeviceFlag = 1,
                                InternetAccess = "Non-Blocking",
                                RssiRealFlag = 1,
                                RssiValue = "Good",
                                dhcpLeaseTime = "1559608754"
                        ),
                        EndDeviceProfile(
                                UserDefineName = "Louis-LG V20",
                                Name = "android-e5784d2ba14b34c5",
                                MAC = "40:4e:36:b3:fd:15",
                                Active = "Connect",
                                Blocking = "Non-Blocking",
                                IPAddress = "192.168.1.133",
                                ConnectionType = "WiFi",
                                CapabilityType = "Client",
                                HostType = 1,
                                SignalStrength = 5,
                                PhyRate = 846,
                                Neighbor = "gateway",
                                Manufacturer = "unknown",
                                Rssi = -29,
                                Band = 2,
                                Channel5G = 36,
                                InternetAccess = "Non-Blocking",
                                RssiRealFlag = 1,
                                RssiValue = "TooClose",
                                dhcpLeaseTime = "1559608754"
                        ),
                        EndDeviceProfile(
                                UserDefineName = "Louis-LG V21",
                                Name = "android-e5784d2ba14b34c7",
                                MAC = "40:4e:36:b3:fd:17",
                                Active = "Connect",
                                Blocking = "Non-Blocking",
                                IPAddress = "192.168.1.135",
                                ConnectionType = "WiFi",
                                CapabilityType = "Client",
                                HostType = 1,
                                SignalStrength = 5,
                                PhyRate = 846,
                                Neighbor = "gateway",
                                Manufacturer = "unknown",
                                Rssi = -29,
                                Band = 2,
                                Channel5G = 36,
                                InternetAccess = "Non-Blocking",
                                RssiRealFlag = 1,
                                RssiValue = "TooClose",
                                dhcpLeaseTime = "1559608754"
                        ),
                        EndDeviceProfile(
                                UserDefineName = "ASUS_Phone",
                                Name = "ASUS_Phone",
                                MAC = "4c:ed:fb:4f:84:9c",
                                Active = "Connect",
                                Blocking = "Non-Blocking",
                                IPAddress = "192.168.1.205",
                                ConnectionType = "WiFi",
                                CapabilityType = "Client",
                                HostType = 1,
                                SignalStrength = 5,
                                PhyRate = 228,
                                Neighbor = "gateway",
                                GuestGroup = 1,
                                Manufacturer = "unknown",
                                Rssi = -29,
                                Band = 2,
                                Channel5G = 36,
                                InternetAccess = "Non-Blocking",
                                RssiRealFlag = 1,
                                RssiValue = "Good",
                                dhcpLeaseTime = "1561534851"
                        )
                )

                for(i in endDeviceProfileArrayList.indices)
                {
                    if(endDeviceProfileArrayList[i].Neighbor != "")
                    {
                        neighborMAC = endDeviceProfileArrayList[i].Neighbor
                        for(j in endDeviceProfileArrayList.indices)
                        {
                            if(endDeviceProfileArrayList[j].CapabilityType == "L2Device" && endDeviceProfileArrayList[j].MAC == neighborMAC)
                                connectedModel = endDeviceProfileArrayList[j].Name
                        }
                    }
                }
                result = true
            }

            uiThread{
                GlobalBus.publish(MainEvent.HideLoading())
                if(result)
                {
                    for(i in scanResultList.indices)
                    {
                        if(scanResultList[i].BSSID.equals(connectionInfo.bssid, ignoreCase = true))
                        {
                            c2gRssi = scanResultList[i].level
                            frequency = scanResultList[i].frequency
                            wifi_signal_info_band_text.text = if(frequency > 5000) "5GHz" else "2.4GHz"
                        }
                    }

                    wifi_signal_info_name_text.text = ssid.substring(1, ssid.length - 1)
                    //wifi_signal_info_connect_text.text = if(connectedModel == "") GlobalData.getCurrentGatewayInfo().userDefineName else connectedModel
                }
                else
                    GlobalBus.publish(MainEvent.EnterSearchGatewayPage())
            }
        }*/
    }

    fun getRssiTask()
    {
        var mobileMAC = ""
        var c2gRssi = 0

        doAsync{
            mobileMAC = getMobileDeviceMacAddress()
            if(mobileMAC.equals("", ignoreCase = true))
            {

            }
            else if(mobileMAC.equals("02:00:00:00:00:00", ignoreCase = true))
            {

            }
            else
            {
                c2gRssi = -64
            }

            uiThread{ updateSignalValue(c2gRssi.toFloat()) }
        }
    }
}