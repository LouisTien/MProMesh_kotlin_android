package zyxel.com.multyproneo.socketconnect

/**
 * Created by LouisTien on 2019/7/8.
 */
class PacketReceiverUtil
{
    companion object
    {
        private var packetReceiver: PacketReceiver? = null

        fun getPacketReceiver(): PacketReceiver?
        {
            if(packetReceiver == null)
                packetReceiver = PacketReceiver()

            return packetReceiver
        }
    }
}