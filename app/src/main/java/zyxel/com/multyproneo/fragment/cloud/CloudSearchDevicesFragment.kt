package zyxel.com.multyproneo.fragment.cloud

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.fragment_devices_search.*
import org.jetbrains.anko.sdk27.coroutines.textChangedListener
import zyxel.com.multyproneo.R
import zyxel.com.multyproneo.adapter.cloud.CloudSearchEndDeviceItemAdapter
import zyxel.com.multyproneo.event.GlobalBus
import zyxel.com.multyproneo.event.MainEvent
import zyxel.com.multyproneo.model.DevicesInfoObject
import zyxel.com.multyproneo.tool.CommonTool
import zyxel.com.multyproneo.util.GlobalData

class CloudSearchDevicesFragment : Fragment()
{
    private val TAG = "CloudSearchDevicesFragment"
    private lateinit var itemAdapter: CloudSearchEndDeviceItemAdapter
    private lateinit var homeEndDeviceList: MutableList<DevicesInfoObject>
    private lateinit var guestEndDeviceList: MutableList<DevicesInfoObject>
    private var searchResultDeviceList = mutableListOf<DevicesInfoObject>()
    private var searchStr = ""

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
    {
        return inflater.inflate(R.layout.fragment_devices_search, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)

        GlobalData.currentFrag = TAG

        with(arguments){ this?.getString("Search")?.let{ searchStr = it } }

        itemAdapter = CloudSearchEndDeviceItemAdapter(activity!!)

        setClickListener()
        initUI()
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

    private val clickListener = View.OnClickListener{ view ->
        when(view)
        {
            devices_search_clear_image -> devices_search_input_edit.setText("")

            devices_search_cancel_text ->
            {
                CommonTool.hideKeyboard(activity!!)
                GlobalBus.publish(MainEvent.EnterCloudDevicesPage())
            }
        }
    }

    private fun setClickListener()
    {
        devices_search_clear_image.setOnClickListener(clickListener)
        devices_search_cancel_text.setOnClickListener(clickListener)
    }

    private fun initUI()
    {
        searchResultDeviceList.clear()

        homeEndDeviceList = GlobalData.homeEndDeviceList
        guestEndDeviceList = GlobalData.guestEndDeviceList

        for(item in homeEndDeviceList)
            searchResultDeviceList.add(item)

        for(item in guestEndDeviceList)
            searchResultDeviceList.add(item)

        itemAdapter.endDeviceList = searchResultDeviceList
        devices_search_devices_list.adapter = itemAdapter

        initSearchEdit()

        devices_search_input_edit.setText(searchStr)
    }

    private fun initSearchEdit()
    {
        devices_search_input_edit.textChangedListener{
            onTextChanged{
                _: CharSequence?, _: Int, _: Int, _: Int ->
                val search = devices_search_input_edit.text.toString()

                searchResultDeviceList.clear()
                for(item in homeEndDeviceList)
                {
                    if(item.getName().toUpperCase().contains(search.toUpperCase()))
                        searchResultDeviceList.add(item)
                }

                for(item in guestEndDeviceList)
                {
                    if(item.getName().toUpperCase().contains(search.toUpperCase()))
                        searchResultDeviceList.add(item)
                }

                devices_search_devices_list.adapter = null
                itemAdapter.searchStr = search
                itemAdapter.endDeviceList = searchResultDeviceList
                devices_search_devices_list.adapter = itemAdapter
            }
        }
    }
}