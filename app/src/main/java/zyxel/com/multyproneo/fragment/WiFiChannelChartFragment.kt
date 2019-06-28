package zyxel.com.multyproneo.fragment

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.wifi.WifiManager
import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import kotlinx.android.synthetic.main.fragment_wifi_channel_chart.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import zyxel.com.multyproneo.R
import zyxel.com.multyproneo.event.DiagnosticEvent
import zyxel.com.multyproneo.event.GlobalBus
import zyxel.com.multyproneo.event.MainEvent
import zyxel.com.multyproneo.tool.DensityPixelConverter
import zyxel.com.multyproneo.wifichart.Band24GChannelChart
import zyxel.com.multyproneo.wifichart.Band5GWholeChannelChart
import zyxel.com.multyproneo.wifichart.SSIDAnalyzerWhole
import zyxel.com.multyproneo.wifichart.WiFiSignalWaveProfile
import java.util.*
import kotlin.concurrent.schedule

/**
 * Created by LouisTien on 2019/6/24.
 */
class WiFiChannelChartFragment : Fragment()
{
    private lateinit var activity: Activity
    private lateinit var wifiManager: WifiManager
    private val wifiReceiver = WifiReceiver()
    private lateinit var ssidAnalyzerWhole: SSIDAnalyzerWhole
    private val displaymetrics = DisplayMetrics()
    private var initialScan = false
    private var _24GBand = false
    private var _5GBand = false
    private var m5GConnectedSSIDMargin = 0
    private var mWiFiStrengthIndex = 0
    private var scrollPosition = 0
    private var height = 0
    private var width = 0
    private var densityDpi = 0
    private var downX = 0f
    private var downY = 0f
    private var upX = 0f
    private var upY = 0f
    private lateinit var getWiiInfoTimer: Timer
    private lateinit var band24GChannelChart: Band24GChannelChart
    private lateinit var band5GWholeChannelChart: Band5GWholeChannelChart
    private lateinit var wiFiSignalWaveProfileArrayList: ArrayList<WiFiSignalWaveProfile>
    private lateinit var _24GWiFiSignalWaveProfileArrayList: ArrayList<WiFiSignalWaveProfile>
    private lateinit var _5GWiFiSignalWaveProfileArrayList: ArrayList<WiFiSignalWaveProfile>
    private lateinit var _24GChannelsSignalWaveProfileArrayList: ArrayList<ArrayList<WiFiSignalWaveProfile>>
    private lateinit var _5GChannelsSignalWaveProfileArrayList: ArrayList<ArrayList<WiFiSignalWaveProfile>>
    private lateinit var saved24GChannelsSignalWaveProfileArrayList: ArrayList<ArrayList<WiFiSignalWaveProfile>>
    private lateinit var saved5GChannelsSignalWaveProfileArrayList: ArrayList<ArrayList<WiFiSignalWaveProfile>>

    override fun onAttach(activity: Activity)
    {
        super.onAttach(activity)
        this.activity = activity
        getActivity()!!.windowManager.defaultDisplay.getMetrics(displaymetrics)
        height = displaymetrics.heightPixels
        width = displaymetrics.widthPixels
        densityDpi = (displaymetrics.density * 160f).toInt()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
    {
        return inflater.inflate(R.layout.fragment_wifi_channel_chart, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)
        with(arguments){ this?.getInt("Channel")?.let{ mWiFiStrengthIndex = it } }
        setClickListener()
        ssidAnalyzerWhole = SSIDAnalyzerWhole(height, width, activity)
        wifiManager = getActivity()!!.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        if(densityDpi >= 640)
        {
            val densityPixelConverter = DensityPixelConverter()
            val params = LinearLayout.LayoutParams(ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT))
            params.setMargins(densityPixelConverter.convertDpToPixel(25f, displaymetrics).toInt(), densityPixelConverter.convertDpToPixel(30f, displaymetrics).toInt(), 0, 0)
            wifi_channel_strength_30db_text.layoutParams = params
            wifi_channel_strength_40db_text.layoutParams = params
            wifi_channel_strength_50db_text.layoutParams = params
            wifi_channel_strength_60db_text.layoutParams = params
            wifi_channel_strength_70db_text.layoutParams = params
            wifi_channel_strength_80db_text.layoutParams = params
            wifi_channel_strength_90db_text.layoutParams = params
        }
        wifi_channel_chart_horizontal_scroll.isHorizontalScrollBarEnabled = true
        wifi_channel_chart_horizontal_scroll.setOnTouchListener{ v, event ->
            val scrollX = v.scrollX
            val scrollY = v.scrollY

            var deltaY = 0f
            scrollPosition = scrollX

            when(event.action)
            {
                MotionEvent.ACTION_DOWN ->
                {
                    downX = event.x
                    downY = event.y
                }

                MotionEvent.ACTION_UP ->
                {
                    upX = event.x
                    upY = event.y

                    val deltaX = downX - upX
                    deltaY = downY - upY
                }
            }

            if(Math.abs(deltaY) > 10)
            {
                when(mWiFiStrengthIndex)
                {
                    0 ->
                    {
                        band24GChannelChart = Band24GChannelChart(getActivity()!!.applicationContext, activity)
                        band24GChannelChart.setStartEndPoint(width, height)
                        band24GChannelChart.setDataSet(saved24GChannelsSignalWaveProfileArrayList, scrollPosition)
                        wifi_channel_chart_horizontal_scroll.scrollTo(scrollPosition, 0)
                        GlobalBus.publish(MainEvent.ShowLoading())
                    }

                    1 ->
                    {
                        initialScan = false
                        band5GWholeChannelChart = Band5GWholeChannelChart(getActivity()!!.applicationContext, activity)
                        band5GWholeChannelChart.setStartEndPoint(width, height)
                        band5GWholeChannelChart.setDataSet(saved5GChannelsSignalWaveProfileArrayList, scrollPosition)
                        wifi_channel_chart_horizontal_scroll.scrollTo(scrollPosition, 0)
                        GlobalBus.publish(MainEvent.ShowLoading())
                    }
                }
            }

            false
        }
    }

    override fun onResume()
    {
        super.onResume()
        GlobalBus.publish(MainEvent.ShowLoading())
        initialScan = true
        checkFocusTab()
        getActivity()!!.applicationContext.registerReceiver(wifiReceiver, IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION))
        regularUpdateWifiInfo()
    }

    override fun onPause()
    {
        super.onPause()
    }

    override fun onDestroyView()
    {
        super.onDestroyView()
        initialScan = false
        getActivity()!!.applicationContext.unregisterReceiver(wifiReceiver)
    }

    private val clickListener = View.OnClickListener{ view ->
        clearTabTextsBackground()

        when(view)
        {
            wifi_channel_tab_24g_text ->
            {
                wifi_channel_tab_24g_text.setBackgroundResource(R.drawable.button_style_white_small_bg)
                if(mWiFiStrengthIndex != 0)
                    GlobalBus.publish(DiagnosticEvent.EnterWiFiChannelChartPage(0))
            }

            wifi_channel_tab_5g_text ->
            {
                wifi_channel_tab_5g_text.setBackgroundResource(R.drawable.button_style_white_small_bg)
                if(mWiFiStrengthIndex != 1)
                    GlobalBus.publish(DiagnosticEvent.EnterWiFiChannelChartPage(1))
            }
        }
    }

    private fun setClickListener()
    {
        wifi_channel_tab_24g_text.setOnClickListener(clickListener)
        wifi_channel_tab_5g_text.setOnClickListener(clickListener)
    }

    private fun clearTabTextsBackground()
    {
        wifi_channel_tab_24g_text.background = null
        wifi_channel_tab_5g_text.background = null
    }

    private fun checkFocusTab()
    {
        wifi_channel_tab_24g_text.background = null
        wifi_channel_tab_5g_text.background = null

        if(mWiFiStrengthIndex == 0)
            wifi_channel_tab_24g_text.setBackgroundResource(R.drawable.button_style_white_small_bg)
        else
            wifi_channel_tab_5g_text.setBackgroundResource(R.drawable.button_style_white_small_bg)
    }

    private fun regularUpdateWifiInfo()
    {
        getWiiInfoTimer = Timer()
        getWiiInfoTimer.schedule(1000, 10000){
            doAsync{ uiThread{ wifiManager.startScan() } }
        }
    }

    private inner class WifiReceiver : BroadcastReceiver()
    {
        override fun onReceive(context: Context?, intent: Intent?)
        {
            val scanResultList = wifiManager.scanResults
            wiFiSignalWaveProfileArrayList = ArrayList()
            _24GWiFiSignalWaveProfileArrayList = ArrayList()
            _5GWiFiSignalWaveProfileArrayList = ArrayList()

            var wiFiSignalWaveProfile: WiFiSignalWaveProfile
            for(i in scanResultList.indices)
            {
                wiFiSignalWaveProfile = WiFiSignalWaveProfile()
                with(wiFiSignalWaveProfile)
                {
                    ssid = scanResultList[i].SSID
                    frequency = scanResultList[i].frequency
                    rssi = scanResultList[i].level
                    amplitude = ssidAnalyzerWhole.getWiFiWaveAmpFromRssi(scanResultList[i].level)
                    channel = ssidAnalyzerWhole.getWiFiChannelFromFrequency(scanResultList[i].frequency)
                    bssid = scanResultList[i].BSSID
                }

                if(scanResultList[i].frequency > 5000)
                {
                    _5GBand = true
                    wiFiSignalWaveProfile.band = 1
                    wiFiSignalWaveProfile.channelMargin = ssidAnalyzerWhole.getWiFiChannelMarginFromFrequency(scanResultList[i].frequency)
                }
                else
                {
                    _24GBand = true
                    wiFiSignalWaveProfile.channelMargin = ssidAnalyzerWhole.getWiFiChannelMarginFromFrequency(scanResultList[i].frequency)
                    wiFiSignalWaveProfile.band = 0
                }

                wiFiSignalWaveProfileArrayList.add(wiFiSignalWaveProfile)
            }

            for(i in wiFiSignalWaveProfileArrayList.indices)
            {
                if(wiFiSignalWaveProfileArrayList[i].band == 0)
                    _24GWiFiSignalWaveProfileArrayList.add(wiFiSignalWaveProfileArrayList[i])
                else
                    _5GWiFiSignalWaveProfileArrayList.add(wiFiSignalWaveProfileArrayList[i])
            }

            wifi_channel_tab_24g_text.isEnabled = false
            wifi_channel_tab_5g_text.isEnabled = false
            wifi_channel_tab_24g_text.alpha = 1f
            wifi_channel_tab_5g_text.alpha = 1f

            if(_24GBand)
            {
                wifi_channel_tab_24g_text.isEnabled = true
                wifi_channel_tab_24g_text.alpha = 1f
            }

            if(_5GBand)
            {
                wifi_channel_tab_5g_text.isEnabled = true
                wifi_channel_tab_5g_text.alpha = 1f
            }

            when(mWiFiStrengthIndex)
            {
                0 ->
                {
                    wifi_channel_chart_image.setImageResource(R.drawable.advanced_tools_bg)
                    wifi_channel_chart_horizontal_scroll.isEnabled = true
                    _24GChannelsSignalWaveProfileArrayList = ssidAnalyzerWhole.getAllChannelWiFiSignalWaveProfileByBand(0, _24GWiFiSignalWaveProfileArrayList)
                    saved24GChannelsSignalWaveProfileArrayList = _24GChannelsSignalWaveProfileArrayList

                    band24GChannelChart = Band24GChannelChart(getActivity()!!.applicationContext, activity)
                    band24GChannelChart.setStartEndPoint(width, height)
                    band24GChannelChart.setDataSet(_24GChannelsSignalWaveProfileArrayList, scrollPosition)
                    wifi_channel_chart_linear.removeAllViews()
                    wifi_channel_chart_linear.addView(band24GChannelChart)
                    wifi_channel_chart_horizontal_scroll.bringToFront()
                    wifi_channel_chart_horizontal_scroll.scrollTo(scrollPosition, 0)
                }

                1 ->
                {
                    if(densityDpi >= 600)
                        wifi_channel_chart_image.setImageResource(R.drawable.advanced_tools_bg_phone_560)
                    else
                        wifi_channel_chart_image.setImageResource(R.drawable.advanced_tools_bg_phone)

                    wifi_channel_chart_horizontal_scroll.isEnabled = true
                    wifi_channel_chart_horizontal_scroll.visibility = View.VISIBLE
                    ssidAnalyzerWhole.getAllChannelWiFiSignalWaveProfileByBand(1, _5GWiFiSignalWaveProfileArrayList)

                    val tmp1 = ssidAnalyzerWhole.get5GChannelsSignalWaveProfileArrayListByBand(1)
                    val tmp2 = ssidAnalyzerWhole.get5GChannelsSignalWaveProfileArrayListByBand(2)
                    val tmp3 = ssidAnalyzerWhole.get5GChannelsSignalWaveProfileArrayListByBand(3)
                    val tmp4 = ssidAnalyzerWhole.get5GChannelsSignalWaveProfileArrayListByBand(4)

                    _5GChannelsSignalWaveProfileArrayList = ArrayList()
                    for(i in tmp1.indices)
                        _5GChannelsSignalWaveProfileArrayList.add(tmp1[i])

                    for(i in tmp2.indices)
                        _5GChannelsSignalWaveProfileArrayList.add(tmp2[i])

                    for(i in tmp3.indices)
                        _5GChannelsSignalWaveProfileArrayList.add(tmp3[i])

                    for(i in tmp4.indices)
                        _5GChannelsSignalWaveProfileArrayList.add(tmp4[i])

                    saved5GChannelsSignalWaveProfileArrayList = _5GChannelsSignalWaveProfileArrayList
                    band5GWholeChannelChart = Band5GWholeChannelChart(getActivity()!!.applicationContext, activity)
                    band5GWholeChannelChart.setStartEndPoint(width, height)
                    if(initialScan)
                        band5GWholeChannelChart.setDataSet(_5GChannelsSignalWaveProfileArrayList, m5GConnectedSSIDMargin)
                    else
                        band5GWholeChannelChart.setDataSet(_5GChannelsSignalWaveProfileArrayList, scrollPosition)

                    wifi_channel_chart_linear.removeAllViews()
                    wifi_channel_chart_linear.addView(band5GWholeChannelChart)
                    wifi_channel_chart_horizontal_scroll.bringToFront()
                    if(initialScan)
                        wifi_channel_chart_horizontal_scroll.scrollTo(m5GConnectedSSIDMargin, 0)
                    else
                        wifi_channel_chart_horizontal_scroll.scrollTo(scrollPosition, 0)
                }
            }
            GlobalBus.publish(MainEvent.HideLoading())
        }
    }
}