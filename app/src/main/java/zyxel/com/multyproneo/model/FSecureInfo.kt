package zyxel.com.multyproneo.model

/**
 * Created by LouisTien on 2019/8/15.
 */
data class FSecureInfo
(
    val Object: FSecureInfoObject = FSecureInfoObject(),
    val oper_status: String = "N/A",
    val requested_path: String = "N/A"
)

data class FSecureInfoObject
(
    val Cyber_Security_FSC: Boolean = false
)