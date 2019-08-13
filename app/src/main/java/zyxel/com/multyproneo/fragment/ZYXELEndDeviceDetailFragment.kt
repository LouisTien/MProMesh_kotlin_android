package zyxel.com.multyproneo.fragment

import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.text.Html
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.fragment_zyxel_end_device_detail.*
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
import zyxel.com.multyproneo.model.GatewayInfo
import zyxel.com.multyproneo.model.WanInfo
import zyxel.com.multyproneo.tool.SpecialCharacterHandler
import zyxel.com.multyproneo.util.AppConfig
import zyxel.com.multyproneo.util.DatabaseUtil

/**
 * Created by LouisTien on 2019/6/6.
 */
class ZYXELEndDeviceDetailFragment : Fragment()
{
    private lateinit var msgDialogResponse: Disposable
    private lateinit var inputMethodManager: InputMethodManager
    private lateinit var deviceInfo: GatewayInfo
    private lateinit var deviceWanInfo: WanInfo
    private lateinit var endDeviceInfo: DevicesInfoObject
    private var isGatewayMode = false
    private var isEditMode = false
    private var isConnect = false
    private var deviceLanIP = "N/A"
    private var modelName = "N/A"
    private var status = "N/A"
    private var connectType = "N/A"
    private var wanIP = "N/A"
    private var dnsIP = "N/A"
    private var lanIP = "N/A"
    private var mac = "N/A"
    private var fwVer = "N/A"
    private var ip = "N/A"

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
    {
        return inflater.inflate(R.layout.fragment_zyxel_end_device_detail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)

        with(arguments)
        {
            this?.getBoolean("GatewayMode")?.let{ isGatewayMode = it }
            this?.getSerializable("GatewayInfo")?.let{ deviceInfo = it as GatewayInfo }
            this?.getSerializable("WanInfo")?.let { deviceWanInfo = it as WanInfo }
            this?.getString("GatewayLanIP")?.let{ deviceLanIP = it }
            this?.getSerializable("DevicesInfo")?.let{ endDeviceInfo = it as DevicesInfoObject }
        }

        inputMethodManager = activity?.applicationContext?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

        msgDialogResponse = GlobalBus.listen(DialogEvent.OnPositiveBtn::class.java).subscribe{
            when(it.action)
            {
                AppConfig.DialogAction.ACT_REBOOT ->
                {
                    val bundle = Bundle().apply{
                        putString("Title", "")
                        putString("Description", resources.getString(R.string.loading_transition_take_few_minutes))
                        putString("Sec_Description", resources.getString(R.string.loading_transition_reboot))
                        putInt("LoadingSecond", AppConfig.rebootTime)
                        putSerializable("Anim", AppConfig.LoadingAnimation.ANIM_REBOOT)
                        putSerializable("DesPage", if(isGatewayMode) AppConfig.LoadingGoToPage.FRAG_SEARCH else AppConfig.LoadingGoToPage.FRAG_HOME)
                        putBoolean("ShowCountDownTimer", false)
                    }
                    GlobalBus.publish(MainEvent.SwitchToFrag(LoadingTransitionFragment().apply{ arguments = bundle }))
                }

                AppConfig.DialogAction.ACT_DELETE_DEVICE -> {}
            }
        }

        setClickListener()
    }

    override fun onResume()
    {
        super.onResume()
        GlobalBus.publish(MainEvent.HideBottomToolbar())
        initUI()
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
            zyxel_end_device_detail_back_image ->
            {
                if(isEditMode)
                {
                    isEditMode = false
                    inputMethodManager.hideSoftInputFromWindow(zyxel_end_device_detail_model_name_edit.applicationWindowToken, 0)
                    setEditModeUI()
                }
                else
                    GlobalBus.publish(MainEvent.EnterHomePage())
            }

            zyxel_end_device_detail_confirm_image -> setDeviceNameTask()

            zyxel_end_device_detail_edit_image ->
            {
                isEditMode = true
                setEditModeUI()
            }

            zyxel_end_device_detail_reboot_button ->
            {
                MessageDialog(
                        activity!!,
                        getString(R.string.message_dialog_reboot_reminder_title),
                        getString(R.string.message_dialog_reboot_reminder),
                        arrayOf(getString(R.string.message_dialog_restart), getString(R.string.message_dialog_cancel)),
                        AppConfig.DialogAction.ACT_REBOOT
                ).show()
            }

            zyxel_end_device_detail_remove_device_text ->
            {
                MessageDialog(
                        activity!!,
                        "",
                        getString(R.string.message_dialog_delete_lower_case) + " " + endDeviceInfo.UserDefineName + " ?",
                        arrayOf(getString(R.string.message_dialog_delete), getString(R.string.message_dialog_cancel)),
                        AppConfig.DialogAction.ACT_DELETE_ZYXEL_DEVICE
                ).show()
            }
        }
    }

    private fun setClickListener()
    {
        zyxel_end_device_detail_back_image.setOnClickListener(clickListener)
        zyxel_end_device_detail_confirm_image.setOnClickListener(clickListener)
        zyxel_end_device_detail_edit_image.setOnClickListener(clickListener)
        zyxel_end_device_detail_reboot_button.setOnClickListener(clickListener)
        zyxel_end_device_detail_remove_device_text.setOnClickListener(clickListener)
    }

    private fun initUI()
    {
        val isGatewayConnect = deviceWanInfo.Object.Status == "Enable"
        val isEndDeviceConnect = endDeviceInfo.Active
        isConnect = if(isGatewayMode) isGatewayConnect else isEndDeviceConnect

        setConnectTypeTextListVisibility(isConnect)

        modelName = SpecialCharacterHandler.checkEmptyTextValue(if(isGatewayMode) deviceInfo.UserDefineName else endDeviceInfo.UserDefineName)
        status = SpecialCharacterHandler.checkEmptyTextValue(getString(if(isConnect) R.string.device_detail_connecting else R.string.device_detail_disconnect))
        connectType = SpecialCharacterHandler.checkEmptyTextValue(if(isGatewayMode) (if(isConnect) getString(R.string.device_detail_wire) else "") else endDeviceInfo.X_ZYXEL_ConnectionType)
        ip = SpecialCharacterHandler.checkEmptyTextValue(if(isGatewayMode) "" else endDeviceInfo.IPAddress)
        wanIP = SpecialCharacterHandler.checkEmptyTextValue(if(isGatewayMode) deviceWanInfo.Object.IPAddress else "")
        dnsIP = SpecialCharacterHandler.checkEmptyTextValue(if(isGatewayMode) deviceWanInfo.Object.DNSServer else "")
        lanIP = SpecialCharacterHandler.checkEmptyTextValue(if(isGatewayMode) deviceLanIP else "")
        mac = SpecialCharacterHandler.checkEmptyTextValue(if(isGatewayMode) deviceWanInfo.Object.MAC else endDeviceInfo.PhysAddress)
        fwVer = SpecialCharacterHandler.checkEmptyTextValue(if(isGatewayMode) deviceInfo.SoftwareVersion else endDeviceInfo.X_ZYXEL_SoftwareVersion)

        zyxel_end_device_detail_model_name_text.text = modelName
        zyxel_end_device_detail_model_name_edit.setText(modelName)
        zyxel_end_device_detail_status_text.text = status
        zyxel_end_device_detail_connect_type_text.text = connectType
        zyxel_end_device_detail_ip_text.text = ip
        zyxel_end_device_detail_wan_ip_text.text = wanIP
        zyxel_end_device_detail_dns_ip_text.text = dnsIP
        zyxel_end_device_detail_mac_text.text = mac
        zyxel_end_device_detail_fw_text.text = fwVer
        zyxel_end_device_detail_status_text.textColor = resources.getColor(if(isConnect) R.color.color_3c9f00 else R.color.color_575757)
        with(zyxel_end_device_detail_lan_ip_text)
        {
            if(lanIP == "N/A")
                text = lanIP
            else
            {
                isClickable = true
                movementMethod = LinkMovementMethod.getInstance()
                text = Html.fromHtml("<a href='http://" + lanIP + "'>" + lanIP + "</a>")
            }
        }

        initEndDeviceDetailModelNameEdit()

        if(isGatewayMode) setGatewayModeUI() else setEndDeviceModeUI()
    }

    private fun setConnectTypeTextListVisibility(value: Boolean)
    {
        zyxel_end_device_detail_connect_type_text.visibility = if(value) View.VISIBLE else View.INVISIBLE
        zyxel_end_device_detail_connect_type_title_text.visibility = if(value) View.VISIBLE else View.INVISIBLE
    }

    private fun setContentLinearListVisibility(value: Boolean)
    {
        zyxel_end_device_detail_ip_linear.visibility = if(value) View.VISIBLE else View.INVISIBLE
        zyxel_end_device_detail_wan_ip_linear.visibility = if(value) View.VISIBLE else View.INVISIBLE
        zyxel_end_device_detail_dns_ip_linear.visibility = if(value) View.VISIBLE else View.INVISIBLE
        zyxel_end_device_detail_lan_ip_linear.visibility = if(value) View.VISIBLE else View.INVISIBLE
        zyxel_end_device_detail_mac_linear.visibility = if(value) View.VISIBLE else View.INVISIBLE
        zyxel_end_device_detail_fw_linear.visibility = if(value) View.VISIBLE else View.INVISIBLE
    }

    private fun setGatewayModeUI()
    {
        zyxel_end_device_detail_title_text.text = endDeviceInfo.X_ZYXEL_HostType + " " + getString(R.string.device_detail_detail)
        setContentLinearListVisibility(true)
        zyxel_end_device_detail_ip_linear.visibility = View.GONE
        zyxel_end_device_detail_reboot_button.visibility = if(isConnect) View.VISIBLE else View.INVISIBLE
        zyxel_end_device_detail_remove_device_text.visibility = if(isConnect) View.INVISIBLE else View.VISIBLE
    }

    private fun setEndDeviceModeUI()
    {
        zyxel_end_device_detail_title_text.text = endDeviceInfo.X_ZYXEL_HostType + " " + getString(R.string.device_detail_detail)
        setContentLinearListVisibility(true)
        zyxel_end_device_detail_wan_ip_linear.visibility = View.GONE
        zyxel_end_device_detail_dns_ip_linear.visibility = View.GONE
        zyxel_end_device_detail_lan_ip_linear.visibility = View.GONE
        zyxel_end_device_detail_reboot_button.visibility = View.INVISIBLE
        zyxel_end_device_detail_remove_device_text.visibility = if(isConnect) View.INVISIBLE else View.VISIBLE
    }

    private fun setEditModeUI()
    {
        when(isEditMode)
        {
            true ->
            {
                zyxel_end_device_detail_model_name_edit.setText(modelName)
                zyxel_end_device_detail_model_name_relative.visibility = View.GONE
                zyxel_end_device_detail_model_name_edit_relative.visibility = View.VISIBLE
                zyxel_end_device_detail_content_area_relative.alpha = 0.6.toFloat()
                zyxel_end_device_detail_reboot_button.isEnabled = false
                zyxel_end_device_detail_remove_device_text.isEnabled = false
            }

            false ->
            {
                zyxel_end_device_detail_model_name_relative.visibility = View.VISIBLE
                zyxel_end_device_detail_model_name_edit_relative.visibility = View.GONE
                zyxel_end_device_detail_content_area_relative.alpha = 1.toFloat()
                zyxel_end_device_detail_reboot_button.isEnabled = true
                zyxel_end_device_detail_remove_device_text.isEnabled = true
            }
        }
    }

    private fun checkInputEditUI(illegalInput: Boolean)
    {
        when(illegalInput)
        {
            true ->
            {
                with(zyxel_end_device_detail_model_name_edit_error_text)
                {
                    text = getString(R.string.login_no_support_character)
                    visibility = View.VISIBLE
                }

                zyxel_end_device_detail_edit_line_image.setImageResource(R.color.color_ff2837)

                with(zyxel_end_device_detail_confirm_image)
                {
                    isEnabled = false
                    alpha = 0.3.toFloat()
                }
            }

            false ->
            {
                zyxel_end_device_detail_model_name_edit_error_text.visibility = View.INVISIBLE
                zyxel_end_device_detail_edit_line_image.setImageResource(R.color.color_ffc800)
                with(zyxel_end_device_detail_confirm_image)
                {
                    isEnabled = true
                    alpha = 1.toFloat()
                }
            }
        }
    }

    private fun initEndDeviceDetailModelNameEdit()
    {
        zyxel_end_device_detail_model_name_edit.textChangedListener{
            onTextChanged{
                _: CharSequence?, _: Int, _: Int, _: Int ->
                checkInputEditUI(SpecialCharacterHandler.containsEmoji(zyxel_end_device_detail_model_name_edit.text.toString()))
            }
        }
    }

    private fun setDeviceNameTask()
    {
        var editDeviceName = ""
        doAsync{
            inputMethodManager.hideSoftInputFromWindow(zyxel_end_device_detail_model_name_edit.applicationWindowToken, 0)
            editDeviceName = zyxel_end_device_detail_model_name_edit.text.toString()

            uiThread{
                isEditMode = false

                if(isGatewayMode)
                {
                    deviceInfo.UserDefineName = editDeviceName
                    DatabaseUtil.getInstance(activity!!)?.updateInformationToDB(deviceInfo)
                }

                if(isVisible)
                {
                    zyxel_end_device_detail_model_name_text.text = editDeviceName
                    zyxel_end_device_detail_model_name_edit.setText(editDeviceName)
                    setEditModeUI()
                }
            }
        }
    }
}