package zyxel.com.multyproneo.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import kotlinx.android.synthetic.main.adapter_name_your_controller_item.view.*
import org.jetbrains.anko.sdk27.coroutines.onClick
import zyxel.com.multyproneo.R
import zyxel.com.multyproneo.event.GlobalBus
import zyxel.com.multyproneo.event.LocationNamesListEvent
import zyxel.com.multyproneo.util.AppConfig
import zyxel.com.multyproneo.util.GlobalData

class LocationNamesItemAdapter : BaseAdapter()
{
    override fun getCount(): Int = AppConfig.locationNamesArray.size

    override fun getItem(position: Int): Any = AppConfig.locationNamesArray[position]

    override fun getItemId(position: Int): Long = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View?
    {
        val view: View?
        val holder: ViewHolder
        if(convertView == null)
        {
            view = LayoutInflater.from(parent?.context).inflate(R.layout.adapter_name_your_controller_item, parent, false)
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
            if(position == GlobalData.locationNamesSelectIndex)
                view.location_select_image.visibility = View.VISIBLE
            else
                view.location_select_image.visibility = View.INVISIBLE

            view.location_name_text.text = AppConfig.locationNamesArray[position]

            view.location_name_relative.onClick{
                GlobalData.locationNamesSelectIndex = position
                GlobalBus.publish(LocationNamesListEvent.OnDeviceSelected(AppConfig.locationNamesArray[position]))
            }
        }
    }
}