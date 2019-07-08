package zyxel.com.multyproneo.socketconnect

/**
 * Created by LouisTien on 2019/7/8.
 */
interface IMessageListener
{
    fun messageReceived(session_object: SessionObject)
}