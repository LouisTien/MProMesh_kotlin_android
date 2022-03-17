package zyxel.com.multyproneo.api

import okhttp3.MediaType
import okhttp3.Request
import okhttp3.RequestBody
import zyxel.com.multyproneo.util.AppConfig
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
            val setChangeGatewayNameInfoURL = "${GlobalData.getAPIPath()}${AppConfig.API_SYSTEM_INFO}?sessionkey=${GlobalData.loginInfo.sessionkey}"
            val requestParam = RequestBody.create(JSON, getParams().toString())
            return Request.Builder()
                    .addHeader("Cookie", GlobalData.cookie)
                    .url(setChangeGatewayNameInfoURL)
                    .put(requestParam)
                    .build()
        }
    }

    class GetSystemInfo : Commander()
    {
        override fun composeRequest(): Request
        {
            val getChangeGatewayNameInfoURL = "${GlobalData.getAPIPath()}${AppConfig.API_SYSTEM_INFO}"
            return Request.Builder()
                    .addHeader("Cookie", GlobalData.cookie)
                    .url(getChangeGatewayNameInfoURL)
                    .build()
        }
    }

    class GetWanInfo : Commander()
    {
        override fun composeRequest(): Request
        {
            val getWanInfoURL = "${GlobalData.getAPIPath()}${AppConfig.API_WAN_INFO}"
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
            val getFSecureInfoURL = "${GlobalData.getAPIPath()}${AppConfig.API_FSECURE_INFO}"
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
            val getHostNameReplaceInfoURL = "${GlobalData.getAPIPath()}${AppConfig.API_HOST_NAME_REPLACE_INFO}"
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
            val rebootURL = "${GlobalData.getAPIPath()}${AppConfig.API_REBOOT}?sessionkey=${GlobalData.loginInfo.sessionkey}"
            val requestParam = RequestBody.create(JSON, getParams().toString())
            return Request.Builder()
                    .addHeader("Cookie", GlobalData.cookie)
                    .url(rebootURL)
                    .post(requestParam)
                    .build()
        }
    }

    class GetRssiInfo(val index: Int = 0) : Commander()
    {
        override fun composeRequest(): Request
        {
            val getRssiInfoURL = "${GlobalData.getAPIPath()}${AppConfig.API_DEVICE_HOST_INFO}$index."
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
            val startSpeedTestURL = "${GlobalData.getAPIPath()}${AppConfig.API_SPEED_TEST_INFO}?sessionkey=${GlobalData.loginInfo.sessionkey}"
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
            val getSpeedTestStatusURL = "${GlobalData.getAPIPath()}${AppConfig.API_SPEED_TEST_INFO}"
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
            val getInternetBlockingInfoURL = "${GlobalData.getAPIPath()}${AppConfig.API_INTERNET_BLOCKING_INFO}"
            return Request.Builder()
                    .addHeader("Cookie", GlobalData.cookie)
                    .url(getInternetBlockingInfoURL)
                    .build()
        }
    }

    class GetAPPUICustom : Commander()
    {
        override fun composeRequest(): Request
        {
            val getAPPUICustomURL = "${GlobalData.getAPIPath()}${AppConfig.API_APP_UI_CUSTOM}"
            return Request.Builder()
                    .addHeader("Cookie", GlobalData.cookie)
                    .url(getAPPUICustomURL)
                    .build()
        }
    }

    class GetCloudAgentInfo : Commander()
    {
        override fun composeRequest(): Request
        {
            val getCloudAgentInfoURL = "${GlobalData.getAPIPath()}${AppConfig.API_CLOUD_AGENT}"
            return Request.Builder()
                    .addHeader("Cookie", GlobalData.cookie)
                    .url(getCloudAgentInfoURL)
                    .build()
        }
    }

    class ControlCloudAgent : Commander()
    {
        override fun composeRequest(): Request
        {
            val controlURL = "${GlobalData.getAPIPath()}${AppConfig.API_CLOUD_AGENT}?sessionkey=${GlobalData.loginInfo.sessionkey}"
            val requestParam = RequestBody.create(JSON, getParams().toString())
            return Request.Builder()
                    .addHeader("Cookie", GlobalData.cookie)
                    .url(controlURL)
                    .put(requestParam)
                    .build()
        }
    }

    class SetMultiObjects : Commander()
    {
        override fun composeRequest(): Request
        {
            val setMultiObjectsURL = "${GlobalData.getAPIPath()}${AppConfig.API_MULTI_OBJECTS}?sessionkey=${GlobalData.loginInfo.sessionkey}"
            val requestParam = RequestBody.create(JSON, getParams().toString())
            return Request.Builder()
                    .addHeader("Cookie", GlobalData.cookie)
                    .url(setMultiObjectsURL)
                    .put(requestParam)
                    .build()
        }
    }

    class GetSystemDate : Commander()
    {
        override fun composeRequest(): Request
        {
            val getSystemDateURL = "${GlobalData.getAPIPath()}${AppConfig.API_FW_SYSTEM_STATE}"
            return Request.Builder()
                    .addHeader("Cookie", GlobalData.cookie)
                    .url(getSystemDateURL)
                    .build()
        }
    }
}