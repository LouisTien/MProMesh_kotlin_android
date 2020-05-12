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
import zyxel.com.multyproneo.adapter.HomeGuestEndDeviceItemAdapter
import zyxel.com.multyproneo.event.DevicesEvent
import zyxel.com.multyproneo.event.GlobalBus
import zyxel.com.multyproneo.event.MainEvent
import zyxel.com.multyproneo.util.GlobalData

/**
 * Created by LouisTien on 2019/6/10.
 */
class DevicesFragment : Fragment()
{
    private val TAG = javaClass.simpleName
    private lateinit var getInfoCompleteDisposable: Disposable

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
    {
        return inflater.inflate(R.layout.fragment_devices, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)
        getInfoCompleteDisposable = GlobalBus.listen(DevicesEvent.GetDeviceInfoComplete::class.java).subscribe{ updateUI() }

        devices_home_devices_list_swipe.setOnRefreshListener{
            GlobalBus.publish(MainEvent.ShowLoadingOnlyGrayBG())
            GlobalBus.publish(MainEvent.StopGetDeviceInfoTask())
            GlobalBus.publish(MainEvent.StartGetDeviceInfoTask())
        }

        devices_guest_devices_list_swipe.setOnRefreshListener{
            GlobalBus.publish(MainEvent.ShowLoadingOnlyGrayBG())
            GlobalBus.publish(MainEvent.StopGetDeviceInfoTask())
            GlobalBus.publish(MainEvent.StartGetDeviceInfoTask())
        }

        setClickListener()
    }

    override fun onResume()
    {
        super.onResume()
        GlobalBus.publish(MainEvent.ShowBottomToolbar())
        updateUI()
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
        if(!getInfoCompleteDisposable.isDisposed) getInfoCompleteDisposable.dispose()
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
        }
    }

    private fun setClickListener()
    {
        devices_search_image.setOnClickListener(clickListener)
        devices_home_devices_sort_image.setOnClickListener(clickListener)
        devices_guest_devices_sort_image.setOnClickListener(clickListener)
        devices_home_devices_title_text.setOnClickListener(clickListener)
    }

    private fun updateUI()
    {
        if(GlobalData.currentFrag != TAG) return

        if(!isVisible) return

        runOnUiThread{
            GlobalBus.publish(MainEvent.HideLoading())
            devices_home_devices_list_swipe.setRefreshing(false)
            devices_guest_devices_list_swipe.setRefreshing(false)

            devices_activated_value_text.text = GlobalData.getActivatedDeviceCount().toString()
            devices_total_value_text.text = GlobalData.getTotalDeviceCount().toString()

            devices_home_devices_sort_image.setImageResource(if(GlobalData.homeDevAscendingOrder) R.drawable.device_sorting_1 else R.drawable.device_sorting_2)
            GlobalData.sortHomeDeviceList()
            devices_home_devices_list.adapter = HomeGuestEndDeviceItemAdapter(activity!!, GlobalData.homeEndDeviceList)

            if(GlobalData.guestEndDeviceList.size > 0)
            {
                devices_guest_devices_sort_image.setImageResource(if(GlobalData.guestDevAscendingOrder) R.drawable.device_sorting_1 else R.drawable.device_sorting_2)
                GlobalData.sortGuestDeviceList()
                devices_guest_devices_list.adapter = HomeGuestEndDeviceItemAdapter(activity!!, GlobalData.guestEndDeviceList)
                devices_guest_devices_area_linear.visibility = View.VISIBLE
            }
            else
                devices_guest_devices_area_linear.visibility = View.GONE
        }
    }
}