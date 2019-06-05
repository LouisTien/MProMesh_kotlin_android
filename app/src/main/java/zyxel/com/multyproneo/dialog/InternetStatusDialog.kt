package zyxel.com.multyproneo.dialog

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import kotlinx.android.synthetic.main.dialog_internet_status_help.*
import zyxel.com.multyproneo.R

/**
 * Created by LouisTien on 2019/6/5.
 */
class InternetStatusDialog(context: Context) : Dialog(context)
{

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_internet_status_help)
        setCancelable(true)
        msg_alert_positive.setOnClickListener { dismiss() }
    }
}