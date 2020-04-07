package zyxel.com.multyproneo.fragment.cloud

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_setup_connect_troubleshooting.*
import zyxel.com.multyproneo.R
import zyxel.com.multyproneo.api.cloud.TUTKP2PBaseApi
import zyxel.com.multyproneo.event.GlobalBus
import zyxel.com.multyproneo.event.MainEvent
import zyxel.com.multyproneo.util.AppConfig

class SetupConnectTroubleshootingFragment : Fragment()
{
    private var pageMode = AppConfig.TroubleshootingPage.PAGE_CANNOT_CONNECT_CONTROLLER
    private var needConnectFlowForRetry = false
    private var mac = ""

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
    {
        return inflater.inflate(R.layout.fragment_setup_connect_troubleshooting, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)

        with(arguments)
        {
            this?.getSerializable("pageMode")?.let{ pageMode = it as AppConfig.TroubleshootingPage }
            this?.getBoolean("needConnectFlowForRetry")?.let{ needConnectFlowForRetry = it }
            this?.getString("MAC")?.let{ mac = it }
        }

        updateUI()

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

    private fun updateUI()
    {
        setup_connect_troubleshooting_description1_linear.visibility = View.GONE
        setup_connect_troubleshooting_description2_linear.visibility = View.GONE
        setup_connect_troubleshooting_description3_linear.visibility = View.GONE
        setup_connect_troubleshooting_description4_linear.visibility = View.GONE
        setup_connect_troubleshooting_description5_linear.visibility = View.GONE

        when(pageMode)
        {
            AppConfig.TroubleshootingPage.PAGE_CANNOT_CONNECT_CONTROLLER,
            AppConfig.TroubleshootingPage.PAGE_CANNOT_CONNECT_CONTROLLER_PREVIOUS_SET,
            AppConfig.TroubleshootingPage.PAGE_P2P_INIT_FAIL_IN_GATEWAY_LIST,
            AppConfig.TroubleshootingPage.PAGE_CLOUD_API_ERROR
            ->
            {

                if( (pageMode == AppConfig.TroubleshootingPage.PAGE_P2P_INIT_FAIL_IN_GATEWAY_LIST)
                    || (pageMode == AppConfig.TroubleshootingPage.PAGE_CLOUD_API_ERROR) )
                    setup_connect_troubleshooting_back_image.visibility = View.INVISIBLE

                setup_connect_troubleshooting_title_text.text = getString(R.string.setup_connect_troubleshooting_cannot_connect_controller_title)
                setup_connect_troubleshooting_sub_title_text.text = getString(R.string.setup_connect_troubleshooting_cannot_connect_controller_sub_title)

                setup_connect_troubleshooting_description1_linear.visibility = View.VISIBLE
                setup_connect_troubleshooting_description1_text.text = getString(R.string.setup_connect_troubleshooting_cannot_connect_controller_description1)

                setup_connect_troubleshooting_description2_linear.visibility = View.VISIBLE
                setup_connect_troubleshooting_description2_text.text = getString(R.string.setup_connect_troubleshooting_cannot_connect_controller_description2)

                setup_connect_troubleshooting_description3_linear.visibility = View.VISIBLE
                setup_connect_troubleshooting_description3_text.text = getString(R.string.setup_connect_troubleshooting_cannot_connect_controller_description3)
            }

            AppConfig.TroubleshootingPage.PAGE_NO_INTERNET ->
            {
                setup_connect_troubleshooting_back_image.visibility = View.INVISIBLE

                setup_connect_troubleshooting_title_text.text = getString(R.string.setup_connect_troubleshooting_no_internet_title)
                setup_connect_troubleshooting_sub_title_text.text = getString(R.string.setup_connect_troubleshooting_no_internet_sub_title)

                setup_connect_troubleshooting_description1_linear.visibility = View.VISIBLE
                setup_connect_troubleshooting_description1_text.text = getString(R.string.setup_connect_troubleshooting_no_internet_description1)

                setup_connect_troubleshooting_description2_linear.visibility = View.VISIBLE
                setup_connect_troubleshooting_description2_text.text = getString(R.string.setup_connect_troubleshooting_no_internet_description2)

                setup_connect_troubleshooting_description3_linear.visibility = View.VISIBLE
                setup_connect_troubleshooting_description3_text.text = getString(R.string.setup_connect_troubleshooting_no_internet_description3)

                setup_connect_troubleshooting_description4_linear.visibility = View.VISIBLE
                setup_connect_troubleshooting_description4_text.text = getString(R.string.setup_connect_troubleshooting_no_internet_description4)
            }

            AppConfig.TroubleshootingPage.PAGE_CANNOT_CONNECT_TO_CLOUD ->
            {
                setup_connect_troubleshooting_back_image.visibility = View.INVISIBLE

                setup_connect_troubleshooting_title_text.text = getString(R.string.setup_connect_troubleshooting_cannot_connect_cloud_title)
                setup_connect_troubleshooting_sub_title_text.text = getString(R.string.setup_connect_troubleshooting_cannot_connect_controller_sub_title)

                setup_connect_troubleshooting_description1_linear.visibility = View.VISIBLE
                setup_connect_troubleshooting_description1_text.text = getString(R.string.setup_connect_troubleshooting_cannot_connect_cloud_description1)

                setup_connect_troubleshooting_description2_linear.visibility = View.VISIBLE
                setup_connect_troubleshooting_description2_text.text = getString(R.string.setup_connect_troubleshooting_cannot_connect_cloud_description2)
            }
        }
    }

    private val clickListener = View.OnClickListener{ view ->
        when(view)
        {
            setup_connect_troubleshooting_back_image ->
            {
                when(pageMode)
                {
                    AppConfig.TroubleshootingPage.PAGE_CANNOT_CONNECT_CONTROLLER -> GlobalBus.publish(MainEvent.SwitchToFrag(SetupConnectControllerFragment()))
                    AppConfig.TroubleshootingPage.PAGE_CANNOT_CONNECT_CONTROLLER_PREVIOUS_SET -> GlobalBus.publish(MainEvent.SwitchToFrag(SetupConnectControllerFragment()))
                    else -> {}
                }
            }

            setup_connect_troubleshooting_retry_image ->
            {
                when(pageMode)
                {
                    AppConfig.TroubleshootingPage.PAGE_CANNOT_CONNECT_CONTROLLER ->
                    {
                        val bundle = Bundle().apply{
                            putBoolean("needConnectFlowForRetry", needConnectFlowForRetry)
                        }

                        GlobalBus.publish(MainEvent.SwitchToFrag(SetupConnectingControllerFragment().apply{ arguments = bundle }))
                    }

                    AppConfig.TroubleshootingPage.PAGE_NO_INTERNET -> GlobalBus.publish(MainEvent.SwitchToFrag(SetupConnectingInternetFragment()))

                    AppConfig.TroubleshootingPage.PAGE_CANNOT_CONNECT_CONTROLLER_PREVIOUS_SET ->
                    {
                        /*val bundle = Bundle().apply{
                            putString("MAC", mac)
                        }
                        GlobalBus.publish(MainEvent.SwitchToFrag(SetupReconnectRouterPreviousSettingsFragment().apply{ arguments = bundle }))*/

                        GlobalBus.publish(MainEvent.SwitchToFrag(SetupControllerReadyFragment()))
                    }

                    AppConfig.TroubleshootingPage.PAGE_P2P_INIT_FAIL_IN_GATEWAY_LIST ->
                    {
                        val bundle = Bundle().apply{
                            putSerializable("AutoLogin", false)
                        }

                        GlobalBus.publish(MainEvent.SwitchToFrag(CloudGatewayListFragment().apply{ arguments = bundle }))
                    }

                    else ->
                    {
                        TUTKP2PBaseApi.stopSession()
                        GlobalBus.publish(MainEvent.SwitchToFrag(CloudWelcomeFragment()))
                    }
                }
            }
        }
    }

    private fun setClickListener()
    {
        setup_connect_troubleshooting_back_image.setOnClickListener(clickListener)
        setup_connect_troubleshooting_retry_image.setOnClickListener(clickListener)
    }
}