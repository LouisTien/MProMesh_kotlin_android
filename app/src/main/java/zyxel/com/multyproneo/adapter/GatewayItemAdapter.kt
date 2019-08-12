package zyxel.com.multyproneo.adapter

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.adapter_gateway_list_item.view.*
import zyxel.com.multyproneo.R
import zyxel.com.multyproneo.event.GatewayListEvent
import zyxel.com.multyproneo.event.GlobalBus
import zyxel.com.multyproneo.model.GatewayInfo

/**
 * Created by LouisTien on 2019/5/30.
 */
class GatewayItemAdapter(private val gatewayList: MutableList<GatewayInfo>) : RecyclerView.Adapter<GatewayItemAdapter.ViewHolder>()
{
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder
    {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.adapter_gateway_list_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int)
    {
        holder.bind(gatewayList[position], position)
    }

    override fun getItemCount(): Int = gatewayList.size

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view)
    {
        fun bind(gatewayInfo: GatewayInfo, position: Int)
        {
            /*itemView.gateway_model_name_text.text = gatewayInfo.userDefineName

            with(gatewayInfo.modelName)
            {
                when
                {
                    contains("WAP6804") -> itemView.gateway_model_image.setImageResource(R.drawable.device_wap6804)
                    contains("WAP4927") -> itemView.gateway_model_image.setImageResource(R.drawable.device_wap4927)
                    contains("WAP4825") -> itemView.gateway_model_image.setImageResource(R.drawable.device_wap4825)
                    else -> itemView.gateway_model_image.setImageResource(R.drawable.device_wap4825)
                }
            }*/

            itemView.gateway_model_name_text.text = gatewayInfo.ModelName

            with(gatewayInfo.DeviceMode)
            {
                when
                {
                    contains("Router") -> itemView.gateway_model_image.setImageResource(R.drawable.device_wap4825)
                    else -> itemView.gateway_model_image.setImageResource(R.drawable.device_wap4825)
                }
            }

            itemView.gateway_model_enter_image.setOnClickListener{
                GlobalBus.publish(GatewayListEvent.OnDeviceSelected(position))
            }
        }
    }
}