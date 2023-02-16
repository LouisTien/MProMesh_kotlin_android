package zyxel.com.multyproneo.dialog

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import kotlinx.android.synthetic.main.dialog_mesh_topology_rssi_status.*
import zyxel.com.multyproneo.R

class MeshTopologyRssiStatusDialog(
    context: Context,
    private val linkRate: String,
    private val rssi: String
) : Dialog(context) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_mesh_topology_rssi_status)
        setCancelable(false)
        mesh_topology_rssi_link_rate_text.text = "$linkRate Mbps"
        mesh_topology_rssi_text.text = rssi

        msg_alert_cancel.setOnClickListener { dismiss() }
    }
}