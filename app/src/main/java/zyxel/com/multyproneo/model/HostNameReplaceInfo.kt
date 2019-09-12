package zyxel.com.multyproneo.model

/**
 * Created by LouisTien on 2019/9/12.
 */
data class HostNameReplaceInfo
(
    val Object: HostNameReplaceInfoObject = HostNameReplaceInfoObject(),
    val oper_status: String = "N/A",
    val requested_path: String = "N/A"
)

data class HostNameReplaceInfoObject
(
    val Enable: Boolean = false
)