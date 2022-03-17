package zyxel.com.multyproneo.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import kotlinx.android.synthetic.main.adapter_parental_control_device_list_item.view.*
import org.jetbrains.anko.sdk27.coroutines.onClick
import zyxel.com.multyproneo.R
import zyxel.com.multyproneo.event.GlobalBus
import zyxel.com.multyproneo.event.ParentalControlEvent
import zyxel.com.multyproneo.model.DevicesInfoObject

class ParentalControlDeviceItemAdapter(private var deviceList: MutableList<DevicesInfoObject>) : BaseAdapter()
{
    override fun getCount(): Int = deviceList.size

    override fun getItem(position: Int): Any = deviceList[position]

    override fun getItemId(position: Int): Long = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View?
    {
        val view: View?
        val holder: ViewHolder
        if(convertView == null)
        {
            view = LayoutInflater.from(parent?.context).inflate(R.layout.adapter_parental_control_device_list_item, parent, false)
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
            view.parental_control_device_name_text.text = deviceList[position].getName()

            when(deviceList[position].X_ZYXEL_HostType)
            {
                "Desktop" -> view.parental_control_device_type_image.setImageResource(R.drawable.device_desktop_icon)
                "Laptop" -> view.parental_control_device_type_image.setImageResource(R.drawable.device_laptop_icon)
                "SmartPhone" -> view.parental_control_device_type_image.setImageResource(R.drawable.device_phone_icon)
                else -> view.parental_control_device_type_image.setImageResource(R.drawable.device_phone_icon)
            }

            view.parental_control_device_detail_image.onClick{
                GlobalBus.publish(ParentalControlEvent.DeviceMenu(deviceList[position]))
            }
        }
    }
}