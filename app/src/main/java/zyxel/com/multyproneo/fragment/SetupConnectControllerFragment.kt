package zyxel.com.multyproneo.fragment

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.zxing.ResultPoint
import com.journeyapps.barcodescanner.BarcodeCallback
import com.journeyapps.barcodescanner.BarcodeResult
import com.journeyapps.barcodescanner.CaptureManager
import kotlinx.android.synthetic.main.fragment_setup_connect_controller.*
import zyxel.com.multyproneo.R
import zyxel.com.multyproneo.dialog.SetupConnectControllerHelpDialog
import zyxel.com.multyproneo.event.GlobalBus
import zyxel.com.multyproneo.event.MainEvent
import zyxel.com.multyproneo.util.LogUtil

class SetupConnectControllerFragment : Fragment()
{
    private val TAG = javaClass.simpleName
    private var QRCodeScanMode = false
    private lateinit var captureManager: CaptureManager
    private lateinit var helpDlg: SetupConnectControllerHelpDialog

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
                result?.let{
                    LogUtil.d(TAG,"QRCode result:${it.text}")

                    val vib: Vibrator = activity!!.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

                    if(vib.hasVibrator())
                    {
                        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                        {
                            // void vibrate (VibrationEffect vibe)
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

            override fun possibleResultPoints(resultPoints: MutableList<ResultPoint>?)
            {

            }
        })

        setClickListener()
    }

    override fun onResume()
    {
        super.onResume()
        captureManager.onResume()
        GlobalBus.publish(MainEvent.HideBottomToolbar())
    }

    override fun onPause()
    {
        super.onPause()
        captureManager.onPause()
    }

    override fun onDestroyView()
    {
        super.onDestroyView()
        captureManager.onDestroy()
    }

    private fun updateUI()
    {
        when(QRCodeScanMode)
        {
            true ->
            {
                setup_connect_controller_qrcodeView.visibility = View.VISIBLE
                setup_connect_controller_editView.visibility = View.GONE
                setup_connect_controller_qrcode_image.setImageResource(R.drawable.icon_qrcode_on)
                setup_connect_controller_edit_image.setImageResource(R.drawable.icon_edit_off)
            }

            false ->
            {
                setup_connect_controller_qrcodeView.visibility = View.GONE
                setup_connect_controller_editView.visibility = View.VISIBLE
                setup_connect_controller_qrcode_image.setImageResource(R.drawable.icon_qrcode_off)
                setup_connect_controller_edit_image.setImageResource(R.drawable.icon_edit_on)
            }
        }
    }

    private val clickListener = View.OnClickListener{ view ->
        when(view)
        {
            setup_connect_controller_back_image -> GlobalBus.publish(MainEvent.SwitchToFrag(SetupControllerReadyFragment()))

            setup_connect_controller_qrcode_image, setup_connect_controller_edit_image ->
            {
                QRCodeScanMode = !QRCodeScanMode
                updateUI()
            }

            setup_connect_controller_help_image ->
            {
                helpDlg = SetupConnectControllerHelpDialog(activity!!)
                helpDlg.show()
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
}