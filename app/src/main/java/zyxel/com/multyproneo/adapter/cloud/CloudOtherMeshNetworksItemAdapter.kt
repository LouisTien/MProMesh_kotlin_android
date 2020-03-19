package zyxel.com.multyproneo.adapter.cloud

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import kotlinx.android.synthetic.main.adapter_other_mesh_list_item.view.*
import zyxel.com.multyproneo.R
import zyxel.com.multyproneo.event.DialogEvent
import zyxel.com.multyproneo.event.GlobalBus
import zyxel.com.multyproneo.model.cloud.TUTKAllDeviceInfo

class CloudOtherMeshNetworksItemAdapter(private var gatewayListInfo: TUTKAllDeviceInfo) : BaseAdapter()
{
    override fun getCount(): Int = gatewayListInfo.data.size

    override fun getItem(position: Int): Any = gatewayListInfo.data[position]

    override fun getItemId(position: Int): Long = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View?
    {
        val view: View?
        val holder: ViewHolder
        if(convertView == null)
        {
            view = LayoutInflater.from(parent?.context).inflate(R.layout.adapter_other_mesh_list_item, parent, false)
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
            Glide.with(view).load(R.drawable.img_locationdefault).apply(RequestOptions.circleCropTransform()).into(view.other_mesh_list_item_image)
            view.other_mesh_list_item_text.text = gatewayListInfo.data[position].displayName
            view.other_mesh_list_item_relative.setOnClickListener{
                GlobalBus.publish(DialogEvent.OnOtherSiteSelect(gatewayListInfo.data[position].udid))
            }
        }
    }
}