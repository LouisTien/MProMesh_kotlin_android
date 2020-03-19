package zyxel.com.multyproneo.adapter.cloud

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import kotlinx.android.synthetic.main.adapter_cloud_account_wifi_router_list_item.view.*
import zyxel.com.multyproneo.R
import zyxel.com.multyproneo.event.CloudAccountEvent
import zyxel.com.multyproneo.event.GlobalBus
import zyxel.com.multyproneo.model.cloud.TUTKAllDeviceInfo
import zyxel.com.multyproneo.util.GlobalData

class CloudAccountWiFiRouterItemAdapter(private var gatewayListInfo: TUTKAllDeviceInfo, private var deleteMode: Boolean) : BaseAdapter()
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
            view = LayoutInflater.from(parent?.context).inflate(R.layout.adapter_cloud_account_wifi_router_list_item, parent, false)
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
            var isSelf = false

            Glide.with(view).load(R.drawable.img_locationdefault).apply(RequestOptions.circleCropTransform()).into(view.cloud_account_wifi_router_list_item_image)

            if(GlobalData.currentUID == gatewayListInfo.data[position].udid)
            {
                isSelf = true
                view.cloud_account_wifi_router_list_item_hint_text.visibility = View.VISIBLE
            }
            else
            {
                isSelf = false
                view.cloud_account_wifi_router_list_item_hint_text.visibility = View.GONE
            }

            if(deleteMode)
                view.cloud_account_wifi_router_list_item_delete_image.visibility = View.VISIBLE
            else
                view.cloud_account_wifi_router_list_item_delete_image.visibility = View.GONE

            view.cloud_account_wifi_router_list_item_text.text = gatewayListInfo.data[position].displayName

            view.cloud_account_wifi_router_list_item_relative.setOnClickListener{
                GlobalBus.publish(CloudAccountEvent.OnSiteSelect(gatewayListInfo.data[position].udid))
            }

            view.cloud_account_wifi_router_list_item_delete_image.setOnClickListener{
                GlobalBus.publish(CloudAccountEvent.OnSiteDelete(gatewayListInfo.data[position], isSelf))
            }
        }
    }
}