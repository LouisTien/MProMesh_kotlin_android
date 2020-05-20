package zyxel.com.multyproneo.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.viewpager.widget.PagerAdapter
import kotlinx.android.synthetic.main.adapter_controller_ready_help_item.view.*
import zyxel.com.multyproneo.R

class SetupControllerReadyHelpAdapter(private var context: Context) : PagerAdapter()
{
    private val helpImagesRes = intArrayOf(R.drawable.img_setupcontroller_02, R.drawable.img_setupcontroller_03, R.drawable.img_setupcontroller_04)
    private lateinit var inflator: LayoutInflater

    override fun instantiateItem(container: ViewGroup, position: Int): Any
    {
        inflator = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val itemView: View = inflator.inflate(R.layout.adapter_controller_ready_help_item, container,false)
        itemView.controller_ready_help_content_image.setImageResource(helpImagesRes[position])
        when(position)
        {
            0 -> itemView.controller_ready_help_description_text.text = context.resources.getString(R.string.setup_controller_ready_help_connect_description)

            1 -> itemView.controller_ready_help_description_text.text = context.resources.getString(R.string.setup_controller_ready_help_power_description)

            2 -> itemView.controller_ready_help_description_text.text = context.resources.getString(R.string.setup_controller_ready_help_led_description)

            else -> {}
        }
        container.addView(itemView)
        return itemView
    }

    override fun isViewFromObject(view: View, `object`: Any): Boolean = view == `object`

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) = container.removeView(`object` as View)

    override fun getCount(): Int = helpImagesRes.size
}