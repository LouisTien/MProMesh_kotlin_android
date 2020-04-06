package zyxel.com.multyproneo.fragment.cloud

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_cloud_settings_troubleshooting_detail.*
import zyxel.com.multyproneo.R
import zyxel.com.multyproneo.event.GlobalBus
import zyxel.com.multyproneo.event.MainEvent
import zyxel.com.multyproneo.util.AppConfig

class CloudSettingsTroubleshootingDetailFragment : Fragment()
{
    private var pageMode = AppConfig.TroubleshootingDetailPage.PAGE_DETAIL_NO_INTERNET

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
    {
        return inflater.inflate(R.layout.fragment_cloud_settings_troubleshooting_detail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)

        with(arguments)
        {
            this?.getSerializable("pageMode")?.let{ pageMode = it as AppConfig.TroubleshootingDetailPage }
        }

        updateUI()

        settings_troubleshooting_detail_back_image.setOnClickListener{ GlobalBus.publish(MainEvent.SwitchToFrag(CloudSettingsTroubleshootingFragment())) }
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

    private fun updateUI()
    {
        settings_troubleshooting_detail_description1_linear.visibility = View.GONE
        settings_troubleshooting_detail_description2_linear.visibility = View.GONE
        settings_troubleshooting_detail_description3_linear.visibility = View.GONE
        settings_troubleshooting_detail_description4_linear.visibility = View.GONE
        settings_troubleshooting_detail_description5_linear.visibility = View.GONE

        when(pageMode)
        {
            AppConfig.TroubleshootingDetailPage.PAGE_DETAIL_NO_INTERNET ->
            {
                settings_troubleshooting_detail_description1_linear.visibility = View.VISIBLE
                settings_troubleshooting_detail_description1_text.text = getString(R.string.setup_connect_troubleshooting_no_internet_description1)

                settings_troubleshooting_detail_description2_linear.visibility = View.VISIBLE
                settings_troubleshooting_detail_description2_text.text = getString(R.string.setup_connect_troubleshooting_no_internet_description2)

                settings_troubleshooting_detail_description3_linear.visibility = View.VISIBLE
                settings_troubleshooting_detail_description3_text.text = getString(R.string.setup_connect_troubleshooting_no_internet_description3)

                settings_troubleshooting_detail_description4_linear.visibility = View.VISIBLE
                settings_troubleshooting_detail_description4_text.text = getString(R.string.setup_connect_troubleshooting_no_internet_description4)
            }

            AppConfig.TroubleshootingDetailPage.PAGE_DETAIL_NO_ETHERNET ->
            {
                settings_troubleshooting_detail_description1_linear.visibility = View.VISIBLE
                settings_troubleshooting_detail_description1_text.text = getString(R.string.settings_troubleshooting_no_ethernet_description1)

                settings_troubleshooting_detail_description2_linear.visibility = View.VISIBLE
                settings_troubleshooting_detail_description2_text.text = getString(R.string.settings_troubleshooting_no_ethernet_description2)

                settings_troubleshooting_detail_description3_linear.visibility = View.VISIBLE
                settings_troubleshooting_detail_description3_text.text = getString(R.string.settings_troubleshooting_no_ethernet_description3)

                settings_troubleshooting_detail_description4_linear.visibility = View.VISIBLE
                settings_troubleshooting_detail_description4_text.text = getString(R.string.settings_troubleshooting_no_ethernet_description4)
            }

            AppConfig.TroubleshootingDetailPage.PAGE_DETAIL_QRCODE_ERROR ->
            {
                settings_troubleshooting_detail_description1_linear.visibility = View.VISIBLE
                settings_troubleshooting_detail_description1_text.text = getString(R.string.settings_troubleshooting_qrcode_error_description1)

                settings_troubleshooting_detail_description2_linear.visibility = View.VISIBLE
                settings_troubleshooting_detail_description2_text.text = getString(R.string.settings_troubleshooting_qrcode_error_description2)
            }

            AppConfig.TroubleshootingDetailPage.PAGE_DETAIL_CANNOT_CONNECT_TO_WIFI ->
            {
                settings_troubleshooting_detail_description1_linear.visibility = View.VISIBLE
                settings_troubleshooting_detail_description1_text.text = getString(R.string.setup_connect_troubleshooting_cannot_connect_controller_description1)

                settings_troubleshooting_detail_description2_linear.visibility = View.VISIBLE
                settings_troubleshooting_detail_description2_text.text = getString(R.string.setup_connect_troubleshooting_cannot_connect_controller_description2)

                settings_troubleshooting_detail_description3_linear.visibility = View.VISIBLE
                settings_troubleshooting_detail_description3_text.text = getString(R.string.setup_connect_troubleshooting_cannot_connect_controller_description3)
            }
        }
    }
}