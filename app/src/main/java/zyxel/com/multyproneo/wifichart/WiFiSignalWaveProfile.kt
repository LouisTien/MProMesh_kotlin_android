package zyxel.com.multyproneo.wifichart

/**
 * Created by LouisTien on 2019/6/14.
 */
class WiFiSignalWaveProfile
{
    var old_x: Float = 0.toFloat()
    var old_y: Float = 0.toFloat()
    var new_x: Float = 0.toFloat()
    var new_y: Float = 0.toFloat()
    var color: Int = 0
    var amplitude: Double = 0.toDouble()
    var channelMargin: Int = 0
    var radius: Int = 0
    var frequency: Int = 0
    var channel: Int = 0
    var rssi: Int = 0
    var ssid: String = ""
    var band: Int = 0
    var channelSection: Int = 0
    var bssid: String = ""
    var fillColor: Int = 0
    var isConnected = false
}