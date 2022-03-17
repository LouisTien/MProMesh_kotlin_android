package zyxel.com.multyproneo.dialog

import android.content.Context
import android.os.Bundle
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.dialog_parental_control_setup_device.*
import org.jetbrains.anko.sdk27.coroutines.onClick
import zyxel.com.multyproneo.R
import zyxel.com.multyproneo.adapter.ParentalControlSelectDeviceItemAdapter
import zyxel.com.multyproneo.event.GlobalBus
import zyxel.com.multyproneo.event.ParentalControlEvent
import zyxel.com.multyproneo.util.GlobalData

class ParentalControlSetupDeviceSlideDialog(context: Context) : BottomSheetDialog(context)
{
    private lateinit var getParentalControlDeviceSelect: Disposable

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_parental_control_setup_device)
        setCancelable(true)

        window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        behavior.state = BottomSheetBehavior.STATE_EXPANDED

        parental_control_device_list.adapter = ParentalControlSelectDeviceItemAdapter(context, GlobalData.homeEndDeviceList)

        getParentalControlDeviceSelect = GlobalBus.listen(ParentalControlEvent.DeviceSelect::class.java).subscribe{ checkAddEnable() }

        parental_control_device_add_text.onClick{
            dismiss()
            GlobalBus.publish(ParentalControlEvent.SelectParentalControlDeviceComplete())
        }

        parental_control_device_cancel_text.onClick{ dismiss() }

        checkAddEnable()
    }

    override fun dismiss()
    {
        super.dismiss()
        if(!getParentalControlDeviceSelect.isDisposed) getParentalControlDeviceSelect.dispose()
    }

    private fun checkAddEnable()
    {
        var devCount = 0
        GlobalData.homeEndDeviceList.forEach{
            if(it.ParentalControlSelect)
                devCount += 1
        }

        parental_control_device_add_text.text = String.format(context.getString(R.string.parental_control_add), devCount.toString())

        if(devCount == 0)
        {
            parental_control_device_add_text.alpha = 0.3f
            parental_control_device_add_text.isEnabled = false
        }
        else
        {
            parental_control_device_add_text.alpha = 1f
            parental_control_device_add_text.isEnabled = true
        }
    }
}