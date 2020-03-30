package zyxel.com.multyproneo.api.cloud

import android.content.Context
import okhttp3.Request
import zyxel.com.multyproneo.util.AppConfig

object NotificationApi
{
    class Common(var context: Context) : TUTKCommander()
    {
        override fun composeRequest(): Request
        {
            val getRegisterURL = AppConfig.TUTK_NOTI_KPNS
            return Request.Builder()
                    .headers(getHeaders().build())
                    .url(getRegisterURL)
                    .post(getFormBody().build())
                    .build()
        }
    }
}