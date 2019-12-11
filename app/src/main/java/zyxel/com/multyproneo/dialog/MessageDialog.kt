package zyxel.com.multyproneo.dialog

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.View
import kotlinx.android.synthetic.main.dialog_message.*
import zyxel.com.multyproneo.R
import zyxel.com.multyproneo.event.DialogEvent
import zyxel.com.multyproneo.event.GlobalBus
import zyxel.com.multyproneo.util.AppConfig

/**
 * Created by LouisTien on 2019/5/28.
 */
class MessageDialog(context: Context, private var title: String, var description: String, private var btnTexts: Array<String>, private var action: AppConfig.DialogAction) : Dialog(context)
{
    private var alwaysBlock = false

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_message)
        setCancelable(true)
        setClickListener()
    }

    override fun show()
    {
        super.show()

        if(title.isEmpty())
            msg_alert_title.visibility = View.GONE
        else
            msg_alert_title.text = title

        msg_alert_description.text = description
        msg_alert_positive.text = btnTexts[0]

        if(btnTexts.size >= 2)
        {
            msg_alert_cancel.visibility = View.VISIBLE
            msg_alert_cancel.text = btnTexts[1]
        }
        else
            msg_alert_cancel.visibility = View.GONE

        if(action == AppConfig.DialogAction.ACT_BLOCK_DEVICE)
            block_check_linear.visibility = View.VISIBLE
        else
            block_check_linear.visibility = View.GONE
    }

    private val clickListener = View.OnClickListener{ view ->
        when(view)
        {
            msg_alert_positive ->
            {
                GlobalBus.publish(DialogEvent.OnPositiveBtn(action, alwaysBlock))
                dismiss()
            }

            msg_alert_cancel -> dismiss()

            block_check_image ->
            {
                alwaysBlock = !alwaysBlock
                if(alwaysBlock)
                    block_check_image.setImageResource(R.drawable.checkbox_check)
                else
                    block_check_image.setImageResource(R.drawable.checkbox_uncheck)
            }
        }
    }

    private fun setClickListener()
    {
        msg_alert_positive.setOnClickListener(clickListener)
        msg_alert_cancel.setOnClickListener(clickListener)
        block_check_image.setOnClickListener(clickListener)
    }
}