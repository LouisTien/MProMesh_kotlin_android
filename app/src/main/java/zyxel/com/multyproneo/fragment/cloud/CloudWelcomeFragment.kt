package zyxel.com.multyproneo.fragment.cloud

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.os.Bundle
import android.os.Handler
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import io.reactivex.disposables.Disposable
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import zyxel.com.multyproneo.R
import zyxel.com.multyproneo.api.cloud.TUTKP2PBaseApi
import zyxel.com.multyproneo.dialog.MessageDialog
import zyxel.com.multyproneo.event.DialogEvent
import zyxel.com.multyproneo.event.GlobalBus
import zyxel.com.multyproneo.event.MainEvent
import zyxel.com.multyproneo.fragment.FindingDeviceFragment
import zyxel.com.multyproneo.util.*

class CloudWelcomeFragment : Fragment()
{
    private val TAG = javaClass.simpleName
    private lateinit var startWiFiSettingDisposable: Disposable
    private lateinit var db: DatabaseCloudUtil

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
    {
        return inflater.inflate(R.layout.fragment_cloud_welcome, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)
        TUTKP2PBaseApi.stopSession()
    }

    override fun onResume()
    {
        super.onResume()

        GlobalBus.publish(MainEvent.HideBottomToolbar())

        startWiFiSettingDisposable = GlobalBus.listen(DialogEvent.OnPositiveBtn::class.java).subscribe{
            startActivity(Intent(Settings.ACTION_WIFI_SETTINGS))
        }

        /*if(!isNetworkAvailable())
        {
            MessageDialog(
                    activity!!,
                    "",
                    getString(R.string.message_dialog_wifi_off),
                    arrayOf(getString(R.string.message_dialog_ok)),
                    AppConfig.DialogAction.ACT_NONE
            ).show()
        }
        else
            Handler().postDelayed({ decideFlow() }, AppConfig.WELCOME_DISPLAY_TIME_IN_MILLISECONDS)*/

        Handler().postDelayed({ decideFlow() }, AppConfig.WELCOME_DISPLAY_TIME_IN_MILLISECONDS)
    }

    override fun onPause()
    {
        super.onPause()
        if(!startWiFiSettingDisposable.isDisposed) startWiFiSettingDisposable.dispose()
    }

    override fun onDestroyView()
    {
        super.onDestroyView()
    }

    private fun isNetworkAvailable(): Boolean
    {
        val connectivityManager = activity!!.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager?
        val wifiNetInfo = connectivityManager!!.getNetworkInfo(ConnectivityManager.TYPE_WIFI)
        return wifiNetInfo.isAvailable && wifiNetInfo.isConnected
    }

    private fun decideFlow()
    {
        var oldDBExist = false
        var newDBExist = false

        doAsync{
            val gatewayInfoArrayListDB = DatabaseUtil.getInstance(activity!!)?.getGatewayFromDB()
            if(gatewayInfoArrayListDB!!.isNotEmpty())
            {
                LogUtil.d(TAG,"old DB has data!!")
                oldDBExist = true
            }
            else
                LogUtil.d(TAG,"old DB is empty!!")

            val db = DatabaseCloudUtil.getInstance(activity!!)!!
            val siteInfoList = db.getSiteInfoDao().getAll()
            if(siteInfoList.isNotEmpty())
            {
                LogUtil.d(TAG,"new DB has data!!")
                newDBExist = true
            }
            else
                LogUtil.d(TAG,"new DB is empty!!")

            uiThread{
                if(oldDBExist)
                    GlobalBus.publish(MainEvent.SwitchToFrag(FindingDeviceFragment()))
                else if(newDBExist)
                    GlobalBus.publish(MainEvent.RefreshToken(false))
                else
                {
                    val firstTimeUse by SharedPreferencesUtil(activity!!, AppConfig.SHAREDPREF_SETUP_FIRST_TIME_KEY, true)
                    if(firstTimeUse)
                        GlobalBus.publish(MainEvent.SwitchToFrag(CloudFirstTimeUsingFragment()))
                    else
                        GlobalBus.publish(MainEvent.SwitchToFrag(SetupControllerReadyFragment()))
                }
            }
        }
    }
}