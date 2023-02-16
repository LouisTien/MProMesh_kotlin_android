package zyxel.com.multyproneo.adapter.cloud

import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import kotlinx.android.synthetic.main.adapter_cloud_home_guest_end_device_list_item.view.*
import zyxel.com.multyproneo.R
import zyxel.com.multyproneo.event.DevicesEvent
import zyxel.com.multyproneo.event.GlobalBus
import zyxel.com.multyproneo.event.MainEvent
import zyxel.com.multyproneo.fragment.EndDeviceDetailFragment
import zyxel.com.multyproneo.fragment.cloud.CloudEndDeviceDetailFragment
import zyxel.com.multyproneo.model.DevicesInfoObject
import zyxel.com.multyproneo.util.FeatureConfig
import zyxel.com.multyproneo.util.GlobalData
import zyxel.com.multyproneo.util.OUIUtil

class CloudHomeGuestEndDeviceItemAdapter
    (
    private var activity: Activity,
    private var endDeviceList: MutableList<DevicesInfoObject>,
    private var isCloud: Boolean,
    private var showUserTip: Boolean = false,
    private var isFromMeshTopology: Boolean = false,
    private var selectedNodeMAC: String = ""
) : BaseAdapter() {


    override fun getCount(): Int = endDeviceList.size

    override fun getItem(position: Int): Any = endDeviceList[position]

    override fun getItemId(position: Int): Long = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View? {
        val view: View?
        val holder: ViewHolder
        if (convertView == null) {
            view = LayoutInflater.from(parent?.context)
                .inflate(R.layout.adapter_cloud_home_guest_end_device_list_item, parent, false)
            holder = ViewHolder(view, parent!!)
            view.tag = holder
        } else {
            view = convertView
            holder = view.tag as ViewHolder
        }

        holder.bind(position)

        return view
    }

    inner class ViewHolder(private var view: View, private var parent: ViewGroup) {
        fun bind(position: Int) {
            var imageId = R.drawable.icon_wifi_noconnection

            if (!endDeviceList[position].Active)
                imageId = R.drawable.icon_wifi_noconnection
            else {
                if (endDeviceList[position].X_ZYXEL_ConnectionType.contains(
                        "WiFi",
                        ignoreCase = true
                    )
                    || endDeviceList[position].X_ZYXEL_ConnectionType.contains(
                        "Wi-Fi",
                        ignoreCase = true
                    )
                ) {
                    val status =
                        when {
                            endDeviceList[position].Internet_Blocking_Enable or endDeviceList[position].ParentalControlBlock -> "Blocked"
                            endDeviceList[position].X_ZYXEL_RSSI_STAT.equals(
                                "TooClose",
                                ignoreCase = true
                            ) or endDeviceList[position].X_ZYXEL_RSSI_STAT.equals(
                                "Too Close",
                                ignoreCase = true
                            ) -> "Good"
                            else -> endDeviceList[position].X_ZYXEL_RSSI_STAT
                        }

                    imageId = getSignalStatusImage(status)
                } else if (endDeviceList[position].X_ZYXEL_ConnectionType.contains(
                        "Ethernet",
                        ignoreCase = true
                    )
                )
                    imageId =
                        if (endDeviceList[position].Internet_Blocking_Enable || endDeviceList[position].ParentalControlBlock) R.drawable.icon_block else R.drawable.icon_wired
                else
                    imageId = R.drawable.icon_wifi_good
            }

            view.connect_status_image.setImageResource(imageId)

            if (!isCloud) {
                if (endDeviceList[position].UserDefineName.equals("N/A", ignoreCase = true)
                    && endDeviceList[position].PhysAddress == endDeviceList[position].HostName
                )
                    view.user_tips_image.visibility = View.VISIBLE
                else if (showUserTip)
                    view.user_tips_image.visibility = View.INVISIBLE
                else
                    view.user_tips_image.visibility = View.GONE
            }

            var modelName = endDeviceList[position].getName()

            if (FeatureConfig.hostNameReplaceStatus) {
                if (modelName.equals("unknown", ignoreCase = true) || modelName.equals(
                        "<unknown>",
                        ignoreCase = true
                    )
                )
                    modelName = OUIUtil.getOUI(activity, endDeviceList[position].PhysAddress)
            }

            view.user_define_name_text.text = modelName

            view.connect_status_image.setOnClickListener {
                GlobalBus.publish(DevicesEvent.MeshDevicePlacementStatus(false))
            }

            view.home_guest_end_device_relative.setOnClickListener {
                val bundle = Bundle().apply {
                    putSerializable("DevicesInfo", endDeviceList[position])
                    putString("Search", "")
                    putBoolean("FromSearch", false)
                    putBoolean("FromMeshTopology", isFromMeshTopology)
                    putString(GlobalData.SelectedNodeMAC, selectedNodeMAC)
                }

                if (isCloud)
                    GlobalBus.publish(MainEvent.SwitchToFrag(CloudEndDeviceDetailFragment().apply {
                        arguments = bundle
                    }))
                else
                    GlobalBus.publish(MainEvent.SwitchToFrag(EndDeviceDetailFragment().apply {
                        arguments = bundle
                    }))
            }

            view.user_tips_image.setOnClickListener { GlobalBus.publish(DevicesEvent.ShowTips()) }
        }

        private fun getSignalStatusImage(status: String): Int {
            var imageId = R.drawable.icon_wifi_good

            with(status)
            {
                when {
                    equals("Good", ignoreCase = true) -> imageId = R.drawable.icon_wifi_good
                    equals("TooClose", ignoreCase = true) || equals(
                        "Too Close",
                        ignoreCase = true
                    ) -> imageId = R.drawable.icon_wifi_tooclose
                    equals("Weak", ignoreCase = true) -> imageId = R.drawable.icon_wifi_tooforaway
                    equals("Blocked", ignoreCase = true) -> return R.drawable.icon_block
                }
            }

            return imageId
        }
    }
}