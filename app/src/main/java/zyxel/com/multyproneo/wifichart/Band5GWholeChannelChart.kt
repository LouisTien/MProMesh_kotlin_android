package zyxel.com.multyproneo.wifichart

import android.app.Activity
import android.content.Context
import android.graphics.*
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

    private var pathArrayList = arrayListOf<Path>()
    private var paintArrayList = arrayListOf<Paint>()
    private var ssidArrayList = arrayListOf<String>()

    private var mapListP0 = arrayListOf<Map<Int, Float>>()
    private var mapListP1 = arrayListOf<Map<Int, Float>>()
    private var mapListP2 = arrayListOf<Map<Int, Float>>()
    private var mapListChannel = arrayListOf<Map<Int, Int>>()

    private var listArrayListP0 = arrayListOf<List<Map<Int, Float>>>()
    private var listArrayListP1 = arrayListOf<List<Map<Int, Float>>>()
    private var listArrayListP2 = arrayListOf<List<Map<Int, Float>>>()
    private var listArrayListChannel = arrayListOf<List<Map<Int, Int>>>()

    private var canvasWiFiSiganlWaveProfileAL = arrayListOf<WiFiSignalWaveProfile>()
    private var ssidCenterArrayList = arrayListOf<Float>()
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

        /*pathArrayList = ArrayList<Path>()
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
        ssidCenterArrayList = ArrayList<Float>()*/

        canvas.drawPaint(paint)
        paint.isAntiAlias = true

        paint.style = Paint.Style.FILL
        paint.strokeWidth = 3F

        val screenHeight = canvas.height
        for(z in 0 until _5GWiFiSignalWaveProfileArrayList.size)
        {
            signalWaveProfileArrayList = _5GWiFiSignalWaveProfileArrayList[z]
            drawWaveIndex++

            for(j in 0 until signalWaveProfileArrayList.size)
            {
                val wifiSignalWaveProfile = signalWaveProfileArrayList[j]

                for(i in 0 .. period.toInt())
                {
                    paint.color = wifiSignalWaveProfile.color
                    wifiSignalWaveProfile.new_x = i.toFloat()
                    wifiSignalWaveProfile.new_y = (screenHeight - (wifiSignalWaveProfile.amplitude * (Math.sin(wifiSignalWaveProfile.new_x / period * Math.PI))) - channelTextSection).toFloat()
                    when(i)
                    {
                        0 ->
                        {
                            val tmpMap = HashMap<Int, Float>()
                            tmpMap[i + wifiSignalWaveProfile.channelMargin - scrollPosition] = wifiSignalWaveProfile.new_y
                            mapListP0.add(tmpMap)
                            listArrayListP0.add(mapListP0)
                            val pathPaint = Paint()
                            pathPaint.color = wifiSignalWaveProfile.color
                            paintArrayList.add(pathPaint)
                            ssidArrayList.add(wifiSignalWaveProfile.ssid)
                            canvasWiFiSiganlWaveProfileAL.add(wifiSignalWaveProfile)
                        }

                        (period/2).toInt() ->
                        {
                            val tmpMap = HashMap<Int, Float>()
                            tmpMap[i + wifiSignalWaveProfile.channelMargin - scrollPosition] = wifiSignalWaveProfile.new_y
                            mapListP1.add(tmpMap)
                            listArrayListP1.add(mapListP1)
                        }

                        period.toInt() ->
                        {
                            val tmpMap = HashMap<Int, Float>()
                            tmpMap[i + wifiSignalWaveProfile.channelMargin - scrollPosition] = wifiSignalWaveProfile.new_y
                            mapListP2.add(tmpMap)
                            listArrayListP2.add(mapListP2)
                        }

                        (period/2 - 15).toInt() ->
                        {
                            val tmpMap = HashMap<Int, Int>()
                            tmpMap[(i + (mWidth/6) * (drawWaveIndex - 1)) - scrollPosition] = canvas.height - channelTextMargin
                            mapListChannel.add(tmpMap)
                            listArrayListChannel.add(mapListChannel)
                        }
                    }
                    wifiSignalWaveProfile.old_x = wifiSignalWaveProfile.new_x
                    wifiSignalWaveProfile.old_y = wifiSignalWaveProfile.new_y
                }
            }
        }

        for(i in 0 .. listArrayListP0.size)
        {
            val mPath = Path()
            val iteratorP0 = listArrayListP0[i][i].entries.iterator()
            val iteratorP1 = listArrayListP1[i][i].entries.iterator()
            val iteratorP2 = listArrayListP2[i][i].entries.iterator()
            while(iteratorP0.hasNext())
            {
                val entryP0 = iteratorP0.next()
                val entryP1 = iteratorP1.next()
                val entryP2 = iteratorP2.next()
                val p0x = entryP0.key
                val p0y = entryP0.value;
                val p1x = entryP1.key
                val p1y = entryP1.value;
                val p2x = entryP2.key
                val p2y = entryP2.value;
                mPath.moveTo(p0x.toFloat(), p0y)
                mPath.quadTo(p1x.toFloat(), p1y, p2x.toFloat(), p2y)
                pathArrayList.add(mPath)
            }
        }

        paint.style = Paint.Style.STROKE

        for(i in pathArrayList.indices)
        {
            paint.color = paintArrayList[i].color
            paint.strokeWidth = 5f
            canvas.drawPath(pathArrayList[i], paint)
            paint.strokeWidth = 2f
            val pathMeasure = PathMeasure(pathArrayList[i], false)
            ssidCenterArrayList.add((canvas.height - pathMeasure.length / 2) - ssidTextMargin)
        }

        paint.style = Paint.Style.FILL

        for(i in pathArrayList.indices)
        {
            paint.color = canvasWiFiSiganlWaveProfileAL[i].fillColor
            canvas.drawPath(pathArrayList[i], paint)
        }

        for(i in pathArrayList.indices)
        {
            val iteratorP1 = listArrayListP1[i][i].entries.iterator()
            val iteratorP2 = listArrayListP2[i][i].entries.iterator()
            while(iteratorP1.hasNext())
            {
                val entryP1 = iteratorP1.next()

                val p1X = entryP1.key
                val p1Y = entryP1.value

                paint.style = Paint.Style.STROKE
                paint.strokeWidth = 2f
                paint.color = paintArrayList[i].color
                paint.textSize = ssidTextSize.toFloat()

                val centerY = ssidCenterArrayList[i]
                val centerX = p1X
                val measuredSSID = paint.measureText(ssidArrayList[i])
                if(connectedSSID != "" && connectedSSID.equals(ssidArrayList[i], ignoreCase = true))
                {
                    val entryP2 = iteratorP2.next()
                    val p2x = entryP2.key
                    val startX = p2x - (measuredSSID / 2)
                    val endX = p2x + (measuredSSID / 2)
                    canvas.drawText(ssidArrayList[i], startX, 240f, paint)
                    paint.strokeWidth = 5f
                    canvas.drawRect((startX - 2), 200f, (endX + 2), 260f, paint)
                    canvas.drawLine(centerX.toFloat(), centerY, startX - 2, 230f, paint)
                    drawArrows(canvas, 50f, centerX.toFloat(), centerY, startX - 2, 230f)
                }
                else
                {
                    canvas.drawRect(centerX - (measuredSSID / 2), centerY - 50, centerX + (measuredSSID / 2), centerY + 10, paint)
                    canvas.drawText(ssidArrayList[i], centerX - (measuredSSID / 2), centerY - 10, paint)
                }
            }
        }

        for(i in listArrayListChannel.indices)
        {
            val iteratorChannel = listArrayListChannel[i][i].entries.iterator()
            while (iteratorChannel.hasNext())
            {
                val entryChannel = iteratorChannel.next()
                val channelX = entryChannel.key
                val channelY = entryChannel.value
                paint.style = Paint.Style.FILL
                paint.color = Color.parseColor("#6b6b6b")
                paint.textSize = textSize.toFloat()
                canvas.drawText(canvasWiFiSiganlWaveProfileAL[i].channel.toString(), channelX.toFloat(), channelY.toFloat(), paint)
            }
        }

        mCanvas = canvas
        wiFiChannelChartHandler.notifyWiFiChannelChartDraw()
    }

    private fun drawArrows(canvas: Canvas, arrowSize: Float, x1: Float, y1: Float, x2: Float, y2: Float)
    {
        val awrad = Math.atan(3.5 / 8)
        val arrXY_1 = rotateVec(x1 - x2, y1 - y2, awrad, arrowSize.toDouble())
        val arrXY_2 = rotateVec(x1 - x2, y1 - y2, -awrad, arrowSize.toDouble())

        val X3 = java.lang.Double.valueOf(x1 - arrXY_1[0])
        val x3 = X3!!.toInt()
        val Y3 = java.lang.Double.valueOf(y1 - arrXY_1[1])
        val y3 = Y3!!.toInt()

        val X4 = java.lang.Double.valueOf(x1 - arrXY_2[0])
        val x4 = X4!!.toInt()
        val Y4 = java.lang.Double.valueOf(y1 - arrXY_2[1])
        val y4 = Y4!!.toInt()

        val arrowsPath = Path()
        arrowsPath.moveTo(x1, y1)
        arrowsPath.lineTo(x3.toFloat(), y3.toFloat())
        arrowsPath.lineTo(x4.toFloat(), y4.toFloat())
        arrowsPath.close()
        paint.style = Paint.Style.FILL
        canvas.drawPath(arrowsPath, paint)
    }

    private fun rotateVec(px: Float, py: Float, ang: Double, arrowSize: Double): DoubleArray
    {
        val mathstr = DoubleArray(2)
        var vx = px * Math.cos(ang) - py * Math.sin(ang)
        var vy = px * Math.sin(ang) + py * Math.cos(ang)
        val d = Math.sqrt(vx * vx + vy * vy)
        vx = vx / d * arrowSize
        vy = vy / d * arrowSize
        mathstr[0] = vx
        mathstr[1] = vy
        return mathstr
    }
}