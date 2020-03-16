package zyxel.com.multyproneo.dialog

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import kotlinx.android.synthetic.main.dialog_gateway_status.*
import zyxel.com.multyproneo.R

class GatewayStatusDialog(context: Context) : Dialog(context)
{
    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_gateway_status)
        setCancelable(false)
        gateway_status_close_image.setOnClickListener{
            dismiss()
        }
    }
}