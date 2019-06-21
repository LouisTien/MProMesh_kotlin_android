package zyxel.com.multyproneo.wifichart

/**
 * Created by LouisTien on 2019/6/13.
 */
class WiFiChannelChartHandler
{
    var wiFiChannelChartListenerArrayList = arrayListOf<WiFiChannelChartListener>()

    fun addListener(wiFiChannelChartListener: WiFiChannelChartListener) = wiFiChannelChartListenerArrayList.add(wiFiChannelChartListener)

    fun removeListener()
    {
        for(i in wiFiChannelChartListenerArrayList.indices)
            wiFiChannelChartListenerArrayList.removeAt(i)
    }

    fun notifyWiFiChannelChartDraw()
    {
        for(i in wiFiChannelChartListenerArrayList.indices)
            wiFiChannelChartListenerArrayList[i].onDrawCompleted()
    }
}