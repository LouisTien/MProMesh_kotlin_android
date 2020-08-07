package zyxel.com.multyproneo.api.cloud

import org.jetbrains.anko.doAsync
import zyxel.com.multyproneo.util.AppConfig

abstract class TUTKP2PCommander
{
    private var requestPageName = ""
    private var requestPayload = ""

    fun getRequestPageName(): String
    {
        return requestPageName
    }

    fun setRequestPageName(pageName: String): TUTKP2PCommander
    {
        requestPageName = pageName
        return this
    }

    fun getRequestPayload(): String
    {
        return requestPayload
    }

    fun setRequestPayload(payload: String): TUTKP2PCommander
    {
        requestPayload = payload
        return this
    }

    abstract fun requestURL(): String

    abstract fun method(): AppConfig.TUTKP2PMethod

    fun setResponseListener(listener: TUTKP2PResponseCallback): TUTKP2PCommander
    {
        TUTKP2PBaseApi.setCallback(listener)
        return this
    }

    fun execute(): TUTKP2PCommander
    {
        doAsync{
            TUTKP2PBaseApi.sendData(method(), requestURL())
            if(requestURL() == "{\"URI\":\"${AppConfig.API_SYS_LOG}\"}")
                TUTKP2PBaseApi.receiveDataForFWLogFile()
            else
                TUTKP2PBaseApi.receiveData()
        }

        return this
    }
}