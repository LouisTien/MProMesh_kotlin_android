package zyxel.com.multyproneo.wifichart

import android.app.Activity
import android.graphics.Color
import android.util.DisplayMetrics
import zyxel.com.multyproneo.util.LogUtil
import java.util.ArrayList

/**
 * Created by LouisTien on 2019/6/21.
 */
class SSIDAnalyzerWhole(private val height: Int, private val width: Int, private val activity: Activity)
{
    private val TAG = javaClass.simpleName
    private var _24GChannelMargin = width / 18
    private var _5GChannelMargin = width / 6
    private var displayMetrics = activity.resources.displayMetrics
    private lateinit var _24GChannelsSignalWaveProfileArrayList: ArrayList<ArrayList<WiFiSignalWaveProfile>>
    private lateinit var _5GChannelsSignalWaveProfileArrayList: ArrayList<ArrayList<WiFiSignalWaveProfile>>
    private lateinit var all5GBandChannelsSignalWaveProfileArrayList: ArrayList<ArrayList<ArrayList<WiFiSignalWaveProfile>>>

    private lateinit var channel1WiFiSignalWaveProfileArrayList: ArrayList<WiFiSignalWaveProfile>
    private lateinit var channel2WiFiSignalWaveProfileArrayList: ArrayList<WiFiSignalWaveProfile>
    private lateinit var channel3WiFiSignalWaveProfileArrayList: ArrayList<WiFiSignalWaveProfile>
    private lateinit var channel4WiFiSignalWaveProfileArrayList: ArrayList<WiFiSignalWaveProfile>
    private lateinit var channel5WiFiSignalWaveProfileArrayList: ArrayList<WiFiSignalWaveProfile>
    private lateinit var channel6WiFiSignalWaveProfileArrayList: ArrayList<WiFiSignalWaveProfile>
    private lateinit var channel7WiFiSignalWaveProfileArrayList: ArrayList<WiFiSignalWaveProfile>
    private lateinit var channel8WiFiSignalWaveProfileArrayList: ArrayList<WiFiSignalWaveProfile>
    private lateinit var channel9WiFiSignalWaveProfileArrayList: ArrayList<WiFiSignalWaveProfile>
    private lateinit var channel10WiFiSignalWaveProfileArrayList: ArrayList<WiFiSignalWaveProfile>
    private lateinit var channel11WiFiSignalWaveProfileArrayList: ArrayList<WiFiSignalWaveProfile>
    private lateinit var channel12WiFiSignalWaveProfileArrayList: ArrayList<WiFiSignalWaveProfile>
    private lateinit var channel13WiFiSignalWaveProfileArrayList: ArrayList<WiFiSignalWaveProfile>
    private lateinit var channel14WiFiSignalWaveProfileArrayList: ArrayList<WiFiSignalWaveProfile>

    private lateinit var channel36WiFiSignalWaveProfileArrayList: ArrayList<WiFiSignalWaveProfile>
    private lateinit var channel38WiFiSignalWaveProfileArrayList: ArrayList<WiFiSignalWaveProfile>
    private lateinit var channel40WiFiSignalWaveProfileArrayList: ArrayList<WiFiSignalWaveProfile>
    private lateinit var channel42WiFiSignalWaveProfileArrayList: ArrayList<WiFiSignalWaveProfile>
    private lateinit var channel44WiFiSignalWaveProfileArrayList: ArrayList<WiFiSignalWaveProfile>
    private lateinit var channel46WiFiSignalWaveProfileArrayList: ArrayList<WiFiSignalWaveProfile>
    private lateinit var channel48WiFiSignalWaveProfileArrayList: ArrayList<WiFiSignalWaveProfile>

    private lateinit var channel50WiFiSignalWaveProfileArrayList: ArrayList<WiFiSignalWaveProfile>
    private lateinit var channel52WiFiSignalWaveProfileArrayList: ArrayList<WiFiSignalWaveProfile>
    private lateinit var channel54WiFiSignalWaveProfileArrayList: ArrayList<WiFiSignalWaveProfile>
    private lateinit var channel56WiFiSignalWaveProfileArrayList: ArrayList<WiFiSignalWaveProfile>
    private lateinit var channel58WiFiSignalWaveProfileArrayList: ArrayList<WiFiSignalWaveProfile>
    private lateinit var channel60WiFiSignalWaveProfileArrayList: ArrayList<WiFiSignalWaveProfile>
    private lateinit var channel62WiFiSignalWaveProfileArrayList: ArrayList<WiFiSignalWaveProfile>
    private lateinit var channel64WiFiSignalWaveProfileArrayList: ArrayList<WiFiSignalWaveProfile>
    private lateinit var channel66WiFiSignalWaveProfileArrayList: ArrayList<WiFiSignalWaveProfile>

    private lateinit var channel100WiFiSignalWaveProfileArrayList: ArrayList<WiFiSignalWaveProfile>
    private lateinit var channel102WiFiSignalWaveProfileArrayList: ArrayList<WiFiSignalWaveProfile>
    private lateinit var channel104WiFiSignalWaveProfileArrayList: ArrayList<WiFiSignalWaveProfile>
    private lateinit var channel106WiFiSignalWaveProfileArrayList: ArrayList<WiFiSignalWaveProfile>
    private lateinit var channel108WiFiSignalWaveProfileArrayList: ArrayList<WiFiSignalWaveProfile>
    private lateinit var channel110WiFiSignalWaveProfileArrayList: ArrayList<WiFiSignalWaveProfile>
    private lateinit var channel112WiFiSignalWaveProfileArrayList: ArrayList<WiFiSignalWaveProfile>
    private lateinit var channel114WiFiSignalWaveProfileArrayList: ArrayList<WiFiSignalWaveProfile>
    private lateinit var channel116WiFiSignalWaveProfileArrayList: ArrayList<WiFiSignalWaveProfile>
    private lateinit var channel118WiFiSignalWaveProfileArrayList: ArrayList<WiFiSignalWaveProfile>
    private lateinit var channel120WiFiSignalWaveProfileArrayList: ArrayList<WiFiSignalWaveProfile>
    private lateinit var channel122WiFiSignalWaveProfileArrayList: ArrayList<WiFiSignalWaveProfile>
    private lateinit var channel124WiFiSignalWaveProfileArrayList: ArrayList<WiFiSignalWaveProfile>
    private lateinit var channel126WiFiSignalWaveProfileArrayList: ArrayList<WiFiSignalWaveProfile>
    private lateinit var channel128WiFiSignalWaveProfileArrayList: ArrayList<WiFiSignalWaveProfile>
    private lateinit var channel130WiFiSignalWaveProfileArrayList: ArrayList<WiFiSignalWaveProfile>
    private lateinit var channel132WiFiSignalWaveProfileArrayList: ArrayList<WiFiSignalWaveProfile>
    private lateinit var channel134WiFiSignalWaveProfileArrayList: ArrayList<WiFiSignalWaveProfile>
    private lateinit var channel136WiFiSignalWaveProfileArrayList: ArrayList<WiFiSignalWaveProfile>
    private lateinit var channel138WiFiSignalWaveProfileArrayList: ArrayList<WiFiSignalWaveProfile>
    private lateinit var channel140WiFiSignalWaveProfileArrayList: ArrayList<WiFiSignalWaveProfile>
    private lateinit var channel142WiFiSignalWaveProfileArrayList: ArrayList<WiFiSignalWaveProfile>


    private lateinit var channel149WiFiSignalWaveProfileArrayList: ArrayList<WiFiSignalWaveProfile>
    private lateinit var channel151WiFiSignalWaveProfileArrayList: ArrayList<WiFiSignalWaveProfile>
    private lateinit var channel153WiFiSignalWaveProfileArrayList: ArrayList<WiFiSignalWaveProfile>
    private lateinit var channel155WiFiSignalWaveProfileArrayList: ArrayList<WiFiSignalWaveProfile>
    private lateinit var channel157WiFiSignalWaveProfileArrayList: ArrayList<WiFiSignalWaveProfile>
    private lateinit var channel159WiFiSignalWaveProfileArrayList: ArrayList<WiFiSignalWaveProfile>
    private lateinit var channel161WiFiSignalWaveProfileArrayList: ArrayList<WiFiSignalWaveProfile>
    private lateinit var channel163WiFiSignalWaveProfileArrayList: ArrayList<WiFiSignalWaveProfile>
    private lateinit var channel165WiFiSignalWaveProfileArrayList: ArrayList<WiFiSignalWaveProfile>

    private lateinit var _5GBand1ChannelsSignalWaveProfileArrayList: ArrayList<ArrayList<WiFiSignalWaveProfile>>
    private lateinit var _5GBand2ChannelsSignalWaveProfileArrayList: ArrayList<ArrayList<WiFiSignalWaveProfile>>
    private lateinit var _5GBand3ChannelsSignalWaveProfileArrayList: ArrayList<ArrayList<WiFiSignalWaveProfile>>
    private lateinit var _5GBand4ChannelsSignalWaveProfileArrayList: ArrayList<ArrayList<WiFiSignalWaveProfile>>

    fun getWiFiWaveAmpFromRssi(rssi: Int): Double
    {
        var amp = 0.15 // 0.1
        if(rssi > -100 && rssi <= -95)
        {
            amp = 0.15 // 0.1
        }
        else if(rssi > -95 && rssi <= -90)
        {
            amp = 0.225 // 0.15
        }
        else if(rssi > -90 && rssi <= -85)
        {
            amp = 0.3 // 0.2
        }
        else if(rssi > -85 && rssi <= -80)
        {
            amp = 0.375 // 0.25
        }
        else if(rssi > -80 && rssi <= -75)
        {
            amp = 0.45 // 0.3
        }
        else if(rssi > -75 && rssi <= -70)
        {
            amp = 0.525 // 0.35
        }
        else if(rssi > -70 && rssi <= -65)
        {
            amp = 0.6 // 0.4
        }
        else if(rssi > -65 && rssi <= -60)
        {
            amp = 0.675 // 0.45
        }
        else if(rssi > -60 && rssi <= -55)
        {
            amp = 0.75 // 0.5
        }
        else if(rssi > -55 && rssi <= -50)
        {
            amp = 0.825 // 0.55
        }
        else if(rssi > -50 && rssi <= -45)
        {
            amp = 0.9 // 0.6
        }
        else if(rssi > -45 && rssi <= -40)
        {
            amp = 0.975 // 0.65
        }
        else if(rssi > -40 && rssi <= -35)
        {
            amp = 1.05 // 0.7
        }
        else if(rssi > -35 && rssi <= -30)
        {
            amp = 1.125 // 0.75
        }
        else if(rssi > -30 && rssi <= -25)
        {
            amp = 1.2 // 0.8
        }
        else if(rssi > -15 && rssi <= -20)
        {
            amp = 1.275 // 0.85
        }
        else if(rssi > -20 && rssi <= -15)
        {
            amp = 1.35 // 0.9
        }
        return amp
    }

    fun getWiFiChannelFromFrequency(frequency: Int): Int
    {
        var channel = 0
        if(frequency > 5000)
        {
            when(frequency)
            {
                5180 -> channel = 36
                5190 -> channel = 38
                5200 -> channel = 40
                5210 -> channel = 42
                5220 -> channel = 44
                5230 -> channel = 46
                5240 -> channel = 48
                5250 -> channel = 50
                5260 -> channel = 52
                5270 -> channel = 54
                5280 -> channel = 56
                5290 -> channel = 58
                5300 -> channel = 60
                5310 -> channel = 62
                5320 -> channel = 64
                5330 -> channel = 66
                5500 -> channel = 100
                5510 -> channel = 102
                5520 -> channel = 104
                5530 -> channel = 106
                5540 -> channel = 108
                5550 -> channel = 110
                5560 -> channel = 112
                5570 -> channel = 114
                5580 -> channel = 116
                5590 -> channel = 118
                5600 -> channel = 120
                5610 -> channel = 122
                5620 -> channel = 124
                5630 -> channel = 126
                5640 -> channel = 128
                5650 -> channel = 130
                5660 -> channel = 132
                5670 -> channel = 134
                5680 -> channel = 136
                5690 -> channel = 138
                5700 -> channel = 140
                5710 -> channel = 142
                5745 -> channel = 149
                5755 -> channel = 151
                5765 -> channel = 153
                5775 -> channel = 155
                5785 -> channel = 157
                5795 -> channel = 159
                5805 -> channel = 161
                5815 -> channel = 163
                5825 -> channel = 165
            }
        }
        else
        {
            when(frequency)
            {
                2412 -> channel = 1
                2417 -> channel = 2
                2422 -> channel = 3
                2427 -> channel = 4
                2432 -> channel = 5
                2437 -> channel = 6
                2442 -> channel = 7
                2447 -> channel = 8
                2452 -> channel = 9
                2457 -> channel = 10
                2462 -> channel = 11
                2467 -> channel = 12
                2472 -> channel = 13
                2484 -> channel = 14
            }
        }
        return channel
    }

    fun getWiFiChannelMarginFromFrequency(frequency: Int): Int
    {
        var margin = 0
        if(frequency > 5000)
        {
            when(frequency)
            {
                5180 //36
                -> margin = 0
                5190 //38
                -> margin = _5GChannelMargin * 1
                5200 //40
                -> margin = _5GChannelMargin * 2
                5210 //42
                -> margin = _5GChannelMargin * 3
                5220 //44
                -> margin = _5GChannelMargin * 4
                5230 //46
                -> margin = _5GChannelMargin * 5
                5240 //48
                -> margin = _5GChannelMargin * 6
                5250 //50
                -> margin = _5GChannelMargin * 7
                5260 //52
                -> margin = _5GChannelMargin * 8
                5270 //54
                -> margin = _5GChannelMargin * 9
                5280 //56
                -> margin = _5GChannelMargin * 10
                5290 //58
                -> margin = _5GChannelMargin * 11
                5300 //60
                -> margin = _5GChannelMargin * 12
                5310 //62
                -> margin = _5GChannelMargin * 13
                5320 //64
                -> margin = _5GChannelMargin * 14
                5330 //66
                -> margin = _5GChannelMargin * 15
                5500 //100
                -> margin = _5GChannelMargin * 16
                5510 //102
                -> margin = _5GChannelMargin * 17
                5520 //104
                -> margin = _5GChannelMargin * 18
                5530 //106
                -> margin = _5GChannelMargin * 19
                5540 //108
                -> margin = _5GChannelMargin * 20
                5550 //110
                -> margin = _5GChannelMargin * 21
                5560 //112
                -> margin = _5GChannelMargin * 22
                5570 //114
                -> margin = _5GChannelMargin * 23
                5580 //116
                -> margin = _5GChannelMargin * 24
                5590 //118
                -> margin = _5GChannelMargin * 25
                5600 //120
                -> margin = _5GChannelMargin * 26
                5610 //122
                -> margin = _5GChannelMargin * 27
                5620 //124
                -> margin = _5GChannelMargin * 28
                5630 //126
                -> margin = _5GChannelMargin * 29
                5640 //128
                -> margin = _5GChannelMargin * 30
                5650 //130
                -> margin = _5GChannelMargin * 31
                5660 //132
                -> margin = _5GChannelMargin * 32
                5670 //134
                -> margin = _5GChannelMargin * 33
                5680 //136
                -> margin = _5GChannelMargin * 34
                5690 //138
                -> margin = _5GChannelMargin * 35
                5700 //140
                -> margin = _5GChannelMargin * 36
                5710 //142
                -> margin = _5GChannelMargin * 37
                5745 //149
                -> margin = _5GChannelMargin * 38
                5755 //151
                -> margin = _5GChannelMargin * 39
                5765 //153
                -> margin = _5GChannelMargin * 40
                5775 //155
                -> margin = _5GChannelMargin * 41
                5785 //157
                -> margin = _5GChannelMargin * 42
                5795 //159
                -> margin = _5GChannelMargin * 43
                5805 //161
                -> margin = _5GChannelMargin * 44
                5815 //163
                -> margin = _5GChannelMargin * 45
                5825 //165
                -> margin = _5GChannelMargin * 46
            }
        }
        else
        {
            when(frequency)
            {
                2412 -> margin = 0
                2417 -> margin = _24GChannelMargin * 1
                2422 -> margin = _24GChannelMargin * 2
                2427 -> margin = _24GChannelMargin * 3
                2432 -> margin = _24GChannelMargin * 4
                2437 -> margin = _24GChannelMargin * 5
                2442 -> margin = _24GChannelMargin * 6
                2447 -> margin = _24GChannelMargin * 7
                2452 -> margin = _24GChannelMargin * 8
                2457 -> margin = _24GChannelMargin * 9
                2462 -> margin = _24GChannelMargin * 10
                2467 -> margin = _24GChannelMargin * 11
                2472 -> margin = _24GChannelMargin * 12
                2484 -> margin = _24GChannelMargin * 13
            }
        }
        return margin
    }

    fun getAllChannelWiFiSiganlWaveProfileByBand(band: Int, scanResults: ArrayList<WiFiSignalWaveProfile>): ArrayList<ArrayList<WiFiSignalWaveProfile>>
    {
        var results = ArrayList<ArrayList<WiFiSignalWaveProfile>>()
        _24GChannelsSignalWaveProfileArrayList = ArrayList()
        _5GChannelsSignalWaveProfileArrayList = ArrayList()
        channel1WiFiSignalWaveProfileArrayList = ArrayList()
        channel2WiFiSignalWaveProfileArrayList = ArrayList()
        channel3WiFiSignalWaveProfileArrayList = ArrayList()
        channel4WiFiSignalWaveProfileArrayList = ArrayList()
        channel5WiFiSignalWaveProfileArrayList = ArrayList()
        channel6WiFiSignalWaveProfileArrayList = ArrayList()
        channel7WiFiSignalWaveProfileArrayList = ArrayList()
        channel8WiFiSignalWaveProfileArrayList = ArrayList()
        channel9WiFiSignalWaveProfileArrayList = ArrayList()
        channel10WiFiSignalWaveProfileArrayList = ArrayList()
        channel11WiFiSignalWaveProfileArrayList = ArrayList()
        channel12WiFiSignalWaveProfileArrayList = ArrayList()
        channel13WiFiSignalWaveProfileArrayList = ArrayList()
        channel14WiFiSignalWaveProfileArrayList = ArrayList()

        channel36WiFiSignalWaveProfileArrayList = ArrayList()
        channel38WiFiSignalWaveProfileArrayList = ArrayList()
        channel40WiFiSignalWaveProfileArrayList = ArrayList()
        channel42WiFiSignalWaveProfileArrayList = ArrayList()
        channel44WiFiSignalWaveProfileArrayList = ArrayList()
        channel46WiFiSignalWaveProfileArrayList = ArrayList()
        channel48WiFiSignalWaveProfileArrayList = ArrayList()
        channel50WiFiSignalWaveProfileArrayList = ArrayList()

        channel52WiFiSignalWaveProfileArrayList = ArrayList()
        channel54WiFiSignalWaveProfileArrayList = ArrayList()
        channel56WiFiSignalWaveProfileArrayList = ArrayList()
        channel58WiFiSignalWaveProfileArrayList = ArrayList()
        channel60WiFiSignalWaveProfileArrayList = ArrayList()
        channel62WiFiSignalWaveProfileArrayList = ArrayList()
        channel64WiFiSignalWaveProfileArrayList = ArrayList()
        channel66WiFiSignalWaveProfileArrayList = ArrayList()

        channel100WiFiSignalWaveProfileArrayList = ArrayList()
        channel102WiFiSignalWaveProfileArrayList = ArrayList()
        channel104WiFiSignalWaveProfileArrayList = ArrayList()
        channel106WiFiSignalWaveProfileArrayList = ArrayList()
        channel108WiFiSignalWaveProfileArrayList = ArrayList()
        channel110WiFiSignalWaveProfileArrayList = ArrayList()
        channel112WiFiSignalWaveProfileArrayList = ArrayList()
        channel114WiFiSignalWaveProfileArrayList = ArrayList()
        channel116WiFiSignalWaveProfileArrayList = ArrayList()
        channel118WiFiSignalWaveProfileArrayList = ArrayList()
        channel120WiFiSignalWaveProfileArrayList = ArrayList()
        channel122WiFiSignalWaveProfileArrayList = ArrayList()
        channel124WiFiSignalWaveProfileArrayList = ArrayList()
        channel126WiFiSignalWaveProfileArrayList = ArrayList()
        channel128WiFiSignalWaveProfileArrayList = ArrayList()
        channel130WiFiSignalWaveProfileArrayList = ArrayList()
        channel132WiFiSignalWaveProfileArrayList = ArrayList()
        channel134WiFiSignalWaveProfileArrayList = ArrayList()
        channel136WiFiSignalWaveProfileArrayList = ArrayList()
        channel138WiFiSignalWaveProfileArrayList = ArrayList()
        channel140WiFiSignalWaveProfileArrayList = ArrayList()
        channel142WiFiSignalWaveProfileArrayList = ArrayList()

        channel149WiFiSignalWaveProfileArrayList = ArrayList()
        channel151WiFiSignalWaveProfileArrayList = ArrayList()
        channel153WiFiSignalWaveProfileArrayList = ArrayList()
        channel155WiFiSignalWaveProfileArrayList = ArrayList()
        channel157WiFiSignalWaveProfileArrayList = ArrayList()
        channel159WiFiSignalWaveProfileArrayList = ArrayList()
        channel161WiFiSignalWaveProfileArrayList = ArrayList()
        channel163WiFiSignalWaveProfileArrayList = ArrayList()
        channel165WiFiSignalWaveProfileArrayList = ArrayList()

        _5GBand1ChannelsSignalWaveProfileArrayList = ArrayList()
        _5GBand2ChannelsSignalWaveProfileArrayList = ArrayList()
        _5GBand3ChannelsSignalWaveProfileArrayList = ArrayList()
        _5GBand4ChannelsSignalWaveProfileArrayList = ArrayList()

        when(band)
        {
            0 ->
            {
                for(i in scanResults.indices)
                {
                    when(scanResults[i].channel)
                    {
                        1 -> channel1WiFiSignalWaveProfileArrayList.add(scanResults[i])
                        2 -> channel2WiFiSignalWaveProfileArrayList.add(scanResults[i])
                        3 -> channel3WiFiSignalWaveProfileArrayList.add(scanResults[i])
                        4 -> channel4WiFiSignalWaveProfileArrayList.add(scanResults[i])
                        5 -> channel5WiFiSignalWaveProfileArrayList.add(scanResults[i])
                        6 -> channel6WiFiSignalWaveProfileArrayList.add(scanResults[i])
                        7 -> channel7WiFiSignalWaveProfileArrayList.add(scanResults[i])
                        8 -> channel8WiFiSignalWaveProfileArrayList.add(scanResults[i])
                        9 -> channel9WiFiSignalWaveProfileArrayList.add(scanResults[i])
                        10 -> channel10WiFiSignalWaveProfileArrayList.add(scanResults[i])
                        11 -> channel11WiFiSignalWaveProfileArrayList.add(scanResults[i])
                        12 -> channel12WiFiSignalWaveProfileArrayList.add(scanResults[i])
                        13 -> channel13WiFiSignalWaveProfileArrayList.add(scanResults[i])
                        14 -> channel14WiFiSignalWaveProfileArrayList.add(scanResults[i])
                    }
                }

                _24GChannelsSignalWaveProfileArrayList.add(channel1WiFiSignalWaveProfileArrayList)
                _24GChannelsSignalWaveProfileArrayList.add(channel2WiFiSignalWaveProfileArrayList)
                _24GChannelsSignalWaveProfileArrayList.add(channel3WiFiSignalWaveProfileArrayList)
                _24GChannelsSignalWaveProfileArrayList.add(channel4WiFiSignalWaveProfileArrayList)
                _24GChannelsSignalWaveProfileArrayList.add(channel5WiFiSignalWaveProfileArrayList)
                _24GChannelsSignalWaveProfileArrayList.add(channel6WiFiSignalWaveProfileArrayList)
                _24GChannelsSignalWaveProfileArrayList.add(channel7WiFiSignalWaveProfileArrayList)
                _24GChannelsSignalWaveProfileArrayList.add(channel8WiFiSignalWaveProfileArrayList)
                _24GChannelsSignalWaveProfileArrayList.add(channel9WiFiSignalWaveProfileArrayList)
                _24GChannelsSignalWaveProfileArrayList.add(channel10WiFiSignalWaveProfileArrayList)
                _24GChannelsSignalWaveProfileArrayList.add(channel11WiFiSignalWaveProfileArrayList)
                _24GChannelsSignalWaveProfileArrayList.add(channel12WiFiSignalWaveProfileArrayList)
                _24GChannelsSignalWaveProfileArrayList.add(channel13WiFiSignalWaveProfileArrayList)
                _24GChannelsSignalWaveProfileArrayList.add(channel14WiFiSignalWaveProfileArrayList)

                for(i in _24GChannelsSignalWaveProfileArrayList.indices)
                {
                    if(_24GChannelsSignalWaveProfileArrayList[i].size == 0)
                    {
                        val tmp = WiFiSignalWaveProfile()
                        with(tmp)
                        {
                            channel = i + 1
                            this.band = 0
                            channelMargin = _24GChannelMargin * i
                            ssid = ""
                            amplitude = 0.0
                            color = Color.TRANSPARENT
                        }
                        _24GChannelsSignalWaveProfileArrayList[i].add(tmp)
                    }
                }
                results = _24GChannelsSignalWaveProfileArrayList
            }
            1 ->
            {
                for(i in scanResults.indices)
                {
                    when(scanResults[i].channel)
                    {
                        36 -> channel36WiFiSignalWaveProfileArrayList.add(scanResults[i])
                        38 -> channel38WiFiSignalWaveProfileArrayList.add(scanResults[i])
                        40 -> channel40WiFiSignalWaveProfileArrayList.add(scanResults[i])
                        42 -> channel42WiFiSignalWaveProfileArrayList.add(scanResults[i])
                        44 -> channel44WiFiSignalWaveProfileArrayList.add(scanResults[i])
                        46 -> channel46WiFiSignalWaveProfileArrayList.add(scanResults[i])
                        48 -> channel48WiFiSignalWaveProfileArrayList.add(scanResults[i])
                        50 -> channel50WiFiSignalWaveProfileArrayList.add(scanResults[i])
                        52 -> channel52WiFiSignalWaveProfileArrayList.add(scanResults[i])
                        54 -> channel54WiFiSignalWaveProfileArrayList.add(scanResults[i])
                        56 -> channel56WiFiSignalWaveProfileArrayList.add(scanResults[i])
                        58 -> channel58WiFiSignalWaveProfileArrayList.add(scanResults[i])
                        60 -> channel60WiFiSignalWaveProfileArrayList.add(scanResults[i])
                        62 -> channel62WiFiSignalWaveProfileArrayList.add(scanResults[i])
                        64 -> channel64WiFiSignalWaveProfileArrayList.add(scanResults[i])
                        66 -> channel66WiFiSignalWaveProfileArrayList.add(scanResults[i])
                        100 -> channel100WiFiSignalWaveProfileArrayList.add(scanResults[i])
                        102 -> channel102WiFiSignalWaveProfileArrayList.add(scanResults[i])
                        104 -> channel104WiFiSignalWaveProfileArrayList.add(scanResults[i])
                        106 -> channel106WiFiSignalWaveProfileArrayList.add(scanResults[i])
                        108 -> channel108WiFiSignalWaveProfileArrayList.add(scanResults[i])
                        110 -> channel110WiFiSignalWaveProfileArrayList.add(scanResults[i])
                        112 -> channel112WiFiSignalWaveProfileArrayList.add(scanResults[i])
                        114 -> channel114WiFiSignalWaveProfileArrayList.add(scanResults[i])
                        116 -> channel116WiFiSignalWaveProfileArrayList.add(scanResults[i])
                        118 -> channel118WiFiSignalWaveProfileArrayList.add(scanResults[i])
                        120 -> channel120WiFiSignalWaveProfileArrayList.add(scanResults[i])
                        122 -> channel122WiFiSignalWaveProfileArrayList.add(scanResults[i])
                        124 -> channel124WiFiSignalWaveProfileArrayList.add(scanResults[i])
                        126 -> channel126WiFiSignalWaveProfileArrayList.add(scanResults[i])
                        128 -> channel128WiFiSignalWaveProfileArrayList.add(scanResults[i])
                        130 -> channel130WiFiSignalWaveProfileArrayList.add(scanResults[i])
                        132 -> channel132WiFiSignalWaveProfileArrayList.add(scanResults[i])
                        134 -> channel134WiFiSignalWaveProfileArrayList.add(scanResults[i])
                        136 -> channel136WiFiSignalWaveProfileArrayList.add(scanResults[i])
                        138 -> channel138WiFiSignalWaveProfileArrayList.add(scanResults[i])
                        140 -> channel140WiFiSignalWaveProfileArrayList.add(scanResults[i])
                        142 -> channel142WiFiSignalWaveProfileArrayList.add(scanResults[i])
                        149 -> channel149WiFiSignalWaveProfileArrayList.add(scanResults[i])
                        151 -> channel151WiFiSignalWaveProfileArrayList.add(scanResults[i])
                        153 -> channel153WiFiSignalWaveProfileArrayList.add(scanResults[i])
                        155 -> channel155WiFiSignalWaveProfileArrayList.add(scanResults[i])
                        157 -> channel157WiFiSignalWaveProfileArrayList.add(scanResults[i])
                        159 -> channel159WiFiSignalWaveProfileArrayList.add(scanResults[i])
                        161 -> channel161WiFiSignalWaveProfileArrayList.add(scanResults[i])
                        163 -> channel163WiFiSignalWaveProfileArrayList.add(scanResults[i])
                        165 -> channel165WiFiSignalWaveProfileArrayList.add(scanResults[i])
                    }
                }

                _5GChannelsSignalWaveProfileArrayList.add(channel36WiFiSignalWaveProfileArrayList)
                _5GChannelsSignalWaveProfileArrayList.add(channel38WiFiSignalWaveProfileArrayList)
                _5GChannelsSignalWaveProfileArrayList.add(channel40WiFiSignalWaveProfileArrayList)
                _5GChannelsSignalWaveProfileArrayList.add(channel42WiFiSignalWaveProfileArrayList)
                _5GChannelsSignalWaveProfileArrayList.add(channel44WiFiSignalWaveProfileArrayList)
                _5GChannelsSignalWaveProfileArrayList.add(channel46WiFiSignalWaveProfileArrayList)
                _5GChannelsSignalWaveProfileArrayList.add(channel48WiFiSignalWaveProfileArrayList)
                _5GChannelsSignalWaveProfileArrayList.add(channel50WiFiSignalWaveProfileArrayList)

                _5GChannelsSignalWaveProfileArrayList.add(channel52WiFiSignalWaveProfileArrayList)
                _5GChannelsSignalWaveProfileArrayList.add(channel54WiFiSignalWaveProfileArrayList)
                _5GChannelsSignalWaveProfileArrayList.add(channel56WiFiSignalWaveProfileArrayList)
                _5GChannelsSignalWaveProfileArrayList.add(channel58WiFiSignalWaveProfileArrayList)
                _5GChannelsSignalWaveProfileArrayList.add(channel60WiFiSignalWaveProfileArrayList)
                _5GChannelsSignalWaveProfileArrayList.add(channel62WiFiSignalWaveProfileArrayList)
                _5GChannelsSignalWaveProfileArrayList.add(channel64WiFiSignalWaveProfileArrayList)
                _5GChannelsSignalWaveProfileArrayList.add(channel66WiFiSignalWaveProfileArrayList)

                _5GChannelsSignalWaveProfileArrayList.add(channel100WiFiSignalWaveProfileArrayList)
                _5GChannelsSignalWaveProfileArrayList.add(channel102WiFiSignalWaveProfileArrayList)
                _5GChannelsSignalWaveProfileArrayList.add(channel104WiFiSignalWaveProfileArrayList)
                _5GChannelsSignalWaveProfileArrayList.add(channel106WiFiSignalWaveProfileArrayList)
                _5GChannelsSignalWaveProfileArrayList.add(channel108WiFiSignalWaveProfileArrayList)
                _5GChannelsSignalWaveProfileArrayList.add(channel110WiFiSignalWaveProfileArrayList)
                _5GChannelsSignalWaveProfileArrayList.add(channel112WiFiSignalWaveProfileArrayList)
                _5GChannelsSignalWaveProfileArrayList.add(channel114WiFiSignalWaveProfileArrayList)
                _5GChannelsSignalWaveProfileArrayList.add(channel116WiFiSignalWaveProfileArrayList)
                _5GChannelsSignalWaveProfileArrayList.add(channel118WiFiSignalWaveProfileArrayList)
                _5GChannelsSignalWaveProfileArrayList.add(channel120WiFiSignalWaveProfileArrayList)
                _5GChannelsSignalWaveProfileArrayList.add(channel122WiFiSignalWaveProfileArrayList)
                _5GChannelsSignalWaveProfileArrayList.add(channel124WiFiSignalWaveProfileArrayList)
                _5GChannelsSignalWaveProfileArrayList.add(channel126WiFiSignalWaveProfileArrayList)
                _5GChannelsSignalWaveProfileArrayList.add(channel128WiFiSignalWaveProfileArrayList)
                _5GChannelsSignalWaveProfileArrayList.add(channel130WiFiSignalWaveProfileArrayList)
                _5GChannelsSignalWaveProfileArrayList.add(channel132WiFiSignalWaveProfileArrayList)
                _5GChannelsSignalWaveProfileArrayList.add(channel134WiFiSignalWaveProfileArrayList)
                _5GChannelsSignalWaveProfileArrayList.add(channel136WiFiSignalWaveProfileArrayList)
                _5GChannelsSignalWaveProfileArrayList.add(channel138WiFiSignalWaveProfileArrayList)
                _5GChannelsSignalWaveProfileArrayList.add(channel140WiFiSignalWaveProfileArrayList)
                _5GChannelsSignalWaveProfileArrayList.add(channel142WiFiSignalWaveProfileArrayList)

                _5GChannelsSignalWaveProfileArrayList.add(channel149WiFiSignalWaveProfileArrayList)
                _5GChannelsSignalWaveProfileArrayList.add(channel151WiFiSignalWaveProfileArrayList)
                _5GChannelsSignalWaveProfileArrayList.add(channel153WiFiSignalWaveProfileArrayList)
                _5GChannelsSignalWaveProfileArrayList.add(channel155WiFiSignalWaveProfileArrayList)
                _5GChannelsSignalWaveProfileArrayList.add(channel157WiFiSignalWaveProfileArrayList)
                _5GChannelsSignalWaveProfileArrayList.add(channel159WiFiSignalWaveProfileArrayList)
                _5GChannelsSignalWaveProfileArrayList.add(channel161WiFiSignalWaveProfileArrayList)
                _5GChannelsSignalWaveProfileArrayList.add(channel163WiFiSignalWaveProfileArrayList)
                _5GChannelsSignalWaveProfileArrayList.add(channel165WiFiSignalWaveProfileArrayList)

                for(i in _5GChannelsSignalWaveProfileArrayList.indices)
                {
                    if(_5GChannelsSignalWaveProfileArrayList[i].size == 0)
                    {
                        val tmp = WiFiSignalWaveProfile()
                        when(i)
                        {
                            0 -> tmp.channel = 36
                            1 -> tmp.channel = 38
                            2 -> tmp.channel = 40
                            3 -> tmp.channel = 42
                            4 -> tmp.channel = 44
                            5 -> tmp.channel = 46
                            6 -> tmp.channel = 48
                            7 -> tmp.channel = 50
                            8 -> tmp.channel = 52
                            9 -> tmp.channel = 54
                            10 -> tmp.channel = 56
                            11 -> tmp.channel = 58
                            12 -> tmp.channel = 60
                            13 -> tmp.channel = 62
                            14 -> tmp.channel = 64
                            15 -> tmp.channel = 66
                            16 -> tmp.channel = 100
                            17 -> tmp.channel = 102
                            18 -> tmp.channel = 104
                            19 -> tmp.channel = 106
                            20 -> tmp.channel = 108
                            21 -> tmp.channel = 110
                            22 -> tmp.channel = 112
                            23 -> tmp.channel = 114
                            24 -> tmp.channel = 116
                            25 -> tmp.channel = 118
                            26 -> tmp.channel = 120
                            27 -> tmp.channel = 122
                            28 -> tmp.channel = 124
                            29 -> tmp.channel = 126
                            30 -> tmp.channel = 128
                            31 -> tmp.channel = 130
                            32 -> tmp.channel = 132
                            33 -> tmp.channel = 134
                            34 -> tmp.channel = 136
                            35 -> tmp.channel = 138
                            36 -> tmp.channel = 140
                            37 -> tmp.channel = 142
                            38 -> tmp.channel = 149
                            39 -> tmp.channel = 151
                            40 -> tmp.channel = 153
                            41 -> tmp.channel = 155
                            42 -> tmp.channel = 157
                            43 -> tmp.channel = 159
                            44 -> tmp.channel = 161
                            45 -> tmp.channel = 163
                            46 -> tmp.channel = 165
                        }

                        with(tmp)
                        {
                            this.band = 1
                            channelMargin = _5GChannelMargin * i
                            ssid = ""
                            amplitude = 0.0
                            color = Color.TRANSPARENT
                        }
                        _5GChannelsSignalWaveProfileArrayList[i].add(tmp)
                    }
                }

                if(channel36WiFiSignalWaveProfileArrayList.size > 0)
                    _5GBand1ChannelsSignalWaveProfileArrayList.add(channel36WiFiSignalWaveProfileArrayList)
                if(channel38WiFiSignalWaveProfileArrayList.size > 0)
                    _5GBand1ChannelsSignalWaveProfileArrayList.add(channel38WiFiSignalWaveProfileArrayList)
                if(channel40WiFiSignalWaveProfileArrayList.size > 0)
                    _5GBand1ChannelsSignalWaveProfileArrayList.add(channel40WiFiSignalWaveProfileArrayList)
                if(channel42WiFiSignalWaveProfileArrayList.size > 0)
                    _5GBand1ChannelsSignalWaveProfileArrayList.add(channel42WiFiSignalWaveProfileArrayList)
                if(channel44WiFiSignalWaveProfileArrayList.size > 0)
                    _5GBand1ChannelsSignalWaveProfileArrayList.add(channel44WiFiSignalWaveProfileArrayList)
                if(channel46WiFiSignalWaveProfileArrayList.size > 0)
                    _5GBand1ChannelsSignalWaveProfileArrayList.add(channel46WiFiSignalWaveProfileArrayList)
                if(channel48WiFiSignalWaveProfileArrayList.size > 0)
                    _5GBand1ChannelsSignalWaveProfileArrayList.add(channel48WiFiSignalWaveProfileArrayList)
                if(channel50WiFiSignalWaveProfileArrayList.size > 0)
                    _5GBand2ChannelsSignalWaveProfileArrayList.add(channel50WiFiSignalWaveProfileArrayList)

                if(channel52WiFiSignalWaveProfileArrayList.size > 0)
                    _5GBand2ChannelsSignalWaveProfileArrayList.add(channel52WiFiSignalWaveProfileArrayList)
                if(channel54WiFiSignalWaveProfileArrayList.size > 0)
                    _5GBand2ChannelsSignalWaveProfileArrayList.add(channel54WiFiSignalWaveProfileArrayList)
                if(channel56WiFiSignalWaveProfileArrayList.size > 0)
                    _5GBand2ChannelsSignalWaveProfileArrayList.add(channel56WiFiSignalWaveProfileArrayList)
                if(channel58WiFiSignalWaveProfileArrayList.size > 0)
                    _5GBand2ChannelsSignalWaveProfileArrayList.add(channel58WiFiSignalWaveProfileArrayList)
                if(channel60WiFiSignalWaveProfileArrayList.size > 0)
                    _5GBand2ChannelsSignalWaveProfileArrayList.add(channel60WiFiSignalWaveProfileArrayList)
                if(channel62WiFiSignalWaveProfileArrayList.size > 0)
                    _5GBand2ChannelsSignalWaveProfileArrayList.add(channel62WiFiSignalWaveProfileArrayList)
                if(channel64WiFiSignalWaveProfileArrayList.size > 0)
                    _5GBand2ChannelsSignalWaveProfileArrayList.add(channel64WiFiSignalWaveProfileArrayList)
                if(channel66WiFiSignalWaveProfileArrayList.size > 0)
                    _5GBand2ChannelsSignalWaveProfileArrayList.add(channel66WiFiSignalWaveProfileArrayList)

                if(channel100WiFiSignalWaveProfileArrayList.size > 0)
                    _5GBand3ChannelsSignalWaveProfileArrayList.add(channel100WiFiSignalWaveProfileArrayList)
                if(channel102WiFiSignalWaveProfileArrayList.size > 0)
                    _5GBand3ChannelsSignalWaveProfileArrayList.add(channel102WiFiSignalWaveProfileArrayList)
                if(channel104WiFiSignalWaveProfileArrayList.size > 0)
                    _5GBand3ChannelsSignalWaveProfileArrayList.add(channel104WiFiSignalWaveProfileArrayList)
                if(channel106WiFiSignalWaveProfileArrayList.size > 0)
                    _5GBand3ChannelsSignalWaveProfileArrayList.add(channel106WiFiSignalWaveProfileArrayList)
                if(channel108WiFiSignalWaveProfileArrayList.size > 0)
                    _5GBand3ChannelsSignalWaveProfileArrayList.add(channel108WiFiSignalWaveProfileArrayList)
                if(channel110WiFiSignalWaveProfileArrayList.size > 0)
                    _5GBand3ChannelsSignalWaveProfileArrayList.add(channel110WiFiSignalWaveProfileArrayList)
                if(channel112WiFiSignalWaveProfileArrayList.size > 0)
                    _5GBand3ChannelsSignalWaveProfileArrayList.add(channel112WiFiSignalWaveProfileArrayList)
                if(channel114WiFiSignalWaveProfileArrayList.size > 0)
                    _5GBand3ChannelsSignalWaveProfileArrayList.add(channel114WiFiSignalWaveProfileArrayList)
                if(channel116WiFiSignalWaveProfileArrayList.size > 0)
                    _5GBand3ChannelsSignalWaveProfileArrayList.add(channel116WiFiSignalWaveProfileArrayList)
                if(channel118WiFiSignalWaveProfileArrayList.size > 0)
                    _5GBand3ChannelsSignalWaveProfileArrayList.add(channel118WiFiSignalWaveProfileArrayList)
                if(channel120WiFiSignalWaveProfileArrayList.size > 0)
                    _5GBand3ChannelsSignalWaveProfileArrayList.add(channel120WiFiSignalWaveProfileArrayList)
                if(channel122WiFiSignalWaveProfileArrayList.size > 0)
                    _5GBand3ChannelsSignalWaveProfileArrayList.add(channel122WiFiSignalWaveProfileArrayList)
                if(channel124WiFiSignalWaveProfileArrayList.size > 0)
                    _5GBand3ChannelsSignalWaveProfileArrayList.add(channel124WiFiSignalWaveProfileArrayList)
                if(channel126WiFiSignalWaveProfileArrayList.size > 0)
                    _5GBand3ChannelsSignalWaveProfileArrayList.add(channel126WiFiSignalWaveProfileArrayList)
                if(channel128WiFiSignalWaveProfileArrayList.size > 0)
                    _5GBand3ChannelsSignalWaveProfileArrayList.add(channel128WiFiSignalWaveProfileArrayList)
                if(channel130WiFiSignalWaveProfileArrayList.size > 0)
                    _5GBand3ChannelsSignalWaveProfileArrayList.add(channel130WiFiSignalWaveProfileArrayList)
                if(channel132WiFiSignalWaveProfileArrayList.size > 0)
                    _5GBand3ChannelsSignalWaveProfileArrayList.add(channel132WiFiSignalWaveProfileArrayList)
                if(channel134WiFiSignalWaveProfileArrayList.size > 0)
                    _5GBand3ChannelsSignalWaveProfileArrayList.add(channel134WiFiSignalWaveProfileArrayList)
                if(channel136WiFiSignalWaveProfileArrayList.size > 0)
                    _5GBand3ChannelsSignalWaveProfileArrayList.add(channel136WiFiSignalWaveProfileArrayList)
                if(channel138WiFiSignalWaveProfileArrayList.size > 0)
                    _5GBand3ChannelsSignalWaveProfileArrayList.add(channel138WiFiSignalWaveProfileArrayList)
                if(channel140WiFiSignalWaveProfileArrayList.size > 0)
                    _5GBand3ChannelsSignalWaveProfileArrayList.add(channel140WiFiSignalWaveProfileArrayList)
                if(channel142WiFiSignalWaveProfileArrayList.size > 0)
                    _5GBand3ChannelsSignalWaveProfileArrayList.add(channel142WiFiSignalWaveProfileArrayList)

                if(channel149WiFiSignalWaveProfileArrayList.size > 0)
                    _5GBand4ChannelsSignalWaveProfileArrayList.add(channel149WiFiSignalWaveProfileArrayList)
                if(channel151WiFiSignalWaveProfileArrayList.size > 0)
                    _5GBand4ChannelsSignalWaveProfileArrayList.add(channel151WiFiSignalWaveProfileArrayList)
                if(channel153WiFiSignalWaveProfileArrayList.size > 0)
                    _5GBand4ChannelsSignalWaveProfileArrayList.add(channel153WiFiSignalWaveProfileArrayList)
                if(channel155WiFiSignalWaveProfileArrayList.size > 0)
                    _5GBand4ChannelsSignalWaveProfileArrayList.add(channel155WiFiSignalWaveProfileArrayList)
                if(channel157WiFiSignalWaveProfileArrayList.size > 0)
                    _5GBand4ChannelsSignalWaveProfileArrayList.add(channel157WiFiSignalWaveProfileArrayList)
                if(channel159WiFiSignalWaveProfileArrayList.size > 0)
                    _5GBand4ChannelsSignalWaveProfileArrayList.add(channel159WiFiSignalWaveProfileArrayList)
                if(channel161WiFiSignalWaveProfileArrayList.size > 0)
                    _5GBand4ChannelsSignalWaveProfileArrayList.add(channel161WiFiSignalWaveProfileArrayList)
                if(channel163WiFiSignalWaveProfileArrayList.size > 0)
                    _5GBand4ChannelsSignalWaveProfileArrayList.add(channel163WiFiSignalWaveProfileArrayList)
                if(channel165WiFiSignalWaveProfileArrayList.size > 0)
                    _5GBand4ChannelsSignalWaveProfileArrayList.add(channel165WiFiSignalWaveProfileArrayList)

                results = _5GChannelsSignalWaveProfileArrayList
            }
        }
        return results
    }

    private fun filterEmpty5GChannelByBand(band: Int, dataSet: ArrayList<ArrayList<WiFiSignalWaveProfile>>)
    {
        for(i in dataSet.indices)
        {
            if(dataSet[i].size == 0)
                dataSet.removeAt(i)
        }

        if(dataSet.size == 0)
        {
            LogUtil.d(TAG, "data set size = 0 on band $band")
        }

        when(band)
        {
            1 -> _5GBand1ChannelsSignalWaveProfileArrayList = dataSet
            2 -> _5GBand2ChannelsSignalWaveProfileArrayList = dataSet
            3 -> _5GBand3ChannelsSignalWaveProfileArrayList = dataSet
            4 -> _5GBand4ChannelsSignalWaveProfileArrayList = dataSet
        }
    }

    fun set5GChannelsMarginSignalWaveProfileByBand(band: Int)
    {
        val px = 271 * (displayMetrics.densityDpi / 160f)
        var channelSection = 10
        when(band)
        {
            1 ->
            {
                channelSection = _5GBand1ChannelsSignalWaveProfileArrayList.size + 2
                _5GChannelMargin = width / channelSection
                for(i in _5GBand1ChannelsSignalWaveProfileArrayList.indices)
                {
                    for(j in 0 until _5GBand1ChannelsSignalWaveProfileArrayList[i].size)
                    {
                        _5GBand1ChannelsSignalWaveProfileArrayList[i][j].channelMargin = _5GChannelMargin * i
                        _5GBand1ChannelsSignalWaveProfileArrayList[i][j].channelSection = channelSection
                    }
                }
            }
            2 ->
            {
                channelSection = _5GBand1ChannelsSignalWaveProfileArrayList.size + 2
                _5GChannelMargin = width / channelSection
                for(i in _5GBand2ChannelsSignalWaveProfileArrayList.indices)
                {
                    for(j in 0 until _5GBand2ChannelsSignalWaveProfileArrayList[i].size)
                    {
                        _5GBand2ChannelsSignalWaveProfileArrayList[i][j].channelMargin = _5GChannelMargin * i
                        _5GBand2ChannelsSignalWaveProfileArrayList[i][j].channelSection = channelSection
                    }
                }
            }
            3 ->
            {
                channelSection = _5GBand1ChannelsSignalWaveProfileArrayList.size + 2
                _5GChannelMargin = width / channelSection
                for(i in _5GBand3ChannelsSignalWaveProfileArrayList.indices)
                {
                    for(j in 0 until _5GBand3ChannelsSignalWaveProfileArrayList[i].size)
                    {
                        _5GBand3ChannelsSignalWaveProfileArrayList[i][j].channelMargin = _5GChannelMargin * i
                        _5GBand3ChannelsSignalWaveProfileArrayList[i][j].channelSection = channelSection
                    }
                }
            }
            4 ->
            {
                channelSection = _5GBand1ChannelsSignalWaveProfileArrayList.size + 2
                _5GChannelMargin = width / channelSection
                for(i in _5GBand4ChannelsSignalWaveProfileArrayList.indices)
                {
                    for(j in 0 until _5GBand4ChannelsSignalWaveProfileArrayList[i].size)
                    {
                        _5GBand4ChannelsSignalWaveProfileArrayList[i][j].channelMargin = _5GChannelMargin * i
                        _5GBand4ChannelsSignalWaveProfileArrayList[i][j].channelSection = channelSection
                    }
                }
            }
        }
    }

    fun get5GChannelsSignalWaveProfileArrayListByBand(band: Int): ArrayList<ArrayList<WiFiSignalWaveProfile>>
    {
        var arrayLists = ArrayList<ArrayList<WiFiSignalWaveProfile>>()
        when(band)
        {
            1 -> arrayLists = _5GBand1ChannelsSignalWaveProfileArrayList
            2 -> arrayLists = _5GBand2ChannelsSignalWaveProfileArrayList
            3 -> arrayLists = _5GBand3ChannelsSignalWaveProfileArrayList
            4 -> arrayLists = _5GBand4ChannelsSignalWaveProfileArrayList
        }
        return arrayLists
    }

    fun getAll5GBandChannelSignalWaveProfile(): ArrayList<ArrayList<ArrayList<WiFiSignalWaveProfile>>>
    {
        all5GBandChannelsSignalWaveProfileArrayList = ArrayList()
        all5GBandChannelsSignalWaveProfileArrayList.add(_5GBand1ChannelsSignalWaveProfileArrayList)
        all5GBandChannelsSignalWaveProfileArrayList.add(_5GBand2ChannelsSignalWaveProfileArrayList)
        all5GBandChannelsSignalWaveProfileArrayList.add(_5GBand3ChannelsSignalWaveProfileArrayList)
        all5GBandChannelsSignalWaveProfileArrayList.add(_5GBand4ChannelsSignalWaveProfileArrayList)
        return all5GBandChannelsSignalWaveProfileArrayList
    }
}