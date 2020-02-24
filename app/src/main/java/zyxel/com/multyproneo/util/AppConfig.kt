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

    //TUTK cloud
    const val TUTK_AM_SITE = "https://am1.tutk.com"
    const val TUTK_AM_STATE = "1234"
    const val TUTK_AM_CLIENT_ID = "vXudLCmYSwonVSetUPZrfiVDOjL5kmv2NQaUDmRG"
    const val TUTK_AM_CLIENT_SECRET = "8Xj9bgS9IY7JspwEttChbyoHAEJrp05E6oGW6kqx4OsrITwYFxUae5x4wloWPYYoGC8XdQZoVlKm0clXdeyjgLpBkO08rt0yINEUZC35tkqjwR2oVMD3uCiOr9Z5rboz"
    const val TUTK_DM_SITE = "https://hst-dm1.kalayservice.com"
    const val TUTK_API_VER = "/hestia/api/v2"
    const val TUTK_REALM = "zyxel"
    const val TUTK_DM_AUTHORIZATION = "ZXoycXlNTllOQVE0aVVwSkZYM0hacGhHSVd2bnpqSUliaTVDMGVxbTpUSzRLbFZiVkgyV2hPdlpQUHhYUGpEdURVeTBocXdOVEc0NnM4bk5vSzFRWVJSYXVEUmxNZGhHVTN5c1FPSG93R1pnRVN4UW8wRGdoTFpLMXEzRll3TjFQR296TFZTWFE2RTdUVEh6a2M5bmlhdWlONTA1dngxNFpsWFJja2d5OQ=="

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
        ACT_QRCODE_SCAN_OK
    }

    enum class TroubleshootingPage
    {
        PAGE_NO_INTERNET,
        PAGE_CONNOT_CONNECT_CONTROLLER
    }
}