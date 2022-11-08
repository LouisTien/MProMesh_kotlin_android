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
        val F_Secure_New: Boolean = false,
        val Internet_Blocking: Boolean = true,
        val Guest_WiFi: Boolean = true,
        val Client_Max_Speed: Boolean = true,
        val AP_FW_Version: Boolean = true,
        val AP_Reboot: Boolean = true,
        val Parental_Control: Boolean = false,
        val Add_Mesh_WiFi: Boolean = true,
        val GW_LAN_IP_PORT: Boolean = false
)

/*
     https://www.evernote.com/shard/s201/sh/ce69958f-3393-b406-f5c3-24f2e658fbb1/d9fcea28a7c1a6d6d7f2d97952c1f5f4
    //既有部分
         "Home_MESH_status": true,   //如沒此欄位，預設值為true. (顯示Home Page "Mesh : Up/Down" 文字)
         "Home_Amber_Show": true,   //如沒此欄位，預設值為true. (點擊裝置訊號說明時，顯示too close說明與否)
         "Host_Name_Replace": true,   //如沒此欄位，預設值為true (決定會下/TR181/Value/Device.X_ZYXEL_EXT.HostNameReplace.與否)
         "F_Secure": true,   //如沒此欄位，預設值為true(決定會下/TR181/Value/Device.X_ZYXEL_License.與否)
         "F_Secure_New": true,   //如沒此欄位，預設值為false. (決定是否parental control採用外部的F-Secure APP來控制)，因為新mode上面的"F_Secure": true屏棄不用，但舊的model還有，F_Secure_New優先判斷，F_Secure_New為true情況下就不需判斷F_Secure
         "Internet_Blocking": true,   //如沒此欄位，預設值為true(決定會下/TR181/Value/Device.X_ZYXEL_EXT.InternetBlocking.與否)

	//EMG3524新增部分
         "Guest_WiFi": true,  //如沒此欄位，預設值為true (Guest WiFi功能開啟與否)
         "Client_Max_Speed" : true,  //如沒此欄位，預設值為true (client detail頁面，max speed欄位是否顯示)
         "AP_FW_Version": true,  //如沒此欄位，預設值為true (client detail頁面，FW Version欄位是否顯示)
         "AP_Reboot": true,  //如沒此欄位，預設值為true (extender detail頁面，reboot欄位是否顯示)

	//EMG5523新增部分
         "Parental_Control": false  //如沒此欄位，預設值為false (Parental Control功能開啟與否)
         "Add_Mesh_WiFi": true,  //如沒此欄位，預設值為true(add mesh頁面，使用WiFi配對流程是否顯示)
         "GW_LAN_IP_PORT": false,  //如沒此欄位，預設值為false(決定會下/TR181/Value/Device.X_ZYXEL_RemoteManagement.Service.與否)
       }
 */