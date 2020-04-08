package zyxel.com.multyproneo.api.cloud

import okhttp3.MediaType
import okhttp3.Request
import okhttp3.RequestBody
import zyxel.com.multyproneo.BuildConfig
import zyxel.com.multyproneo.util.AppConfig

object AMDMApi
{
    private val JSON = MediaType.parse("application/json; charset=utf-8")

    /*
    curl -X POST https://hst-dm1.kalayservice.com/hestia/api/v2/auth/oauth_token?realm=zyxel -H 'authorization: Basic ZXoycXlNTllOQVE0aVVwSkZYM0hacGhHSVd2bnpqSUliaTVDMGVxbTpUSzRLbFZiVkgyV2hPdlpQUHhYUGpEdURVeTBocXdOVEc0NnM4bk5vSzFRWVJSYXVEUmxNZGhHVTN5c1FPSG93R1pnRVN4UW8wRGdoTFpLMXEzRll3TjFQR296TFZTWFE2RTdUVEh6a2M5bmlhdWlONTA1dngxNFpsWFJja2d5OQ==' -H 'content-type: application/x-www-form-urlencoded' -d 'grant_type=authorization_code&code=23eXpqNbEnwYU9I1wcqCdSzuXFCFId'

    {"access_token":"8hbdm5rdlmylmugzwj79","expires_in":86400,"refresh_token":"fyapmezdlmylmugzwkaa","scope":"hestia.dm","token_type":"Bearer"}
    or
    {"err_msg":"get amToken fails."}
     */
    class GetToken : TUTKCommander()
    {
        override fun composeRequest(): Request
        {
            val getTokenURL = "${BuildConfig.TUTK_DM_SITE}${BuildConfig.TUTK_API_VER}/auth/oauth_token?realm=${BuildConfig.TUTK_REALM}"
            return Request.Builder()
                    .headers(getHeaders().build())
                    .url(getTokenURL)
                    .post(getFormBody().build())
                    .build()
        }
    }

    /*
    curl -X POST https://hst-dm1.kalayservice.com/hestia/api/v2/auth/refresh_token -H 'authorization:Basic ZXoycXlNTllOQVE0aVVwSkZYM0hacGhHSVd2bnpqSUliaTVDMGVxbTpUSzRLbFZiVkgyV2hPdlpQUHhYUGpEdURVeTBocXdOVEc0NnM4bk5vSzFRWVJSYXVEUmxNZGhHVTN5c1FPSG93R1pnRVN4UW8wRGdoTFpLMXEzRll3TjFQR296TFZTWFE2RTdUVEh6a2M5bmlhdWlONTA1dngxNFpsWFJja2d5OQ==' -H 'content-type: application/x-www-form-urlencoded' -d 'grant_type=refresh_token&refresh_token=fyapmezdlmylmugzwkaa'

    {"access_token":"b42tcbzdlmylmugzwlb9","expires_in":86400,"refresh_token":"zrjmjvzdlmylmugzwlca","scope":"hestia.dm","token_type":"Bearer"}
     */
    class RefreshToken : TUTKCommander()
    {
        override fun composeRequest(): Request
        {
            val refreshTokenURL = "${BuildConfig.TUTK_DM_SITE}${BuildConfig.TUTK_API_VER}/auth/refresh_token"
            return Request.Builder()
                    .headers(getHeaders().build())
                    .url(refreshTokenURL)
                    .post(getFormBody().build())
                    .build()
        }
    }

    /*
    curl -X GET  https://hst-dm1.kalayservice.com/hestia/api/v2/dm/account -H 'authorization: Bearer b42tcbzdlmylmugzwlb9'

    {"email":"louis.tien@zyxel.com.tw"}
    */
    class GetUserInfo : TUTKCommander()
    {
        override fun composeRequest(): Request
        {
            val getUserInfoURL = "${BuildConfig.TUTK_DM_SITE}${BuildConfig.TUTK_API_VER}/dm/account"
            return Request.Builder()
                    .headers(getHeaders().build())
                    .url(getUserInfoURL)
                    .build()
        }
    }

    /*
    curl -X GET https://hst-dm1.kalayservice.com/hestia/api/v2/dm/devices -H 'authorization:Bearer b42tcbzdlmylmugzwlb9'

    {"data":[{"udid":"D0011IPC000000000011SN000000000000000002","displayName":"LouisOffice","fwVer":"v1.0.0","dmToken":"1k1e1cjdlmylmugzwl4a","credential":"***********************","state":0},{"udid":"D0011IPC000000000011SN000000000000000004","displayName":"LouisOffice","fwVer":"v1.0.0","dmToken":"h8pn5xzdlmylmugzwl5a","credential":"***********************","state":0},{"udid":"EBKUAX3MUD7M9G6GU1CJ","displayName":"LouisOffice","fwVer":"v1.0.0","dmToken":"fw9xyujdlmylmugzw8ga","credential":"***********************","state":0}]}
    */
    class GetAllDevice : TUTKCommander()
    {
        override fun composeRequest(): Request
        {
            val getAllDeviceURL = "${BuildConfig.TUTK_DM_SITE}${BuildConfig.TUTK_API_VER}/dm/devices"
            return Request.Builder()
                    .headers(getHeaders().build())
                    .url(getAllDeviceURL)
                    .build()
        }
    }

    /*
    curl -X GET https://hst-dm1.kalayservice.com/hestia/api/v2/dm/devices/D0011IPC000000000011SN000000000000000005 -H 'authorization: Bearer b42tcbzdlmylmugzwlb9’

    {"data":{"udid":"D0011IPC000000000011SN000000000000000005","displayName":"LouisOffice","fwVer":"v1.0.0","dmToken":"3l5brcc61gx4ydwcz5x9","credential":"***********************","state":0}}
    */
    class GetSpecificDevice(val udid: String = "N/A") : TUTKCommander()
    {
        override fun composeRequest(): Request
        {
            val getSpecificDeviceURL = "${BuildConfig.TUTK_DM_SITE}${BuildConfig.TUTK_API_VER}/dm/devices/$udid"
            return Request.Builder()
                    .headers(getHeaders().build())
                    .url(getSpecificDeviceURL)
                    .build()
        }
    }

    /*
    curl -X POST https://hst-dm1.kalayservice.com/hestia/api/v2/dm/devices -H 'authorization:Bearer b42tcbzdlmylmugzwlb9' -H 'content-type:application/json' -d '{"udid":"D0011IPC000000000011SN000000000000000005","fwVer":"v1.0.0","displayName":"LouisOffice","credential":"***********************"}'

    {"data":{"dmToken":"b16yd6bdlmylmugzwl99","credential":null,"state":0}}
     */
    class AddDevice : TUTKCommander()
    {
        override fun composeRequest(): Request
        {
            val addDeviceURL = "${BuildConfig.TUTK_DM_SITE}${BuildConfig.TUTK_API_VER}/dm/devices"
            val requestParam = RequestBody.create(JSON, getParams().toString())
            return Request.Builder()
                    .headers(getHeaders().build())
                    .url(addDeviceURL)
                    .post(requestParam)
                    .build()
        }
    }

    /*
    curl -X PATCH https://hst-dm1.kalayservice.com/hestia/api/v2/dm/devices/D0011IPC000000000011SN000000000000000005 -H 'authorization: Bearer b42tcbzdlmylmugzwlb9' -H 'content-type: application/json' -d '{"fwVer": "v2.0.0","displayName": "Amykitchen","credential": "jadfdjgaijgaiopjgiajgijopagjgfpajgopgako"}'

    {"data":{"_id":"5e26cb3e6740680001797182","pk":"5e26cb3e6740680001797182","created":"2020-01-21T09:58:22.394Z","updated":"2020-01-21T09:58:22.394Z","account_id":6622668763420035183,"udid":"D0011IPC000000000011SN000000000000000005","displayName":"LouisOffice","fwVer":"v1.0.0","dmtoken":"3l5brcc61gx4ydwcz5x9","credential":"***********************"}}
     */
    class UpdateDevice(val udid: String = "N/A") : TUTKCommander()
    {
        override fun composeRequest(): Request
        {
            val updateDeviceURL = "${BuildConfig.TUTK_DM_SITE}${BuildConfig.TUTK_API_VER}/dm/devices/$udid"
            val requestParam = RequestBody.create(JSON, getParams().toString())
            return Request.Builder()
                    .headers(getHeaders().build())
                    .url(updateDeviceURL)
                    .patch(requestParam)
                    .build()
        }
    }

    class DeleteDevice(val udid: String = "N/A") : TUTKCommander()
    {
        override fun composeRequest(): Request
        {
            val deleteDeviceURL = "${BuildConfig.TUTK_DM_SITE}${BuildConfig.TUTK_API_VER}/dm/devices/$udid"
            return Request.Builder()
                    .headers(getHeaders().build())
                    .url(deleteDeviceURL)
                    .delete()
                    .build()
        }
    }
}