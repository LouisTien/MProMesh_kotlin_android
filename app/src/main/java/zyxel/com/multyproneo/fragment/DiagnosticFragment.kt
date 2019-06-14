package zyxel.com.multyproneo.fragment

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentTransaction
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_diagnostic.*
import zyxel.com.multyproneo.R
import zyxel.com.multyproneo.util.LogUtil

/**
 * Created by LouisTien on 2019/6/13.
 */
class DiagnosticFragment : Fragment()
{
    private val TAG = javaClass.simpleName
    private var currentFrag = ""

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
    {
        return inflater.inflate(R.layout.fragment_diagnostic, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)
        setClickListener()
    }

    override fun onResume()
    {
        super.onResume()
    }

    override fun onPause()
    {
        super.onPause()
    }

    override fun onDestroyView()
    {
        super.onDestroyView()
    }

    private val clickListener = View.OnClickListener { view ->
        diagnostic_tab_wifi_channel_text.background = null
        diagnostic_tab_wifi_signal_text.background = null

        when(view)
        {
            diagnostic_tab_wifi_channel_text ->
            {
                diagnostic_tab_wifi_channel_text.setBackgroundResource(R.drawable.button_style_white_bg)
                if(currentFrag != "WiFiChannelChartFragment")
                {

                }
            }

            diagnostic_tab_wifi_signal_text ->
            {
                diagnostic_tab_wifi_signal_text.setBackgroundResource(R.drawable.button_style_white_bg)
                if(currentFrag != "WiFiSignalMeterFragment")
                {

                }
            }

            diagnostic_back_image -> {}
        }
    }

    private fun setClickListener()
    {
        diagnostic_tab_wifi_channel_text.setOnClickListener(clickListener)
        diagnostic_tab_wifi_signal_text.setOnClickListener(clickListener)
        diagnostic_back_image.setOnClickListener(clickListener)
    }

    private fun switchToFragContainer(fragment: Fragment)
    {
        val transaction = childFragmentManager.beginTransaction()
        transaction.replace(R.id.diagnostic_content_area_frame, fragment)
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
        transaction.commitAllowingStateLoss()
        currentFrag = fragment.javaClass.simpleName
        LogUtil.d(TAG, "currentFrag:$currentFrag")
    }
}