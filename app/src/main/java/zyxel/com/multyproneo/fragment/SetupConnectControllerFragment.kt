package zyxel.com.multyproneo.fragment

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.provider.Settings
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.zxing.ResultPoint
import com.journeyapps.barcodescanner.BarcodeCallback
import com.journeyapps.barcodescanner.BarcodeResult
import com.journeyapps.barcodescanner.CaptureManager
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.fragment_setup_connect_controller.*
import zyxel.com.multyproneo.R
import zyxel.com.multyproneo.dialog.MessageDialog
import zyxel.com.multyproneo.dialog.SetupConnectControllerHelpDialog
import zyxel.com.multyproneo.event.DialogEvent
import zyxel.com.multyproneo.event.GlobalBus
import zyxel.com.multyproneo.event.MainEvent
import zyxel.com.multyproneo.util.AppConfig
import zyxel.com.multyproneo.util.GlobalData
import zyxel.com.multyproneo.util.LogUtil

class SetupConnectControllerFragment : Fragment()
{
    private val TAG = javaClass.simpleName
    private var availableScan = true
    private lateinit var captureManager: CaptureManager
    private lateinit var msgDialogPositiveResponse: Disposable
    private lateinit var msgDialogCancelResponse: Disposable

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
    {
        return inflater.inflate(R.layout.fragment_setup_connect_controller, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)

        captureManager = CaptureManager(activity, setup_connect_controller_qrcodeView)
        captureManager.initializeFromIntent(Intent(activity, SetupConnectControllerFragment::class.java), savedInstanceState)
        setup_connect_controller_qrcodeView.decodeContinuous(object: BarcodeCallback
        {
            override fun barcodeResult(result: BarcodeResult?)
            {
                if(availableScan)
                {
                    availableScan = false

                    doVibrate()

                    LogUtil.d(TAG,"QRCode result:$result")
                    //WIFI:T:WPA;S:EX5510-B0;P:12345678;LoginA:admin;LoginP:12345678;
                    val parts = result!!.text.split(":", ";")
                    if( parts.size == 12
                        && parts[0] == "WIFI"
                        && parts[1] == "T"
                        && parts[3] == "S"
                        && parts[5] == "P"
                        && parts[7] == "LoginA"
                        && parts[9] == "LoginP" )
                    {
                        GlobalData.scanSSID = parts[4]
                        GlobalData.scanPWD = parts[6]
                        GlobalData.scanAccount = parts[8]
                        GlobalData.scanAccountPWD = parts[10]

                        LogUtil.d(TAG,"QRCode SSID:${GlobalData.scanSSID}")
                        LogUtil.d(TAG,"QRCode PWD:${GlobalData.scanPWD}")
                        LogUtil.d(TAG,"QRCode Account:${GlobalData.scanAccount}")
                        LogUtil.d(TAG,"QRCode AccountPWD:${GlobalData.scanAccountPWD}")

                        MessageDialog(
                                activity!!,
                                "",
                                getString(R.string.setup_connect_controller_scan_ok_dialog_description) + " 「${GlobalData.scanSSID}」 ?",
                                arrayOf(getString(R.string.setup_connect_controller_scan_ok_dialog_confirm), getString(R.string.setup_connect_controller_setting_dialog_cancel)),
                                AppConfig.DialogAction.ACT_QRCODE_SCAN_OK
                        ).show()
                    }
                    else
                    {
                        MessageDialog(
                                activity!!,
                                getString(R.string.setup_connect_controller_format_error_dialog_title),
                                getString(R.string.setup_connect_controller_format_error_dialog_description),
                                arrayOf(getString(R.string.setup_connect_controller_format_error_dialog_confirm)),
                                AppConfig.DialogAction.ACT_QRCODE_SCAN_ERROR
                        ).show()
                    }
                }
            }

            override fun possibleResultPoints(resultPoints: MutableList<ResultPoint>?)
            {

            }
        })

        msgDialogPositiveResponse = GlobalBus.listen(DialogEvent.OnPositiveBtn::class.java).subscribe{
            when(it.action)
            {
                AppConfig.DialogAction.ACT_GOTO_SETTING -> startActivity(Intent(Settings.ACTION_WIFI_SETTINGS))
                AppConfig.DialogAction.ACT_QRCODE_SCAN_ERROR -> availableScan = true
                AppConfig.DialogAction.ACT_QRCODE_SCAN_OK -> GlobalBus.publish(MainEvent.SwitchToFrag(SetupConnectingControllerFragment()))
            }
        }

        msgDialogCancelResponse = GlobalBus.listen(DialogEvent.OnCancelBtn::class.java).subscribe{
            when(it.action)
            {
                AppConfig.DialogAction.ACT_GOTO_SETTING, AppConfig.DialogAction.ACT_QRCODE_SCAN_HELP -> availableScan = true
            }
        }

        setClickListener()
    }

    override fun onResume()
    {
        super.onResume()
        availableScan = true
        captureManager.onResume()
        GlobalBus.publish(MainEvent.HideBottomToolbar())
    }

    override fun onPause()
    {
        super.onPause()
        availableScan = false
        captureManager.onPause()
    }

    override fun onDestroyView()
    {
        super.onDestroyView()
        captureManager.onDestroy()
        if(!msgDialogPositiveResponse.isDisposed) msgDialogPositiveResponse.dispose()
        if(!msgDialogCancelResponse.isDisposed) msgDialogCancelResponse.dispose()
    }

    private val clickListener = View.OnClickListener{ view ->
        when(view)
        {
            setup_connect_controller_back_image -> GlobalBus.publish(MainEvent.SwitchToFrag(SetupControllerReadyFragment()))

            setup_connect_controller_qrcode_image -> {}

            setup_connect_controller_edit_image ->
            {
                availableScan = false

                MessageDialog(
                        activity!!,
                        "",
                        getString(R.string.setup_connect_controller_setting_dialog),
                        arrayOf(getString(R.string.setup_connect_controller_setting_dialog_confirm), getString(R.string.setup_connect_controller_setting_dialog_cancel)),
                        AppConfig.DialogAction.ACT_GOTO_SETTING
                ).show()
            }

            setup_connect_controller_help_image ->
            {
                availableScan = false
                SetupConnectControllerHelpDialog(activity!!).show()
            }
        }
    }

    private fun setClickListener()
    {
        setup_connect_controller_back_image.setOnClickListener(clickListener)
        setup_connect_controller_help_image.setOnClickListener(clickListener)
        setup_connect_controller_qrcode_image.setOnClickListener(clickListener)
        setup_connect_controller_edit_image.setOnClickListener(clickListener)
        setup_connect_controller_next_image.setOnClickListener(clickListener)
    }

    private fun doVibrate()
    {
        val vib: Vibrator = activity!!.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

        if(vib.hasVibrator())
        {
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            {
                vib.vibrate(
                        VibrationEffect.createOneShot(
                                100,
                                // The default vibration strength of the device.
                                VibrationEffect.DEFAULT_AMPLITUDE
                        )
                )
            }
            else
            {
                // This method was deprecated in API level 26
                vib.vibrate(100)
            }
        }
    }
}