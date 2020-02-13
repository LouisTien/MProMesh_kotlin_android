package zyxel.com.multyproneo.dialog

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import kotlinx.android.synthetic.main.dialog_login_help.*
import zyxel.com.multyproneo.R

class SetupLoginHelpDialog(context: Context) : Dialog(context)
{
    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_login_help)
        setCancelable(false)
        login_help_close_image.setOnClickListener{
            dismiss()
        }
    }
}