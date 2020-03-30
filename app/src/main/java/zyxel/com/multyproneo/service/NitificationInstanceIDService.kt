package zyxel.com.multyproneo.service

import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.iid.FirebaseInstanceIdService
import zyxel.com.multyproneo.util.AppConfig
import zyxel.com.multyproneo.util.LogUtil
import zyxel.com.multyproneo.util.SharedPreferencesUtil

class NitificationInstanceIDService : FirebaseInstanceIdService()
{
    private val TAG = javaClass.simpleName

    override fun onTokenRefresh()
    {
        var notificationToken by SharedPreferencesUtil(this, AppConfig.SHAREDPREF_NOTIFICATION_TOKEN, "")
        notificationToken = FirebaseInstanceId.getInstance().getToken()?:""
        LogUtil.d(TAG, "Refreshed token: " + notificationToken)
    }
}