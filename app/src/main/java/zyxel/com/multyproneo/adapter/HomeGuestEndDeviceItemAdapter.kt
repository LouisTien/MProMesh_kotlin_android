package zyxel.com.multyproneo.adapter

import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import kotlinx.android.synthetic.main.adapter_home_guest_end_device_list_item.view.*
import org.jetbrains.anko.textColor
import zyxel.com.multyproneo.R
import zyxel.com.multyproneo.event.GlobalBus
import zyxel.com.multyproneo.event.MainEvent
import zyxel.com.multyproneo.fragment.EndDeviceDetailFragment
import zyxel.com.multyproneo.model.DevicesInfoObject
import zyxel.com.multyproneo.util.FeatureConfig
import zyxel.com.multyproneo.util.OUIUtil

/**
 * Created by LouisTien on 2019/6/10.
 */
class HomeGuestEndDeviceItemAdapter(private var activity: Activity, private var endDeviceList: MutableList<DevicesInfoObject>) : BaseAdapter()
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
            view = LayoutInflater.from(parent?.context).inflate(R.layout.adapter_home_guest_end_device_list_item, parent, false)
            holder = ViewHolder(view, parent!!)
            view.tag = holder
        }
        else
        {
            view = convertView
            holder = view.tag as ViewHolder
        }

        holder.bind(position)

        return view
    }

    inner class ViewHolder(private var view: View, private var parent: ViewGroup)
    {
        fun bind(position: Int)
        {
            var status =
                when
                {
                    endDeviceList[position].Internet_Blocking_Enable -> "Blocked"
                    endDeviceList[position].X_ZYXEL_RSSI_STAT.equals("TooClose", ignoreCase = true) or endDeviceList[position].X_ZYXEL_RSSI_STAT.equals("Too Close", ignoreCase = true) -> "Good"
                    else -> endDeviceList[position].X_ZYXEL_RSSI_STAT
                }

            view.link_quality_text.textColor = parent.context.resources.getColor(
                    with(status)
                    {
                        when
                        {
                            equals("Good", ignoreCase = true) -> R.color.color_3c9f00
                            equals("TooClose", ignoreCase = true) or equals("Too Close", ignoreCase = true) -> R.color.color_ff6800
                            equals("Weak", ignoreCase = true) or equals("Blocked", ignoreCase = true) -> R.color.color_d9003c
                            else -> R.color.color_575757
                        }
                    }
            )

            if(!endDeviceList[position].Active)
            {
                status = ""
                view.user_define_name_text.textColor = parent.context.resources.getColor(R.color.color_b4b4b4)
            }
            else
                view.user_define_name_text.textColor = parent.context.resources.getColor(R.color.color_000000)

            view.link_quality_text.text = status

            /*var modelName = SpecialCharacterHandler.checkEmptyTextValue(endDeviceList[position].UserDefineName)
            if(modelName.equals("N/A", ignoreCase = true))
                modelName = endDeviceList[position].Name*/
            var modelName = endDeviceList[position].getName()

            if(FeatureConfig.hostNameReplaceStatus)
            {
                if(modelName.equals("unknown", ignoreCase = true) || modelName.equals("<unknown>", ignoreCase = true))
                    modelName = OUIUtil.getOUI(activity, endDeviceList[position].PhysAddress)
            }

            view.user_define_name_text.text = modelName
            view.profile_name_text.text = ""

            view.home_guest_end_device_relative.setOnClickListener{
                val bundle = Bundle().apply{
                    putSerializable("DevicesInfo", endDeviceList[position])
                    putString("Search", "")
                    putBoolean("FromSearch", false)
                }
                GlobalBus.publish(MainEvent.SwitchToFrag(EndDeviceDetailFragment().apply{ arguments = bundle }))
            }
        }
    }
}