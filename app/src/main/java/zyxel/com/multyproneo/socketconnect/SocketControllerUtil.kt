package zyxel.com.multyproneo.socketconnect

/**
 * Created by LouisTien on 2019/7/8.
 */
class SocketControllerUtil
{
    companion object
    {
        private var socketController: SocketController? = null

        fun getSocketController(): SocketController?
        {
            if(socketController == null)
                socketController = SocketController()

            return socketController
        }
    }
}