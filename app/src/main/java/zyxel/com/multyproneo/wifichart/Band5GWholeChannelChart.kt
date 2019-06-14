package zyxel.com.multyproneo.wifichart

import android.app.Activity
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.util.DisplayMetrics
import android.view.View
import java.util.ArrayList
import java.util.HashMap

/**
 * Created by LouisTien on 2019/6/13.
 */
class Band5GWholeChannelChart(context: Context, private var activity: Activity) : View(context)
{
    var wiFiChannelChartHandler = WiFiChannelChartHandler()
    private var paint = Paint()
    private lateinit var mCanvas: Canvas
    private var mWidth = 0
    private var mHeight = 0
    private var drawWaveIndex = 0
    private var band = 1
    private var scrollPosition = 0
    private var displayMetrics: DisplayMetrics
    private var period = 360.0
    private var scale = 1
    private var textSize = 50
    private var ssidTextSize = 24
    private var ssidTextMargin = 80
    private var channelTextSection = 110
    private var channelTextMargin = 20
    private lateinit var _5GWiFiSignalWaveProfileArrayList: ArrayList<ArrayList<WiFiSignalWaveProfile>>
    private lateinit var signalWaveProfileArrayList: ArrayList<WiFiSignalWaveProfile>

    private lateinit var pathArrayList: ArrayList<Path>
    private lateinit var paintArrayList: ArrayList<Paint>
    private lateinit var ssidArrayList: ArrayList<String>

    private lateinit var mapListP0: List<Map<Int, Float>>
    private lateinit var mapListP1: List<Map<Int, Float>>
    private lateinit var mapListP2: List<Map<Int, Float>>
    private lateinit var mapListChannel: List<Map<Int, Int>>

    private lateinit var listArrayListP0: ArrayList<List<Map<Int, Float>>>
    private lateinit var listArrayListP1: ArrayList<List<Map<Int, Float>>>
    private lateinit var listArrayListP2: ArrayList<List<Map<Int, Float>>>
    private lateinit var listArrayListChannel: ArrayList<List<Map<Int, Int>>>

    private lateinit var canvasWiFiSiganlWaveProfileAL: ArrayList<WiFiSignalWaveProfile>
    private lateinit var ssidCenterArrayList: ArrayList<Float>
    private var connectedSSID = ""

    init
    {
        paint.color = Color.parseColor("#ccf0ff")
        wiFiChannelChartHandler.addListener(activity as WiFiChannelChartListener)
        displayMetrics = resources.displayMetrics
        when
        {
            (displayMetrics.densityDpi in 240 until 320) ->
            {
                scale = 1
                textSize = 20
                period = 360.0
                channelTextSection = 30
                ssidTextSize = 18
                channelTextMargin = 5
                ssidTextMargin = 20
            }

            (displayMetrics.densityDpi in 320 until 480) ->
            {
                scale = 2
                textSize = 30
                period = 240.0
                channelTextSection = 50
                ssidTextSize = 20
                channelTextMargin = 10
                ssidTextMargin = 40
            }

            (displayMetrics.densityDpi in 480 until 560) ->
            {
                scale = 3
                textSize = 40
                period = 360.0
                channelTextSection = 100
                ssidTextSize = 22
                channelTextMargin = 15
                ssidTextMargin = 75
            }

            (displayMetrics.densityDpi >= 560) ->
            {
                scale = 4
                textSize = 50
                period = 480.0
                channelTextSection = 110
                ssidTextSize = 24
                channelTextMargin = 20
                ssidTextMargin = 80
            }
        }
    }

    fun setStartEndPoint(width: Int, height: Int)
    {
        mHeight = height
        mWidth = width
        displayMetrics = resources.displayMetrics
    }

    fun setDataSet(dataSet: ArrayList<ArrayList<WiFiSignalWaveProfile>>, scrollX: Int)
    {
        _5GWiFiSignalWaveProfileArrayList = dataSet
        scrollPosition = scrollX

        for(z in 0 until _5GWiFiSignalWaveProfileArrayList.size)
        {
            signalWaveProfileArrayList = _5GWiFiSignalWaveProfileArrayList[z]
            for(x in 0 until signalWaveProfileArrayList.size)
            {
                signalWaveProfileArrayList[x].radius = (signalWaveProfileArrayList[x].amplitude * 10) as Int
                signalWaveProfileArrayList[x].amplitude = height * signalWaveProfileArrayList[x].amplitude + (x * 40)
                signalWaveProfileArrayList[x].old_x = 0.0F
                signalWaveProfileArrayList[x].old_y = (height - (signalWaveProfileArrayList[x].amplitude * (Math.sin(signalWaveProfileArrayList[x].old_x / period * Math.PI)))).toFloat()
                when(x)
                {
                    0 ->
                    {
                        if(signalWaveProfileArrayList[x].amplitude == 0.toDouble())
                        {
                            signalWaveProfileArrayList[x].color = Color.TRANSPARENT
                            signalWaveProfileArrayList[x].fillColor = Color.TRANSPARENT
                        }
                        else
                        {
                            signalWaveProfileArrayList[x].color = Color.argb(144, 243, 167, 77)
                            signalWaveProfileArrayList[x].fillColor = Color.argb(51, 243, 167, 77)
                        }
                    }

                    1 ->
                    {
                        signalWaveProfileArrayList[x].color = Color.argb(144, 59, 181, 160)
                        signalWaveProfileArrayList[x].fillColor = Color.argb(51, 59, 181, 160)
                    }

                    2 ->
                    {
                        signalWaveProfileArrayList[x].color = Color.argb(144, 235, 118, 108)
                        signalWaveProfileArrayList[x].fillColor = Color.argb(51, 235, 118, 108)
                    }

                    3 ->
                    {
                        signalWaveProfileArrayList[x].color = Color.argb(144, 217, 83, 151)
                        signalWaveProfileArrayList[x].fillColor = Color.argb(51, 217, 83, 151)
                    }

                    4 ->
                    {
                        signalWaveProfileArrayList[x].color = Color.argb(144, 177, 181, 218)
                        signalWaveProfileArrayList[x].fillColor = Color.argb(51, 177, 181, 218)
                    }

                    5 ->
                    {
                        signalWaveProfileArrayList[x].color = Color.argb(144, 0, 148, 255)
                        signalWaveProfileArrayList[x].fillColor = Color.argb(51, 0, 148, 255)
                    }

                    6 ->
                    {
                        signalWaveProfileArrayList[x].color = Color.argb(144, 100, 190, 0)
                        signalWaveProfileArrayList[x].fillColor = Color.argb(51, 100, 190, 0)
                    }

                    7 ->
                    {
                        signalWaveProfileArrayList[x].color = Color.argb(144, 102, 108, 188)
                        signalWaveProfileArrayList[x].fillColor = Color.argb(51, 102, 108, 188)
                    }

                    8 ->
                    {
                        signalWaveProfileArrayList[x].color = Color.argb(144, 242, 93, 78)
                        signalWaveProfileArrayList[x].fillColor = Color.argb(51, 242, 93, 78)
                    }

                    9 ->
                    {
                        signalWaveProfileArrayList[x].color = Color.argb(144, 54, 94, 221)
                        signalWaveProfileArrayList[x].fillColor = Color.argb(51, 54, 94, 221)
                    }

                    10 ->
                    {
                        signalWaveProfileArrayList[x].color = Color.argb(144, 237, 151, 93)
                        signalWaveProfileArrayList[x].fillColor = Color.argb(51, 237, 151, 93)
                    }

                    11 ->
                    {
                        signalWaveProfileArrayList[x].color = Color.argb(144, 114, 206, 133)
                        signalWaveProfileArrayList[x].fillColor = Color.argb(51, 114, 206, 133)
                    }

                    12 ->
                    {
                        signalWaveProfileArrayList[x].color = Color.argb(144, 229, 72, 72)
                        signalWaveProfileArrayList[x].fillColor = Color.argb(51, 229, 72, 72)
                    }

                    13 ->
                    {
                        signalWaveProfileArrayList[x].color = Color.argb(144, 140, 77, 112)
                        signalWaveProfileArrayList[x].fillColor = Color.argb(51, 140, 77, 112)
                    }

                    14 ->
                    {
                        signalWaveProfileArrayList[x].color = Color.argb(144, 70, 152, 209)
                        signalWaveProfileArrayList[x].fillColor = Color.argb(51, 70, 152, 209)
                    }

                    15 ->
                    {
                        signalWaveProfileArrayList[x].color = Color.argb(144, 158, 214, 142)
                        signalWaveProfileArrayList[x].fillColor = Color.argb(51, 158, 214, 142)
                    }

                    16 ->
                    {
                        signalWaveProfileArrayList[x].color = Color.argb(144, 175, 43, 109)
                        signalWaveProfileArrayList[x].fillColor = Color.argb(51, 175, 43, 109)
                    }

                    17 ->
                    {
                        signalWaveProfileArrayList[x].color = Color.argb(144, 127, 98, 126)
                        signalWaveProfileArrayList[x].fillColor = Color.argb(51, 127, 98, 126)
                    }

                    18 ->
                    {
                        signalWaveProfileArrayList[x].color = Color.argb(144, 239, 234, 139)
                        signalWaveProfileArrayList[x].fillColor = Color.argb(51, 239, 234, 139)
                    }

                    19 ->
                    {
                        signalWaveProfileArrayList[x].color = Color.argb(144, 186, 86, 86)
                        signalWaveProfileArrayList[x].fillColor = Color.argb(51, 186, 86, 86)
                    }

                    else ->
                    {
                        signalWaveProfileArrayList[x].color = Color.argb(144, 186, 86, 86)
                        signalWaveProfileArrayList[x].fillColor = Color.argb(51, 186, 86, 86)
                    }
                }

                if(signalWaveProfileArrayList[x].isConnected)
                {
                    connectedSSID = signalWaveProfileArrayList[x].ssid
                    signalWaveProfileArrayList[x].color = Color.argb(127, 0, 121, 255)
                    signalWaveProfileArrayList[x].fillColor = Color.argb(127, 0, 121, 255)
                }
            }
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int)
    {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }

    override fun onDraw(canvas: Canvas)
    {
        super.onDraw(canvas)

        pathArrayList = ArrayList<Path>()
        paintArrayList = ArrayList<Paint>()
        ssidArrayList = ArrayList<String>()

        mapListP0 = ArrayList<Map<Int, Float>>()
        mapListP1 = ArrayList<Map<Int, Float>>()
        mapListP2 = ArrayList<Map<Int, Float>>()
        mapListChannel = ArrayList<Map<Int, Int>>()

        listArrayListP0 = ArrayList<List<Map<Int, Float>>>()
        listArrayListP1 = ArrayList<List<Map<Int, Float>>>()
        listArrayListP2 = ArrayList<List<Map<Int, Float>>>()
        listArrayListChannel = ArrayList<List<Map<Int, Int>>>()

        canvasWiFiSiganlWaveProfileAL = ArrayList<WiFiSignalWaveProfile>()
        ssidCenterArrayList = ArrayList<Float>()

        canvas.drawPaint(paint)
        paint.isAntiAlias = true

        paint.style = Paint.Style.FILL
        paint.strokeWidth = 3F

        var screenHeight = canvas.height
        for(z in 0 until _5GWiFiSignalWaveProfileArrayList.size)
        {
            signalWaveProfileArrayList = _5GWiFiSignalWaveProfileArrayList[z]
            drawWaveIndex++

            for(j in 0 until signalWaveProfileArrayList.size)
            {
                var wifiSignalWaveProfile = signalWaveProfileArrayList[j]

                for(i in 0 .. period.toInt())
                {
                    paint.color = wifiSignalWaveProfile.color
                    wifiSignalWaveProfile.new_x = i.toFloat()
                    wifiSignalWaveProfile.new_y = (screenHeight - (wifiSignalWaveProfile.amplitude * (Math.sin(wifiSignalWaveProfile.new_x / period * Math.PI))) - channelTextSection).toFloat()
                    when(i)
                    {
                        0 ->
                        {
                            var tmpMap = HashMap<Int, Float>()
                            tmpMap[i + wifiSignalWaveProfile.channelMargin - scrollPosition] = wifiSignalWaveProfile.new_y
                            //mapListP0.add(tmpMap)
                        }
                    }
                }
            }
        }
    }
}