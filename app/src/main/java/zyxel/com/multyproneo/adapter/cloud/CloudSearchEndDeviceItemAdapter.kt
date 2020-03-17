package zyxel.com.multyproneo.adapter.cloud

import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import kotlinx.android.synthetic.main.adapter_search_end_device_list_item.view.*
import zyxel.com.multyproneo.R
import zyxel.com.multyproneo.event.GlobalBus
import zyxel.com.multyproneo.event.MainEvent
import zyxel.com.multyproneo.fragment.cloud.CloudEndDeviceDetailFragment
import zyxel.com.multyproneo.model.DevicesInfoObject
import zyxel.com.multyproneo.tool.CommonTool

class CloudSearchEndDeviceItemAdapter(private var activity: Activity) : BaseAdapter()
{
    public var endDeviceList = mutableListOf<DevicesInfoObject>()
    public var searchStr = ""

    override fun getCount(): Int = endDeviceList.size

    override fun getItem(position: Int): Any = endDeviceList[position]

    override fun getItemId(position: Int): Long = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View?
    {
        val view: View?
        val holder: ViewHolder
        if(convertView == null)
        {
            view = LayoutInflater.from(parent?.context).inflate(R.layout.adapter_search_end_device_list_item, parent, false)
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
            view.search_user_define_name_text.text = endDeviceList[position].getName()
            view.search_enter_detail_image.setOnClickListener{
                CommonTool.hideKeyboard(activity)
                val bundle = Bundle().apply{
                    putSerializable("DevicesInfo", endDeviceList[position])
                    putString("Search", searchStr)
                    putBoolean("FromSearch", true)
                }
                GlobalBus.publish(MainEvent.SwitchToFrag(CloudEndDeviceDetailFragment().apply{ arguments = bundle }))
            }
        }
    }
}