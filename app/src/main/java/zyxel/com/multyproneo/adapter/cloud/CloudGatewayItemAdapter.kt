package zyxel.com.multyproneo.adapter.cloud

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.adapter_gateway_list_item.view.*
import org.jetbrains.anko.backgroundResource
import zyxel.com.multyproneo.R
import zyxel.com.multyproneo.event.GatewayListEvent
import zyxel.com.multyproneo.event.GlobalBus
import zyxel.com.multyproneo.model.cloud.AllDeviceInfo
import zyxel.com.multyproneo.model.cloud.TUTKAllDeviceInfo

class CloudGatewayItemAdapter(private val gatewayListInfo: TUTKAllDeviceInfo, private var deleteMode: Boolean) : RecyclerView.Adapter<CloudGatewayItemAdapter.ViewHolder>()
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

            if(deleteMode)
            {
                itemView.gateway_model_frame.backgroundResource = R.drawable.op_card_bg1
                itemView.gateway_delete_image.visibility = View.VISIBLE
            }
            else
            {
                itemView.gateway_model_frame.backgroundResource = R.drawable.op_card_bg
                itemView.gateway_delete_image.visibility = View.GONE
            }

            itemView.gateway_model_frame.setOnClickListener{
                if(!deleteMode)
                    GlobalBus.publish(GatewayListEvent.OnDeviceSelected(position))
            }

            itemView.gateway_delete_image.setOnClickListener{
                if(deleteMode)
                    GlobalBus.publish(GatewayListEvent.OnDeviceDelete(position))
            }
        }
    }
}