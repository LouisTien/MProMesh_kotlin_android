package zyxel.com.multyproneo.socketconnect

import java.net.DatagramPacket
import java.net.InetAddress

/**
 * Created by LouisTien on 2019/7/8.
 */
class Transport
{
    private var broadip = InetAddress.getByAddress(byteArrayOf(255.toByte(), 255.toByte(), 255.toByte(), 255.toByte()))

    fun broadcastUDP(packet: Packet, dest_port: Int, local_port: Int) = Thread(BroadcastUDP(packet.getBytes(), dest_port, local_port)).start()

    private inner class BroadcastUDP(private var data: ByteArray, private var broadcastPort: Int, private var listen_port: Int) : Runnable
    {
        private var dp: DatagramPacket? = null

        override fun run()
        {
            dp = DatagramPacket(data, data.size, broadip, broadcastPort)
            PacketReceiverUtil.instance.sendUDPPacket(dp!!)
        }
    }
}