package zyxel.com.multyproneo.dialog

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.View
import kotlinx.android.synthetic.main.dialog_mesh_device_status.*
import zyxel.com.multyproneo.R

class MeshDeviceStatusDialog(context: Context, var isHomePage: Boolean, var isCloud: Boolean = true) : Dialog(context)
{
    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_mesh_device_status)
        setCancelable(false)

        if(isHomePage) {
            mesh_device_status_block_relative.visibility = View.GONE
            if(isCloud)
                mesh_device_status_too_close_relative.visibility = View.VISIBLE
            else
                mesh_device_status_too_close_relative.visibility = View.GONE
        }
        else
        {
            mesh_device_status_too_close_relative.visibility = View.GONE
            mesh_device_status_title_text.text = context.getString(R.string.mesh_device_status_title_device)
        }

        mesh_device_status_close_image.setOnClickListener{ dismiss() }
    }
}