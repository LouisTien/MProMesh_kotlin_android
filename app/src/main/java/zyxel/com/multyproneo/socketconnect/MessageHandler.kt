package zyxel.com.multyproneo.socketconnect

import zyxel.com.multyproneo.util.SocketControllerUtil

/**
 * Created by LouisTien on 2019/7/8.
 */
class MessageHandler : IMessageListener
{
    override fun messageReceived(session_object: SessionObject)
    {
        var agent_ip = session_object.src_ip
        agent_ip = agent_ip!!.substring(1)
        val agent_port = session_object.src_port
        //val nodeTree = DeviceNode.creatDeviceNodeByJsonString(session_object.data)
        //SocketController.getController().receivedDiscoveryResp(nodeTree, agent_ip, agent_port)
        SocketControllerUtil.instance.receivedDiscoveryResp(session_object.data!!)
    }
}