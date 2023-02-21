package zyxel.com.multyproneo.fragment

import android.content.Context
import android.os.Bundle
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.Fragment
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.fragment_home_guest_end_device_detail.*
import org.jetbrains.anko.sdk27.coroutines.textChangedListener
import org.jetbrains.anko.support.v4.runOnUiThread
import org.jetbrains.anko.textColor
import org.json.JSONException
import org.json.JSONObject
import zyxel.com.multyproneo.R
import zyxel.com.multyproneo.api.ApiHandler
import zyxel.com.multyproneo.api.Commander
import zyxel.com.multyproneo.api.DevicesApi
import zyxel.com.multyproneo.dialog.MessageDialog
import zyxel.com.multyproneo.event.ApiEvent
import zyxel.com.multyproneo.event.GlobalBus
import zyxel.com.multyproneo.event.MainEvent
import zyxel.com.multyproneo.model.DevicesInfoObject
import zyxel.com.multyproneo.tool.CommonTool
import zyxel.com.multyproneo.tool.SpecialCharacterHandler
import zyxel.com.multyproneo.util.*

/**
 * Created by LouisTien on 2019/6/11.
 */
class EndDeviceDetailFragment : Fragment()
{
    private val TAG = "EndDeviceDetailFragment"
    private lateinit var getInfoCompleteDisposable: Disposable
    private lateinit var inputMethodManager: InputMethodManager
    private lateinit var endDeviceInfo: DevicesInfoObject
    private var isEditMode = false
    private var isBlocked = false
    private var isFromSearch = false
    private var userIllegalInput = false
    private var modelName = "N/A"
    private var dhcpTime = "N/A"
    private var connectType = "N/A"
    private var connectTo = "N/A"
    private var wifiBand = "N/A"
    private var wifiChannel = "N/A"
    private var ip = "N/A"
    private var mac = "N/A"
    private var maxSpeed = "N/A"
    private var rssi = "N/A"
    private var manufacturer = "N/A"
    private var searchStr = ""
    private var editDeviceName = "N/A"
    private var isFromTopology = false
    private var selectedNodeMAC = ""

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
    {
        return inflater.inflate(R.layout.fragment_home_guest_end_device_detail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)

        GlobalData.currentFrag = TAG

        with(arguments)
        {
            this?.getSerializable("DevicesInfo")?.let{ endDeviceInfo = it as DevicesInfoObject }
            this?.getString("Search")?.let{ searchStr = it }
            this?.getBoolean("FromSearch")?.let{ isFromSearch = it }
            this?.getBoolean("FromMeshTopology")?.let{ isFromTopology = it }
            this?.getString(GlobalData.SelectedNodeMAC)?.let{ selectedNodeMAC = it }
        }

        inputMethodManager = activity?.applicationContext?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

        getInfoCompleteDisposable = GlobalBus.listen(ApiEvent.ApiExecuteComplete::class.java).subscribe{
            GlobalBus.publish(MainEvent.HideLoading())
            isEditMode = false

            when(it.event)
            {
                ApiHandler.API_RES_EVENT.API_RES_EVENT_END_DEVICE_EDIT ->
                {
                    runOnUiThread{
                        if(isVisible)
                        {
                            end_device_detail_model_name_text.text = editDeviceName
                            end_device_detail_model_name_edit.setText(editDeviceName)
                            setEditModeUI()

                            for(item in GlobalData.endDeviceList)
                            {
                                if(item.PhysAddress == endDeviceInfo.PhysAddress)
                                {
                                    endDeviceInfo = item
                                    break
                                }
                            }

                            updateUI()
                        }
                    }
                }

                else -> {}
            }
        }

//        end_device_detail_fsecure_text.text = Html.fromHtml("<u>"+"F-Secure"+"</u>")

        setClickListener()
    }

    override fun onResume()
    {
        super.onResume()
        GlobalBus.publish(MainEvent.HideBottomToolbar())
        updateUI()
    }

    override fun onPause()
    {
        super.onPause()
    }

    override fun onDestroyView()
    {
        super.onDestroyView()
        if(!getInfoCompleteDisposable.isDisposed) getInfoCompleteDisposable.dispose()
    }

    private val clickListener = View.OnClickListener{ view ->
        when(view)
        {
            end_device_detail_back_image ->
            {
                when(isEditMode)
                {
                    true ->
                    {
                        isEditMode = false
                        inputMethodManager.hideSoftInputFromWindow(end_device_detail_model_name_edit.applicationWindowToken, 0)
                        setEditModeUI()
                    }

                    false ->
                    {
                        when(isFromSearch)
                        {
                            true ->
                            {
                                val bundle = Bundle().apply{
                                    putString("Search", searchStr)
                                }
                                GlobalBus.publish(MainEvent.SwitchToFrag(SearchDevicesFragment().apply{ arguments = bundle }))
                            }

                            false -> {
                                when(isFromTopology)
                                {
                                    true ->{
                                        val temp =
                                            GlobalData.ZYXELEndDeviceListTreeNode.filter { it.data.PhysAddress == selectedNodeMAC }

                                        if(temp.isNotEmpty()) {
                                            val bundle = Bundle().apply {
                                                putString(GlobalData.SelectedNodeMAC, selectedNodeMAC)
                                                putString(GlobalData.RootNodeMAC,
                                                    temp[0].parent?.data?.PhysAddress
                                                )
                                            }

                                            GlobalBus.publish(
                                                MainEvent.SwitchToFrag(
                                                    DevicesListFragment().apply {
                                                        arguments = bundle
                                                    })
                                            )
                                        }else{
                                            val bundle = Bundle().apply {
                                                putBoolean("isGateway", true)
                                            }

                                            GlobalBus.publish(MainEvent.SwitchToFrag(MeshTopologyFragment().apply {
                                                arguments = bundle
                                            }))
                                        }
                                    }
                                    false ->{
                                        GlobalBus.publish(MainEvent.EnterDevicesPage())}
                                }
                            }
                        }
                    }
                }

            }

            end_device_detail_confirm_image -> setDeviceInfoTask()

            end_device_detail_edit_image ->
            {
                isEditMode = true
                setEditModeUI()
            }

            end_device_detail_internet_blocking_image ->
            {
                if(!isEditMode)
                {
                    isBlocked = !isBlocked
                    endDeviceInfo.Internet_Blocking_Enable = isBlocked
                    setDeviceInfoTask()
                }
            }

//            end_device_detail_fsecure_text ->
//            {
//                if(!isEditMode)
//                {
//                    MessageDialog(
//                            requireActivity(),
//                            "",
//                            getString(R.string.device_detail_f_secure_msg),
//                            arrayOf(getString(R.string.message_dialog_ok)),
//                            AppConfig.DialogAction.ACT_NONE
//                    ).show()
//                }
//            }

            end_device_detail_user_tips_image ->
            {
                if(!isEditMode)
                {
                    MessageDialog(
                            requireActivity(),
                            "",
                            getString(R.string.devices_user_tips),
                            arrayOf(getString(R.string.message_dialog_ok)),
                            AppConfig.DialogAction.ACT_NONE
                    ).show()
                }
            }
        }
    }

    private fun setClickListener()
    {
        end_device_detail_back_image.setOnClickListener(clickListener)
        end_device_detail_confirm_image.setOnClickListener(clickListener)
        end_device_detail_edit_image.setOnClickListener(clickListener)
        end_device_detail_internet_blocking_image.setOnClickListener(clickListener)
//        end_device_detail_fsecure_text.setOnClickListener(clickListener)
        end_device_detail_user_tips_image.setOnClickListener(clickListener)
    }

    private fun updateUI()
    {
        if(GlobalData.currentFrag != TAG) return

        if(!isVisible) return

        modelName = "N/A"
        dhcpTime = "N/A"
        connectType = "N/A"
        connectTo = "N/A"
        wifiBand = "N/A"
        wifiChannel = "N/A"
        ip = "N/A"
        mac = "N/A"
        maxSpeed = "N/A"
        rssi = "N/A"
        manufacturer = "N/A"

        modelName = SpecialCharacterHandler.checkEmptyTextValue(endDeviceInfo.getName())
        connectType = SpecialCharacterHandler.checkEmptyTextValue(endDeviceInfo.X_ZYXEL_ConnectionType)
        if(connectType.contains("WiFi", ignoreCase = true) or connectType.contains("Wi-Fi", ignoreCase = true))
            connectType = getString(R.string.device_detail_wireless)
        else
            connectType = getString(R.string.device_detail_wired)

        with(endDeviceInfo.X_ZYXEL_Neighbor)
        {
            when
            {
                equals("gateway", ignoreCase = true) ||
                equals("unknown", ignoreCase = true) ||
                equals("NULL", ignoreCase = true) ||
                equals("N/A", ignoreCase = true) ||
                equals("", ignoreCase = true) ||
                equals(GlobalData.getCurrentGatewayInfo().MAC, ignoreCase = true) ||
                isEmpty() ->
                { connectTo = SpecialCharacterHandler.checkEmptyTextValue(GlobalData.getCurrentGatewayInfo().UserDefineName) }

                else ->
                {
                    /*for(item in GlobalData.ZYXELEndDeviceList)
                    {
                        if(endDeviceInfo.X_ZYXEL_Neighbor.equals(item.PhysAddress, ignoreCase = true))
                            connectTo = SpecialCharacterHandler.checkEmptyTextValue(item.getName())
                    }*/

                    //if mac = 00:11:22:33:44:50, check the mac 00:11:22:33:44:5 to compare for work around of FW bug#117125
                    LogUtil.d(TAG,"NeighborMAC:${endDeviceInfo.X_ZYXEL_Neighbor}")
                    for(item in GlobalData.ZYXELEndDeviceList) {
                        if(CommonTool.checkIsTheSameDeviceMac(endDeviceInfo.X_ZYXEL_Neighbor,item.PhysAddress))
                            connectTo = SpecialCharacterHandler.checkEmptyTextValue(item.getName())
                    }
                }
            }
        }

        ip = SpecialCharacterHandler.checkEmptyTextValue(endDeviceInfo.IPAddress)
        mac = SpecialCharacterHandler.checkEmptyTextValue(endDeviceInfo.PhysAddress)
        manufacturer = SpecialCharacterHandler.checkEmptyTextValue(OUIUtil.getOUI(requireActivity(), endDeviceInfo.PhysAddress))
        dhcpTime = SpecialCharacterHandler.checkEmptyTextValue(endDeviceInfo.X_ZYXEL_DHCPLeaseTime.toString())

        if(FeatureConfig.hostNameReplaceStatus)
        {
            if(modelName.equals("unknown", ignoreCase = true) || modelName.equals("<unknown>", ignoreCase = true))
                modelName = OUIUtil.getOUI(requireActivity(), endDeviceInfo.PhysAddress)
        }

        if(connectType == getString(R.string.device_detail_wireless))
        {
            when(endDeviceInfo.X_ZYXEL_Band)
            {
                2 -> wifiBand = "5GHz"
                3 -> wifiBand = "2.4GHz/5GHz"
                else -> wifiBand = "2.4GHz"
            }

            val channel = if(endDeviceInfo.X_ZYXEL_Band == 2) endDeviceInfo.X_ZYXEL_Channel_5G else endDeviceInfo.X_ZYXEL_Channel_24G
            wifiChannel =
                    if(channel == 0)
                        getString(R.string.device_detail_max_speed_updating)
                    else
                        SpecialCharacterHandler.checkEmptyTextValue(channel.toString())

            rssi =
                    if(endDeviceInfo.X_ZYXEL_RSSI == 0)
                        getString(R.string.device_detail_max_speed_updating)
                    else
                        SpecialCharacterHandler.checkEmptyTextValue(endDeviceInfo.X_ZYXEL_RSSI.toString())

            maxSpeed =
                    if(endDeviceInfo.X_ZYXEL_PhyRate == 0L)
                        getString(R.string.device_detail_max_speed_updating)
                    else
                        SpecialCharacterHandler.checkEmptyTextValue(endDeviceInfo.X_ZYXEL_PhyRate.toString()) + getString(R.string.device_detail_speed_test_unit)
        }

        end_device_detail_model_name_text.text = modelName
        end_device_detail_model_name_edit.setText(modelName)
        end_device_detail_connect_to_text.text = connectTo
        end_device_detail_wifi_band_text.text = wifiBand
        end_device_detail_wifi_channel_text.text = wifiChannel
        end_device_detail_ip_text.text = ip
        end_device_detail_mac_text.text = mac
        end_device_detail_max_speed_text.text = maxSpeed
        end_device_detail_rssi_text.text = rssi
        end_device_detail_manufacturer_text.text = manufacturer

        isBlocked = endDeviceInfo.Internet_Blocking_Enable

        when(endDeviceInfo.Active)
        {
            true ->
            {
                with(end_device_detail_status_text)
                {
                    text = if(isBlocked || endDeviceInfo.ParentalControlBlock) getString(R.string.device_detail_blocked) else getString(R.string.device_detail_connecting)
                    textColor = if(isBlocked || endDeviceInfo.ParentalControlBlock) resources.getColor(R.color.color_ff2837) else resources.getColor(R.color.color_3c9f00)
                }

                with(end_device_detail_connect_type_dhcp_time_text)
                {
                    text = connectType
                    textColor = resources.getColor(R.color.color_000000)
                }

                end_device_detail_connect_type_dhcp_time_title_text.text = getString(R.string.device_detail_connect_type)

                when(FeatureConfig.internetBlockingStatus)
                {
                    true ->
                    {
                        end_device_detail_ib_pt_area_relative.visibility = View.VISIBLE

                        if(endDeviceInfo.ParentalControlBlock)
                        {
                            end_device_detail_block_msg_text.text = String.format(getString(R.string.device_detail_blocke_by_schedule), endDeviceInfo.InParentalControlProfileName)
                            end_device_detail_block_msg_text.visibility = View.VISIBLE
                            end_device_detail_internet_blocking_image.isEnabled = false
                            end_device_detail_internet_blocking_area_linear.alpha = 0.3f
                        }
                        else
                        {
                            end_device_detail_block_msg_text.visibility = View.GONE
                            end_device_detail_internet_blocking_image.isEnabled = true
                            end_device_detail_internet_blocking_area_linear.alpha = 1f
                        }
                    }

                    false -> end_device_detail_ib_pt_area_relative.visibility = View.GONE
                }

                when(FeatureConfig.FSecureStatus)
                {
                    true ->
                    {
                        end_device_detail_internet_blocking_area_linear.visibility = View.GONE
                        end_device_detail_internet_blocking_line_image.visibility = View.GONE
                        end_device_detail_parental_control_area_linear.visibility = View.VISIBLE
                        end_device_detail_parental_control_line_image.visibility = View.VISIBLE
                    }

                    false ->
                    {
                        end_device_detail_internet_blocking_area_linear.visibility = View.VISIBLE
                        end_device_detail_internet_blocking_line_image.visibility = View.VISIBLE
                        end_device_detail_parental_control_area_linear.visibility = View.GONE
                        end_device_detail_parental_control_line_image.visibility = View.GONE
                    }
                }

                end_device_detail_internet_blocking_image.setImageResource(if(isBlocked) R.drawable.switch_on else R.drawable.switch_off_2)

                if(connectType == getString(R.string.device_detail_wired))
                {
                    end_device_detail_wifi_band_linear.visibility = View.GONE
                    end_device_detail_wifi_channel_linear.visibility = View.GONE
                    end_device_detail_max_speed_linear.visibility = View.GONE
                    end_device_detail_rssi_linear.visibility = View.GONE
                }
                else
                {
                    end_device_detail_max_speed_linear.visibility =
                            if(FeatureConfig.FeatureInfo.APPUICustomList.Client_Max_Speed)
                                View.VISIBLE
                            else
                                View.GONE
                }
            }

            false ->
            {
                with(end_device_detail_status_text)
                {
                    text = getString(R.string.device_detail_disconnect)
                    textColor = resources.getColor(R.color.color_575757)
                }

                with(end_device_detail_connect_type_dhcp_time_text)
                {
                    text = CommonTool.formatData("yyyy-MM-dd HH:mm", dhcpTime.toLong())
                    textColor = resources.getColor(R.color.color_575757)
                }

                end_device_detail_connect_type_dhcp_time_title_text.text = getString(R.string.device_detail_last_seen)
                end_device_detail_connect_to_linear.visibility = View.GONE
                end_device_detail_wifi_band_linear.visibility = View.GONE
                end_device_detail_wifi_channel_linear.visibility = View.GONE
                end_device_detail_max_speed_linear.visibility = View.GONE
                end_device_detail_rssi_linear.visibility = View.GONE
                end_device_detail_ib_pt_area_relative.visibility = View.GONE
            }
        }

        if(endDeviceInfo.UserDefineName.equals("N/A", ignoreCase = true)
            && endDeviceInfo.PhysAddress == endDeviceInfo.HostName)
            end_device_detail_user_tips_image.visibility = View.VISIBLE
        else
            end_device_detail_user_tips_image.visibility = View.GONE

        initEndDeviceDetailModelNameEdit()
    }

    private fun setEditModeUI()
    {
        if(!isVisible) return

        when(isEditMode)
        {
            true ->
            {
                end_device_detail_model_name_edit.setText(endDeviceInfo.getName())
                end_device_detail_model_name_relative.visibility = View.GONE
                end_device_detail_model_name_edit_relative.visibility = View.VISIBLE
                end_device_detail_content_area_relative.alpha = 0.6.toFloat()
            }

            false ->
            {
                end_device_detail_model_name_relative.visibility = View.VISIBLE
                end_device_detail_model_name_edit_relative.visibility = View.GONE
                end_device_detail_content_area_relative.alpha = 1.toFloat()
            }
        }
    }

    private fun checkInputEditUI()
    {
        when(userIllegalInput)
        {
            true ->
            {
                with(end_device_detail_model_name_edit_error_text)
                {
                    text = getString(R.string.login_no_support_character)
                    visibility = View.VISIBLE
                }

                end_device_detail_edit_line_image.setImageResource(R.color.color_ff2837)
            }

            false ->
            {
                end_device_detail_model_name_edit_error_text.visibility = View.INVISIBLE
                end_device_detail_edit_line_image.setImageResource(R.color.color_ffc800)
            }
        }

        when
        {
            end_device_detail_model_name_edit.text.length >= AppConfig.deviceUserNameRequiredLength
            && !userIllegalInput
            ->
                with(end_device_detail_confirm_image)
                {
                    isEnabled = true
                    alpha = 1.toFloat()
                }

            else ->
                with(end_device_detail_confirm_image)
                {
                    isEnabled = false
                    alpha = 0.3.toFloat()
                }
        }
    }

    private fun initEndDeviceDetailModelNameEdit()
    {
        end_device_detail_model_name_edit.textChangedListener{
            onTextChanged{
                str: CharSequence?, start: Int, _: Int, count: Int ->
                userIllegalInput = SpecialCharacterHandler.containsEmoji(str.toString())
                                || SpecialCharacterHandler.containsSpecialCharacter(str.toString())
                                || SpecialCharacterHandler.containsExcludeASCII(str.toString())
                checkInputEditUI()
            }
        }
    }

    private fun setDeviceInfoTask()
    {
        LogUtil.d(TAG,"setDeviceInfoTask()")
        inputMethodManager.hideSoftInputFromWindow(end_device_detail_model_name_edit.applicationWindowToken, 0)
        GlobalBus.publish(MainEvent.ShowLoading())

        editDeviceName = end_device_detail_model_name_edit.text.toString()
        val params = JSONObject()
        params.put("HostName", editDeviceName)
        params.put("MacAddress", endDeviceInfo.PhysAddress)
        params.put("Internet_Blocking_Enable", isBlocked)
        LogUtil.d(TAG,"setDeviceInfoTask param:$params")

        var index = 0
        for(i in GlobalData.changeIconNameList.indices)
        {
            if(GlobalData.changeIconNameList[i].MacAddress == endDeviceInfo.PhysAddress)
            {
                index = i + 1
                break
            }
        }

        if(index == 0)
        {
            DevicesApi.SetChangeIconNameInfo()
                    .setRequestPageName(TAG)
                    .setParams(params)
                    .setResponseListener(object: Commander.ResponseListener()
                    {
                        override fun onSuccess(responseStr: String)
                        {
                            try
                            {
                                val data = JSONObject(responseStr)
                                val sessionkey = data.get("sessionkey").toString()
                                GlobalData.loginInfo.sessionkey = sessionkey
                                startGetDeviceInfo()
                            }
                            catch(e: JSONException)
                            {
                                e.printStackTrace()
                                GlobalBus.publish(MainEvent.HideLoading())
                            }
                        }
                    }).execute()
        }
        else
        {
            DevicesApi.SetChangeIconNameInfoByIndex(index)
                    .setRequestPageName(TAG)
                    .setParams(params)
                    .setResponseListener(object: Commander.ResponseListener()
                    {
                        override fun onSuccess(responseStr: String)
                        {
                            try
                            {
                                val data = JSONObject(responseStr)
                                val sessionkey = data.get("sessionkey").toString()
                                GlobalData.loginInfo.sessionkey = sessionkey
                                startGetDeviceInfo()
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

    private fun startGetDeviceInfo()
    {
        GlobalBus.publish(MainEvent.ShowLoading())
        val apiList = arrayListOf<ApiHandler.API_REF>()
        apiList.add(ApiHandler.API_REF.API_GET_CHANGE_ICON_NAME)
        apiList.add(ApiHandler.API_REF.API_GET_DEVICE_INFO)

        if(FeatureConfig.FeatureInfo.APPUICustomList.Parental_Control)
        {
            apiList.add(ApiHandler.API_REF.API_GET_PARENTAL_CONTROL_INFO)
            apiList.add(ApiHandler.API_REF.API_GET_GATEWAY_SYSTEM_DATE)
            apiList.add(ApiHandler.API_REF.API_CHECK_IN_USE_SELECT_DEVICE)
        }

        ApiHandler().execute(
                ApiHandler.API_RES_EVENT.API_RES_EVENT_END_DEVICE_EDIT,
                apiList
        )
    }
}