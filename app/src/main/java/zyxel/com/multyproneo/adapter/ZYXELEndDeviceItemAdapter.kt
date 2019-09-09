package zyxel.com.multyproneo.adapter

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import kotlinx.android.synthetic.main.adapter_zyxel_end_device_list_item.view.*
import org.jetbrains.anko.textColor
import zyxel.com.multyproneo.R
import zyxel.com.multyproneo.event.GlobalBus
import zyxel.com.multyproneo.event.MainEvent
import zyxel.com.multyproneo.fragment.ZYXELEndDeviceDetailFragment
import zyxel.com.multyproneo.model.DevicesInfoObject
import zyxel.com.multyproneo.model.GatewayInfo
import zyxel.com.multyproneo.model.WanInfo

/**
 * Created by LouisTien on 2019/6/4.
 */
class ZYXELEndDeviceItemAdapter
(
        private var endDeviceList: MutableList<DevicesInfoObject>,
        private var deviceInfo: GatewayInfo,
        private var deviceWanInfo: WanInfo
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
            view = LayoutInflater.from(parent?.context).inflate(R.layout.adapter_zyxel_end_device_list_item, parent, false)
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
            var status = ""
            if(endDeviceList[position].X_ZYXEL_HostType.equals("Router", ignoreCase = true))
                view.connect_status_text.text = status
            else
            {
                if(!endDeviceList[position].Active)
                    status = "N/A"
                else
                {
                    if(endDeviceList[position].X_ZYXEL_ConnectionType.equals("WiFi", ignoreCase = true))
                        status = endDeviceList[position].X_ZYXEL_RSSI_STAT
                    else
                        status = "Good"
                }

                view.connect_status_text.text = status

                view.connect_status_text.textColor = parent.context.resources.getColor(
                        with(status)
                        {
                            when
                            {
                                equals("Good", ignoreCase = true) -> R.color.color_3c9f00
                                equals("TooClose", ignoreCase = true) -> R.color.color_ff6800
                                equals("Weak", ignoreCase = true) -> R.color.color_d9003c
                                else -> R.color.color_575757
                            }
                        }
                )
            }

            val mode = if(endDeviceList[position].X_ZYXEL_HostType.equals("Router", ignoreCase = true)) "Gateway" else endDeviceList[position].X_ZYXEL_HostType +
                    if(status.equals("N/A", ignoreCase = true)) " disconnected" else ""
            view.device_mode_text.text = mode

            view.user_define_name_text.text = endDeviceList[position].getName()

            view.zyxel_end_device_relative.setOnClickListener{
                val bundle = Bundle().apply{
                    putBoolean("GatewayMode", position == 0)
                    putSerializable("GatewayInfo", deviceInfo)
                    putSerializable("WanInfo", deviceWanInfo)
                    putSerializable("DevicesInfo", endDeviceList[position])
                }
                GlobalBus.publish(MainEvent.SwitchToFrag(ZYXELEndDeviceDetailFragment().apply{ arguments = bundle }))
            }
        }
    }
}