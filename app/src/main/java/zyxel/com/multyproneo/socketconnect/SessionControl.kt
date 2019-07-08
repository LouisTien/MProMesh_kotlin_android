package zyxel.com.multyproneo.socketconnect

import zyxel.com.multyproneo.util.AppConfig

/**
 * Created by LouisTien on 2019/7/8.
 */
class SessionControl(private val transportObject: Transport) : IPacketListener
{
    private val packetVersion = 1
    private var messagelistener: IMessageListener? = null

    fun deviceDiscoveryRequest(so: SessionObject)
    {
        val packet = Packet(packetVersion, AppConfig.DEVICE_DISCOVER_REQ, so.data!!.toByteArray())
        transportObject.broadcastUDP(packet, so.dest_port, so.src_port)
    }

    fun setMessageListener(im: IMessageListener)
    {
        messagelistener = im
    }

    override fun packetReceived(packetdata: Packet)
    {
        val decrypt_payload = String(packetdata.payload!!)
        val so = SessionObject()
        so.data = decrypt_payload
        so.type = packetdata.type
        so.version = packetdata.version
        so.src_ip = packetdata.receivedIP
        so.src_port = packetdata.receivedPort
        messagelistener!!.messageReceived(so)
    }
}