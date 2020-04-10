package zyxel.com.multyproneo.dialog

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import androidx.viewpager.widget.ViewPager
import kotlinx.android.synthetic.main.dialog_controller_ready_help.*
import zyxel.com.multyproneo.R
import zyxel.com.multyproneo.adapter.SetupControllerReadyHelpAdapter

class SetupControllerReadyHelpDialog(context: Context) : Dialog(context)
{
    private val helpAdapter = SetupControllerReadyHelpAdapter(context)

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_controller_ready_help)
        setCancelable(true)
        controller_ready_help_pager.adapter = helpAdapter
        controller_ready_help_pager.offscreenPageLimit = 3
        controller_ready_help_pager.addOnPageChangeListener(object: ViewPager.OnPageChangeListener
        {
            override fun onPageScrollStateChanged(state: Int)
            {
            }

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int)
            {
            }

            override fun onPageSelected(position: Int)
            {
            }

        })
        controller_ready_help_pager_indicator.attachTo(controller_ready_help_pager)
        controller_ready_help_close_image.setOnClickListener{ dismiss() }
    }
}