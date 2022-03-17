package zyxel.com.multyproneo.dialog

import android.content.Context
import android.os.Bundle
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.android.synthetic.main.dialog_parental_control_device_action_menu.*
import org.jetbrains.anko.sdk27.coroutines.onClick
import zyxel.com.multyproneo.R
import zyxel.com.multyproneo.event.GlobalBus
import zyxel.com.multyproneo.event.ParentalControlEvent
import zyxel.com.multyproneo.model.DevicesInfoObject

class ParentalControlSetupDeviceActionMenuSlideDialog
(
        context: Context,
        var deviceInfo: DevicesInfoObject
) : BottomSheetDialog(context, R.style.AppBottomSheetDialogTheme)
{
    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_parental_control_device_action_menu)
        window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        setCancelable(true)

        parental_control_device_action_name_text.text = deviceInfo.getName()

        parental_control_device_action_delete_relative.onClick {
            dismiss()
            GlobalBus.publish(ParentalControlEvent.DeviceDelete(deviceInfo))
        }
    }
}