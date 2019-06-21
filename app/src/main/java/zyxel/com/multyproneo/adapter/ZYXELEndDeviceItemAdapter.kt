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
import zyxel.com.multyproneo.model.EndDeviceProfile
import zyxel.com.multyproneo.model.GatewayProfile
import zyxel.com.multyproneo.model.WanInfoProfile

/**
 * Created by LouisTien on 2019/6/4.
 */
class ZYXELEndDeviceItemAdapter(
        private var endDeviceList: MutableList<EndDeviceProfile>,
        private var deviceInfo: GatewayProfile,
        private var deviceWanInfo: WanInfoProfile,
        private var deviceLanIP: String) : BaseAdapter()
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

        holder.bind(position, deviceInfo, deviceWanInfo, deviceLanIP)

        return view
    }

    inner class ViewHolder(private var view: View, private var parent: ViewGroup)
    {
        fun bind(position: Int, deviceInfo: GatewayProfile, deviceWanInfo: WanInfoProfile, deviceLanIP: String)
        {
            var status = ""
            if(endDeviceList[position].DeviceMode.equals("GATEWAY", ignoreCase = true))
                view.connect_status_text.text = status
            else
            {
                if(endDeviceList[position].Active.equals("Disconnect", ignoreCase = true))
                    status = "N/A"
                else
                {
                    if(endDeviceList[position].ConnectionType.equals("WiFi", ignoreCase = true))
                        status = endDeviceList[position].RssiValue
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

            val mode = endDeviceList[position].DeviceMode + if(status.equals("N/A", ignoreCase = true)) " disconnected" else ""
            view.device_mode_text.text = mode

            view.user_define_name_text.text = endDeviceList[position].UserDefineName

            view.enter_detail_image.setOnClickListener{
                val bundle = Bundle().apply{
                    putBoolean("GatewayMode", position == 0)
                    putSerializable("GatewayProfile", deviceInfo)
                    putSerializable("GatewayWanInfo", deviceWanInfo)
                    putString("GatewayLanIP", deviceLanIP)
                    putSerializable("EndDeviceProfile", endDeviceList[position])
                }
                GlobalBus.publish(MainEvent.SwitchToFrag(ZYXELEndDeviceDetailFragment().apply{ arguments = bundle }))
            }
        }
    }
}