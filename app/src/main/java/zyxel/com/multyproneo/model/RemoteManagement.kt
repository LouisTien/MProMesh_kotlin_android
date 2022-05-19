package zyxel.com.multyproneo.model

data class RemoteManagement
(
    val Object: List<RemoteManagementObject> = listOf(RemoteManagementObject()),
    val oper_status: String = "N/A",
    val requested_path: String = "N/A"
)

data class RemoteManagementObject
(
    val Name: String = "N/A",
    val Enable: Boolean = false,
    val Protocol: Int = 0,
    val IPAddress: String = "N/A",
    val Port: Int = 0,
    val Mode: String = "N/A",
    val OldMode: String = "N/A",
    val RestartDaemon: Boolean = false,
    val LifeTime: Int = 0,
    val BoundInterfaceList: String = "N/A",
    val TrustAll: Boolean = false,
    val DisableSshPasswordLogin: Boolean = false,
    val SshKeyBaseAuthPublicKey: String = "N/A"
)