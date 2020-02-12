package zyxel.com.multyproneo.util

/**
 * Created by LouisTien on 2019/5/28.
 */
object AppConfig
{
    //Diagnostic permission request
    const val PERMISSION_LOCATION_REQUESTCODE = 1

    //UDP broadcast for searching devices
    const val SCAN_LISTENPORT = 6000
    const val SCAN_REMOTEPORT = 263
    const val SCAN_APPVERSION = 1
    const val SCAN_SOCKET_TIMEOUT = 4000
    const val DEVICE_DISCOVER_REQ_MAGICNUM = "Device.X_ZyXEL_Ext.AppInfo.MagicNum"
    const val DEVICE_DISCOVER_REQ_MAGICNUM_VALUE = "Z3704"
    const val DEVICE_DISCOVER_REQ_APPVERSION = "Device.X_ZyXEL_Ext.AppInfo.AppVersion"
    const val DEVICE_DISCOVER_REQ_APPVERSION_VALUE = "xxxxx"
    const val DEVICE_DISCOVER_REQ = 1
    const val DEVICE_DISCOVER_RESP = 2

    //WiFi setting
    const val endDeviceListUpdateTime = 60
    const val WiFiSettingTime = 30
    const val rebootTime = 20

    //Add Mesh
    const val WPSStatusUpdateTime = 5
    const val addMeshTime = 180

    //Other
    const val keepScreenTime = 300

    //Restful
    const val RESTfulBroadcastSet = true

    //Fabric
    const val NoUploadFabric = false

    //device username required length
    const val deviceUserNameRequiredLength = 1

    //Login username and password required length
    const val loginUserNameRequiredLength = 1
    const val loginPwdRequiredLength = 1

    //WiFi Setting name and password required length
    const val wifiNameRequiredLength = 1
    const val wifiPwdRequiredLength = 8

    //Speed Test
    const val SpeedTestActive = false
    const val SpeedTestActiveDebug = false
    const val SpeedTestStatusUpdateTime = 5
    const val SpeedTestTimeout = 180

    enum class LoadingAnimation
    {
        ANIM_REBOOT,
        ANIM_SEARCH,
        ANIM_NOFOUND
    }

    enum class LoadingGoToPage
    {
        FRAG_SEARCH,
        FRAG_HOME,
        FRAG_MESH_SUCCESS,
        FRAG_MESH_FAIL
    }

    enum class DialogAction
    {
        ACT_NONE,
        ACT_REBOOT,
        ACT_DELETE_DEVICE,
        ACT_BLOCK_DEVICE,
        ACT_DELETE_ZYXEL_DEVICE,
        ACT_LOCATION_PERMISSION,
        ACT_LOGOUT,
        ACT_RESEARCH,
        ACT_GOTO_SETTING,
        ACT_QRCODE_SCAN_HELP,
        ACT_QRCODE_SCAN_ERROR,
        ACT_QRCODE_SCAN_OK
    }

    enum class TroubleshootingPage
    {
        PAGE_NO_INTERNET,
        PAGE_CONNOT_CONNECT_CONTROLLER
    }
}