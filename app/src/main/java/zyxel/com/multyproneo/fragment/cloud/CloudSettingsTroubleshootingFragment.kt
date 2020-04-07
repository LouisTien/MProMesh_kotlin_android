package zyxel.com.multyproneo.fragment.cloud

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_cloud_settings_troubleshooting.*
import zyxel.com.multyproneo.R
import zyxel.com.multyproneo.event.GlobalBus
import zyxel.com.multyproneo.event.MainEvent
import zyxel.com.multyproneo.util.AppConfig

class CloudSettingsTroubleshootingFragment : Fragment()
{
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
    {
        return inflater.inflate(R.layout.fragment_cloud_settings_troubleshooting, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)
        setClickListener()
    }

    override fun onResume()
    {
        super.onResume()
        GlobalBus.publish(MainEvent.HideBottomToolbar())
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
        when(view)
        {
            settings_troubleshooting_back_image -> GlobalBus.publish(MainEvent.SwitchToFrag(CloudSettingsFragment()))

            settings_troubleshooting_internet_relative -> gotoTroubleshootingDetailPage(AppConfig.TroubleshootingDetailPage.PAGE_DETAIL_NO_INTERNET)

            settings_troubleshooting_ethernet_relative -> gotoTroubleshootingDetailPage(AppConfig.TroubleshootingDetailPage.PAGE_DETAIL_NO_ETHERNET)

            settings_troubleshooting_qrcode_relative -> gotoTroubleshootingDetailPage(AppConfig.TroubleshootingDetailPage.PAGE_DETAIL_QRCODE_ERROR)

            settings_troubleshooting_wifi_relative -> gotoTroubleshootingDetailPage(AppConfig.TroubleshootingDetailPage.PAGE_DETAIL_CANNOT_CONNECT_TO_WIFI)
        }
    }

    private fun setClickListener()
    {
        settings_troubleshooting_back_image.setOnClickListener(clickListener)
        settings_troubleshooting_internet_relative.setOnClickListener(clickListener)
        settings_troubleshooting_ethernet_relative.setOnClickListener(clickListener)
        settings_troubleshooting_qrcode_relative.setOnClickListener(clickListener)
        settings_troubleshooting_wifi_relative.setOnClickListener(clickListener)
    }

    private fun gotoTroubleshootingDetailPage(mode: AppConfig.TroubleshootingDetailPage)
    {
        val bundle = Bundle().apply{
            putSerializable("pageMode", mode)
        }

        GlobalBus.publish(MainEvent.SwitchToFrag(CloudSettingsTroubleshootingDetailFragment().apply{ arguments = bundle }))
    }
}