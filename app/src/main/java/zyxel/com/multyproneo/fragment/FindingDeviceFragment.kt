package zyxel.com.multyproneo.fragment

import android.content.Context
import android.content.Intent
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
import zyxel.com.multyproneo.util.AppConfig

/**
 * Created by LouisTien on 2019/5/28.
 */
class FindingDeviceFragment : Fragment()
{

    private lateinit var startWiFiSettingDisposable: Disposable
    private var retryTimes: Int = 0

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
    {
        return inflater.inflate(R.layout.fragment_loading_transition, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)

        startWiFiSettingDisposable = GlobalBus.listen(DialogEvent.OnPositiveBtn::class.java).subscribe{
            startActivity(Intent(Settings.ACTION_WIFI_SETTINGS))
        }

        loading_retry_image.setOnClickListener {
            retryTimes = 6
            startFindDevice()
        }
    }

    override fun onResume()
    {
        super.onResume()
        GlobalBus.publish(MainEvent.HideBottomToolbar())

        val wifiManager = activity?.applicationContext?.getSystemService(Context.WIFI_SERVICE) as WifiManager
        if(!wifiManager.isWifiEnabled)
        {
            val msgDialog = MessageDialog(
                    activity!!,
                    getString(R.string.message_dialog_not_connect),
                    getString(R.string.message_dialog_not_connect_try_again),
                    arrayOf(getString(R.string.message_dialog_ok)),
                    AppConfig.Companion.DialogAction.ACT_NONE
            )
            msgDialog.show()
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
    }

    override fun onDestroyView()
    {
        super.onDestroyView()
        if(!startWiFiSettingDisposable.isDisposed) startWiFiSettingDisposable.dispose()
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
        doAsync {
            retryTimes++

            uiThread {
                loading_animation_view.setAnimation("searching.json")
                loading_animation_view.playAnimation()
            }
        }
    }
}