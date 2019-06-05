package zyxel.com.multyproneo.fragment

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.fragment_home.*
import zyxel.com.multyproneo.R
import zyxel.com.multyproneo.adapter.ZYXELEndDeviceItemAdapter
import zyxel.com.multyproneo.dialog.InternetStatusDialog
import zyxel.com.multyproneo.event.GlobalBus
import zyxel.com.multyproneo.event.HomeEvent
import zyxel.com.multyproneo.event.MainEvent
import zyxel.com.multyproneo.util.GlobalData
import zyxel.com.multyproneo.util.LogUtil

/**
 * Created by LouisTien on 2019/6/4.
 */
class HomeFragment : Fragment()
{

    private val TAG = javaClass.simpleName
    private lateinit var getInfoCompleteDisposable: Disposable
    private lateinit var internetStatusHelper: InternetStatusDialog
    private lateinit var adapter: ZYXELEndDeviceItemAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
    {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)
        getInfoCompleteDisposable = GlobalBus.listen(HomeEvent.GetDeviceInfoComplete::class.java).subscribe {
            updateUI()
        }
        setClickListener()
    }

    override fun onResume()
    {
        super.onResume()
        GlobalBus.publish(MainEvent.SetHomeIconFocus())
        GlobalBus.publish(MainEvent.ShowBottomToolbar())
        if (GlobalData.ZYXELEndDeviceList.size > 0) updateUI()
        GlobalBus.publish(MainEvent.StartGetDeviceInfoTask())
    }

    override fun onPause()
    {
        super.onPause()
        GlobalBus.publish(MainEvent.StopGetDeviceInfoTask())
    }

    override fun onDestroyView()
    {
        super.onDestroyView()
        if (!getInfoCompleteDisposable.isDisposed) getInfoCompleteDisposable.dispose()
    }

    private val clickListener = View.OnClickListener { view ->
        when (view)
        {
            home_internet_status_help_image ->
            {
                internetStatusHelper = InternetStatusDialog(activity!!)
                internetStatusHelper.show()
            }

            //home_guest_wifi_switch ->
            //home_connect_device_enter_image ->
            //home_guest_wifi_enter_image ->
            //home_add_mesh_image ->
        }
    }

    private fun setClickListener()
    {
        home_internet_status_help_image.setOnClickListener(clickListener)
        home_guest_wifi_switch.setOnClickListener(clickListener)
        home_connect_device_enter_image.setOnClickListener(clickListener)
        home_guest_wifi_enter_image.setOnClickListener(clickListener)
        home_add_mesh_image.setOnClickListener(clickListener)
    }

    private fun updateUI()
    {
        LogUtil.d(TAG, "updateUI()")
        home_connect_device_count_text.text = GlobalData.getConnectDeviceCount().toString()
        adapter = ZYXELEndDeviceItemAdapter(
                GlobalData.ZYXELEndDeviceList,
                GlobalData.getCurrentGatewayInfo(),
                GlobalData.gatewayWanInfo,
                GlobalData.gatewayLanIP)
        home_device_list.adapter = adapter

        home_internet_status_content_text.text = getString(if (GlobalData.gatewayWanInfo.WanStatus.equals("Enable")) R.string.home_online else R.string.home_offline)

        when (GlobalData.guestWiFiStatus)
        {
            true ->
            {
                home_guest_wifi_status_text.text = getString(R.string.home_guest_wifi_status_on)
                home_guest_wifi_switch.setImageResource(R.drawable.switch_on)
            }

            else ->
            {
                home_guest_wifi_status_text.text = getString(R.string.home_guest_wifi_status_off)
                home_guest_wifi_switch.setImageResource(R.drawable.switch_off)
            }
        }
    }
}