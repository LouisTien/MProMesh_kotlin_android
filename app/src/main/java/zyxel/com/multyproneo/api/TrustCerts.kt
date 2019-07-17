package zyxel.com.multyproneo.api

import java.security.cert.CertificateException
import java.security.cert.X509Certificate
import javax.net.ssl.X509TrustManager

/**
 * Created by LouisTien on 2019/7/16.
 */
object TrustCerts : X509TrustManager
{
    @Throws(CertificateException::class)
    override fun checkClientTrusted(chain: Array<X509Certificate>?, authType: String){}

    @Throws(CertificateException::class)
    override fun checkServerTrusted(chain: Array<X509Certificate>?, authType: String){}

    override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf<X509Certificate>()
}