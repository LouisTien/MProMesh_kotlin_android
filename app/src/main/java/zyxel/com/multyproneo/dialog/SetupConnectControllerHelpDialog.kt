package zyxel.com.multyproneo.dialog

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import kotlinx.android.synthetic.main.dialog_connect_controller_help.*
import zyxel.com.multyproneo.R

class SetupConnectControllerHelpDialog(context: Context) : Dialog(context)
{
    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_connect_controller_help)
        setCancelable(true)
        connect_controller_help_close_image.setOnClickListener{ dismiss() }
    }
}