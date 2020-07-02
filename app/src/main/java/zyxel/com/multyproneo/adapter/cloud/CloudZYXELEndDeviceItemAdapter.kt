package zyxel.com.multyproneo.adapter.cloud

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import kotlinx.android.synthetic.main.adapter_cloud_zyxel_end_device_list_item.view.*
import zyxel.com.multyproneo.R
import zyxel.com.multyproneo.event.GlobalBus
import zyxel.com.multyproneo.event.HomeEvent
import zyxel.com.multyproneo.event.MainEvent
import zyxel.com.multyproneo.fragment.ZYXELEndDeviceDetailFragment
import zyxel.com.multyproneo.fragment.cloud.CloudZYXELEndDeviceDetailFragment
import zyxel.com.multyproneo.model.DevicesInfoObject
import zyxel.com.multyproneo.model.GatewayInfo
import zyxel.com.multyproneo.model.WanInfo

class CloudZYXELEndDeviceItemAdapter
(
        private var endDeviceList: MutableList<DevicesInfoObject>,
        private var deviceInfo: GatewayInfo,
        private var deviceWanInfo: WanInfo,
        private var isCloud: Boolean
) : BaseAdapter()
{
    override fun getCount(): Int = endDeviceList.size

    override fun getItem(position: Int): Any = endDeviceList[position]

    override fun getItemId(position: Int): Long = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View?
    {
        val view: View?
        val holder: ViewHolder
        if(convertView == null)
        {
            view = LayoutInflater.from(parent?.context).inflate(R.layout.adapter_cloud_zyxel_end_device_list_item, parent, false)
            holder = ViewHolder(view, parent!!)
            view.tag = holder
        }
        else
        {
            view = convertView
            holder = view.tag as ViewHolder
        }

        holder.bind(position, deviceInfo, deviceWanInfo)

        return view
    }

    inner class ViewHolder(private var view: View, private var parent: ViewGroup)
    {
        fun bind(position: Int, deviceInfo: GatewayInfo, deviceWanInfo: WanInfo)
        {
            var imageId = R.drawable.icon_wifi_noconnection

            if(!endDeviceList[position].Active)
                imageId = R.drawable.icon_wifi_noconnection
            else
            {
                if(endDeviceList[position].X_ZYXEL_ConnectionType.contains("WiFi", ignoreCase = true)
                    || endDeviceList[position].X_ZYXEL_ConnectionType.contains("Wi-Fi", ignoreCase = true))
                    imageId = getSignalStatusImage(endDeviceList[position].X_ZYXEL_RSSI_STAT)
                else if(endDeviceList[position].X_ZYXEL_ConnectionType.contains("Ethernet", ignoreCase = true))
                    imageId = R.drawable.icon_wired
                else
                    imageId = R.drawable.icon_wifi_good
            }

            view.connect_status_image.setImageResource(imageId)

            view.user_define_name_text.text =
                    if(endDeviceList[position].getName().contains("unknown") || endDeviceList[position].getName().contains("Unknown"))
                        view.resources.getString(R.string.home_extender)
                    else
                        endDeviceList[position].getName()

            view.connect_status_image.setOnClickListener{
                GlobalBus.publish(HomeEvent.MeshDevicePlacementStatus(true))
            }

            view.zyxel_end_device_relative.setOnClickListener{
                val bundle = Bundle().apply{
                    putBoolean("GatewayMode", false)
                    putSerializable("GatewayInfo", deviceInfo)
                    putSerializable("WanInfo", deviceWanInfo)
                    putSerializable("DevicesInfo", endDeviceList[position])
                }

                if(isCloud)
                    GlobalBus.publish(MainEvent.SwitchToFrag(CloudZYXELEndDeviceDetailFragment().apply{ arguments = bundle }))
                else
                    GlobalBus.publish(MainEvent.SwitchToFrag(ZYXELEndDeviceDetailFragment().apply{ arguments = bundle }))
            }
        }

        private fun getSignalStatusImage(status: String): Int
        {
            var imageId = R.drawable.icon_wifi_good

            with(status)
            {
                when
                {
                    equals("Good", ignoreCase = true) -> imageId = R.drawable.icon_wifi_good
                    equals("TooClose", ignoreCase = true) || equals("Too Close", ignoreCase = true) -> imageId = R.drawable.icon_wifi_tooclose
                    equals("Weak", ignoreCase = true) -> imageId = R.drawable.icon_wifi_tooforaway
                }
            }

            return imageId
        }
    }
}