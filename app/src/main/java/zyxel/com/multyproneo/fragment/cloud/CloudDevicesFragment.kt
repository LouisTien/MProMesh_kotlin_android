package zyxel.com.multyproneo.fragment.cloud

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.gson.Gson
import kotlinx.android.synthetic.main.fragment_devices.*
import org.jetbrains.anko.support.v4.runOnUiThread
import org.json.JSONException
import zyxel.com.multyproneo.R
import zyxel.com.multyproneo.adapter.cloud.CloudHomeGuestEndDeviceItemAdapter
import zyxel.com.multyproneo.api.cloud.P2PDevicesApi
import zyxel.com.multyproneo.api.cloud.TUTKP2PResponseCallback
import zyxel.com.multyproneo.event.GlobalBus
import zyxel.com.multyproneo.event.MainEvent
import zyxel.com.multyproneo.model.ChangeIconNameInfo
import zyxel.com.multyproneo.model.DevicesInfo
import zyxel.com.multyproneo.model.DevicesInfoObject
import zyxel.com.multyproneo.util.GlobalData
import zyxel.com.multyproneo.util.LogUtil

class CloudDevicesFragment : Fragment()
{
    private val TAG = javaClass.simpleName
    private lateinit var changeIconNameInfo: ChangeIconNameInfo
    private lateinit var devicesInfo: DevicesInfo

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
    {
        return inflater.inflate(R.layout.fragment_devices, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)

        devices_home_devices_list_swipe.setOnRefreshListener{
            GlobalBus.publish(MainEvent.ShowLoadingOnlyGrayBG())
            startGetAllNeedDeviceInfoTask()
        }

        devices_guest_devices_list_swipe.setOnRefreshListener{
            GlobalBus.publish(MainEvent.ShowLoadingOnlyGrayBG())
            startGetAllNeedDeviceInfoTask()
        }

        setClickListener()

        GlobalBus.publish(MainEvent.ShowLoading())
        startGetAllNeedDeviceInfoTask()
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
            devices_home_devices_list.adapter = CloudHomeGuestEndDeviceItemAdapter(activity!!, GlobalData.homeEndDeviceList)

            if(GlobalData.guestEndDeviceList.size > 0)
            {
                devices_guest_devices_sort_image.setImageResource(if(GlobalData.guestDevAscendingOrder) R.drawable.device_sorting_1 else R.drawable.device_sorting_2)
                GlobalData.sortGuestDeviceList()
                devices_guest_devices_list.adapter = CloudHomeGuestEndDeviceItemAdapter(activity!!, GlobalData.guestEndDeviceList)
                devices_guest_devices_area_linear.visibility = View.VISIBLE
            }
            else
                devices_guest_devices_area_linear.visibility = View.GONE
        }
    }

    private fun startGetAllNeedDeviceInfoTask()
    {
        getChangeIconNameInfoTask()
    }

    private fun getChangeIconNameInfoTask()
    {
        LogUtil.d(TAG,"getChangeIconNameInfoTask()")
        P2PDevicesApi.GetChangeIconNameInfo()
                .setRequestPageName(TAG)
                .setResponseListener(object: TUTKP2PResponseCallback()
                {
                    override fun onSuccess(responseStr: String)
                    {
                        try
                        {
                            changeIconNameInfo = Gson().fromJson(responseStr, ChangeIconNameInfo::class.javaObjectType)
                            LogUtil.d(TAG,"changeIconNameInfo:$changeIconNameInfo")
                            GlobalData.changeIconNameList = changeIconNameInfo.Object.toMutableList()
                            getDeviceInfoTask()
                        }
                        catch(e: JSONException)
                        {
                            e.printStackTrace()

                            GlobalBus.publish(MainEvent.HideLoading())
                        }
                    }
                }).execute()
    }

    private fun getDeviceInfoTask()
    {
        LogUtil.d(TAG,"getDeviceInfoTask()")
        P2PDevicesApi.GetDevicesInfo()
                .setRequestPageName(TAG)
                .setResponseListener(object: TUTKP2PResponseCallback()
                {
                    override fun onSuccess(responseStr: String)
                    {
                        try
                        {
                            devicesInfo = Gson().fromJson(responseStr, DevicesInfo::class.javaObjectType)
                            LogUtil.d(TAG,"devicesInfo:$devicesInfo")

                            val newEndDeviceList = mutableListOf<DevicesInfoObject>()
                            val newHomeEndDeviceList = mutableListOf<DevicesInfoObject>()
                            val newZYXELEndDeviceList = mutableListOf<DevicesInfoObject>()
                            val newGuestEndDeviceList = mutableListOf<DevicesInfoObject>()

                            /*newZYXELEndDeviceList.add(
                                    DevicesInfoObject
                                    (
                                            Active = true,
                                            HostName = GlobalData.getCurrentGatewayInfo().getName(),
                                            IPAddress = GlobalData.getCurrentGatewayInfo().IP,
                                            X_ZYXEL_CapabilityType = "L2Device",
                                            X_ZYXEL_ConnectionType = "WiFi",
                                            X_ZYXEL_HostType = GlobalData.getCurrentGatewayInfo().DeviceMode,
                                            X_ZYXEL_SoftwareVersion = GlobalData.getCurrentGatewayInfo().SoftwareVersion
                                    )
                            )*/

                            var index = 1
                            for(item in devicesInfo.Object)
                            {
                                item.IndexFromFW = index

                                if( (item.HostName == "N/A") || (item.HostName == "") )
                                {
                                    index++
                                    continue
                                }

                                for(itemCin in GlobalData.changeIconNameList)
                                {
                                    if(item.PhysAddress == itemCin.MacAddress)
                                    {
                                        item.UserDefineName = itemCin.HostName
                                        item.Internet_Blocking_Enable = itemCin.Internet_Blocking_Enable
                                    }
                                }

                                if(item.X_ZYXEL_CapabilityType == "L2Device")
                                    newZYXELEndDeviceList.add(item)
                                else
                                {
                                    if(item.X_ZYXEL_Conn_Guest == 1)
                                        newGuestEndDeviceList.add(item)
                                    else
                                        newHomeEndDeviceList.add(item)
                                }

                                newEndDeviceList.add(item)

                                LogUtil.d(TAG,"update devicesInfo:$item")

                                index++
                            }

                            GlobalData.endDeviceList = newEndDeviceList.toMutableList()
                            GlobalData.homeEndDeviceList = newHomeEndDeviceList.toMutableList()
                            GlobalData.ZYXELEndDeviceList = newZYXELEndDeviceList.toMutableList()
                            GlobalData.guestEndDeviceList = newGuestEndDeviceList.toMutableList()

                            updateUI()
                        }
                        catch(e: JSONException)
                        {
                            e.printStackTrace()

                            GlobalBus.publish(MainEvent.HideLoading())
                        }
                    }
                }).execute()
    }
}