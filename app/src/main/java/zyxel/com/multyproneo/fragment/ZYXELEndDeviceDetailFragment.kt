package zyxel.com.multyproneo.fragment

import android.content.Context
import android.os.Bundle
import android.text.Html
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.Fragment
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.fragment_zyxel_end_device_detail.*
import org.jetbrains.anko.sdk27.coroutines.textChangedListener
import org.jetbrains.anko.support.v4.runOnUiThread
import org.jetbrains.anko.textColor
import org.json.JSONException
import org.json.JSONObject
import zyxel.com.multyproneo.R
import zyxel.com.multyproneo.api.Commander
import zyxel.com.multyproneo.api.DevicesApi
import zyxel.com.multyproneo.api.GatewayApi
import zyxel.com.multyproneo.dialog.MessageDialog
import zyxel.com.multyproneo.event.*
import zyxel.com.multyproneo.model.DevicesInfoObject
import zyxel.com.multyproneo.model.GatewayInfo
import zyxel.com.multyproneo.model.WanInfo
import zyxel.com.multyproneo.tool.SpecialCharacterHandler
import zyxel.com.multyproneo.util.AppConfig
import zyxel.com.multyproneo.util.DatabaseUtil
import zyxel.com.multyproneo.util.GlobalData
import zyxel.com.multyproneo.util.LogUtil

/**
 * Created by LouisTien on 2019/6/6.
 */
class ZYXELEndDeviceDetailFragment : Fragment()
{
    private val TAG = "ZYXELEndDeviceDetailFragment"
    private lateinit var msgDialogResponse: Disposable
    private lateinit var getInfoCompleteDisposable: Disposable
    private lateinit var getSpeedTestResultDisposable: Disposable
    private lateinit var inputMethodManager: InputMethodManager
    private lateinit var deviceInfo: GatewayInfo
    private lateinit var deviceWanInfo: WanInfo
    private lateinit var endDeviceInfo: DevicesInfoObject
    private var isGatewayMode = false
    private var isEditMode = false
    private var isConnect = false
    private var userIllegalInput = false
    private var modelName = "N/A"
    private var status = "N/A"
    private var connectType = "N/A"
    private var wanIP = "N/A"
    private var dnsIP = "N/A"
    private var lanIP = "N/A"
    private var mac = "N/A"
    private var fwVer = "N/A"
    private var ip = "N/A"
    private var editDeviceName = "N/A"

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
    {
        return inflater.inflate(R.layout.fragment_zyxel_end_device_detail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)

        GlobalData.currentFrag = TAG

        with(arguments)
        {
            this?.getBoolean("GatewayMode")?.let{ isGatewayMode = it }
            this?.getSerializable("GatewayInfo")?.let{ deviceInfo = it as GatewayInfo }
            this?.getSerializable("WanInfo")?.let { deviceWanInfo = it as WanInfo }
            this?.getSerializable("DevicesInfo")?.let{ endDeviceInfo = it as DevicesInfoObject }
        }

        if(isGatewayMode)
            editDeviceName = deviceInfo.UserDefineName

        inputMethodManager = activity?.applicationContext?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

        msgDialogResponse = GlobalBus.listen(DialogEvent.OnPositiveBtn::class.java).subscribe{
            when(it.action)
            {
                AppConfig.DialogAction.ACT_REBOOT ->
                {
                    rebootTask()

                    if(isGatewayMode)
                    {
                        val bundle = Bundle().apply{
                            putString("Title", getString(R.string.loading_transition_reboot))
                            putInt("LoadingSecond", AppConfig.rebootTime)
                            putSerializable("DesPage", if(isGatewayMode) AppConfig.LoadingGoToPage.FRAG_SEARCH else AppConfig.LoadingGoToPage.FRAG_HOME)
                            putBoolean("IsCloud", false)
                        }
                        GlobalBus.publish(MainEvent.SwitchToFrag(LoadingTransitionProgressFragment().apply{ arguments = bundle }))
                    }
                }

                else -> {}
            }
        }

        getInfoCompleteDisposable = GlobalBus.listen(DevicesDetailEvent.GetDeviceInfoComplete::class.java).subscribe{
            isEditMode = false

            runOnUiThread{
                if(isVisible)
                {
                    zyxel_end_device_detail_model_name_text.text = editDeviceName
                    zyxel_end_device_detail_model_name_edit.setText(editDeviceName)
                    setEditModeUI()

                    if(isGatewayMode)
                        deviceInfo.UserDefineName = editDeviceName
                    else
                    {
                        for(item in GlobalData.endDeviceList)
                        {
                            if(item.PhysAddress == endDeviceInfo.PhysAddress)
                            {
                                endDeviceInfo = item
                                break
                            }
                        }
                    }

                    initUI()
                }
            }
        }

        getSpeedTestResultDisposable = GlobalBus.listen(GatewayEvent.GetSpeedTestComplete::class.java).subscribe{
            runOnUiThread{
                zyxel_end_device_detail_speed_test_download_content_text.text = it.downloadResult
                zyxel_end_device_detail_speed_test_upload_content_text.text = it.uploadResult
                zyxel_end_device_detail_speed_test_linear.visibility = View.VISIBLE
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
        GlobalBus.publish(MainEvent.StopGetSpeedTestStatusTask())
    }

    override fun onDestroyView()
    {
        super.onDestroyView()
        if(!msgDialogResponse.isDisposed) msgDialogResponse.dispose()
        if(!getInfoCompleteDisposable.isDisposed) getInfoCompleteDisposable.dispose()
        if(!getSpeedTestResultDisposable.isDisposed) getSpeedTestResultDisposable.dispose()
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
                if(!isEditMode)
                {
                    MessageDialog(
                            activity!!,
                            getString(R.string.message_dialog_reboot_reminder_title),
                            getString(R.string.message_dialog_reboot_reminder),
                            arrayOf(getString(R.string.message_dialog_restart), getString(R.string.message_dialog_cancel)),
                            AppConfig.DialogAction.ACT_REBOOT
                    ).show()
                }
            }

            zyxel_end_device_detail_speed_test_button ->
            {
                if(!isEditMode)
                    startSpeedTestTask()
            }
        }
    }

    private fun setClickListener()
    {
        zyxel_end_device_detail_back_image.setOnClickListener(clickListener)
        zyxel_end_device_detail_confirm_image.setOnClickListener(clickListener)
        zyxel_end_device_detail_edit_image.setOnClickListener(clickListener)
        zyxel_end_device_detail_reboot_button.setOnClickListener(clickListener)
        zyxel_end_device_detail_speed_test_button.setOnClickListener(clickListener)
    }

    private fun initUI()
    {
        val isGatewayConnect = deviceWanInfo.Object.Status == "Enable"
        val isEndDeviceConnect = endDeviceInfo.Active
        isConnect = if(isGatewayMode) isGatewayConnect else isEndDeviceConnect

        setConnectTypeTextListVisibility(isConnect)

        if(isGatewayMode) {
            val connectMode = deviceWanInfo.Object.Mode
            connectType = getString(if(connectMode.toLowerCase() == "repeater") R.string.device_detail_wireless else R.string.device_detail_wired)
        }
        else {
            connectType = SpecialCharacterHandler.checkEmptyTextValue(endDeviceInfo.X_ZYXEL_ConnectionType)
            if(connectType.contains("WiFi", ignoreCase = true) or connectType.contains("Wi-Fi", ignoreCase = true))
                connectType = getString(R.string.device_detail_wireless)
            else
                connectType = getString(R.string.device_detail_wired)
        }

        modelName = SpecialCharacterHandler.checkEmptyTextValue(if(isGatewayMode) deviceInfo.getName() else endDeviceInfo.getName())
        status = SpecialCharacterHandler.checkEmptyTextValue(getString(if(isConnect) R.string.device_detail_connecting else R.string.device_detail_disconnect))
        //connectType = SpecialCharacterHandler.checkEmptyTextValue(if(isGatewayMode) (if(isConnect) getString(R.string.device_detail_wired) else "") else endDeviceInfo.X_ZYXEL_ConnectionType)
        ip = SpecialCharacterHandler.checkEmptyTextValue(if(isGatewayMode) "" else endDeviceInfo.IPAddress)
        wanIP = SpecialCharacterHandler.checkEmptyTextValue(if(isGatewayMode) deviceWanInfo.Object.IPAddress else "")
        //dnsIP = SpecialCharacterHandler.checkEmptyTextValue(if(isGatewayMode) deviceWanInfo.Object.DNSServer else "")
        dnsIP = SpecialCharacterHandler.checkEmptyTextValue(deviceWanInfo.Object.DNSServer)
        lanIP = SpecialCharacterHandler.checkEmptyTextValue(if(isGatewayMode) deviceInfo.IP else "")
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
        zyxel_end_device_detail_title_text.text = String.format("%s %s", getString(R.string.home_device_gateway), getString(R.string.device_detail_detail))
        setContentLinearListVisibility(true)
        zyxel_end_device_detail_ip_linear.visibility = View.GONE
        zyxel_end_device_detail_reboot_button.visibility = View.VISIBLE
        zyxel_end_device_detail_speed_test_linear.visibility = View.GONE
        zyxel_end_device_detail_speed_test_button.visibility = if(AppConfig.SpeedTestActive) View.VISIBLE else View.INVISIBLE
    }

    private fun setEndDeviceModeUI()
    {
        zyxel_end_device_detail_title_text.text = String.format("%s %s",
                with(endDeviceInfo.X_ZYXEL_HostType)
                {
                    when
                    {
                        equals("Router", ignoreCase = true) -> getString(R.string.home_device_gateway)
                        equals("AccessPoint", ignoreCase = true) || equals("Access Point", ignoreCase = true) || equals("AP", ignoreCase = true) -> getString(R.string.home_device_ap)
                        equals("Repeater", ignoreCase = true) || equals("RP", ignoreCase = true) -> getString(R.string.home_device_rp)
                        else -> endDeviceInfo.X_ZYXEL_HostType
                    }
                },
                getString(R.string.device_detail_detail)
        )
        setContentLinearListVisibility(true)
        zyxel_end_device_detail_wan_ip_linear.visibility = View.GONE
        zyxel_end_device_detail_lan_ip_linear.visibility = View.GONE
        zyxel_end_device_detail_reboot_button.visibility = if(isConnect) View.VISIBLE else View.INVISIBLE
        zyxel_end_device_detail_speed_test_linear.visibility = View.GONE
        zyxel_end_device_detail_speed_test_button.visibility = View.INVISIBLE
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
                zyxel_end_device_detail_content_area_relative.alpha = 0.3.toFloat()
                zyxel_end_device_detail_reboot_button.isEnabled = false
                zyxel_end_device_detail_lan_ip_text.isEnabled = false
            }

            false ->
            {
                zyxel_end_device_detail_model_name_relative.visibility = View.VISIBLE
                zyxel_end_device_detail_model_name_edit_relative.visibility = View.GONE
                zyxel_end_device_detail_content_area_relative.alpha = 1.toFloat()
                zyxel_end_device_detail_reboot_button.isEnabled = true
                zyxel_end_device_detail_lan_ip_text.isEnabled = true
            }
        }
    }

    private fun checkInputEditUI()
    {
        when(userIllegalInput)
        {
            true ->
            {
                with(zyxel_end_device_detail_model_name_edit_error_text)
                {
                    text = getString(R.string.login_no_support_character)
                    visibility = View.VISIBLE
                }

                zyxel_end_device_detail_edit_line_image.setImageResource(R.color.color_ff2837)
            }

            false ->
            {
                zyxel_end_device_detail_model_name_edit_error_text.visibility = View.INVISIBLE
                zyxel_end_device_detail_edit_line_image.setImageResource(R.color.color_ffc800)
            }
        }

        when
        {
            zyxel_end_device_detail_model_name_edit.text.length >= AppConfig.deviceUserNameRequiredLength
            && !userIllegalInput
            ->
                with(zyxel_end_device_detail_confirm_image)
                {
                    isEnabled = true
                    alpha = 1.toFloat()
                }

            else ->
                with(zyxel_end_device_detail_confirm_image)
                {
                    isEnabled = false
                    alpha = 0.3.toFloat()
                }
        }
    }

    private fun initEndDeviceDetailModelNameEdit()
    {
        zyxel_end_device_detail_model_name_edit.textChangedListener{
            onTextChanged{
                str: CharSequence?, _: Int, _: Int, _: Int ->
                userIllegalInput = SpecialCharacterHandler.containsEmoji(str.toString())
                                || SpecialCharacterHandler.containsSpecialCharacter(str.toString())
                                || SpecialCharacterHandler.containsExcludeASCII(str.toString())
                checkInputEditUI()
            }
        }
    }

    private fun setDeviceNameTask()
    {
        LogUtil.d(TAG,"setDeviceNameTask()")
        inputMethodManager.hideSoftInputFromWindow(zyxel_end_device_detail_model_name_edit.applicationWindowToken, 0)
        editDeviceName = zyxel_end_device_detail_model_name_edit.text.toString()
        isEditMode = false

        if(isGatewayMode)
            setGatewayInfoTask()
        else
            setDeviceInfoTask()
    }

    private fun setGatewayInfoTask()
    {
        LogUtil.d(TAG,"setGatewayInfoTask()")
        GlobalBus.publish(MainEvent.ShowLoading())

        val params = JSONObject()
        params.put("HostName", editDeviceName)
        LogUtil.d(TAG,"setGatewayInfoTask param:$params")

        GatewayApi.SetSystemInfo()
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
                            GlobalBus.publish(MainEvent.StartGetDeviceInfoOnceTask())
                        }
                        catch(e: JSONException)
                        {
                            e.printStackTrace()
                            GlobalBus.publish(MainEvent.HideLoading())
                        }
                    }
                }).execute()
    }

    private fun setDeviceInfoTask()
    {
        LogUtil.d(TAG,"setDeviceInfoTask()")
        GlobalBus.publish(MainEvent.ShowLoading())

        val params = JSONObject()
        params.put("HostName", editDeviceName)
        params.put("MacAddress", endDeviceInfo.PhysAddress)
        params.put("Internet_Blocking_Enable", endDeviceInfo.Internet_Blocking_Enable)
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
                                GlobalBus.publish(MainEvent.StartGetDeviceInfoOnceTask())
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
                                GlobalBus.publish(MainEvent.StartGetDeviceInfoOnceTask())
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

    private fun rebootTask()
    {
        val params = JSONObject()

        if(isGatewayMode)
        {
            GatewayApi.GatewayReboot()
                    .setRequestPageName(TAG)
                    .setParams(params)
                    .setResponseListener(object: Commander.ResponseListener()
                    {
                        override fun onSuccess(responseStr: String)
                        {

                        }
                    }).execute()
        }
        else
        {
            params.put("L2DevCtrl_Reboot", true)
            LogUtil.d(TAG,"rebootTask param:$params")
            LogUtil.d(TAG,"rebootTask index:${endDeviceInfo.IndexFromFW}")

            DevicesApi.EndDeviceReboot(endDeviceInfo.IndexFromFW)
                    .setRequestPageName(TAG)
                    .setParams(params)
                    .setResponseListener(object: Commander.ResponseListener()
                    {
                        override fun onSuccess(responseStr: String)
                        {
                            val data = JSONObject(responseStr)
                            val sessionkey = data.get("sessionkey").toString()
                            GlobalData.loginInfo.sessionkey = sessionkey
                            GlobalBus.publish(MainEvent.EnterHomePage())
                        }
                    }).execute()

            /*var index = 0
            for(i in GlobalData.endDeviceList.indices)
            {
                if(GlobalData.endDeviceList[i].PhysAddress == endDeviceInfo.PhysAddress)
                {
                    index = i + 1

                    params.put("L2DevCtrl_Reboot", true)
                    LogUtil.d(TAG,"rebootTask param:$params")

                    GatewayApi.EndDeviceReboot(index)
                            .setRequestPageName(TAG)
                            .setParams(params)
                            .setResponseListener(object: Commander.ResponseListener()
                            {
                                override fun onSuccess(responseStr: String)
                                {

                                }
                            }).execute()

                    break
                }
            }*/
        }
    }

    private fun startSpeedTestTask()
    {
        val params = JSONObject()
        params.put("Start", true)
        LogUtil.d(TAG,"startSpeedTestTask param:$params")

        GatewayApi.StartSpeedTest()
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
                            GlobalBus.publish(MainEvent.StartGetSpeedTestStatusTask())
                        }
                        catch(e: JSONException)
                        {
                            e.printStackTrace()
                        }
                    }
                }).execute()
    }
}