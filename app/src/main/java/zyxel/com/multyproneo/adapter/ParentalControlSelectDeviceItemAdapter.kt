package zyxel.com.multyproneo.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.adapter_parental_control_select_device_list_item.view.*
import org.jetbrains.anko.sdk27.coroutines.onClick
import org.jetbrains.anko.textColor
import zyxel.com.multyproneo.R
import zyxel.com.multyproneo.event.GlobalBus
import zyxel.com.multyproneo.event.ParentalControlEvent
import zyxel.com.multyproneo.model.DevicesInfoObject

class ParentalControlSelectDeviceItemAdapter(private var context: Context, private var contentList: List<DevicesInfoObject>) : BaseAdapter()
{
    override fun getCount(): Int = contentList.size

    override fun getItem(position: Int): Any = contentList[position]

    override fun getItemId(position: Int): Long = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View?
    {
        val view: View?
        val holder: ViewHolder
        if(convertView == null)
        {
            view = LayoutInflater.from(parent?.context).inflate(R.layout.adapter_parental_control_select_device_list_item, parent, false)
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
            view.parental_control_device_name_text.text = contentList[position].getName()

            if(contentList[position].ParentalControlInUse)
            {
                view.parental_control_device_in_use_text.visibility = View.VISIBLE
                view.parental_control_device_select_image.visibility = View.INVISIBLE
                view.parental_control_device_name_text.textColor = ContextCompat.getColor(context, R.color.color_ececec)

                when(contentList[position].X_ZYXEL_HostType)
                {
                    "Desktop" -> view.parental_control_device_type_image.setImageResource(R.drawable.device_desktop_icon_disable)
                    "Laptop" -> view.parental_control_device_type_image.setImageResource(R.drawable.device_laptop_icon_disable)
                    "SmartPhone" -> view.parental_control_device_type_image.setImageResource(R.drawable.device_phone_icon_disable)
                    else -> view.parental_control_device_type_image.setImageResource(R.drawable.device_phone_icon_disable)
                }
            }
            else
            {
                view.parental_control_device_in_use_text.visibility = View.INVISIBLE
                view.parental_control_device_select_image.visibility = View.VISIBLE
                view.parental_control_device_name_text.textColor = ContextCompat.getColor(context, R.color.color_1a1a1a)

                when(contentList[position].X_ZYXEL_HostType)
                {
                    "Desktop" -> view.parental_control_device_type_image.setImageResource(R.drawable.device_desktop_icon)
                    "Laptop" -> view.parental_control_device_type_image.setImageResource(R.drawable.device_laptop_icon)
                    "SmartPhone" -> view.parental_control_device_type_image.setImageResource(R.drawable.device_phone_icon)
                    else -> view.parental_control_device_type_image.setImageResource(R.drawable.device_phone_icon)
                }
            }

            view.parental_control_device_select_image.setImageResource(
                    if(contentList[position].ParentalControlSelect)
                        R.drawable.selected_icon
                    else
                        R.drawable.select_icon)

            view.parental_control_device_select_image.onClick{
                if(contentList[position].ParentalControlSelect)
                {
                    view.parental_control_device_select_image.setImageResource(R.drawable.select_icon)
                    contentList[position].ParentalControlSelect = false
                }
                else
                {
                    view.parental_control_device_select_image.setImageResource(R.drawable.selected_icon)
                    contentList[position].ParentalControlSelect = true
                }
                GlobalBus.publish(ParentalControlEvent.DeviceSelect())
            }
        }
    }
}