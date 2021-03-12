package zyxel.com.multyproneo.model

data class AppUICustomInfo
(
        val APPUICustomList: AppUICustomInfoObject = AppUICustomInfoObject(),
        val oper_status: String = "N/A"
)

data class AppUICustomInfoObject
(
        val Home_MESH_status: Boolean = false
)