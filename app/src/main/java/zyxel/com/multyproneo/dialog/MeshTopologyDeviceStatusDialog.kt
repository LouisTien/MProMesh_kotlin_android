package zyxel.com.multyproneo.dialog

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import kotlinx.android.synthetic.main.dialog_mesh_topology_device_status.*
import zyxel.com.multyproneo.R

class MeshTopologyDeviceStatusDialog(context: Context) : Dialog(context)
{
    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_mesh_topology_device_status)
        setCancelable(false)
        mesh_topology_status_close_image.setOnClickListener{ dismiss() }
    }
}