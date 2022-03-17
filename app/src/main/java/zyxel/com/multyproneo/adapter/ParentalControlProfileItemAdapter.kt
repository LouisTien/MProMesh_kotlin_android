package zyxel.com.multyproneo.adapter

import android.app.Activity
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.adapter_parental_control_profile_list_item.view.*
import org.jetbrains.anko.imageBitmap
import org.jetbrains.anko.sdk27.coroutines.onClick
import org.jetbrains.anko.textColor
import zyxel.com.multyproneo.R
import zyxel.com.multyproneo.event.GlobalBus
import zyxel.com.multyproneo.event.MainEvent
import zyxel.com.multyproneo.fragment.ParentalControlEditProfileFragment
import zyxel.com.multyproneo.model.ParentalControlInfoProfile
import zyxel.com.multyproneo.tool.CommonTool
import zyxel.com.multyproneo.util.GlobalData
import java.io.File

class ParentalControlProfileItemAdapter
(
        private var activity: Activity,
        private var profileList: MutableList<ParentalControlInfoProfile>
) : BaseAdapter()
{
    override fun getCount(): Int = profileList.size

    override fun getItem(position: Int): Any = profileList[position]

    override fun getItemId(position: Int): Long = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View?
    {
        val view: View?
        val holder: ViewHolder
        if(convertView == null)
        {
            view = LayoutInflater.from(parent?.context).inflate(R.layout.adapter_parental_control_profile_list_item, parent, false)
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
            val picFile = File("${GlobalData.parentalControlProfilePicDir.toString()}/${GlobalData.getCurrentGatewayInfo().MAC}-${profileList[position].index}.jpg")
            if(picFile.exists())
            {
                val picBitmap = BitmapFactory.decodeFile(picFile.absolutePath)
                view.parental_control_profile_image.imageBitmap = picBitmap
            }
            else
                view.parental_control_profile_image.setImageResource(R.drawable.default_profile_image)


            view.parental_control_profile_name_text.text = profileList[position].Name
            view.parental_control_profile_status_text.text = activity.getString(R.string.parental_control_allow_internet)

            when(GlobalData.parentalControlMasterSwitch && profileList[position].Enable)
            {
                false ->
                {
                    view.parental_control_profile_image.borderWidth = 0
                    view.parental_control_profile_image.borderColor = Color.TRANSPARENT
                    view.parental_control_profile_status_text.textColor = ContextCompat.getColor(activity, R.color.color_939393)
                    view.parental_control_profile_status_text.text = activity.getString(R.string.parental_control_schedule_off)
                }

                true ->
                {
                    when(CommonTool.checkScheduleBlock(profileList[position]))
                    {
                        true ->
                        {
                            view.parental_control_profile_image.borderWidth = 10
                            view.parental_control_profile_image.borderColor = ContextCompat.getColor(activity, R.color.color_c61a12)
                            view.parental_control_profile_status_text.textColor = ContextCompat.getColor(activity, R.color.color_c61a12)
                            view.parental_control_profile_status_text.text = activity.getString(R.string.parental_control_block_internet)
                        }

                        false ->
                        {
                            view.parental_control_profile_image.borderWidth = 10
                            view.parental_control_profile_image.borderColor = ContextCompat.getColor(activity, R.color.color_64be00)
                            view.parental_control_profile_status_text.textColor = ContextCompat.getColor(activity, R.color.color_64be00)
                            view.parental_control_profile_status_text.text = activity.getString(R.string.parental_control_allow_internet)
                        }
                    }
                }
            }

            view.parental_control_profile_list_item_relative.onClick{
                GlobalBus.publish(MainEvent.SwitchToFrag(ParentalControlEditProfileFragment().apply{ arguments = Bundle().apply{ putSerializable("ProfileInfo", profileList[position]) }}))
            }
        }
    }
}