package zyxel.com.multyproneo.model

data class CustomerInfo
(
    val Object: CustomerInfoObject = CustomerInfoObject(),
    val oper_status: String = "",
    val requested_path: String = ""
)

data class CustomerInfoObject
(
    val Enable: Boolean = false,
    val X_ZYXEL_APP_Customer: String = "",
    val LogFileDeliver: Boolean = false
)