package zyxel.com.multyproneo

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.view.ViewPager
import android.view.View
import kotlinx.android.synthetic.main.activity_welcome.*
import zyxel.com.multyproneo.adapter.WelcomeAdapter

/**
 * Created by LouisTien on 2019/5/21.
 */
class WelcomeActivity : AppCompatActivity()
{

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_welcome)

        val welcomeAdapter = WelcomeAdapter(this)
        welcome_view_pager.adapter = welcomeAdapter
        welcome_view_pager.offscreenPageLimit = 3
        welcome_view_pager.addOnPageChangeListener(object: ViewPager.OnPageChangeListener
        {
            override fun onPageScrollStateChanged(state: Int)
            {
            }

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int)
            {
            }

            override fun onPageSelected(position: Int)
            {
                when (position)
                {
                    0 ->
                    {
                        welcome_left_item_linear.visibility = View.INVISIBLE
                        welcome_right_item_relative.visibility = View.VISIBLE
                        welcome_arrow_image.visibility = View.VISIBLE
                        welcome_right_start_text.visibility = View.INVISIBLE
                    }
                    1 ->
                    {
                        welcome_left_item_linear.visibility = View.VISIBLE
                        welcome_right_item_relative.visibility = View.VISIBLE
                        welcome_arrow_image.visibility = View.VISIBLE
                        welcome_right_start_text.visibility = View.INVISIBLE
                    }
                    2 ->
                    {
                        welcome_left_item_linear.visibility = View.INVISIBLE
                        welcome_right_item_relative.visibility = View.VISIBLE
                        welcome_arrow_image.visibility = View.INVISIBLE
                        welcome_right_start_text.visibility = View.VISIBLE
                    }
                    else ->
                    {
                    }
                }
            }
        })
        welcome_view_pager_indicator.attachTo(welcome_view_pager)
    }
}