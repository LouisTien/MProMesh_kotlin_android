package zyxel.com.multyproneo.fragment.cloud

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
import zyxel.com.multyproneo.api.cloud.P2PApiHandler
import zyxel.com.multyproneo.dialog.MeshDeviceStatusDialog
import zyxel.com.multyproneo.event.DevicesEvent
import zyxel.com.multyproneo.event.GlobalBus
import zyxel.com.multyproneo.event.MainEvent
import zyxel.com.multyproneo.event.P2PApiEvent
import zyxel.com.multyproneo.util.AppConfig
import zyxel.com.multyproneo.util.GlobalData

class CloudDevicesFragment : Fragment()
{
    private val TAG = "CloudDevicesFragment"
    private lateinit var meshDevicePlacementStatusDisposable: Disposable
    private lateinit var getCloudInfoCompleteDisposable: Disposable

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
    {
        return inflater.inflate(R.layout.fragment_devices, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)

        GlobalData.currentFrag = TAG

        meshDevicePlacementStatusDisposable = GlobalBus.listen(DevicesEvent.MeshDevicePlacementStatus::class.java).subscribe{
            MeshDeviceStatusDialog(activity!!, it.isHomePage).show()
        }

        getCloudInfoCompleteDisposable = GlobalBus.listen(P2PApiEvent.ApiExecuteComplete::class.java).subscribe{
            GlobalBus.publish(MainEvent.HideLoading())

            when(it.event)
            {
                P2PApiHandler.API_RES_EVENT.API_RES_EVENT_DEVICES -> updateUI()
                else -> {}
            }
        }

        devices_home_devices_list_swipe.setOnRefreshListener{
            GlobalBus.publish(MainEvent.ShowLoadingOnlyGrayBG())
            startGetCloudDeviceInfo()
        }

        devices_guest_devices_list_swipe.setOnRefreshListener{
            GlobalBus.publish(MainEvent.ShowLoadingOnlyGrayBG())
            startGetCloudDeviceInfo()
        }

        setClickListener()

        GlobalBus.publish(MainEvent.ShowLoading())
        startGetCloudDeviceInfo()
    }

    override fun onResume()
    {
        super.onResume()
        GlobalBus.publish(MainEvent.ShowCloudBottomToolbar())
    }

    override fun onPause()
    {
        super.onPause()
    }

    override fun onDestroyView()
    {
        super.onDestroyView()
        if(!meshDevicePlacementStatusDisposable.isDisposed) meshDevicePlacementStatusDisposable.dispose()
        if(!getCloudInfoCompleteDisposable.isDisposed) getCloudInfoCompleteDisposable.dispose()
    }

    private val clickListener = View.OnClickListener{ view ->
        when(view)
        {
            devices_search_image ->
            {
                val bundle = Bundle().apply{
                    putString("Search", "")
                }
                GlobalBus.publish(MainEvent.SwitchToFrag(CloudSearchDevicesFragment().apply{ arguments = bundle }))
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
                startGetCloudDeviceInfo()
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
        GlobalBus.publish(MainEvent.HideLoading())

        if(GlobalData.currentFrag != TAG) return

        if(!isVisible) return

        runOnUiThread{
            devices_home_devices_list_swipe.setRefreshing(false)
            devices_guest_devices_list_swipe.setRefreshing(false)

            devices_activated_value_text.text = GlobalData.getActivatedDeviceCount().toString()
            devices_total_value_text.text = GlobalData.getTotalDeviceCount().toString()

            devices_home_devices_sort_image.setImageResource(if(GlobalData.homeDevAscendingOrder) R.drawable.device_sorting_1 else R.drawable.device_sorting_2)
            GlobalData.sortHomeDeviceList()
            devices_home_devices_list.adapter = CloudHomeGuestEndDeviceItemAdapter(activity!!, GlobalData.homeEndDeviceList, true)

            if(GlobalData.guestEndDeviceList.size > 0)
            {
                devices_guest_devices_sort_image.setImageResource(if(GlobalData.guestDevAscendingOrder) R.drawable.device_sorting_1 else R.drawable.device_sorting_2)
                GlobalData.sortGuestDeviceList()
                devices_guest_devices_list.adapter = CloudHomeGuestEndDeviceItemAdapter(activity!!, GlobalData.guestEndDeviceList, true)
                devices_guest_devices_area_linear.visibility = View.VISIBLE
            }
            else
                devices_guest_devices_area_linear.visibility = View.GONE
        }
    }

    private fun startGetCloudDeviceInfo()
    {
        P2PApiHandler().execute(
                P2PApiHandler.API_RES_EVENT.API_RES_EVENT_DEVICES,
                arrayListOf
                (
                        P2PApiHandler.API_REF.API_GET_CHANGE_ICON_NAME,
                        P2PApiHandler.API_REF.API_GET_DEVICE_INFO
                )
        )
    }
}