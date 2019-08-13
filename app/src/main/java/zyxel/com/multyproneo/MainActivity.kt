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
import zyxel.com.multyproneo.dialog.MessageDialog
import zyxel.com.multyproneo.event.*
import zyxel.com.multyproneo.fragment.*
import zyxel.com.multyproneo.model.DevicesInfo
import zyxel.com.multyproneo.model.DevicesInfoObject
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
            deviceTimer.schedule(0, (AppConfig.endDeviceListUpdateTime * 1000).toLong()){ getDeviceInfoTask() }
        }

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
        //runOnUiThread{ Runnable{ if(!loadingDlg.isShowing) loadingDlg.show() }.run() }
        runOnUiThread{ if(!loadingDlg.isShowing) loadingDlg.show() }
    }

    private fun hideLoading()
    {
        //runOnUiThread{ Runnable{ if(loadingDlg.isShowing) loadingDlg.dismiss() }.run() }
        runOnUiThread{ if(loadingDlg.isShowing) loadingDlg.dismiss() }
    }

    private fun showToast(msg: String, requestCtxName: String)
    {
        runOnUiThread{ toast(msg) }
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
                                            HostName = GlobalData.getCurrentGatewayInfo().ModelName,
                                            IPAddress = GlobalData.getCurrentGatewayInfo().IP,
                                            X_ZYXEL_CapabilityType = "L2Device",
                                            X_ZYXEL_ConnectionType = "WiFi",
                                            X_ZYXEL_HostType = GlobalData.getCurrentGatewayInfo().DeviceMode,
                                            X_ZYXEL_SoftwareVersion = GlobalData.getCurrentGatewayInfo().SoftwareVersion
                                    )
                            )

                            for(item in devicesInfo.Object)
                            {
                                if(item.X_ZYXEL_CapabilityType == "L2Device")
                                    newZYXELEndDeviceList.add(item)
                                else
                                {
                                    if(item.X_ZYXEL_Conn_Guest == 1)
                                        newGuestEndDeviceList.add(item)
                                    else
                                        newHomeEndDeviceList.add(item)
                                }
                            }

                            GlobalData.endDeviceList = devicesInfo.Object.toMutableList()
                            GlobalData.homeEndDeviceList = newHomeEndDeviceList.toMutableList()
                            GlobalData.ZYXELEndDeviceList = newZYXELEndDeviceList.toMutableList()
                            GlobalData.guestEndDeviceList = newGuestEndDeviceList.toMutableList()
                        }
                        catch(e: JSONException)
                        {
                            e.printStackTrace()
                        }
                    }
                }).execute()
    }

    /*private fun getDeviceInfoTask()
    {
        LogUtil.d(TAG,"getDeviceInfoTask()")
        doAsync{
            val newClientList = mutableListOf<EndDeviceProfile>(
                    EndDeviceProfile(
                            UserDefineName = "Access Point",
                            Name = "WAP6804-AP-40619",
                            MAC = "5c:6a:80:e1:33:27",
                            Active = "Connect",
                            Blocking = "Non-Blocking",
                            IPAddress = "192.168.1.151",
                            ConnectionType = "Other",
                            CapabilityType = "L2Device",
                            SoftwareVersion = "1.00(ABKH.6)C0",
                            HostType = 3,
                            L2AutoConfigEnable = 3,
                            L2WifiStatus = 1,
                            Neighbor = "unknown",
                            Manufacturer = "unknown",
                            Channel24G = 11,
                            Channel5G = 157,
                            DeviceMode = "AP",
                            NewDeviceFlag = 1,
                            InternetAccess = "Non-Blocking",
                            RssiRealFlag = 1,
                            RssiValue = "Good",
                            dhcpLeaseTime = "1559608754"
                    ),
                    EndDeviceProfile(
                            UserDefineName = "Louis-LG V20",
                            Name = "android-e5784d2ba14b34c5",
                            MAC = "40:4e:36:b3:fd:15",
                            Active = "Connect",
                            Blocking = "Non-Blocking",
                            IPAddress = "192.168.1.133",
                            ConnectionType = "WiFi",
                            CapabilityType = "Client",
                            HostType = 1,
                            SignalStrength = 5,
                            PhyRate = 846,
                            Neighbor = "gateway",
                            Manufacturer = "unknown",
                            Rssi = -29,
                            Band = 2,
                            Channel5G = 36,
                            InternetAccess = "Non-Blocking",
                            RssiRealFlag = 1,
                            RssiValue = "TooClose",
                            dhcpLeaseTime = "1559608754"
                    ),
                    EndDeviceProfile(
                            UserDefineName = "Louis-LG V21",
                            Name = "android-e5784d2ba14b34c7",
                            MAC = "40:4e:36:b3:fd:17",
                            Active = "Connect",
                            Blocking = "Non-Blocking",
                            IPAddress = "192.168.1.135",
                            ConnectionType = "WiFi",
                            CapabilityType = "Client",
                            HostType = 1,
                            SignalStrength = 5,
                            PhyRate = 846,
                            Neighbor = "gateway",
                            Manufacturer = "unknown",
                            Rssi = -29,
                            Band = 2,
                            Channel5G = 36,
                            InternetAccess = "Non-Blocking",
                            RssiRealFlag = 1,
                            RssiValue = "TooClose",
                            dhcpLeaseTime = "1559608754"
                    ),
                    EndDeviceProfile(
                            UserDefineName = "Zenfone6",
                            Name = "android-e5784d2ba14b34c6",
                            MAC = "40:4e:36:b3:fd:16",
                            Active = "Connect",
                            Blocking = "Non-Blocking",
                            IPAddress = "192.168.1.134",
                            ConnectionType = "WiFi",
                            CapabilityType = "Client",
                            HostType = 1,
                            SignalStrength = 5,
                            PhyRate = 846,
                            Neighbor = "gateway",
                            GuestGroup = 1,
                            Manufacturer = "unknown",
                            Rssi = -29,
                            Band = 2,
                            Channel5G = 36,
                            InternetAccess = "Non-Blocking",
                            RssiRealFlag = 1,
                            RssiValue = "Good",
                            dhcpLeaseTime = "1559608754"
                    )
            )

            val newHomeEndDeviceList = mutableListOf<EndDeviceProfile>()
            val newZYXELEndDeviceList = mutableListOf<EndDeviceProfile>()
            val newGuestEndDeviceList = mutableListOf<EndDeviceProfile>()

            newZYXELEndDeviceList.add(EndDeviceProfile(
                    UserDefineName = GlobalData.getCurrentGatewayInfo().userDefineName,
                    IPAddress = GlobalData.getCurrentGatewayInfo().IP,
                    CapabilityType = "L2Device",
                    DeviceMode = if (GlobalData.getCurrentGatewayInfo().modelName.contains("WAP")) "Access Point" else "Gateway"
            ))

            for(item in newClientList)
            {
                if(item.CapabilityType == "L2Device")
                    newZYXELEndDeviceList.add(item)
                else
                {
                    if(item.GuestGroup == 1)
                        newGuestEndDeviceList.add(item)
                    else
                        newHomeEndDeviceList.add(item)
                }
            }

            val newGatewayWanInfo = WanInfoProfile(
                    WanStatus = "Enable",
                    WanIP = "10.241.16.44",
                    WanMAC = "bc:99:11:1b:7b:df",
                    WanDNS = "172.21.5.1"
            )

            uiThread{
                GlobalData.endDeviceList = newClientList.toMutableList()
                GlobalData.homeEndDeviceList = newHomeEndDeviceList.toMutableList()
                GlobalData.ZYXELEndDeviceList = newZYXELEndDeviceList.toMutableList()
                GlobalData.guestEndDeviceList = newGuestEndDeviceList.toMutableList()
                GlobalData.guestWiFiStatus = true
                GlobalData.gatewayLanIP = "192.168.1.1"
                GlobalData.gatewayWanInfo = newGatewayWanInfo.copy()
                GlobalBus.publish(HomeEvent.GetDeviceInfoComplete())
                GlobalBus.publish(DevicesEvent.GetDeviceInfoComplete())
            }
        }
    }*/
}
