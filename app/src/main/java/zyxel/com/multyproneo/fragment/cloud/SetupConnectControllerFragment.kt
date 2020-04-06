package zyxel.com.multyproneo.fragment.cloud

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.fragment_setup_connect_controller.*
import zyxel.com.multyproneo.R
import zyxel.com.multyproneo.dialog.MessageDialog
import zyxel.com.multyproneo.event.DialogEvent
import zyxel.com.multyproneo.event.GlobalBus
import zyxel.com.multyproneo.event.MainEvent
import zyxel.com.multyproneo.util.AppConfig

class SetupConnectControllerFragment : Fragment()
{
    private val TAG = javaClass.simpleName
    private lateinit var msgDialogPositiveResponse: Disposable

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
    {
        return inflater.inflate(R.layout.fragment_setup_connect_controller, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)

        msgDialogPositiveResponse = GlobalBus.listen(DialogEvent.OnPositiveBtn::class.java).subscribe{
            when(it.action)
            {
                AppConfig.DialogAction.ACT_GOTO_SETTING -> startActivity(Intent(Settings.ACTION_WIFI_SETTINGS))
                else -> {}
            }
        }

        setClickListener()
    }

    override fun onResume()
    {
        super.onResume()
        GlobalBus.publish(MainEvent.HideBottomToolbar())
    }

    override fun onPause()
    {
        super.onPause()
    }

    override fun onDestroyView()
    {
        super.onDestroyView()
        if(!msgDialogPositiveResponse.isDisposed) msgDialogPositiveResponse.dispose()
    }

    private val clickListener = View.OnClickListener{ view ->
        when(view)
        {
            setup_connect_controller_back_image -> GlobalBus.publish(MainEvent.SwitchToFrag(SetupControllerReadyFragment()))

            setup_connect_controller_manual_image ->
            {
                MessageDialog(
                        activity!!,
                        "",
                        getString(R.string.setup_connect_controller_setting_dialog),
                        arrayOf(getString(R.string.setup_connect_controller_setting_dialog_confirm), getString(R.string.setup_connect_controller_setting_dialog_cancel)),
                        AppConfig.DialogAction.ACT_GOTO_SETTING
                ).show()
            }

            setup_connect_controller_next_text ->
            {
                val bundle = Bundle().apply{
                    putBoolean("needConnectFlow", false)
                }

                GlobalBus.publish(MainEvent.SwitchToFrag(SetupConnectingControllerFragment().apply{ arguments = bundle }))
            }

            setup_connect_controller_scan_code_image -> GlobalBus.publish(MainEvent.SwitchToFrag(SetupConnectControllerScanFragment()))
        }
    }

    private fun setClickListener()
    {
        setup_connect_controller_back_image.setOnClickListener(clickListener)
        setup_connect_controller_manual_image.setOnClickListener(clickListener)
        setup_connect_controller_next_text.setOnClickListener(clickListener)
        setup_connect_controller_scan_code_image.setOnClickListener(clickListener)
    }
}