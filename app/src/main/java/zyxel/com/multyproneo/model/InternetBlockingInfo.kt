package zyxel.com.multyproneo.model

data class InternetBlockingInfo
(
    val Object: InternetBlockingInfoObject = InternetBlockingInfoObject(),
    val oper_status: String = "N/A",
    val requested_path: String = "N/A"
)

data class InternetBlockingInfoObject
(
    val Enable: Boolean = false
)