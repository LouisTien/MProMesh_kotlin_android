package zyxel.com.multyproneo.api

import zyxel.com.multyproneo.util.GlobalData
import javax.net.ssl.HostnameVerifier
import javax.net.ssl.SSLSession

/**
 * Created by LouisTien on 2019/7/17.
 */
object TrustHostnameVerifier : HostnameVerifier
{
    override fun verify(hostname: String?, session: SSLSession?): Boolean
    {
        return hostname == GlobalData.deviceIP
    }
}