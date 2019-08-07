package zyxel.com.multyproneo.socketconnect

import zyxel.com.multyproneo.util.AppConfig
import zyxel.com.multyproneo.util.LogUtil
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.SocketTimeoutException

/**
 * Created by LouisTien on 2019/7/5.
 */
class PacketReceiver : Runnable
{
    private val TAG = javaClass.simpleName
    private var running = false
    private var serversocket: DatagramSocket? = null
    private var localport = 6000
    private var packetReceiver: IPacketListener? = null

    fun setLocalListenPort(port: Int)
    {
        localport = port
        //restart()
    }

    fun restart()
    {
        stop()
        start()
    }

    fun start()
    {
        if(serversocket == null || !running)
        {
            serversocket = DatagramSocket(localport)
            serversocket?.soTimeout = 3000
            running = true
            Thread(this).start()
        }
    }

    fun stop()
    {
        if(running)
            serversocket?.close()

        running = false
    }

    fun setListener(_packetreceiver: IPacketListener)
    {
        packetReceiver = _packetreceiver
    }

    fun sendUDPPacket(dp: DatagramPacket)
    {
        serversocket?.send(dp)
    }

    override fun run()
    {
        while(running)
        {
            val packetsize = ByteArray(65536)
            val datagrampacket = DatagramPacket(packetsize, packetsize.size)
            val version: Int
            val type: Int
            val length: Int
            val payload: ByteArray
            try
            {
                serversocket?.receive(datagrampacket)
            }
            catch(e: SocketTimeoutException)
            {
                LogUtil.d(TAG,"-----SocketTimeoutException-------")
                stop()
                packetReceiver?.packetReceivedDone()
                e.printStackTrace()
                break
            }
            val data = datagrampacket.data
            version = data[0].toInt() and 0xFF
            LogUtil.d(TAG, "version=$version")
            type = data[1].toInt() and 0xFF
            LogUtil.d(TAG, "type=$type")
            length = if(AppConfig.RESTfulBroadcastSet) (data[3].toInt() and 0xFF) + (data[2].toInt() and 0xFF shl 8) else (data[2].toInt() and 0xFF) + (data[3].toInt() and 0xFF shl 8)
            LogUtil.d(TAG, "length=$length")
            payload = ByteArray(length)
            for(i in 0 until length)
                payload[i] = data[4 + i]
            val packet = Packet(version, type, payload)
            packet.receivedIP = datagrampacket.address.toString()
            packet.receivedPort = datagrampacket.port
            packetReceiver?.packetReceived(packet)
        }
    }
}