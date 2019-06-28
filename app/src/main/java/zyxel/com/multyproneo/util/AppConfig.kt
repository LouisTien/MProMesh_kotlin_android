package zyxel.com.multyproneo.util

/**
 * Created by LouisTien on 2019/5/28.
 */
class AppConfig
{
    companion object
    {
        const val PERMISSION_LOCATION_REQUESTCODE = 1
        const val endDeviceListUpdateTime = 60
        const val guestWiFiSettingTime = 20
        const val rebootTime = 20
        const val keepScreenTime = 300
        const val addExtenderTime = 180

        enum class LoadingAnimation
        {
            ANIM_REBOOT,
            ANIM_SEARCH,
            ANIM_NOFOUND
        }

        enum class LoadingGoToPage
        {
            FRAG_SEARCH,
            FRAG_HOME
        }

        enum class DialogAction
        {
            ACT_NONE,
            ACT_REBOOT,
            ACT_DELETE_DEVICE,
            ACT_BLOCK_DEVICE,
            ACT_DELETE_ZYXEL_DEVICE,
            ACT_LOCATION_PERMISSION,
            ACT_LOGOUT
        }
    }
}