package zyxel.com.multyproneo.adapter

import android.content.Context
import android.support.v4.view.PagerAdapter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.adapter_welcome_pager_item.view.*
import zyxel.com.multyproneo.R

/**
 * Created by LouisTien on 2019/5/21.
 */
class WelcomeAdapter(private var context: Context) : PagerAdapter()
{
    //val context: Context
    val welcomeImagesRes = intArrayOf(R.drawable.tutorial_1, R.drawable.tutorial_2, R.drawable.tutorial_3)
    lateinit var inflator: LayoutInflater

    /*constructor(context: Context) : super()
    {
        this.context = context
    }*/

    override fun instantiateItem(container: ViewGroup, position: Int): Any
    {
        inflator = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val itemView: View = inflator.inflate(R.layout.adapter_welcome_pager_item, container,false)
        itemView.welcome_pager_image.setBackgroundResource(welcomeImagesRes[position])
        when(position)
        {
            0 ->
            {
                itemView.welcome_pager_title.text = context.getResources().getString(R.string.welcome_my_home_network_title)
                itemView.welcome_pager_description.text = context.getResources().getString(R.string.welcome_my_home_network_description)
            }
            1 ->
            {
                itemView.welcome_pager_title.text = context.getResources().getString(R.string.welcome_guest_wifi_access_title)
                itemView.welcome_pager_description.text = context.getResources().getString(R.string.welcome_guest_wifi_access_description)
            }
            2 ->
            {
                itemView.welcome_pager_title.text = context.getResources().getString(R.string.welcome_auto_configuration_title)
                itemView.welcome_pager_description.text = context.getResources().getString(R.string.welcome_auto_configuration_description)
            }
            else ->
            {
            }
        }
        container.addView(itemView)
        return itemView
    }

    override fun isViewFromObject(view: View, `object`: Any): Boolean = view == `object`

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) = container.removeView(`object` as View)

    override fun getCount(): Int = welcomeImagesRes.size
}