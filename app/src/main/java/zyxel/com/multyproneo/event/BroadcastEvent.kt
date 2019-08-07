package zyxel.com.multyproneo.event

/**
 * Created by LouisTien on 2019/8/7.
 */
class BroadcastEvent
{
    class ReceivedDiscoveryResp(var ip: String, var data: String)
    class BroadcastDone()
}