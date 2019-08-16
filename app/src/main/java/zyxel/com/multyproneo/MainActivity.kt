package zyxel.com.multyproneo

import android.Manifest
import android.app.Dialog
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentTransaction
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import com.google.gson.Gson
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.activity_main.*
import org.jetbrains.anko.toast
import org.json.JSONException
import zyxel.com.multyproneo.api.Commander
import zyxel.com.multyproneo.api.DevicesApi
import zyxel.com.multyproneo.api.GatewayApi
import zyxel.com.multyproneo.api.WiFiSettingApi
import zyxel.com.multyproneo.dialog.MessageDialog
import zyxel.com.multyproneo.event.*
import zyxel.com.multyproneo.fragment.*
import zyxel.com.multyproneo.model.*
import zyxel.com.multyproneo.util.AppConfig
import zyxel.com.multyproneo.util.GlobalData
import zyxel.com.multyproneo.util.LogUtil
import zyxel.com.multyproneo.util.OUIUtil
import zyxel.com.multyproneo.wifichart.WiFiChannelChartListener
import java.util.*
import kotlin.concurrent.schedule

class MainActivity : AppCompatActivity(), WiFiChannelChartListener
{
    private val TAG = javaClass.simpleName
    private lateinit var switchFrgDisposable: Disposable
    private lateinit var showLoadingDisposable: Disposable
    private lateinit var hideLoadingDisposable: Disposable
    private lateinit var showBottomToolbarDisposable: Disposable
    private lateinit var hideBottomToolbarDisposable: Disposable
    private lateinit var setHomeIconFocusDisposable: Disposable
    private lateinit var startGetDeviceInfoTaskDisposable: Disposable
    private lateinit var startGetDeviceInfoTaskOnceDisposable: Disposable
    private lateinit var stopGetDeviceInfoTaskDisposable: Disposable
    private lateinit var enterHomePageDisposable: Disposable
    private lateinit var enterDevicesPageDisposable: Disposable
    private lateinit var enterWiFiSettingsPageDisposable: Disposable
    private lateinit var enterDiagnosticPageDisposable: Disposable
    private lateinit var enterAccountPageDisposable: Disposable
    private lateinit var enterSearchGatewayPageDisposable: Disposable
    private lateinit var msgDialogResponseDisposable: Disposable
    private lateinit var showToastDisposable: Disposable
    private lateinit var loadingDlg: Dialog
    private lateinit var devicesInfo: DevicesInfo
    private lateinit var changeIconNameInfo: ChangeIconNameInfo
    private lateinit var wanInfo: WanInfo
    private lateinit var guestWiFiInfo: GuestWiFiInfo
    private lateinit var fSecureInfo: FSecureInfo
    private var deviceTimer = Timer()
    private var screenTimer = Timer()
    private var currentFrag = ""

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        loadingDlg = createLoadingDlg(this)
        OUIUtil.executeGetMacOUITask(this)
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
        if (event.action == MotionEvent.ACTION_DOWN)
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
        val builder = AlertDialog.Builder(context, R.style.loadingStyle)
        builder.setCancelable(false)
        builder.setView(layoutInflater.inflate(R.layout.dialog_loading, null))
        return builder.create()
    }

    private fun hasLocationPermission() =
            ContextCompat.checkSelfPermission(this.applicationContext, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED


    private fun listenEvent()
    {
        switchFrgDisposable = GlobalBus.listen(MainEvent.SwitchToFrag::class.java).subscribe{ switchToFragContainer(it.frag) }

        showLoadingDisposable = GlobalBus.listen(MainEvent.ShowLoading::class.java).subscribe{ showLoading() }

        hideLoadingDisposable = GlobalBus.listen(MainEvent.HideLoading::class.java).subscribe{ hideLoading() }

        showBottomToolbarDisposable = GlobalBus.listen(MainEvent.ShowBottomToolbar::class.java).subscribe{ bottom_toolbar.visibility = View.VISIBLE }

        hideBottomToolbarDisposable = GlobalBus.listen(MainEvent.HideBottomToolbar::class.java).subscribe{ bottom_toolbar.visibility = View.GONE }

        setHomeIconFocusDisposable = GlobalBus.listen(MainEvent.SetHomeIconFocus::class.java).subscribe{
            disSelectToolBarIcons()
            home_image.isSelected = true
            home_text.isSelected = true
        }

        startGetDeviceInfoTaskDisposable = GlobalBus.listen(MainEvent.StartGetDeviceInfoTask::class.java).subscribe{
            deviceTimer = Timer()
            deviceTimer.schedule(0, (AppConfig.endDeviceListUpdateTime * 1000).toLong()){ getChangeIconNameInfoTask() }
        }

        startGetDeviceInfoTaskOnceDisposable = GlobalBus.listen(MainEvent.StartGetDeviceInfoOnceTask::class.java).subscribe{ getChangeIconNameInfoTask() }

        stopGetDeviceInfoTaskDisposable = GlobalBus.listen(MainEvent.StopGetDeviceInfoTask::class.java).subscribe{ deviceTimer.cancel() }

        enterHomePageDisposable = GlobalBus.listen(MainEvent.EnterHomePage::class.java).subscribe{ gotoHomeFragment() }

        enterDevicesPageDisposable = GlobalBus.listen(MainEvent.EnterDevicesPage::class.java).subscribe{ gotoDevicesFragment() }

        enterWiFiSettingsPageDisposable = GlobalBus.listen(MainEvent.EnterWiFiSettingsPage::class.java).subscribe{ gotoWiFiFragment() }

        enterDiagnosticPageDisposable = GlobalBus.listen(MainEvent.EnterDiagnosticPage::class.java).subscribe{ gotoDiagnosticFragment() }

        enterAccountPageDisposable = GlobalBus.listen(MainEvent.EnterAccountPage::class.java).subscribe{ gotoAccountFragment() }

        enterSearchGatewayPageDisposable = GlobalBus.listen(MainEvent.EnterSearchGatewayPage::class.java).subscribe{ gotoSearchGatewayFragment() }

        msgDialogResponseDisposable = GlobalBus.listen(DialogEvent.OnPositiveBtn::class.java).subscribe{
            when(it.action)
            {
                AppConfig.DialogAction.ACT_LOCATION_PERMISSION ->
                {
                    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                        requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), AppConfig.PERMISSION_LOCATION_REQUESTCODE)
                }
            }
        }

        showToastDisposable = GlobalBus.listen(MainEvent.ShowToast::class.java).subscribe{ showToast(it.msg, it.requestCtxName) }
    }

    private fun disposeEvent()
    {
        if(!switchFrgDisposable.isDisposed) switchFrgDisposable.dispose()
        if(!showLoadingDisposable.isDisposed) showLoadingDisposable.dispose()
        if(!hideLoadingDisposable.isDisposed) hideLoadingDisposable.dispose()
        if(!showBottomToolbarDisposable.isDisposed) showBottomToolbarDisposable.dispose()
        if(!hideBottomToolbarDisposable.isDisposed) hideBottomToolbarDisposable.dispose()
        if(!setHomeIconFocusDisposable.isDisposed) setHomeIconFocusDisposable.dispose()
        if(!startGetDeviceInfoTaskDisposable.isDisposed) startGetDeviceInfoTaskDisposable.dispose()
        if(!startGetDeviceInfoTaskOnceDisposable.isDisposed) startGetDeviceInfoTaskOnceDisposable.dispose()
        if(!stopGetDeviceInfoTaskDisposable.isDisposed) stopGetDeviceInfoTaskDisposable.dispose()
        if(!enterHomePageDisposable.isDisposed) enterHomePageDisposable.dispose()
        if(!enterDevicesPageDisposable.isDisposed) enterDevicesPageDisposable.dispose()
        if(!enterWiFiSettingsPageDisposable.isDisposed) enterWiFiSettingsPageDisposable.dispose()
        if(!enterDiagnosticPageDisposable.isDisposed) enterDiagnosticPageDisposable.dispose()
        if(!enterAccountPageDisposable.isDisposed) enterAccountPageDisposable.dispose()
        if(!enterSearchGatewayPageDisposable.isDisposed) enterSearchGatewayPageDisposable.dispose()
        if(!msgDialogResponseDisposable.isDisposed) msgDialogResponseDisposable.dispose()
        if(!showToastDisposable.isDisposed) showToastDisposable.dispose()
    }

    private fun switchToFragContainer(fragment: Fragment)
    {
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.fragment_container, fragment)
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
        transaction.commitAllowingStateLoss()
        currentFrag = fragment.javaClass.simpleName
        LogUtil.d(TAG, "currentFrag:$currentFrag")
    }

    private fun gotoHomeFragment()
    {
        disSelectToolBarIcons()

        home_image.isSelected = true
        home_text.isSelected = true

        if(currentFrag != "HomeFragment")
            switchToFragContainer(HomeFragment())
    }

    private fun gotoDevicesFragment()
    {
        disSelectToolBarIcons()

        devices_image.isSelected = true
        devices_text.isSelected = true

        if(currentFrag != "DevicesFragment")
            switchToFragContainer(DevicesFragment())
    }

    private fun gotoWiFiFragment()
    {
        disSelectToolBarIcons()

        wifi_image.isSelected = true
        wifi_text.isSelected = true

        if(currentFrag != "WiFiSettingsFragment")
            switchToFragContainer(WiFiSettingsFragment())
    }

    private fun gotoDiagnosticFragment()
    {
        disSelectToolBarIcons()

        diagnostic_image.isSelected = true
        diagnostic_text.isSelected = true

        if(currentFrag != "DiagnosticFragment")
            switchToFragContainer(DiagnosticFragment())
    }

    private fun gotoAccountFragment()
    {
        disSelectToolBarIcons()

        account_image.isSelected = true
        account_text.isSelected = true

        if(currentFrag != "AccountFragment")
            switchToFragContainer(AccountFragment())
    }

    private fun gotoSearchGatewayFragment()
    {
        switchToFragContainer(FindingDeviceFragment())
    }

    private fun showLoading()
    {
        runOnUiThread{ if(!loadingDlg.isShowing) loadingDlg.show() }
    }

    private fun hideLoading()
    {
        runOnUiThread{ if(loadingDlg.isShowing) loadingDlg.dismiss() }
    }

    private fun showToast(msg: String, requestCtxName: String)
    {
        runOnUiThread{ toast(msg) }
    }

    private fun getChangeIconNameInfoTask()
    {
        LogUtil.d(TAG,"getChangeIconNameInfoTask()")

        if(GlobalData.ZYXELEndDeviceList.isEmpty())
            GlobalBus.publish(MainEvent.ShowLoading())

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

                            for(item in devicesInfo.Object)
                            {
                                if(item.HostName == "N/A")
                                    continue

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

                                LogUtil.d(TAG,"update devicesInfo:${item.toString()}")
                            }

                            GlobalData.endDeviceList = devicesInfo.Object.toMutableList()
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
                            GlobalData.FSecureStatus = fSecureInfo.Object.Cyber_Security_FSC
                            GlobalBus.publish(MainEvent.HideLoading())
                            GlobalBus.publish(HomeEvent.GetDeviceInfoComplete())
                            GlobalBus.publish(DevicesEvent.GetDeviceInfoComplete())
                            GlobalBus.publish(DevicesDetailEvent.GetDeviceInfoComplete())
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