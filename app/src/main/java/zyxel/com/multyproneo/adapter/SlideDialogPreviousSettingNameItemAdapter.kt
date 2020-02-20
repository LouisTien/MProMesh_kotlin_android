package zyxel.com.multyproneo.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import kotlinx.android.synthetic.main.adapter_slide_dialog_list_item.view.*
import org.jetbrains.anko.sdk27.coroutines.onClick
import zyxel.com.multyproneo.R
import zyxel.com.multyproneo.database.room.DatabaseSiteInfoEntity
import zyxel.com.multyproneo.event.DialogEvent
import zyxel.com.multyproneo.event.GlobalBus

class SlideDialogPreviousSettingNameItemAdapter(private var contentList: List<DatabaseSiteInfoEntity>, private var focusIndex: Int) : BaseAdapter()
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
            view = LayoutInflater.from(parent?.context).inflate(R.layout.adapter_slide_dialog_list_item, parent, false)
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
            view.slide_dialog_item_name_text.text = contentList[position].siteName

            if(focusIndex == position)
                view.slide_dialog_item_select_image.visibility = View.VISIBLE
            else
                view.slide_dialog_item_select_image.visibility = View.INVISIBLE

            view.slide_dialog_item_relative.onClick{
                focusIndex = position
                GlobalBus.publish(DialogEvent.OnSlideListSelect(contentList[position]))
            }
        }
    }
}