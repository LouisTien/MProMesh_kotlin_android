package zyxel.com.multyproneo

import android.Manifest
import android.app.Dialog
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentTransaction
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.ProgressBar
import com.google.gson.Gson
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.activity_main.*
import org.jetbrains.anko.toast
import org.json.JSONException
import org.json.JSONObject
import zyxel.com.multyproneo.api.*
import zyxel.com.multyproneo.dialog.MessageDialog
import zyxel.com.multyproneo.event.*
import zyxel.com.multyproneo.fragment.*
import zyxel.com.multyproneo.model.*
import zyxel.com.multyproneo.tool.CryptTool
import zyxel.com.multyproneo.util.*
import zyxel.com.multyproneo.wifichart.WiFiChannelChartListener
import java.util.*
import kotlin.concurrent.schedule

class MainActivity : AppCompatActivity(), WiFiChannelChartListener
{
    private val TAG = javaClass.simpleName
    private lateinit var switchFrgDisposable: Disposable
    private lateinit var showLoadingOnlyGrayBGDisposable: Disposable
    private lateinit var showLoadingDisposable: Disposable
    private lateinit var hideLoadingDisposable: Disposable
    private lateinit var showBottomToolbarDisposable: Disposable
    private lateinit var hideBottomToolbarDisposable: Disposable
    private lateinit var setHomeIconFocusDisposable: Disposable
    private lateinit var startGetDeviceInfoTaskDisposable: Disposable
    private lateinit var startGetDeviceInfoTaskOnceDisposable: Disposable
    private lateinit var stopGetDeviceInfoTaskDisposable: Disposable
    private lateinit var startGetWPSStatusTaskDisposable: Disposable
    private lateinit var stopGetWPSStatusTaskDisposable: Disposable
    private lateinit var startGetSpeedTestStatusTaskDisposable: Disposable
    private lateinit var stopGetSpeedTestStatusTaskDisposable: Disposable
    private lateinit var enterHomePageDisposable: Disposable
    private lateinit var enterDevicesPageDisposable: Disposable
    private lateinit var enterWiFiSettingsPageDisposable: Disposable
    private lateinit var enterDiagnosticPageDisposable: Disposable
    private lateinit var enterAccountPageDisposable: Disposable
    private lateinit var enterSearchGatewayPageDisposable: Disposable
    private lateinit var msgDialogResponseDisposable: Disposable
    private lateinit var showMsgDialogDisposable: Disposable
    private lateinit var showToastDisposable: Disposable
    private lateinit var loadingDlg: Dialog
    private lateinit var devicesInfo: DevicesInfo
    private lateinit var changeIconNameInfo: ChangeIconNameInfo
    private lateinit var wanInfo: WanInfo
    private lateinit var guestWiFiInfo: GuestWiFiInfo
    private lateinit var fSecureInfo: FSecureInfo
    private lateinit var hostNameReplaceInfo: HostNameReplaceInfo
    private lateinit var progressBar: ProgressBar
    private lateinit var getSpeedTestStatusTimer: CountDownTimer
    private var deviceTimer = Timer()
    private var screenTimer = Timer()
    private var getWPSStatusTimer = Timer()

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        loadingDlg = createLoadingDlg(this)
        OUIUtil.executeGetMacOUITask(this)
        randomAESInfo()
        initSpeedTestTimer()
        setClickListener()
        listenEvent()
        switchToFragContainer(FindingDeviceFragment())
    }

    override fun onResume()
    {
        super.onResume()
        regularKeepScreen()
    }

    override fun onDestroy()
    {
        super.onDestroy()
        disposeEvent()
    }

    override fun dispatchTouchEvent(event: MotionEvent): Boolean
    {
        if(event.action == MotionEvent.ACTION_DOWN)
            regularKeepScreen()
        return super.dispatchTouchEvent(event)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray)
    {
        when(requestCode)
        {
            AppConfig.PERMISSION_LOCATION_REQUESTCODE ->
            {
                if((grantResults.isNotEmpty()) && (grantResults[0] == PackageManager.PERMISSION_GRANTED))
                {
                    LogUtil.d(TAG, "Location permission granted!")
                    gotoDiagnosticFragment()
                }
                else
                    LogUtil.d(TAG, "Location permission denied!")
            }
        }
    }

    override fun onDrawCompleted()
    {

    }

    private fun randomAESInfo()
    {
        CryptTool.IvAES = CryptTool.getRandomString(16)
        CryptTool.KeyAES = CryptTool.getRandomString(16)
        LogUtil.d(TAG,"IvAES:${CryptTool.IvAES}")
        LogUtil.d(TAG,"KeyAES:${CryptTool.KeyAES}")
    }

    private fun initSpeedTestTimer()
    {
        getSpeedTestStatusTimer = object : CountDownTimer((AppConfig.SpeedTestTimeout * 1000).toLong(), (AppConfig.SpeedTestStatusUpdateTime * 1000).toLong())
        {
            override fun onTick(millisUntilFinished: Long)
            {
                getSpeedTestStatusInfoTask()
            }

            override fun onFinish()
            {
                stopSpeedTest()
                GlobalBus.publish(GatewayEvent.GetSpeedTestComplete("0", "0"))
            }
        }
    }

    private val clickListener = View.OnClickListener{ view ->
        when(view)
        {
            home_relative -> gotoHomeFragment()

            devices_relative -> gotoDevicesFragment()

            wifi_relative -> gotoWiFiFragment()

            diagnostic_relative ->
            {
                if(hasLocationPermission())
                    gotoDiagnosticFragment()
                else
                {
                    MessageDialog(
                            this,
                            "",
                            getString(R.string.diagnostic_request_location_permission),
                            arrayOf(getString(R.string.message_dialog_ok)),
                            AppConfig.DialogAction.ACT_LOCATION_PERMISSION
                    ).show()
                }
            }

            account_relative -> gotoAccountFragment()
        }
    }

    private fun setClickListener()
    {
        home_relative.setOnClickListener(clickListener)
        devices_relative.setOnClickListener(clickListener)
        parental_relative.setOnClickListener(clickListener)
        wifi_relative.setOnClickListener(clickListener)
        diagnostic_relative.setOnClickListener(clickListener)
        account_relative.setOnClickListener(clickListener)
    }

    private fun disSelectToolBarIcons()
    {
        runOnUiThread{
            home_image.isSelected = false
            devices_image.isSelected = false
            parental_image.isSelected = false
            wifi_image.isSelected = false
            diagnostic_image.isSelected = false
            account_image.isSelected = false

            home_text.isSelected = false
            devices_text.isSelected = false
            parental_text.isSelected = false
            wifi_text.isSelected = false
            diagnostic_text.isSelected = false
            account_text.isSelected = false
        }
    }

    private fun regularKeepScreen()
    {
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        screenTimer.cancel()
        screenTimer = Timer()
        screenTimer.schedule((AppConfig.keepScreenTime * 1000).toLong()){
            runOnUiThread{
                window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                screenTimer.cancel()
            }
        }
    }

    private fun createLoadingDlg(context: Context): Dialog
    {
        /*val builder = AlertDialog.Builder(context, R.style.loadingStyle)
        builder.setCancelable(false)
        builder.setView(layoutInflater.inflate(R.layout.dialog_loading, null))
        return builder.create()*/

        val builder = AlertDialog.Builder(context, R.style.loadingStyle)
        val mView = layoutInflater.inflate(R.layout.dialog_loading, null)
        progressBar = mView.findViewById(R.id.loadingProBar) as ProgressBar
        builder.setCancelable(false)
        builder.setView(mView)
        return builder.create()
    }

    private fun hasLocationPermission() =
            ContextCompat.checkSelfPermission(this.applicationContext, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED


    private fun listenEvent()
    {
        switchFrgDisposable = GlobalBus.listen(MainEvent.SwitchToFrag::class.java).subscribe{ switchToFragContainer(it.frag) }

        showLoadingOnlyGrayBGDisposable = GlobalBus.listen(MainEvent.ShowLoadingOnlyGrayBG::class.java).subscribe{ ShowLoadingOnlyGrayBG() }

        showLoadingDisposable = GlobalBus.listen(MainEvent.ShowLoading::class.java).subscribe{ showLoading() }

        hideLoadingDisposable = GlobalBus.listen(MainEvent.HideLoading::class.java).subscribe{ hideLoading() }

        showBottomToolbarDisposable = GlobalBus.listen(MainEvent.ShowBottomToolbar::class.java).subscribe{ bottom_toolbar.visibility = View.VISIBLE }

        hideBottomToolbarDisposable = GlobalBus.listen(MainEvent.HideBottomToolbar::class.java).subscribe{ bottom_toolbar.visibility = View.GONE }

        setHomeIconFocusDisposable = GlobalBus.listen(MainEvent.SetHomeIconFocus::class.java).subscribe{
            disSelectToolBarIcons()

            runOnUiThread{
                home_image.isSelected = true
                home_text.isSelected = true
            }
        }

        startGetDeviceInfoTaskDisposable = GlobalBus.listen(MainEvent.StartGetDeviceInfoTask::class.java).subscribe{
            deviceTimer = Timer()
            deviceTimer.schedule(0, (AppConfig.endDeviceListUpdateTime * 1000).toLong()){ startGetAllNeedDeviceInfoTask() }
        }

        startGetDeviceInfoTaskOnceDisposable = GlobalBus.listen(MainEvent.StartGetDeviceInfoOnceTask::class.java).subscribe{ startGetAllNeedDeviceInfoTask() }

        stopGetDeviceInfoTaskDisposable = GlobalBus.listen(MainEvent.StopGetDeviceInfoTask::class.java).subscribe{ deviceTimer.cancel() }

        enterHomePageDisposable = GlobalBus.listen(MainEvent.EnterHomePage::class.java).subscribe{ gotoHomeFragment() }

        enterDevicesPageDisposable = GlobalBus.listen(MainEvent.EnterDevicesPage::class.java).subscribe{ gotoDevicesFragment() }

        enterWiFiSettingsPageDisposable = GlobalBus.listen(MainEvent.EnterWiFiSettingsPage::class.java).subscribe{ gotoWiFiFragment() }

        enterDiagnosticPageDisposable = GlobalBus.listen(MainEvent.EnterDiagnosticPage::class.java).subscribe{ gotoDiagnosticFragment() }

        enterAccountPageDisposable = GlobalBus.listen(MainEvent.EnterAccountPage::class.java).subscribe{ gotoAccountFragment() }

        enterSearchGatewayPageDisposable = GlobalBus.listen(MainEvent.EnterSearchGatewayPage::class.java).subscribe{ gotoSearchGatewayFragment() }

        startGetWPSStatusTaskDisposable = GlobalBus.listen(MainEvent.StartGetWPSStatusTask::class.java).subscribe{
            getWPSStatusTimer = Timer()
            getWPSStatusTimer.schedule(0, (AppConfig.WPSStatusUpdateTime * 1000).toLong()){ getWPSStatusInfoTask() }
        }

        stopGetWPSStatusTaskDisposable = GlobalBus.listen(MainEvent.StopGetWPSStatusTask::class.java).subscribe{ getWPSStatusTimer.cancel() }

        startGetSpeedTestStatusTaskDisposable = GlobalBus.listen(MainEvent.StartGetSpeedTestStatusTask::class.java).subscribe{ startSpeedTest() }

        stopGetSpeedTestStatusTaskDisposable = GlobalBus.listen(MainEvent.StopGetSpeedTestStatusTask::class.java).subscribe{ stopSpeedTest() }

        msgDialogResponseDisposable = GlobalBus.listen(DialogEvent.OnPositiveBtn::class.java).subscribe{
            when(it.action)
            {
                AppConfig.DialogAction.ACT_LOCATION_PERMISSION ->
                {
                    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                        requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), AppConfig.PERMISSION_LOCATION_REQUESTCODE)
                }

                AppConfig.DialogAction.ACT_RESEARCH -> gotoSearchGatewayFragment()
            }
        }

        showMsgDialogDisposable = GlobalBus.listen(MainEvent.ShowMsgDialog::class.java).subscribe{ showMsgDialog(it.msg, it.requestCtxName) }

        showToastDisposable = GlobalBus.listen(MainEvent.ShowToast::class.java).subscribe{ showToast(it.msg, it.requestCtxName) }
    }

    private fun disposeEvent()
    {
        if(!switchFrgDisposable.isDisposed) switchFrgDisposable.dispose()
        if(!showLoadingOnlyGrayBGDisposable.isDisposed) showLoadingOnlyGrayBGDisposable.dispose()
        if(!showLoadingDisposable.isDisposed) showLoadingDisposable.dispose()
        if(!hideLoadingDisposable.isDisposed) hideLoadingDisposable.dispose()
        if(!showBottomToolbarDisposable.isDisposed) showBottomToolbarDisposable.dispose()
        if(!hideBottomToolbarDisposable.isDisposed) hideBottomToolbarDisposable.dispose()
        if(!setHomeIconFocusDisposable.isDisposed) setHomeIconFocusDisposable.dispose()
        if(!startGetDeviceInfoTaskDisposable.isDisposed) startGetDeviceInfoTaskDisposable.dispose()
        if(!startGetDeviceInfoTaskOnceDisposable.isDisposed) startGetDeviceInfoTaskOnceDisposable.dispose()
        if(!stopGetDeviceInfoTaskDisposable.isDisposed) stopGetDeviceInfoTaskDisposable.dispose()
        if(!startGetWPSStatusTaskDisposable.isDisposed) startGetWPSStatusTaskDisposable.dispose()
        if(!stopGetWPSStatusTaskDisposable.isDisposed) stopGetWPSStatusTaskDisposable.dispose()
        if(!startGetSpeedTestStatusTaskDisposable.isDisposed) startGetSpeedTestStatusTaskDisposable.dispose()
        if(!stopGetSpeedTestStatusTaskDisposable.isDisposed) stopGetSpeedTestStatusTaskDisposable.dispose()
        if(!enterHomePageDisposable.isDisposed) enterHomePageDisposable.dispose()
        if(!enterDevicesPageDisposable.isDisposed) enterDevicesPageDisposable.dispose()
        if(!enterWiFiSettingsPageDisposable.isDisposed) enterWiFiSettingsPageDisposable.dispose()
        if(!enterDiagnosticPageDisposable.isDisposed) enterDiagnosticPageDisposable.dispose()
        if(!enterAccountPageDisposable.isDisposed) enterAccountPageDisposable.dispose()
        if(!enterSearchGatewayPageDisposable.isDisposed) enterSearchGatewayPageDisposable.dispose()
        if(!msgDialogResponseDisposable.isDisposed) msgDialogResponseDisposable.dispose()
        if(!showMsgDialogDisposable.isDisposed) showMsgDialogDisposable.dispose()
        if(!showToastDisposable.isDisposed) showToastDisposable.dispose()
    }

    private fun switchToFragContainer(fragment: Fragment)
    {
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.fragment_container, fragment)
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
        transaction.commitAllowingStateLoss()
        GlobalData.currentFrag = fragment.javaClass.simpleName
        LogUtil.d(TAG, "currentFrag:${GlobalData.currentFrag}")
    }

    private fun gotoHomeFragment()
    {
        disSelectToolBarIcons()

        runOnUiThread{
            home_image.isSelected = true
            home_text.isSelected = true
        }

        if(GlobalData.currentFrag != "HomeFragment")
            switchToFragContainer(HomeFragment())
    }

    private fun gotoDevicesFragment()
    {
        disSelectToolBarIcons()

        runOnUiThread{
            devices_image.isSelected = true
            devices_text.isSelected = true
        }

        if(GlobalData.currentFrag != "DevicesFragment")
            switchToFragContainer(DevicesFragment())
    }

    private fun gotoWiFiFragment()
    {
        disSelectToolBarIcons()

        runOnUiThread{
            wifi_image.isSelected = true
            wifi_text.isSelected = true
        }

        if(GlobalData.currentFrag != "WiFiSettingsFragment")
            switchToFragContainer(WiFiSettingsFragment())
    }

    private fun gotoDiagnosticFragment()
    {
        disSelectToolBarIcons()

        runOnUiThread{
            diagnostic_image.isSelected = true
            diagnostic_text.isSelected = true
        }

        if(GlobalData.currentFrag != "DiagnosticFragment")
            switchToFragContainer(DiagnosticFragment())
    }

    private fun gotoAccountFragment()
    {
        disSelectToolBarIcons()

        runOnUiThread{
            account_image.isSelected = true
            account_text.isSelected = true
        }

        if(GlobalData.currentFrag != "AccountFragment")
            switchToFragContainer(AccountFragment())
    }

    private fun gotoSearchGatewayFragment()
    {
        switchToFragContainer(FindingDeviceFragment())
    }

    private fun ShowLoadingOnlyGrayBG()
    {
        runOnUiThread{
            progressBar.visibility = View.INVISIBLE
            if(!loadingDlg.isShowing) loadingDlg.show()
        }
    }

    private fun showLoading()
    {
        runOnUiThread{
            progressBar.visibility = View.VISIBLE
            if(!loadingDlg.isShowing) loadingDlg.show()
        }
    }

    private fun hideLoading()
    {
        runOnUiThread{ if(loadingDlg.isShowing) loadingDlg.dismiss() }
    }

    private fun showMsgDialog(msg: String, requestCtxName: String)
    {
        runOnUiThread{
            MessageDialog(
                    this,
                    "",
                    msg,
                    arrayOf(getString(R.string.message_dialog_ok)),
                    AppConfig.DialogAction.ACT_RESEARCH
            ).show()
        }
    }

    private fun showToast(msg: String, requestCtxName: String)
    {
        runOnUiThread{ toast(msg) }
    }

    private fun startSpeedTest()
    {
        showLoading()
        getSpeedTestStatusTimer.start()
    }

    private fun stopSpeedTest()
    {
        hideLoading()
        getSpeedTestStatusTimer.cancel()
    }

    private fun startGetAllNeedDeviceInfoTask()
    {
        if(GlobalData.ZYXELEndDeviceList.isEmpty())
            GlobalBus.publish(MainEvent.ShowLoading())

        getSystemInfoTask()
    }

    private fun stopGetAllNeedDeviceInfoTask()
    {
        GlobalBus.publish(MainEvent.HideLoading())
        GlobalBus.publish(HomeEvent.GetDeviceInfoComplete())
        GlobalBus.publish(DevicesEvent.GetDeviceInfoComplete())
        GlobalBus.publish(DevicesDetailEvent.GetDeviceInfoComplete())
    }

    private fun getSystemInfoTask()
    {
        LogUtil.d(TAG,"getSystemInfoTask()")
        GatewayApi.GetSystemInfo()
                .setRequestPageName(TAG)
                .setResponseListener(object: Commander.ResponseListener()
                {
                    override fun onSuccess(responseStr: String)
                    {
                        try
                        {
                            val data = JSONObject(responseStr)
                            var name = data.getJSONObject("Object").getString("HostName")
                            LogUtil.d(TAG,"HostName:$name")
                            GlobalData.getCurrentGatewayInfo().UserDefineName = name
                            getChangeIconNameInfoTask()
                        }
                        catch(e: JSONException)
                        {
                            e.printStackTrace()

                            GlobalBus.publish(MainEvent.HideLoading())
                        }
                    }
                }).execute()
    }

    private fun getChangeIconNameInfoTask()
    {
        LogUtil.d(TAG,"getChangeIconNameInfoTask()")

        DevicesApi.GetChangeIconNameInfo()
                .setRequestPageName(TAG)
                .setResponseListener(object: Commander.ResponseListener()
                {
                    override fun onSuccess(responseStr: String)
                    {
                        try
                        {
                            changeIconNameInfo = Gson().fromJson(responseStr, ChangeIconNameInfo::class.javaObjectType)
                            LogUtil.d(TAG,"changeIconNameInfo:${changeIconNameInfo.toString()}")
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
        DevicesApi.GetDevicesInfo()
                .setRequestPageName(TAG)
                .setResponseListener(object: Commander.ResponseListener()
                {
                    override fun onSuccess(responseStr: String)
                    {
                        try
                        {
                            devicesInfo = Gson().fromJson(responseStr, DevicesInfo::class.javaObjectType)
                            LogUtil.d(TAG,"devicesInfo:${devicesInfo.toString()}")

                            val newEndDeviceList = mutableListOf<DevicesInfoObject>()
                            val newHomeEndDeviceList = mutableListOf<DevicesInfoObject>()
                            val newZYXELEndDeviceList = mutableListOf<DevicesInfoObject>()
                            val newGuestEndDeviceList = mutableListOf<DevicesInfoObject>()

                            newZYXELEndDeviceList.add(
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
                            )

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

                            getWanInfoTask()
                        }
                        catch(e: JSONException)
                        {
                            e.printStackTrace()

                            GlobalBus.publish(MainEvent.HideLoading())
                        }
                    }
                }).execute()
    }

    private fun getWanInfoTask()
    {
        LogUtil.d(TAG,"getWanInfoTask()")
        GatewayApi.GetWanInfo()
                .setRequestPageName(TAG)
                .setResponseListener(object: Commander.ResponseListener()
                {
                    override fun onSuccess(responseStr: String)
                    {
                        try
                        {
                            wanInfo = Gson().fromJson(responseStr, WanInfo::class.javaObjectType)
                            LogUtil.d(TAG,"wanInfo:${wanInfo.toString()}")
                            GlobalData.gatewayWanInfo = wanInfo.copy()
                            getGuestWiFiEnableTask()
                        }
                        catch(e: JSONException)
                        {
                            e.printStackTrace()

                            GlobalBus.publish(MainEvent.HideLoading())
                        }
                    }
                }).execute()
    }

    private fun getGuestWiFiEnableTask()
    {
        LogUtil.d(TAG,"getGuestWiFiEnableTask()")
        WiFiSettingApi.GetGuestWiFi24GInfo()
                .setRequestPageName(TAG)
                .setResponseListener(object: Commander.ResponseListener()
                {
                    override fun onSuccess(responseStr: String)
                    {
                        try
                        {
                            guestWiFiInfo = Gson().fromJson(responseStr, GuestWiFiInfo::class.javaObjectType)
                            LogUtil.d(TAG,"guestWiFiInfo:${guestWiFiInfo.toString()}")
                            GlobalData.guestWiFiStatus = guestWiFiInfo.Object.Enable
                            getFSecureInfoTask()
                        }
                        catch(e: JSONException)
                        {
                            e.printStackTrace()

                            GlobalBus.publish(MainEvent.HideLoading())
                        }
                    }
                }).execute()
    }

    private fun getFSecureInfoTask()
    {
        LogUtil.d(TAG,"getFSecureInfoTask()")
        GatewayApi.GetFSecureInfo()
                .setRequestPageName(TAG)
                .setResponseListener(object: Commander.ResponseListener()
                {
                    override fun onSuccess(responseStr: String)
                    {
                        try
                        {
                            fSecureInfo = Gson().fromJson(responseStr, FSecureInfo::class.javaObjectType)
                            LogUtil.d(TAG,"fSecureInfo:${fSecureInfo.toString()}")
                            FeatureConfig.FSecureStatus = fSecureInfo.Object.Cyber_Security_FSC
                            getHostNameReplaceInfoTask()
                        }
                        catch(e: JSONException)
                        {
                            e.printStackTrace()

                            GlobalBus.publish(MainEvent.HideLoading())
                        }
                    }
                }).execute()
    }

    private fun getHostNameReplaceInfoTask()
    {
        LogUtil.d(TAG,"getHostNameReplaceInfoTask()")
        GatewayApi.GetHostNameReplaceInfo()
                .setRequestPageName(TAG)
                .setResponseListener(object: Commander.ResponseListener()
                {
                    override fun onSuccess(responseStr: String)
                    {
                        try
                        {
                            hostNameReplaceInfo = Gson().fromJson(responseStr, HostNameReplaceInfo::class.javaObjectType)
                            LogUtil.d(TAG,"hostNameReplaceInfo:${hostNameReplaceInfo.toString()}")
                            FeatureConfig.hostNameReplaceStatus = hostNameReplaceInfo.Object.Enable
                            stopGetAllNeedDeviceInfoTask()
                        }
                        catch(e: JSONException)
                        {
                            e.printStackTrace()

                            GlobalBus.publish(MainEvent.HideLoading())
                        }
                    }
                }).execute()
    }

    private fun getWPSStatusInfoTask()
    {
        LogUtil.d(TAG,"getWPSStatusInfoTask()")
        AddMeshApi.GetWPSStatus()
                .setRequestPageName(TAG)
                .setResponseListener(object: Commander.ResponseListener()
                {
                    override fun onSuccess(responseStr: String)
                    {
                        try
                        {
                            val data = JSONObject(responseStr)
                            var status = data.getJSONObject("Object").getString("X_ZYXEL_WPSRunningStatus")
                            LogUtil.d(TAG,"WPS status:$status")

                            with(status)
                            {
                                when
                                {
                                    contains("OK", ignoreCase = true) ->
                                    {
                                        getWPSStatusTimer.cancel()
                                        GlobalBus.publish(LoadingTransitionEvent.WPSStatusUpdate(true))
                                    }

                                    contains("Requested", ignoreCase = true) -> {}

                                    else ->
                                    {
                                        getWPSStatusTimer.cancel()
                                        GlobalBus.publish(LoadingTransitionEvent.WPSStatusUpdate(false))
                                    }
                                }
                            }
                        }
                        catch(e: JSONException)
                        {
                            e.printStackTrace()
                            getWPSStatusTimer.cancel()
                        }
                    }
                }).execute()
    }

    private fun getSpeedTestStatusInfoTask()
    {
        LogUtil.d(TAG,"getSpeedTestStatusInfoTask()")
        GatewayApi.GetSpeedTestStatus()
                .setRequestPageName(TAG)
                .setResponseListener(object: Commander.ResponseListener()
                {
                    override fun onSuccess(responseStr: String)
                    {
                        try
                        {
                            val data = JSONObject(responseStr)
                            var uploadResult = data.getJSONObject("Object").getString("UploadSpeedResult")
                            var downloadResult = data.getJSONObject("Object").getString("DownloadSpeedResult")
                            var status = data.getJSONObject("Object").getString("Status")
                            LogUtil.d(TAG,"SpeedTest uploadResult:$uploadResult")
                            LogUtil.d(TAG,"SpeedTest downloadResult:$downloadResult")
                            LogUtil.d(TAG,"SpeedTest status:$status")

                            /*var start = data.getJSONObject("Object").getString("Start")
                            var serverIP = data.getJSONObject("Object").getString("ServerIP")
                            var CPE_WANname = data.getJSONObject("Object").getString("CPE_WANname")
                            LogUtil.d(TAG,"SpeedTest start:$start")
                            LogUtil.d(TAG,"SpeedTest serverIP:$serverIP")
                            LogUtil.d(TAG,"SpeedTest CPE_WANname:$CPE_WANname")

                            showToast("uploadResult:$uploadResult\ndownloadResult:$downloadResult\nstatus:$status\nstart:$start\nserverIP:$serverIP\nCPE_WANname:$CPE_WANname", "")*/

                            with(status)
                            {
                                when
                                {
                                    contains("Completed", ignoreCase = true) ->
                                    {
                                        stopSpeedTest()
                                        GlobalBus.publish(GatewayEvent.GetSpeedTestComplete(uploadResult, downloadResult))
                                    }

                                    contains("Ready", ignoreCase = true) or
                                    contains("Doing", ignoreCase = true) -> {}

                                    else ->
                                    {
                                        stopSpeedTest()
                                        GlobalBus.publish(GatewayEvent.GetSpeedTestComplete("- Mbps", "- Mbps"))
                                    }
                                }
                            }
                        }
                        catch(e: JSONException)
                        {
                            e.printStackTrace()
                            stopSpeedTest()
                            GlobalBus.publish(GatewayEvent.GetSpeedTestComplete("- Mbps", "- Mbps"))
                        }
                    }
                }).execute()
    }
}