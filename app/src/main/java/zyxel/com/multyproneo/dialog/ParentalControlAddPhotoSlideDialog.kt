package zyxel.com.multyproneo.dialog

import android.content.Context
import android.os.Bundle
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.android.synthetic.main.dialog_parental_control_add_photo_menu.*
import org.jetbrains.anko.sdk27.coroutines.onClick
import zyxel.com.multyproneo.R
import zyxel.com.multyproneo.event.GlobalBus
import zyxel.com.multyproneo.event.ParentalControlEvent
import zyxel.com.multyproneo.util.AppConfig

class ParentalControlAddPhotoSlideDialog(context: Context) : BottomSheetDialog(context, R.style.AppBottomSheetDialogTheme)
{
    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_parental_control_add_photo_menu)
        window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        setCancelable(true)

        parental_control_add_photo_take_photo_relative.onClick {
            dismiss()
            GlobalBus.publish(ParentalControlEvent.HandleProfilePhoto(AppConfig.ProfilePhotoAction.PRO_PHOTO_TAKE))
        }

        parental_control_add_photo_select_photo_relative.onClick {
            dismiss()
            GlobalBus.publish(ParentalControlEvent.HandleProfilePhoto(AppConfig.ProfilePhotoAction.PRO_PHOTO_SELECT))
        }
    }
}