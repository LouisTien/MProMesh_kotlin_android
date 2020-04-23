package zyxel.com.multyproneo.dialog

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.View
import kotlinx.android.synthetic.main.dialog_mesh_device_status.*
import zyxel.com.multyproneo.R

class MeshDeviceStatusDialog(context: Context, var isHomePage: Boolean) : Dialog(context)
{
    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_mesh_device_status)
        setCancelable(false)

        if(isHomePage)
            mesh_device_status_block_relative.visibility = View.GONE

        mesh_device_status_close_image.setOnClickListener{ dismiss() }
    }
}