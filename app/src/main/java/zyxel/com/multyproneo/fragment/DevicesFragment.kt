package zyxel.com.multyproneo.fragment

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.fragment_devices.*
import org.jetbrains.anko.sdk27.coroutines.onClick
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
    private lateinit var getInfoCompleteDisposable: Disposable

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
    {
        return inflater.inflate(R.layout.fragment_devices, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)

        getInfoCompleteDisposable = GlobalBus.listen(DevicesEvent.GetDeviceInfoComplete::class.java).subscribe{
            updateUI()
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

            devices_home_devices_sort_image ->
            {
                GlobalData.homeDevAscendingOrder = !GlobalData.homeDevAscendingOrder
                updateUI()
            }

            devices_guest_devices_sort_image ->
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
    }

    private fun updateUI()
    {
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
        }
        else
            devices_guest_devices_area_linear.visibility = View.GONE
    }
}