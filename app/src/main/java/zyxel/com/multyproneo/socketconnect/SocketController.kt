package zyxel.com.multyproneo.socketconnect

import org.json.JSONException
import org.json.JSONObject
import zyxel.com.multyproneo.util.AppConfig

/**
 * Created by LouisTien on 2019/7/8.
 */
class SocketController(private val responseListener: IResponseListener)
{
    private val packetreceiver = PacketReceiver()
    private val sessioncontrol = SessionControl(Transport(packetreceiver))
    private val messagehandler = MessageHandler(responseListener)

    init
    {
        sessioncontrol.setMessageListener(messagehandler)
        packetreceiver.setLocalListenPort(AppConfig.SCAN_LISTENPORT)
        packetreceiver.setListener(sessioncontrol)
        //packetreceiver.start()
    }

    fun deviceScan()
    {
        packetreceiver.start()

        val so = SessionObject()
        val dr = JSONObject()

        if(AppConfig.RESTfulBroadcastSet)
        {
            try
            {
                dr.put("AppVersion", AppConfig.SCAN_APPVERSION)
                dr.put("MagicNum", AppConfig.DEVICE_DISCOVER_REQ_MAGICNUM_VALUE)
            }
            catch(e: JSONException)
            {
                e.printStackTrace()
            }
        }
        else
        {
            val packetJSON1 = JSONObject()
            val packetJSON2 = JSONObject()
            val packetJSON3 = JSONObject()
            try
            {
                packetJSON1.put("AppVersion", AppConfig.SCAN_APPVERSION)
                packetJSON1.put("MagicNum", AppConfig.DEVICE_DISCOVER_REQ_MAGICNUM_VALUE)
                packetJSON2.put("AppInfo", packetJSON1)
                packetJSON3.put("X_ZyXEL_Ext", packetJSON2)
                dr.put("Device", packetJSON3)
            }
            catch(e: JSONException)
            {
                e.printStackTrace()
            }
        }

        so.version = 1
        so.type = AppConfig.DEVICE_DISCOVER_REQ
        so.data = dr.toString()
        so.dest_port = AppConfig.SCAN_REMOTEPORT
        sessioncontrol.deviceDiscoveryRequest(so)
    }
}