package zyxel.com.multyproneo.adapter.cloud

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.adapter_gateway_list_item.view.*
import zyxel.com.multyproneo.R
import zyxel.com.multyproneo.event.GatewayListEvent
import zyxel.com.multyproneo.event.GlobalBus
import zyxel.com.multyproneo.model.cloud.AllDeviceInfo
import zyxel.com.multyproneo.model.cloud.TUTKAllDeviceInfo

class CloudGatewayItemAdapter(private val gatewayListInfo: TUTKAllDeviceInfo) : RecyclerView.Adapter<CloudGatewayItemAdapter.ViewHolder>()
{
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder
    {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.adapter_gateway_list_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int)
    {
        holder.bind(gatewayListInfo.data[position], position)
    }

    override fun getItemCount(): Int = gatewayListInfo.data.size

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view)
    {
        fun bind(gatewayInfo: AllDeviceInfo, position: Int)
        {
            itemView.gateway_model_name_text.text = gatewayInfo.displayName

            itemView.gateway_model_frame.setOnClickListener{
                GlobalBus.publish(GatewayListEvent.OnDeviceSelected(position))
            }
        }
    }
}