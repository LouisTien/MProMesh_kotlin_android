package zyxel.com.multyproneo.tool

import android.app.Service
import android.content.Intent
import android.os.IBinder
import org.json.JSONObject
import zyxel.com.multyproneo.api.AccountApi
import zyxel.com.multyproneo.api.Commander
import zyxel.com.multyproneo.util.GlobalData
import zyxel.com.multyproneo.util.LogUtil


class OnClearFromRecentService : Service()
{
    private val TAG = "OnClearFromRecentService"

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int
    {
        LogUtil.d(TAG, "onStartCommand")
        //return super.onStartCommand(intent, flags, startId)
        return START_NOT_STICKY
    }

    override fun onTaskRemoved(rootIntent: Intent?)
    {
        //super.onTaskRemoved(rootIntent)
        LogUtil.d(TAG, "onTaskRemoved")

        if(GlobalData.loginInfo.sessionkey != "")
        {
            val params = JSONObject()
            AccountApi.Logout()
                    .setRequestPageName(TAG)
                    .setParams(params)
                    .setResponseListener(object: Commander.ResponseListener()
                    {
                        override fun onSuccess(responseStr: String)
                        {
                            LogUtil.d(TAG, "[Logout]onSuccess")
                        }
                    }).execute()
        }
    }

    override fun onBind(intent: Intent?): IBinder?
    {
        return null
    }

    override fun onDestroy()
    {
        LogUtil.d(TAG, "onDestroy")
        super.onDestroy()
    }
}