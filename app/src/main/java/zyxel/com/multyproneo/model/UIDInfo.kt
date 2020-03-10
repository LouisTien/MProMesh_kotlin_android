package zyxel.com.multyproneo.model

data class UIDInfo
(
    val Object: UIDInfoObject = UIDInfoObject(),
    val oper_status: String = "N/A",
    val requested_path: String = "N/A"
)

data class UIDInfoObject
(
    val IsBound: Boolean = false,
    val Location: String = "N/A",
    val TUTK_UID: String = "N/A"
)