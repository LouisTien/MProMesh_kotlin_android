package zyxel.com.multyproneo.socketconnect

/**
 * Created by LouisTien on 2019/7/8.
 */
class MessageHandler(private val responseListener: IResponseListener) : IMessageListener
{
    override fun messageReceived(session_object: SessionObject)
    {
        var agent_ip = session_object.src_ip
        agent_ip = agent_ip!!.substring(1)
        val agent_port = session_object.src_port
        //val nodeTree = DeviceNode.creatDeviceNodeByJsonString(session_object.data)
        //SocketController.getController().receivedDiscoveryResp(nodeTree, agent_ip, agent_port)
        //SocketControllerUtil.instance.receivedDiscoveryResp(agent_ip, session_object.data!!)
        responseListener.responseReceived(agent_ip, session_object.data!!)
    }

    override fun messageReceivedDone()
    {
        responseListener.responseReceivedDone()
    }
}