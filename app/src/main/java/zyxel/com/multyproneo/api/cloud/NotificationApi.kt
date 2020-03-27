package zyxel.com.multyproneo.api.cloud

import android.content.Context
import android.provider.Settings
import okhttp3.Request
import zyxel.com.multyproneo.util.AppConfig
import zyxel.com.multyproneo.util.SharedPreferencesUtil
import java.util.*

object NotificationApi
{
    class Register(var context: Context) : TUTKCommander()
    {
        override fun composeRequest(): Request
        {
            var notificationToken by SharedPreferencesUtil(context, AppConfig.SHAREDPREF_NOTIFICATION_TOKEN, "")
            var phoneUdid = Settings.System.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
            //val getRegisterURL = "${AppConfig.TUTK_NOTI_KPNS}/tpns?cmd=client&os=android&appid=${AppConfig.NOTI_APP_ID}&udid=${phoneUdid}&token=${notificationToken}&lang=${Locale.getDefault().language}&bgfetch=1&dev=0"
            val getRegisterURL = "${AppConfig.TUTK_NOTI_KPNS}/tpns"
            return Request.Builder()
                    .headers(getHeaders().build())
                    .url(getRegisterURL)
                    .post(getFormBody().build())
                    .build()
        }
    }

    class Mapping(var context: Context) : TUTKCommander()
    {
        override fun composeRequest(): Request
        {
            var phoneUdid = Settings.System.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
            //val getMappingURL = "${AppConfig.TUTK_NOTI_KPNS}/tpns?cmd=mapping&os=android&appid=${AppConfig.NOTI_APP_ID}&uid=E7KA952WU5RMUH6GY1CJ&udid=${phoneUdid}&format=e2Rldl9uYW1lfSB7ZXZlbnRfdHlwZX0NCnttc2d9&interval=3&customized_payload=eyJjb250ZW50X2F2YWlsYWJsZSI6dHJ1ZSwibm90aWZpY2F0aW9uIjp7InRpdGxlIjp7JU1ZVE9QSUMlfSwiYm9keSI6eyVNWUJPRFklfX19"
            val getMappingURL = "${AppConfig.TUTK_NOTI_KPNS}/tpns"
            return Request.Builder()
                    .headers(getHeaders().build())
                    .url(getMappingURL)
                    .post(getFormBody().build())
                    .build()
        }
    }
}