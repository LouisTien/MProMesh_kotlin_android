package zyxel.com.multyproneo.fragment

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.wifi.WifiManager
import android.os.Bundle
import android.provider.Settings
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.fragment_loading_transition.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import zyxel.com.multyproneo.R
import zyxel.com.multyproneo.dialog.MessageDialog
import zyxel.com.multyproneo.event.DialogEvent
import zyxel.com.multyproneo.event.GlobalBus
import zyxel.com.multyproneo.event.MainEvent
import zyxel.com.multyproneo.model.GatewayProfile
import zyxel.com.multyproneo.util.SocketControllerUtil
import zyxel.com.multyproneo.util.AppConfig
import zyxel.com.multyproneo.util.DatabaseUtil
import zyxel.com.multyproneo.util.GlobalData
import zyxel.com.multyproneo.util.LogUtil

/**
 * Created by LouisTien on 2019/5/28.
 */
class FindingDeviceFragment : Fragment()
{
    private val TAG = javaClass.simpleName
    private lateinit var startWiFiSettingDisposable: Disposable
    private var userDefineName = ""
    private var retryTimes = 0

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
    {
        return inflater.inflate(R.layout.fragment_loading_transition, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)

        loading_retry_image.setOnClickListener{
            retryTimes = 6
            startFindDevice()
        }
    }

    override fun onResume()
    {
        super.onResume()

        GlobalBus.publish(MainEvent.HideBottomToolbar())

        startWiFiSettingDisposable = GlobalBus.listen(DialogEvent.OnPositiveBtn::class.java).subscribe{
            startActivity(Intent(Settings.ACTION_WIFI_SETTINGS))
        }

        if(!isNetworkAvailable())
        {
            MessageDialog(
                    activity!!,
                    getString(R.string.message_dialog_not_connect),
                    getString(R.string.message_dialog_not_connect_try_again),
                    arrayOf(getString(R.string.message_dialog_ok)),
                    AppConfig.DialogAction.ACT_NONE
            ).show()
        }
        else
        {
            retryTimes = 0
            startFindDevice()
        }
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

    private fun setFindDeviceUI()
    {
        loading_retry_image.visibility = View.INVISIBLE
        loading_msg_title_text.visibility = View.INVISIBLE
        loading_msg_working_text.visibility = View.VISIBLE
        loading_msg_status_text.text = getString(R.string.find_device_status_description)
    }

    private fun setNotFindDeviceUI()
    {
        loading_retry_image.visibility = View.VISIBLE
        loading_msg_title_text.visibility = View.VISIBLE
        loading_msg_working_text.visibility = View.INVISIBLE
        loading_msg_status_text.text = getString(R.string.find_device_no_result_description)
    }

    private fun startFindDevice()
    {
        setFindDeviceUI()
        runSearchTask()
    }

    private fun runSearchTask()
    {
        loading_animation_view.setAnimation("searching.json")
        loading_animation_view.playAnimation()

        doAsync{
            SocketControllerUtil.instance.deviceScan()

            var res = false
            val newGatewayProfileMutableList = mutableListOf<GatewayProfile>(
                    GatewayProfile(
                            modelName = "EMG6726-B10A",
                            systemName = "EMG6726-B10A",
                            IP = "192.168.1.1",
                            firmwareVersion = "V5.13(ABNP.1)b2",
                            serial = "S180Y21006075",
                            userDefineName = "EMG6726-B10A",
                            initFlag = -1,
                            multyFlag = 1,
                            internetProtocol = "HTTP",
                            customer = "ZYXEL"
                    ),
                    GatewayProfile(
                            modelName = "WAP6804",
                            systemName = "zyxelsetup",
                            IP = "192.168.1.151",
                            firmwareVersion = "1.00(ABKH.6)C0",
                            serial = "S170Y32040619",
                            userDefineName = "WAP6804",
                            multyFlag = 1,
                            type = -1
                    )
            )

            for(i in newGatewayProfileMutableList.indices)
            {
                userDefineName = DatabaseUtil.getInstance(activity!!)?.getDeviceUserDefineNameFromDB(newGatewayProfileMutableList[i].serial)!!
                LogUtil.d(TAG, "userDefineName from DB:$userDefineName")

                if(userDefineName == "")
                    newGatewayProfileMutableList[i].userDefineName = newGatewayProfileMutableList[i].modelName
                else
                    newGatewayProfileMutableList[i].userDefineName = userDefineName
            }

            retryTimes++
            res = true
            Thread.sleep(3000)

            uiThread{
                if(res)
                {
                    GlobalData.gatewayProfileMutableList = newGatewayProfileMutableList.toMutableList()//copy list to global data
                    GlobalBus.publish(MainEvent.SwitchToFrag(GatewayListFragment()))
                }
                else
                {
                    loading_animation_view.setAnimation("nofound.json")
                    loading_animation_view.playAnimation()
                    setNotFindDeviceUI()
                }
            }
        }
    }
}