package zyxel.com.multyproneo.fragment

import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.fragment_home_guest_end_device_detail.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.sdk27.coroutines.textChangedListener
import org.jetbrains.anko.textColor
import org.jetbrains.anko.uiThread
import zyxel.com.multyproneo.R
import zyxel.com.multyproneo.dialog.MessageDialog
import zyxel.com.multyproneo.event.DialogEvent
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
    private lateinit var msgDialogResponse: Disposable
    private lateinit var inputMethodManager: InputMethodManager
    private lateinit var endDeviceInfo: DevicesInfoObject
    private var isEditMode = false
    private var isBlocked = false
    private var isFromSearch = false
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

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
    {
        return inflater.inflate(R.layout.fragment_home_guest_end_device_detail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)

        with(arguments)
        {
            this?.getSerializable("DevicesInfo")?.let{ endDeviceInfo = it as DevicesInfoObject }
            this?.getString("Search")?.let{ searchStr = it }
            this?.getBoolean("FromSearch")?.let{ isFromSearch = it }
        }

        inputMethodManager = activity?.applicationContext?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

        msgDialogResponse = GlobalBus.listen(DialogEvent.OnPositiveBtn::class.java).subscribe {
            when(it.action)
            {
                AppConfig.DialogAction.ACT_BLOCK_DEVICE -> {}
                AppConfig.DialogAction.ACT_DELETE_ZYXEL_DEVICE -> {}
            }
        }

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
        if(!msgDialogResponse.isDisposed) msgDialogResponse.dispose()
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

                            false -> GlobalBus.publish(MainEvent.EnterDevicesPage())
                        }
                    }
                }
            }

            end_device_detail_confirm_image -> setDeviceNameTask()

            end_device_detail_edit_image ->
            {
                isEditMode = true
                setEditModeUI()
            }

            end_device_detail_block_device_image ->
            {
                MessageDialog(
                        activity!!,
                        getString(R.string.message_dialog_block_title),
                        getString(R.string.message_dialog_block_msg),
                        arrayOf(getString(R.string.message_dialog_ok), getString(R.string.message_dialog_cancel)),
                        AppConfig.DialogAction.ACT_BLOCK_DEVICE
                ).show()
            }

            end_device_detail_profile_image -> {}

            end_device_detail_remove_device_text ->
            {
                MessageDialog(
                        activity!!,
                        "",
                        getString(R.string.message_dialog_delete_lower_case) + " " + endDeviceInfo.getName() + " ?",
                        arrayOf(getString(R.string.message_dialog_delete), getString(R.string.message_dialog_cancel)),
                        AppConfig.DialogAction.ACT_DELETE_DEVICE
                ).show()
            }

            end_device_detail_internet_blocking_image ->
            {
                isBlocked = !isBlocked
                endDeviceInfo.Internet_Blocking_Enable = if(isBlocked) 1 else 0
                updateUI()
            }
        }
    }

    private fun setClickListener()
    {
        end_device_detail_back_image.setOnClickListener(clickListener)
        end_device_detail_confirm_image.setOnClickListener(clickListener)
        end_device_detail_edit_image.setOnClickListener(clickListener)
        end_device_detail_block_device_image.setOnClickListener(clickListener)
        end_device_detail_profile_image.setOnClickListener(clickListener)
        end_device_detail_remove_device_text.setOnClickListener(clickListener)
        end_device_detail_internet_blocking_image.setOnClickListener(clickListener)
    }

    private fun updateUI()
    {
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

        val isConnect = endDeviceInfo.Active

        modelName = SpecialCharacterHandler.checkEmptyTextValue(endDeviceInfo.getName())
        connectType = SpecialCharacterHandler.checkEmptyTextValue(endDeviceInfo.X_ZYXEL_ConnectionType)
        connectTo = SpecialCharacterHandler.checkEmptyTextValue(
                if(endDeviceInfo.X_ZYXEL_Neighbor.equals("gateway", ignoreCase = true))
                    GlobalData.getCurrentGatewayInfo().ModelName
                else
                    endDeviceInfo.X_ZYXEL_Neighbor
        )
        ip = SpecialCharacterHandler.checkEmptyTextValue(endDeviceInfo.IPAddress)
        mac = SpecialCharacterHandler.checkEmptyTextValue(endDeviceInfo.PhysAddress)
        manufacturer = SpecialCharacterHandler.checkEmptyTextValue(OUIUtil.getOUI(activity!!, endDeviceInfo.PhysAddress))
        dhcpTime = SpecialCharacterHandler.checkEmptyTextValue(endDeviceInfo.X_ZYXEL_DHCPLeaseTime.toString())

        if(FeatureConfig.hostNameReplease)
        {
            if(modelName.equals("unknown", ignoreCase = true))
                modelName = OUIUtil.getOUI(activity!!, endDeviceInfo.PhysAddress)
        }

        if(connectType.equals("WiFi", ignoreCase = true))
        {
            when(endDeviceInfo.X_ZYXEL_Band)
            {
                2 -> wifiBand = "5G"
                3 -> wifiBand = "2.4G/5G"
                else -> wifiBand = "2.4G"
            }

            wifiChannel = SpecialCharacterHandler.checkEmptyTextValue(if(endDeviceInfo.X_ZYXEL_Band == 2) endDeviceInfo.X_ZYXEL_Channel_5G.toString() else endDeviceInfo.X_ZYXEL_Channel_24G.toString())
            maxSpeed = SpecialCharacterHandler.checkEmptyTextValue(endDeviceInfo.X_ZYXEL_PhyRate.toString() + "Mbps")
            rssi = SpecialCharacterHandler.checkEmptyTextValue(endDeviceInfo.X_ZYXEL_RSSI.toString())
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

        isBlocked = endDeviceInfo.Internet_Blocking_Enable == 1

        when(isConnect)
        {
            true ->
            {
                with(end_device_detail_status_text)
                {
                    text = if(isBlocked) getString(R.string.device_detail_blocked) else getString(R.string.device_detail_connecting)
                    textColor = if(isBlocked) resources.getColor(R.color.color_ff2837) else resources.getColor(R.color.color_3c9f00)
                }
                end_device_detail_connect_type_dhcp_time_title_text.text = getString(R.string.device_detail_connect_type)
                end_device_detail_connect_type_dhcp_time_text.text = connectType
                end_device_detail_internet_blocking_area_relative.visibility = View.VISIBLE
                end_device_detail_internet_blocking_image.setImageResource(if(isBlocked) R.drawable.switch_on else R.drawable.switch_off)
                end_device_detail_remove_device_text.visibility = View.INVISIBLE
            }

            false ->
            {
                with(end_device_detail_status_text)
                {
                    text = getString(R.string.device_detail_disconnect)
                    textColor = resources.getColor(R.color.color_575757)
                }
                end_device_detail_connect_type_dhcp_time_title_text.text = getString(R.string.device_detail_last_seen)
                end_device_detail_connect_type_dhcp_time_text.text = CommonTool.formatData("yyyy-MM-dd HH:mm:ss", dhcpTime.toLong())
                end_device_detail_connect_to_linear.visibility = View.GONE
                end_device_detail_wifi_band_linear.visibility = View.GONE
                end_device_detail_wifi_channel_linear.visibility = View.GONE
                end_device_detail_max_speed_linear.visibility = View.GONE
                end_device_detail_rssi_linear.visibility = View.GONE
                end_device_detail_internet_blocking_area_relative.visibility = View.GONE
                end_device_detail_remove_device_text.visibility = View.INVISIBLE
            }
        }

        initEndDeviceDetailModelNameEdit()
    }

    private fun setEditModeUI()
    {
        when(isEditMode)
        {
            true ->
            {
                end_device_detail_model_name_edit.setText(endDeviceInfo.getName())
                end_device_detail_model_name_relative.visibility = View.GONE
                end_device_detail_model_name_edit_relative.visibility = View.VISIBLE
                end_device_detail_content_area_relative.alpha = 0.6.toFloat()
                end_device_detail_block_device_image.isEnabled = false
                end_device_detail_profile_image.isEnabled = false
                end_device_detail_remove_device_text.isEnabled = false
            }

            false ->
            {
                end_device_detail_model_name_relative.visibility = View.VISIBLE
                end_device_detail_model_name_edit_relative.visibility = View.GONE
                end_device_detail_content_area_relative.alpha = 1.toFloat()
                end_device_detail_block_device_image.isEnabled = true
                end_device_detail_profile_image.isEnabled = true
                end_device_detail_remove_device_text.isEnabled = true
            }
        }
    }

    private fun checkInputEditUI(illegalInput: Boolean)
    {
        when(illegalInput)
        {
            true ->
            {
                with(end_device_detail_model_name_edit_error_text)
                {
                    text = getString(R.string.login_no_support_character)
                    visibility = View.VISIBLE
                }

                with(end_device_detail_confirm_image)
                {
                    isEnabled = false
                    alpha = 0.3.toFloat()
                }

                end_device_detail_edit_line_image.setImageResource(R.color.color_ff2837)
            }

            false ->
            {
                end_device_detail_model_name_edit_error_text.visibility = View.INVISIBLE
                end_device_detail_edit_line_image.setImageResource(R.color.color_ffc800)
                with(end_device_detail_confirm_image)
                {
                    isEnabled = true
                    alpha = 1.toFloat()
                }
            }
        }
    }

    private fun initEndDeviceDetailModelNameEdit()
    {
        end_device_detail_model_name_edit.textChangedListener{
            onTextChanged{
                _: CharSequence?, _: Int, _: Int, _: Int ->
                checkInputEditUI(SpecialCharacterHandler.containsEmoji(end_device_detail_model_name_edit.text.toString()))
            }
        }
    }

    private fun setDeviceNameTask()
    {
        var editDeviceName = ""
        doAsync{
            inputMethodManager.hideSoftInputFromWindow(end_device_detail_model_name_edit.applicationWindowToken, 0)
            editDeviceName = end_device_detail_model_name_edit.text.toString()

            uiThread{
                isEditMode = false

                if(isVisible)
                {
                    end_device_detail_model_name_text.text = editDeviceName
                    end_device_detail_model_name_edit.setText(editDeviceName)
                    setEditModeUI()
                }
            }
        }
    }
}