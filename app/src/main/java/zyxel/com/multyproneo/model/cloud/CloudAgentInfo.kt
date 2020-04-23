package zyxel.com.multyproneo.model.cloud

data class CloudAgentInfo
(
        val Object: CloudAgentInfoObject = CloudAgentInfoObject(),
        val oper_status: String = "N/A",
        val requested_path: String = "N/A",
        val sessionkey: String = "N/A"
)

data class CloudAgentInfoObject
(
        val Enable: Boolean = false,
        val IsBound: Boolean = false,
        val Location: String = "N/A",
        val Status: String = "N/A",
        val TUTK_UID: String = "N/A",
        val GenCredential: Boolean = false,
        val Credential: String = "N/A"
)