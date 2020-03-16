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

    //API Command
    const val API_LOGOUT = "/UserLogin"
    const val API_MESH = "/TR181/Value/Device.WiFi.AccessPoint.5.WPS."
    const val API_DEVICE_HOST_INFO = "/TR181/Value/Device.Hosts.Host."
    const val API_CHANGE_ICON_NAME_INFO = "/TR181/Value/Device.X_ZYXEL_Change_Icon_Name."
    const val API_SYSTEM_INFO = "/TR181/Value/Device.X_ZYXEL_System_Info."
    const val API_DEVICE_INFO = "/TR181/Value/Device.DeviceInfo."
    const val API_WAN_INFO = "/TR181/Value/Device.DeviceInfo.WanInfo."
    const val API_IP_INTERFACE_INFO = "/TR181/Value/Device.IP.Interface.?first_level_only=false"
    const val API_FSECURE_INFO = "/TR181/Value/Device.X_ZYXEL_License."
    const val API_HOST_NAME_REPLACE_INFO = "/TR181/Value/Device.X_ZYXEL_EXT.HostNameReplace."
    const val API_REBOOT = "/Reboot"
    const val API_SPEED_TEST_INFO = "/TR181/Value/Device.X_ZYXEL_EXT.SpeedTestInfo."
    const val API_INTERNET_BLOCKING_INFO = "/TR181/Value/Device.X_ZYXEL_EXT.InternetBlocking."
    const val API_UID = "/TR181/Value/Device.X_ZYXEL_TUTK_CloudAgent."
    const val API_WIFI_SETTING_INFO = "/TR181/Value/Device.WiFi.?first_level_only=false"
    const val API_MESH_INFO = "/TR181/Value/Device.X_ZYXEL_EXT.EasyMesh."
    const val API_WIFI_24G_INFO = "/TR181/Value/Device.WiFi.SSID.1."
    const val API_WIFI_24G_PWD = "/TR181/Value/Device.WiFi.AccessPoint.1.Security."
    const val API_WIFI_5G_INFO = "/TR181/Value/Device.WiFi.SSID.5."
    const val API_WIFI_5G_PWD = "/TR181/Value/Device.WiFi.AccessPoint.5.Security."
    const val API_GUEST_WIFI_24G_INFO = "/TR181/Value/Device.WiFi.SSID.2."
    const val API_GUEST_WIFI_24G_PWD = "/TR181/Value/Device.WiFi.AccessPoint.2.Security."
    const val API_GUEST_WIFI_5G_INFO = "/TR181/Value/Device.WiFi.SSID.6."
    const val API_GUEST_WIFI_5G_PWD = "/TR181/Value/Device.WiFi.AccessPoint.6.Security."

    //TUTK AMDM
    const val TUTK_AM_SITE = "https://am1.tutk.com"
    const val TUTK_AM_STATE = "1234"
    const val TUTK_AM_CLIENT_ID = "vXudLCmYSwonVSetUPZrfiVDOjL5kmv2NQaUDmRG"
    const val TUTK_AM_CLIENT_SECRET = "8Xj9bgS9IY7JspwEttChbyoHAEJrp05E6oGW6kqx4OsrITwYFxUae5x4wloWPYYoGC8XdQZoVlKm0clXdeyjgLpBkO08rt0yINEUZC35tkqjwR2oVMD3uCiOr9Z5rboz"
    const val TUTK_DM_SITE = "https://hst-dm1.kalayservice.com"
    const val TUTK_API_VER = "/hestia/api/v2"
    const val TUTK_REALM = "zyxel"
    const val TUTK_DM_AUTHORIZATION = "ZXoycXlNTllOQVE0aVVwSkZYM0hacGhHSVd2bnpqSUliaTVDMGVxbTpUSzRLbFZiVkgyV2hPdlpQUHhYUGpEdURVeTBocXdOVEc0NnM4bk5vSzFRWVJSYXVEUmxNZGhHVTN5c1FPSG93R1pnRVN4UW8wRGdoTFpLMXEzRll3TjFQR296TFZTWFE2RTdUVEh6a2M5bmlhdWlONTA1dngxNFpsWFJja2d5OQ=="

    //TUTK P2P
    const val TUTK_MAXSIZE_RECVBUF = 102400
    const val TUTK_STATUS_INIT_SEARCH_DEV = 10
    const val TUTK_RDT_WAIT_TIMEMS = 30000
    const val TUTK_RDT_RECV_TIMEOUT_TIMES = 30
    const val TUTK_RECV_HEADER_LENGTH = 4

    //Database
    const val DATABASE_NAME = "mpromesh.db"
    const val TABLE_SITE_INFO_NAME = "site_information"
    const val TABLE_CLIENT_LIST_NAME = "client_list"

    //SharedPreferences
    const val SHAREDPREF_NAME = "name_device_setting"
    const val SHAREDPREF_FIRST_TIME_KEY = "key_first_time_use"
    const val SHAREDPREF_TUTK_REFRESH_TOKEN_KEY = "TUTK_refresh_token"
    const val SHAREDPREF_TUTK_ACCESS_TOKEN_KEY = "TUTK_access_token"

    //Check Internet
    const val VERFY_INTERNET_DOMAIN_URL = "https://www.google.com"

    //WiFi setting
    const val endDeviceListUpdateTime = 60
    const val WiFiSettingTime = 30
    const val rebootTime = 20

    //Add Mesh
    const val WPSStatusUpdateTime = 5
    const val addMeshTime = 180

    //Keep screen on
    const val keepScreenTime = 300

    //Restful
    const val RESTfulBroadcastSet = true

    //Fabric
    const val NoUploadFabric = false

    //Device username required length
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

    //Location name list
    val locationNamesArray = arrayOf(
            "Attic", "Basement", "Children's Room", "Den",
            "Dining Room", "First Floor", "Foyer", "Kitchen",
            "Laundry", "Loft", "Lounge", "Master Room", "Office",
            "Playroom", "Reception Room"
    )

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
        ACT_QRCODE_SCAN_OK,
        ACT_RESTART
    }

    enum class TroubleshootingPage
    {
        PAGE_NO_INTERNET,
        PAGE_CONNOT_CONNECT_CONTROLLER,
        PAGE_CONNOT_CONNECT_CONTROLLER_PREVIOUS_SET,
        PAGE_P2P_INIT_FAIL_IN_GATEWAY_LIST
    }

    enum class TUTKP2PMethod(val value: Int)
    {
        MTD_GET(0x1),
        MTD_POST(0x2),
        MTD_PUT(0x3),
        MTD_DELETE(0x4)
    }
}