package zyxel.com.multyproneo.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.fragment_diagnostic.*
import zyxel.com.multyproneo.R
import zyxel.com.multyproneo.event.DiagnosticEvent
import zyxel.com.multyproneo.event.GlobalBus
import zyxel.com.multyproneo.util.GlobalData
import zyxel.com.multyproneo.util.LogUtil

/**
 * Created by LouisTien on 2019/6/13.
 */
class DiagnosticFragment : Fragment()
{
    private val TAG = javaClass.simpleName
    private lateinit var enterWiFiChannelChartPageDisposable: Disposable

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
    {
        return inflater.inflate(R.layout.fragment_diagnostic, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)
        setClickListener()
        enterWiFiChannelChartPageDisposable = GlobalBus.listen(DiagnosticEvent.EnterWiFiChannelChartPage::class.java).subscribe{
            enterWiFiChannelChartPage(it.channel)
        }
        clearTabTextsBackground()
        diagnostic_tab_wifi_channel_text.setBackgroundResource(R.drawable.button_style_white_bg)
        enterWiFiChannelChartPage(0)
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
        if(!enterWiFiChannelChartPageDisposable.isDisposed) enterWiFiChannelChartPageDisposable.dispose()
    }

    private val clickListener = View.OnClickListener{ view ->
        clearTabTextsBackground()

        when(view)
        {
            diagnostic_tab_wifi_channel_text ->
            {
                diagnostic_tab_wifi_channel_text.setBackgroundResource(R.drawable.button_style_white_bg)
                if(GlobalData.diagnosticCurrentFrag != "WiFiChannelChartFragment")
                    enterWiFiChannelChartPage(0)
            }

            diagnostic_tab_wifi_signal_text ->
            {
                diagnostic_tab_wifi_signal_text.setBackgroundResource(R.drawable.button_style_white_bg)
                if(GlobalData.diagnosticCurrentFrag != "WiFiSignalMeterFragment")
                    switchToFragContainer(WiFiSignalMeterFragment())
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

    private fun clearTabTextsBackground()
    {
        diagnostic_tab_wifi_channel_text.background = null
        diagnostic_tab_wifi_signal_text.background = null
    }

    private fun enterWiFiChannelChartPage(channel: Int)
    {
        switchToFragContainer(WiFiChannelChartFragment().apply{ arguments = Bundle().apply{ putInt("Channel", channel) } })
    }

    private fun switchToFragContainer(fragment: Fragment)
    {
        val transaction = childFragmentManager.beginTransaction()
        transaction.replace(R.id.diagnostic_content_area_frame, fragment)
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
        transaction.commitAllowingStateLoss()
        GlobalData.diagnosticCurrentFrag = fragment.javaClass.simpleName
        LogUtil.d(TAG, "currentFrag:${GlobalData.diagnosticCurrentFrag}")
    }
}