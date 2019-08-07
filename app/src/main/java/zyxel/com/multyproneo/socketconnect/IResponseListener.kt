package zyxel.com.multyproneo.socketconnect

/**
 * Created by LouisTien on 2019/8/7.
 */
interface IResponseListener
{
    fun responseReceived(ip: String, data: String)
    fun responseReceivedDone()
}