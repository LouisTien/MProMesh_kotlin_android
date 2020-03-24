package zyxel.com.multyproneo.dialog

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.View
import kotlinx.android.synthetic.main.dialog_remove_site.*
import zyxel.com.multyproneo.R
import zyxel.com.multyproneo.event.CloudAccountEvent
import zyxel.com.multyproneo.event.GlobalBus
import zyxel.com.multyproneo.model.cloud.AllDeviceInfo

class RemoveSiteDialog(context: Context, private var info: AllDeviceInfo, private var isSelf: Boolean) : Dialog(context)
{
    private var preserved_set = true

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_remove_site)
        setCancelable(false)
        setClickListener()
    }

    override fun show()
    {
        super.show()

        remove_site_alert_title.text = context.getString(R.string.remove_site_dialog_title, info.displayName)

        if(isSelf)
            remove_site_alert_description.text = context.getString(R.string.remove_site_dialog_description_self)
        else
            remove_site_alert_description.text = context.getString(R.string.remove_site_dialog_description)
    }

    private val clickListener = View.OnClickListener{ view ->
        when(view)
        {
            remove_site_alert_cancel -> dismiss()

            remove_site_alert_remove ->
            {
                GlobalBus.publish(CloudAccountEvent.ConfirmSiteDelete(info, preserved_set))
                dismiss()
            }

            preserved_settings_check_image ->
            {
                preserved_set = !preserved_set
                if(preserved_set)
                    preserved_settings_check_image.setImageResource(R.drawable.icon_check_on)
                else
                    preserved_settings_check_image.setImageResource(R.drawable.icon_check_off)
            }
        }
    }

    private fun setClickListener()
    {
        remove_site_alert_remove.setOnClickListener(clickListener)
        remove_site_alert_cancel.setOnClickListener(clickListener)
        preserved_settings_check_image.setOnClickListener(clickListener)
    }
}