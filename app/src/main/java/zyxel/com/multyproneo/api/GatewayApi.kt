package zyxel.com.multyproneo.api

import okhttp3.MediaType
import okhttp3.Request
import okhttp3.RequestBody
import zyxel.com.multyproneo.util.GlobalData

/**
 * Created by LouisTien on 2019/8/14.
 */
object GatewayApi
{
    private val JSON = MediaType.parse("application/json; charset=utf-8")

    class SetSystemInfo : Commander()
    {
        override fun composeRequest(): Request
        {
            val setChangeGatewayNameInfo = "${GlobalData.getAPIPath()}/TR181/Value/Device.X_ZYXEL_System_Info.?sessionkey=${GlobalData.sessionKey}"
            val requestParam = RequestBody.create(JSON, getParams().toString())
            return Request.Builder()
                    .addHeader("Cookie", GlobalData.cookie)
                    .url(setChangeGatewayNameInfo)
                    .put(requestParam)
                    .build()
        }
    }

    class GetSystemInfo : Commander()
    {
        override fun composeRequest(): Request
        {
            val getChangeGatewayNameInfo = "${GlobalData.getAPIPath()}/TR181/Value/Device.X_ZYXEL_System_Info."
            return Request.Builder()
                    .addHeader("Cookie", GlobalData.cookie)
                    .url(getChangeGatewayNameInfo)
                    .build()
        }
    }

    class GetWanInfo : Commander()
    {
        override fun composeRequest(): Request
        {
            val getWanInfoURL = "${GlobalData.getAPIPath()}/TR181/Value/Device.DeviceInfo.WanInfo."
            return Request.Builder()
                    .addHeader("Cookie", GlobalData.cookie)
                    .url(getWanInfoURL)
                    .build()
        }
    }

    class GetFSecureInfo : Commander()
    {
        override fun composeRequest(): Request
        {
            val getFSecureInfoURL = "${GlobalData.getAPIPath()}/TR181/Value/Device.X_ZYXEL_License."
            return Request.Builder()
                    .addHeader("Cookie", GlobalData.cookie)
                    .url(getFSecureInfoURL)
                    .build()
        }
    }

    class GetHostNameReplaceInfo : Commander()
    {
        override fun composeRequest(): Request
        {
            val getHostNameReplaceInfoURL = "${GlobalData.getAPIPath()}/TR181/Value/Device.X_ZYXEL_EXT.HostNameReplace."
            return Request.Builder()
                    .addHeader("Cookie", GlobalData.cookie)
                    .url(getHostNameReplaceInfoURL)
                    .build()
        }
    }

    class GatewayReboot : Commander()
    {
        override fun composeRequest(): Request
        {
            val rebootURL = "${GlobalData.getAPIPath()}/Reboot?sessionkey=${GlobalData.sessionKey}"
            val requestParam = RequestBody.create(JSON, getParams().toString())
            return Request.Builder()
                    .addHeader("Cookie", GlobalData.cookie)
                    .url(rebootURL)
                    .post(requestParam)
                    .build()
        }
    }

    class EndDeviceReboot(val index: Int = 0) : Commander()
    {
        override fun composeRequest(): Request
        {
            val rebootURL = "${GlobalData.getAPIPath()}/TR181/Value/Device.Hosts.Host.$index.X_ZYXEL_EXT.?sessionkey=${GlobalData.sessionKey}"
            val requestParam = RequestBody.create(JSON, getParams().toString())
            return Request.Builder()
                    .addHeader("Cookie", GlobalData.cookie)
                    .url(rebootURL)
                    .put(requestParam)
                    .build()
        }
    }

    class GetRssiInfo(val index: Int = 0) : Commander()
    {
        override fun composeRequest(): Request
        {
            val getRssiInfoURL = "${GlobalData.getAPIPath()}/TR181/Value/Device.Hosts.Host.$index."
            return Request.Builder()
                    .addHeader("Cookie", GlobalData.cookie)
                    .url(getRssiInfoURL)
                    .build()
        }
    }

    class StartSpeedTest : Commander()
    {
        override fun composeRequest(): Request
        {
            val startSpeedTestURL = "${GlobalData.getAPIPath()}/TR181/Value/Device.X_ZYXEL_EXT.SpeedTestInfo.?sessionkey=${GlobalData.sessionKey}"
            val requestParam = RequestBody.create(JSON, getParams().toString())
            return Request.Builder()
                    .addHeader("Cookie", GlobalData.cookie)
                    .url(startSpeedTestURL)
                    .put(requestParam)
                    .build()
        }
    }

    class GetSpeedTestStatus : Commander()
    {
        override fun composeRequest(): Request
        {
            val getSpeedTestStatusURL = "${GlobalData.getAPIPath()}/TR181/Value/Device.X_ZYXEL_EXT.SpeedTestInfo."
            return Request.Builder()
                    .addHeader("Cookie", GlobalData.cookie)
                    .url(getSpeedTestStatusURL)
                    .build()
        }
    }

    class GetInternetBlockingInfo : Commander()
    {
        override fun composeRequest(): Request
        {
            val getInternetBlockingInfoURL = "${GlobalData.getAPIPath()}/TR181/Value/Device.X_ZYXEL_EXT.InternetBlocking."
            return Request.Builder()
                    .addHeader("Cookie", GlobalData.cookie)
                    .url(getInternetBlockingInfoURL)
                    .build()
        }
    }
}