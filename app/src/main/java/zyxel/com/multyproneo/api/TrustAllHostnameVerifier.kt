package zyxel.com.multyproneo.api

import javax.net.ssl.HostnameVerifier
import javax.net.ssl.SSLSession

/**
 * Created by LouisTien on 2019/7/17.
 */
object TrustAllHostnameVerifier : HostnameVerifier
{
    override fun verify(hostname: String?, session: SSLSession?): Boolean = true
}