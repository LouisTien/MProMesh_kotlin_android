package zyxel.com.multyproneo.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import kotlinx.android.synthetic.main.adapter_parental_control_schedule_list_item.view.*
import org.jetbrains.anko.sdk27.coroutines.onClick
import zyxel.com.multyproneo.R
import zyxel.com.multyproneo.event.GlobalBus
import zyxel.com.multyproneo.event.ParentalControlEvent
import zyxel.com.multyproneo.model.ParentalControlInfoSchedule
import zyxel.com.multyproneo.tool.CommonTool

class ParentalControlScheduleItemAdapter(private var scheduleList: MutableList<ParentalControlInfoSchedule>) : BaseAdapter()
{
    override fun getCount(): Int = scheduleList.size

    override fun getItem(position: Int): Any = scheduleList[position]

    override fun getItemId(position: Int): Long = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View?
    {
        val view: View?
        val holder: ViewHolder
        if(convertView == null)
        {
            view = LayoutInflater.from(parent?.context).inflate(R.layout.adapter_parental_control_schedule_list_item, parent, false)
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
            val formatStr = "%02d"
            view.parental_control_schedule_time_text.text =
                    String.format(formatStr, scheduleList[position].TimeStartHour) + ":" +
                            String.format(formatStr, scheduleList[position].TimeStartMin) + " - " +
                            String.format(formatStr, scheduleList[position].TimeStopHour) + ":" +
                            String.format(formatStr, scheduleList[position].TimeStopMin)

            view.parental_control_schedule_week_text.text = CommonTool.getSelectDays(scheduleList[position])

            view.parental_control_schedule_detail_image.onClick{
                GlobalBus.publish(ParentalControlEvent.ScheduleMenu(position, scheduleList[position]))
            }
        }
    }
}