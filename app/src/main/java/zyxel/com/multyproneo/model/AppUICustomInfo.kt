package zyxel.com.multyproneo.model

data class AppUICustomInfo
(
        val APPUICustomList: AppUICustomInfoObject = AppUICustomInfoObject(),
        val oper_status: String = "N/A"
)

data class AppUICustomInfoObject
(
        val Home_MESH_status: Boolean = true,
        val Home_Amber_Show: Boolean = true,
        val Host_Name_Replace: Boolean = true,
        val F_Secure: Boolean = true,
        val Internet_Blocking: Boolean = true,
        val Guest_WiFi: Boolean = true,
        val Client_Max_Speed: Boolean = true,
        val AP_FW_Version: Boolean = true,
        val AP_Reboot: Boolean = true,
        val Parental_Control: Boolean = false,
        val Add_Mesh_WiFi: Boolean = true,
        val GW_LAN_IP_PORT: Boolean = false
)