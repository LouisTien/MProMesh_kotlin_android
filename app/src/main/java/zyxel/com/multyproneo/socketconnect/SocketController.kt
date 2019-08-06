package zyxel.com.multyproneo.socketconnect

import com.google.gson.Gson
import org.json.JSONException
import org.json.JSONObject
import zyxel.com.multyproneo.model.FindingDeviceInfo
import zyxel.com.multyproneo.util.AppConfig
import zyxel.com.multyproneo.util.GlobalData
import zyxel.com.multyproneo.util.LogUtil
import zyxel.com.multyproneo.util.PacketReceiverUtil

/**
 * Created by LouisTien on 2019/7/8.
 */
class SocketController
{
    private val TAG = javaClass.simpleName
    private val packetreceiver = PacketReceiverUtil.instance
    private val sessioncontrol = SessionControl(Transport())
    private val messagehandler = MessageHandler()
    private lateinit var findingDeviceInfo: FindingDeviceInfo

    init
    {
        sessioncontrol.setMessageListener(messagehandler)
        packetreceiver.setLocalListenPort(AppConfig.SCAN_LISTENPORT)
        packetreceiver.setListener(sessioncontrol)
        packetreceiver.start()
    }

    fun deviceScan()
    {
        GlobalData.gatewayList.clear()

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

    fun receivedDiscoveryResp(ip: String, data: String)
    {
        LogUtil.d(TAG, "ip:$ip, broadcast response:$data")

        /*if(data.contains("ApiName") && data.contains("SupportedApiVersion"))
        {
            val data = JSONObject(data)
            val ApiName = data.get("ApiName").toString()
            LogUtil.d(TAG, "ApiName:$ApiName")
            val ModelName = data.get("ModelName").toString()
            LogUtil.d(TAG, "ModelName:$ModelName")
            val SoftwareVersion = data.get("SoftwareVersion").toString()
            LogUtil.d(TAG, "SoftwareVersion:$SoftwareVersion")
            val DeviceMode = data.get("DeviceMode").toString()
            LogUtil.d(TAG, "DeviceMode:$DeviceMode")
            val SupportedApiVersion = data.getJSONArray("SupportedApiVersion")
            val subdata = JSONObject(SupportedApiVersion[0].toString())
            LogUtil.d(TAG, "subdata:$subdata")
            val ApiVersion = subdata.get("ApiVersion").toString()
            LogUtil.d(TAG, "ApiVersion:$ApiVersion")
            val LoginURL = subdata.get("LoginURL").toString()
            LogUtil.d(TAG, "LoginURL:$LoginURL")
        }*/

        if(data.contains("ApiName") && data.contains("SupportedApiVersion"))
        {
            try
            {
                findingDeviceInfo = Gson().fromJson(data, FindingDeviceInfo::class.javaObjectType)
                findingDeviceInfo.IP = ip
                LogUtil.d(TAG, "findingDeviceInfo:${findingDeviceInfo.toString()}")
                GlobalData.gatewayList.add(findingDeviceInfo)
            }
            catch(e: JSONException)
            {
                e.printStackTrace()
            }
        }
    }
}