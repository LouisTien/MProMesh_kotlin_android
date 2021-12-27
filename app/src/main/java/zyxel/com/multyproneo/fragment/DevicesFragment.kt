package zyxel.com.multyproneo.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.fragment_devices.*
import org.jetbrains.anko.support.v4.runOnUiThread
import zyxel.com.multyproneo.R
import zyxel.com.multyproneo.adapter.cloud.CloudHomeGuestEndDeviceItemAdapter
import zyxel.com.multyproneo.api.ApiHandler
import zyxel.com.multyproneo.dialog.MeshDeviceStatusDialog
import zyxel.com.multyproneo.dialog.MessageDialog
import zyxel.com.multyproneo.event.ApiEvent
import zyxel.com.multyproneo.event.DevicesEvent
import zyxel.com.multyproneo.event.GlobalBus
import zyxel.com.multyproneo.event.MainEvent
import zyxel.com.multyproneo.util.AppConfig
import zyxel.com.multyproneo.util.FeatureConfig
import zyxel.com.multyproneo.util.GlobalData
import java.util.*
import kotlin.concurrent.schedule

/**
 * Created by LouisTien on 2019/6/10.
 */
class DevicesFragment : Fragment()
{
    private val TAG = "DevicesFragment"
    private lateinit var meshDevicePlacementStatusDisposable: Disposable
    private lateinit var getInfoCompleteDisposable: Disposable
    private lateinit var stopRegularTaskDisposable: Disposable
    private lateinit var showTipsDisposable: Disposable
    private var apiTimer = Timer()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
    {
        return inflater.inflate(R.layout.fragment_devices, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)

        GlobalData.currentFrag = TAG

        meshDevicePlacementStatusDisposable = GlobalBus.listen(DevicesEvent.MeshDevicePlacementStatus::class.java).subscribe{
            MeshDeviceStatusDialog(activity!!, it.isHomePage, false).show()
        }

        getInfoCompleteDisposable = GlobalBus.listen(ApiEvent.ApiExecuteComplete::class.java).subscribe {
            when(it.event)
            {
                ApiHandler.API_RES_EVENT.API_RES_EVENT_DEVICES_API_REGULAR -> updateUI()
                else -> { }
            }
        }

        stopRegularTaskDisposable = GlobalBus.listen(ApiEvent.StopRegularTask::class.java).subscribe{
            GlobalBus.publish(MainEvent.HideLoading())
            stopGetDeviceInfo()
        }

        showTipsDisposable = GlobalBus.listen(DevicesEvent.ShowTips::class.java).subscribe{
            MessageDialog(
                    activity!!,
                    "",
                    getString(R.string.devices_user_tips),
                    arrayOf(getString(R.string.message_dialog_ok)),
                    AppConfig.DialogAction.ACT_NONE
            ).show()
        }

        devices_home_devices_list_swipe.setOnRefreshListener{
            GlobalBus.publish(MainEvent.ShowLoadingOnlyGrayBG())
            stopGetDeviceInfo()
            startGetDeviceInfo()
        }

        devices_guest_devices_list_swipe.setOnRefreshListener{
            GlobalBus.publish(MainEvent.ShowLoadingOnlyGrayBG())
            stopGetDeviceInfo()
            startGetDeviceInfo()
        }

        setClickListener()
    }

    override fun onResume()
    {
        super.onResume()
        GlobalBus.publish(MainEvent.ShowBottomToolbar())
        updateUI()
        startGetDeviceInfo()
    }

    override fun onPause()
    {
        super.onPause()
        stopGetDeviceInfo()
    }

    override fun onDestroyView()
    {
        super.onDestroyView()
        if(!meshDevicePlacementStatusDisposable.isDisposed) meshDevicePlacementStatusDisposable.dispose()
        if(!getInfoCompleteDisposable.isDisposed) getInfoCompleteDisposable.dispose()
        if(!stopRegularTaskDisposable.isDisposed) stopRegularTaskDisposable.dispose()
        if(!showTipsDisposable.isDisposed) showTipsDisposable.dispose()
    }

    private val clickListener = View.OnClickListener{ view ->
        when(view)
        {
            devices_search_image ->
            {
                val bundle = Bundle().apply{
                    putString("Search", "")
                }
                GlobalBus.publish(MainEvent.SwitchToFrag(SearchDevicesFragment().apply{ arguments = bundle }))
            }

            devices_home_devices_sort_image, devices_home_devices_title_text ->
            {
                GlobalData.homeDevAscendingOrder = !GlobalData.homeDevAscendingOrder
                updateUI()
            }

            devices_guest_devices_sort_image, devices_guest_devices_title_text ->
            {
                GlobalData.guestDevAscendingOrder = !GlobalData.guestDevAscendingOrder
                updateUI()
            }

            devices_refresh_image ->
            {
                GlobalBus.publish(MainEvent.ShowLoading())
                stopGetDeviceInfo()
                startGetDeviceInfo()
            }
        }
    }

    private fun setClickListener()
    {
        devices_search_image.setOnClickListener(clickListener)
        devices_home_devices_sort_image.setOnClickListener(clickListener)
        devices_guest_devices_sort_image.setOnClickListener(clickListener)
        devices_home_devices_title_text.setOnClickListener(clickListener)
        devices_refresh_image.setOnClickListener(clickListener)
    }

    private fun updateUI()
    {
        if(GlobalData.currentFrag != TAG) return

        if(!isVisible) return

        var showUserTip = false
        for(i in GlobalData.homeEndDeviceList.indices) {
            if(GlobalData.homeEndDeviceList[i].UserDefineName.equals("N/A", ignoreCase = true)
                    && GlobalData.homeEndDeviceList[i].PhysAddress == GlobalData.homeEndDeviceList[i].HostName) {
                showUserTip = true
            }
        }

        if(GlobalData.guestEndDeviceList.size > 0) {
            for(i in GlobalData.guestEndDeviceList.indices) {
                if(GlobalData.guestEndDeviceList[i].UserDefineName.equals("N/A", ignoreCase = true)
                        && GlobalData.guestEndDeviceList[i].PhysAddress == GlobalData.guestEndDeviceList[i].HostName) {
                    showUserTip = true
                }
            }
        }

        runOnUiThread{
            GlobalBus.publish(MainEvent.HideLoading())
            devices_home_devices_list_swipe.setRefreshing(false)
            devices_guest_devices_list_swipe.setRefreshing(false)

            devices_activated_value_text.text = GlobalData.getActivatedDeviceCount().toString()
            devices_total_value_text.text = GlobalData.getTotalDeviceCount().toString()

            devices_home_devices_sort_image.setImageResource(if(GlobalData.homeDevAscendingOrder) R.drawable.device_sorting_1 else R.drawable.device_sorting_2)
            GlobalData.sortHomeDeviceList()
            devices_home_devices_list.adapter = CloudHomeGuestEndDeviceItemAdapter(activity!!, GlobalData.homeEndDeviceList, false, showUserTip)

            if(GlobalData.guestEndDeviceList.size > 0)
            {
                devices_guest_devices_sort_image.setImageResource(if(GlobalData.guestDevAscendingOrder) R.drawable.device_sorting_1 else R.drawable.device_sorting_2)
                GlobalData.sortGuestDeviceList()
                devices_guest_devices_list.adapter = CloudHomeGuestEndDeviceItemAdapter(activity!!, GlobalData.guestEndDeviceList, false, showUserTip)
                devices_guest_devices_area_linear.visibility = View.VISIBLE
            }
            else
                devices_guest_devices_area_linear.visibility = View.GONE
        }
    }

    private fun startGetDeviceInfo()
    {
        apiTimer = Timer()
        apiTimer.schedule(0, (AppConfig.endDeviceListUpdateTime * 1000).toLong())
        {
            ApiHandler().execute(
                    ApiHandler.API_RES_EVENT.API_RES_EVENT_DEVICES_API_REGULAR,
                    arrayListOf
                    (
                            ApiHandler.API_REF.API_GET_CHANGE_ICON_NAME,
                            ApiHandler.API_REF.API_GET_DEVICE_INFO
                    )
            )
        }
    }

    private fun stopGetDeviceInfo()
    {
        apiTimer.cancel()
    }
}