package zyxel.com.multyproneo

import android.Manifest
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.provider.Settings
import android.util.Base64
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.crashlytics.android.Crashlytics
import com.crashlytics.android.core.CrashlyticsCore
import com.google.gson.Gson
import io.fabric.sdk.android.Fabric
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.activity_main.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.longToast
import org.jetbrains.anko.toast
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import zyxel.com.multyproneo.api.*
import zyxel.com.multyproneo.api.cloud.*
import zyxel.com.multyproneo.dialog.MessageDialog
import zyxel.com.multyproneo.event.*
import zyxel.com.multyproneo.fragment.*
import zyxel.com.multyproneo.fragment.cloud.*
import zyxel.com.multyproneo.model.*
import zyxel.com.multyproneo.model.cloud.TUTKAllDeviceInfo
import zyxel.com.multyproneo.model.cloud.TUTKTokenInfo
import zyxel.com.multyproneo.model.cloud.TUTKUserInfo
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
    private lateinit var showLoadingHintDisposable: Disposable
    private lateinit var hideLoadingDisposable: Disposable
    private lateinit var showBottomToolbarDisposable: Disposable
    private lateinit var showCloudBottomToolbarDisposable: Disposable
    private lateinit var hideBottomToolbarDisposable: Disposable
    private lateinit var setHomeIconFocusDisposable: Disposable
    private lateinit var setCloudHomeIconFocusDisposable: Disposable
    private lateinit var startGetDeviceInfoTaskDisposable: Disposable
    private lateinit var startGetCloudDeviceInfoTaskDisposable: Disposable
    private lateinit var startGetCloudDeviceInfoForDevicePageTaskDisposable: Disposable
    private lateinit var startGetDeviceInfoTaskOnceDisposable: Disposable
    private lateinit var stopGetDeviceInfoTaskDisposable: Disposable
    private lateinit var startGetWPSStatusTaskDisposable: Disposable
    private lateinit var stopGetWPSStatusTaskDisposable: Disposable
    private lateinit var startCloudGetWPSStatusTaskDisposable: Disposable
    private lateinit var stopCloudGetWPSStatusTaskDisposable: Disposable
    private lateinit var startGetSpeedTestStatusTaskDisposable: Disposable
    private lateinit var stopGetSpeedTestStatusTaskDisposable: Disposable
    private lateinit var enterHomePageDisposable: Disposable
    private lateinit var enterDevicesPageDisposable: Disposable
    private lateinit var enterWiFiSettingsPageDisposable: Disposable
    private lateinit var enterDiagnosticPageDisposable: Disposable
    private lateinit var enterAccountPageDisposable: Disposable
    private lateinit var enterCloudHomePageDisposable: Disposable
    private lateinit var enterCloudDevicesPageDisposable: Disposable
    private lateinit var enterCloudWiFiSettingsPageDisposable: Disposable
    private lateinit var enterCloudSettingsPageDisposable: Disposable
    private lateinit var enterSearchGatewayPageDisposable: Disposable
    private lateinit var msgDialogResponseDisposable: Disposable
    private lateinit var showErrorMsgDialogDisposable: Disposable
    private lateinit var showToastDisposable: Disposable
    private lateinit var getCloudInfoDisposable: Disposable
    private lateinit var refreshTokenDisposable: Disposable
    private lateinit var syncNotiDisposable: Disposable
    private lateinit var loadingDlg: Dialog
    private lateinit var loadingHintDlg: Dialog
    private lateinit var errorMsgDlg: MessageDialog
    private lateinit var devicesInfo: DevicesInfo
    private lateinit var changeIconNameInfo: ChangeIconNameInfo
    private lateinit var wanInfo: WanInfo
    private lateinit var guestWiFiInfo: GuestWiFiInfo
    private lateinit var fSecureInfo: FSecureInfo
    private lateinit var hostNameReplaceInfo: HostNameReplaceInfo
    private lateinit var internetBlockingInfo: InternetBlockingInfo
    private lateinit var progressBar: ProgressBar
    private lateinit var progressBarHint: ProgressBar
    private lateinit var progressHintText: TextView
    private lateinit var getSpeedTestStatusTimer: CountDownTimer
    private lateinit var userInfo: TUTKUserInfo
    private lateinit var tokenInfo: TUTKTokenInfo
    private lateinit var gateDetailInfo: GatewayDetailInfo
    private lateinit var ipInterfaceInfo: IPInterfaceInfo
    private lateinit var customerInfo: CustomerInfo
    private lateinit var db: DatabaseCloudUtil
    private var deviceTimer = Timer()
    private var screenTimer = Timer()
    private var getWPSStatusTimer = Timer()
    private var getCloudWPSStatusTimer = Timer()
    private var cloudDeviceFragTrigger = false

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        val crashlyticsKit = Crashlytics.Builder()
                .core(CrashlyticsCore.Builder().disabled(AppConfig.NoUploadFabric).build())
                .build()
        Fabric.with(this, crashlyticsKit)
        setContentView(R.layout.activity_main)

        db = DatabaseCloudUtil.getInstance(this)!!

        if(AppConfig.SaveLog)
        {
            SaveLogUtil.filePath = this.getExternalFilesDir(null)
            SaveLogUtil.init()
            SaveLogUtil.deleteOldFiles()
        }

        GlobalData.notiUid = intent.getStringExtra("noti_uid")?:""
        GlobalData.notiMac = intent.getStringExtra("noti_mac")?:""
        LogUtil.d(TAG,"notiUid:${GlobalData.notiUid}")
        LogUtil.d(TAG,"notiMac:${GlobalData.notiMac}")

        loadingDlg = createLoadingDlg(this)
        loadingHintDlg = createLoadingHintDlg(this)
        createErrorMsgDlg()
        OUIUtil.executeGetMacOUITask(this)
        randomAESInfo()
        initSpeedTestTimer()
        setClickListener()
        listenEvent()
        switchToFragContainer(CloudWelcomeFragment())
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

                    if(isGPSEnabled())
                        gotoDiagnosticFragment()
                    else
                    {
                        MessageDialog(
                                this,
                                "",
                                getString(R.string.diagnostic_request_gps_permission),
                                arrayOf(getString(R.string.message_dialog_ok)),
                                AppConfig.DialogAction.ACT_GPS_PERMISSION
                        ).show()
                    }
                }
                else
                    LogUtil.e(TAG, "Location permission denied!")
            }
        }
    }

    override fun onDrawCompleted()
    {

    }

    override fun onBackPressed()
    {
        //super.onBackPressed()
    }

    private fun randomAESInfo()
    {
        CryptTool.IvAES = CryptTool.getRandomString(16)
        CryptTool.KeyAES = CryptTool.getRandomString(16)
        LogUtil.pd(TAG,"IvAES:${CryptTool.IvAES}")
        LogUtil.pd(TAG,"KeyAES:${CryptTool.KeyAES}")
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

            cloud_home_relative -> gotoCloudHomeFragment()

            devices_relative -> gotoDevicesFragment()

            cloud_devices_relative -> gotoCloudDevicesFragment()

            wifi_relative -> gotoWiFiFragment()

            cloud_wifi_relative -> gotoCloudWiFiFragment()

            diagnostic_relative ->
            {
                if(hasLocationPermission())
                {
                    if(isGPSEnabled())
                        gotoDiagnosticFragment()
                    else
                    {
                        MessageDialog(
                                this,
                                "",
                                getString(R.string.diagnostic_request_gps_permission),
                                arrayOf(getString(R.string.message_dialog_ok)),
                                AppConfig.DialogAction.ACT_GPS_PERMISSION
                        ).show()
                    }
                }
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

            cloud_settings_relative -> gotoCloudSettingsFragment()
        }
    }

    private fun setClickListener()
    {
        home_relative.setOnClickListener(clickListener)
        devices_relative.setOnClickListener(clickListener)
        wifi_relative.setOnClickListener(clickListener)
        diagnostic_relative.setOnClickListener(clickListener)
        account_relative.setOnClickListener(clickListener)

        cloud_home_relative.setOnClickListener(clickListener)
        cloud_devices_relative.setOnClickListener(clickListener)
        cloud_wifi_relative.setOnClickListener(clickListener)
        cloud_settings_relative.setOnClickListener(clickListener)
    }

    private fun disSelectToolBarIcons()
    {
        runOnUiThread{
            home_image.isSelected = false
            devices_image.isSelected = false
            wifi_image.isSelected = false
            diagnostic_image.isSelected = false
            account_image.isSelected = false

            home_text.isSelected = false
            devices_text.isSelected = false
            wifi_text.isSelected = false
            diagnostic_text.isSelected = false
            account_text.isSelected = false

            cloud_home_image.isSelected = false
            cloud_devices_image.isSelected = false
            cloud_wifi_image.isSelected = false
            cloud_settings_image.isSelected = false

            cloud_home_text.isSelected = false
            cloud_devices_text.isSelected = false
            cloud_wifi_text.isSelected = false
            cloud_settings_text.isSelected = false
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

    private fun createLoadingHintDlg(context: Context): Dialog
    {
        val builder = AlertDialog.Builder(context, R.style.loadingStyle)
        val mView = layoutInflater.inflate(R.layout.dialog_loading_show_hint, null)
        progressBarHint = mView.findViewById(R.id.loadingProBar) as ProgressBar
        progressHintText = mView.findViewById(R.id.loadingProBarHint) as TextView
        builder.setCancelable(false)
        builder.setView(mView)
        return builder.create()
    }

    private fun createErrorMsgDlg()
    {
        errorMsgDlg = MessageDialog(
                this,
                "",
                "",
                arrayOf(getString(R.string.message_dialog_ok)),
                AppConfig.DialogAction.ACT_RESEARCH
        )
    }

    private fun hasLocationPermission() =
            ContextCompat.checkSelfPermission(this.applicationContext, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED

    private fun isGPSEnabled(): Boolean
    {
        val locationManager = this.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
    }

    private fun listenEvent()
    {
        switchFrgDisposable = GlobalBus.listen(MainEvent.SwitchToFrag::class.java).subscribe{ switchToFragContainer(it.frag) }

        showLoadingOnlyGrayBGDisposable = GlobalBus.listen(MainEvent.ShowLoadingOnlyGrayBG::class.java).subscribe{ ShowLoadingOnlyGrayBG() }

        showLoadingDisposable = GlobalBus.listen(MainEvent.ShowLoading::class.java).subscribe{ showLoading() }

        showLoadingHintDisposable = GlobalBus.listen(MainEvent.ShowHintLoading::class.java).subscribe{ showHintLoading(it.hint) }

        hideLoadingDisposable = GlobalBus.listen(MainEvent.HideLoading::class.java).subscribe{ hideLoading() }

        showBottomToolbarDisposable = GlobalBus.listen(MainEvent.ShowBottomToolbar::class.java).subscribe{
            bottom_toolbar_area.visibility = View.VISIBLE
            bottom_toolbar.visibility = View.VISIBLE
            cloud_bottom_toolbar.visibility = View.GONE
        }

        showCloudBottomToolbarDisposable = GlobalBus.listen(MainEvent.ShowCloudBottomToolbar::class.java).subscribe{
            bottom_toolbar_area.visibility = View.VISIBLE
            bottom_toolbar.visibility = View.GONE
            cloud_bottom_toolbar.visibility = View.VISIBLE
        }

        hideBottomToolbarDisposable = GlobalBus.listen(MainEvent.HideBottomToolbar::class.java).subscribe{
            bottom_toolbar_area.visibility = View.GONE
            bottom_toolbar.visibility = View.GONE
            cloud_bottom_toolbar.visibility = View.GONE
        }

        setHomeIconFocusDisposable = GlobalBus.listen(MainEvent.SetHomeIconFocus::class.java).subscribe{
            disSelectToolBarIcons()

            runOnUiThread{
                home_image.isSelected = true
                home_text.isSelected = true
            }
        }

        setCloudHomeIconFocusDisposable = GlobalBus.listen(MainEvent.SetCloudHomeIconFocus::class.java).subscribe{
            disSelectToolBarIcons()

            runOnUiThread{
                cloud_home_image.isSelected = true
                cloud_home_text.isSelected = true
            }
        }

        startGetDeviceInfoTaskDisposable = GlobalBus.listen(MainEvent.StartGetDeviceInfoTask::class.java).subscribe{
            deviceTimer = Timer()
            deviceTimer.schedule(0, (AppConfig.endDeviceListUpdateTime * 1000).toLong()){ startGetAllNeedDeviceInfoTask() }
        }

        startGetDeviceInfoTaskOnceDisposable = GlobalBus.listen(MainEvent.StartGetDeviceInfoOnceTask::class.java).subscribe{ startGetAllNeedDeviceInfoTask() }

        stopGetDeviceInfoTaskDisposable = GlobalBus.listen(MainEvent.StopGetDeviceInfoTask::class.java).subscribe{ deviceTimer.cancel() }

        startGetCloudDeviceInfoTaskDisposable = GlobalBus.listen(MainEvent.StartGetCloudDeviceInfoTask::class.java).subscribe{ startCloudGetAllNeedDeviceInfoTask(it.style) }

        startGetCloudDeviceInfoForDevicePageTaskDisposable = GlobalBus.listen(MainEvent.StartGetCloudDeviceInfoForDevicePageTask::class.java).subscribe{
            cloudDeviceFragTrigger = true
            getCloudChangeIconNameInfoTask(it.style)
        }

        enterHomePageDisposable = GlobalBus.listen(MainEvent.EnterHomePage::class.java).subscribe{ gotoHomeFragment() }

        enterDevicesPageDisposable = GlobalBus.listen(MainEvent.EnterDevicesPage::class.java).subscribe{ gotoDevicesFragment() }

        enterWiFiSettingsPageDisposable = GlobalBus.listen(MainEvent.EnterWiFiSettingsPage::class.java).subscribe{ gotoWiFiFragment() }

        enterDiagnosticPageDisposable = GlobalBus.listen(MainEvent.EnterDiagnosticPage::class.java).subscribe{ gotoDiagnosticFragment() }

        enterAccountPageDisposable = GlobalBus.listen(MainEvent.EnterAccountPage::class.java).subscribe{ gotoAccountFragment() }

        enterCloudHomePageDisposable = GlobalBus.listen(MainEvent.EnterCloudHomePage::class.java).subscribe{ gotoCloudHomeFragment() }

        enterCloudDevicesPageDisposable = GlobalBus.listen(MainEvent.EnterCloudDevicesPage::class.java).subscribe{ gotoCloudDevicesFragment() }

        enterCloudWiFiSettingsPageDisposable = GlobalBus.listen(MainEvent.EnterCloudWiFiSettingsPage::class.java).subscribe{ gotoCloudWiFiFragment() }

        enterCloudSettingsPageDisposable = GlobalBus.listen(MainEvent.EnterCloudSettingsPage::class.java).subscribe{ gotoCloudSettingsFragment() }

        enterSearchGatewayPageDisposable = GlobalBus.listen(MainEvent.EnterSearchGatewayPage::class.java).subscribe{ gotoSearchGatewayFragment() }

        startGetWPSStatusTaskDisposable = GlobalBus.listen(MainEvent.StartGetWPSStatusTask::class.java).subscribe{
            getWPSStatusTimer = Timer()
            getWPSStatusTimer.schedule(0, (AppConfig.WPSStatusUpdateTime * 1000).toLong()){ getWPSStatusInfoTask() }
        }

        stopGetWPSStatusTaskDisposable = GlobalBus.listen(MainEvent.StopGetWPSStatusTask::class.java).subscribe{ getWPSStatusTimer.cancel() }

        startCloudGetWPSStatusTaskDisposable = GlobalBus.listen(MainEvent.StartCloudGetWPSStatusTask::class.java).subscribe{
            getCloudWPSStatusTimer = Timer()
            getCloudWPSStatusTimer.schedule(0, (AppConfig.WPSStatusUpdateTime * 1000).toLong()){ getCloudWPSStatusInfoTask() }
        }

        stopCloudGetWPSStatusTaskDisposable = GlobalBus.listen(MainEvent.StopCloudGetWPSStatusTask::class.java).subscribe{ getCloudWPSStatusTimer.cancel() }

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

                AppConfig.DialogAction.ACT_GPS_PERMISSION -> startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))

                else -> {}
            }
        }

        showErrorMsgDialogDisposable = GlobalBus.listen(MainEvent.ShowErrorMsgDialog::class.java).subscribe{ showErrorMsgDialog(it.msg, it.requestCtxName) }

        showToastDisposable = GlobalBus.listen(MainEvent.ShowToast::class.java).subscribe{ showToast(it.msg, it.requestCtxName) }

        getCloudInfoDisposable = GlobalBus.listen(MainEvent.GetCloudInfo::class.java).subscribe{ getUserInfo() }

        refreshTokenDisposable = GlobalBus.listen(MainEvent.RefreshToken::class.java).subscribe{ refreshToken(it.isInSetupFlow) }

        syncNotiDisposable = GlobalBus.listen(MainEvent.SyncNoti::class.java).subscribe{ registerNoti() }
    }

    private fun disposeEvent()
    {
        if(!switchFrgDisposable.isDisposed) switchFrgDisposable.dispose()
        if(!showLoadingOnlyGrayBGDisposable.isDisposed) showLoadingOnlyGrayBGDisposable.dispose()
        if(!showLoadingDisposable.isDisposed) showLoadingDisposable.dispose()
        if(!showLoadingHintDisposable.isDisposed) showLoadingHintDisposable.dispose()
        if(!hideLoadingDisposable.isDisposed) hideLoadingDisposable.dispose()
        if(!showBottomToolbarDisposable.isDisposed) showBottomToolbarDisposable.dispose()
        if(!showCloudBottomToolbarDisposable.isDisposed) showCloudBottomToolbarDisposable.dispose()
        if(!hideBottomToolbarDisposable.isDisposed) hideBottomToolbarDisposable.dispose()
        if(!setHomeIconFocusDisposable.isDisposed) setHomeIconFocusDisposable.dispose()
        if(!setCloudHomeIconFocusDisposable.isDisposed) setCloudHomeIconFocusDisposable.dispose()
        if(!startGetDeviceInfoTaskDisposable.isDisposed) startGetDeviceInfoTaskDisposable.dispose()
        if(!startGetCloudDeviceInfoTaskDisposable.isDisposed) startGetCloudDeviceInfoTaskDisposable.dispose()
        if(!startGetCloudDeviceInfoForDevicePageTaskDisposable.isDisposed) startGetCloudDeviceInfoForDevicePageTaskDisposable.dispose()
        if(!startGetDeviceInfoTaskOnceDisposable.isDisposed) startGetDeviceInfoTaskOnceDisposable.dispose()
        if(!stopGetDeviceInfoTaskDisposable.isDisposed) stopGetDeviceInfoTaskDisposable.dispose()
        if(!startGetWPSStatusTaskDisposable.isDisposed) startGetWPSStatusTaskDisposable.dispose()
        if(!stopGetWPSStatusTaskDisposable.isDisposed) stopGetWPSStatusTaskDisposable.dispose()
        if(!startCloudGetWPSStatusTaskDisposable.isDisposed) startGetWPSStatusTaskDisposable.dispose()
        if(!stopCloudGetWPSStatusTaskDisposable.isDisposed) stopGetWPSStatusTaskDisposable.dispose()
        if(!startGetSpeedTestStatusTaskDisposable.isDisposed) startGetSpeedTestStatusTaskDisposable.dispose()
        if(!stopGetSpeedTestStatusTaskDisposable.isDisposed) stopGetSpeedTestStatusTaskDisposable.dispose()
        if(!enterHomePageDisposable.isDisposed) enterHomePageDisposable.dispose()
        if(!enterDevicesPageDisposable.isDisposed) enterDevicesPageDisposable.dispose()
        if(!enterWiFiSettingsPageDisposable.isDisposed) enterWiFiSettingsPageDisposable.dispose()
        if(!enterDiagnosticPageDisposable.isDisposed) enterDiagnosticPageDisposable.dispose()
        if(!enterAccountPageDisposable.isDisposed) enterAccountPageDisposable.dispose()
        if(!enterCloudHomePageDisposable.isDisposed) enterCloudHomePageDisposable.dispose()
        if(!enterCloudDevicesPageDisposable.isDisposed) enterCloudDevicesPageDisposable.dispose()
        if(!enterCloudWiFiSettingsPageDisposable.isDisposed) enterCloudWiFiSettingsPageDisposable.dispose()
        if(!enterCloudSettingsPageDisposable.isDisposed) enterCloudSettingsPageDisposable.dispose()
        if(!enterSearchGatewayPageDisposable.isDisposed) enterSearchGatewayPageDisposable.dispose()
        if(!msgDialogResponseDisposable.isDisposed) msgDialogResponseDisposable.dispose()
        if(!showErrorMsgDialogDisposable.isDisposed) showErrorMsgDialogDisposable.dispose()
        if(!showToastDisposable.isDisposed) showToastDisposable.dispose()
        if(!getCloudInfoDisposable.isDisposed) getCloudInfoDisposable.dispose()
        if(!refreshTokenDisposable.isDisposed) refreshTokenDisposable.dispose()
        if(!syncNotiDisposable.isDisposed) syncNotiDisposable.dispose()
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

    private fun gotoCloudHomeFragment()
    {
        disSelectToolBarIcons()

        runOnUiThread{
            cloud_home_image.isSelected = true
            cloud_home_text.isSelected = true
        }

        if(GlobalData.currentFrag != "CloudHomeFragment")
            switchToFragContainer(CloudHomeFragment())
    }

    private fun gotoCloudDevicesFragment()
    {
        disSelectToolBarIcons()

        runOnUiThread{
            cloud_devices_image.isSelected = true
            cloud_devices_text.isSelected = true
        }

        if(GlobalData.currentFrag != "CloudDevicesFragment")
            switchToFragContainer(CloudDevicesFragment())
    }

    private fun gotoCloudWiFiFragment()
    {
        disSelectToolBarIcons()

        runOnUiThread{
            cloud_wifi_image.isSelected = true
            cloud_wifi_text.isSelected = true
        }

        if(GlobalData.currentFrag != "CloudWiFiSettingsFragment")
            switchToFragContainer(CloudWiFiSettingsFragment())
    }

    private fun gotoCloudSettingsFragment()
    {
        disSelectToolBarIcons()

        runOnUiThread{
            cloud_settings_image.isSelected = true
            cloud_settings_text.isSelected = true
        }

        if(GlobalData.currentFrag != "CloudSettingsFragment")
            switchToFragContainer(CloudSettingsFragment())
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

    private fun showHintLoading(hint: String)
    {
        runOnUiThread{
            progressHintText.text = hint
            if(!loadingHintDlg.isShowing) loadingHintDlg.show()
        }
    }

    private fun hideLoading()
    {
        runOnUiThread{
            if(loadingDlg.isShowing) loadingDlg.dismiss()
            if(loadingHintDlg.isShowing) loadingHintDlg.dismiss()
        }
    }

    private fun showErrorMsgDialog(msg: String, requestCtxName: String)
    {
        setErrorMsgDialog(msg, AppConfig.DialogAction.ACT_RESEARCH)
    }

    private fun setErrorMsgDialog(msg: String, act: AppConfig.DialogAction)
    {
        runOnUiThread{
            errorMsgDlg.description = msg
            errorMsgDlg.action = act
            if(!errorMsgDlg.isShowing)
                errorMsgDlg.show()
        }
    }

    private fun showToast(msg: String, requestCtxName: String)
    {
        runOnUiThread{ toast(msg) }
    }

    private fun showLongToast(msg: String, requestCtxName: String)
    {
        runOnUiThread{ longToast(msg) }
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
        if(!GlobalData.alreadyGetGatewayInfoLocalBase)
            GlobalBus.publish(MainEvent.ShowLoading())

        getSystemInfoTask()
    }

    private fun stopGetAllNeedDeviceInfoTask()
    {
        hideLoading()
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
                            val name = data.getJSONObject("Object").getString("HostName")
                            LogUtil.d(TAG,"HostName:$name")
                            GlobalData.getCurrentGatewayInfo().UserDefineName = name
                            getChangeIconNameInfoTask()
                        }
                        catch(e: JSONException)
                        {
                            e.printStackTrace()

                            hideLoading()
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
                            LogUtil.d(TAG,"changeIconNameInfo:$changeIconNameInfo")
                            GlobalData.changeIconNameList = changeIconNameInfo.Object.toMutableList()
                            getDeviceInfoTask()
                        }
                        catch(e: JSONException)
                        {
                            e.printStackTrace()

                            hideLoading()
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
                            LogUtil.d(TAG,"devicesInfo:$devicesInfo")

                            val newEndDeviceList = mutableListOf<DevicesInfoObject>()
                            val newHomeEndDeviceList = mutableListOf<DevicesInfoObject>()
                            val newZYXELEndDeviceList = mutableListOf<DevicesInfoObject>()
                            val newGuestEndDeviceList = mutableListOf<DevicesInfoObject>()

                            GlobalData.alreadyGetGatewayInfoLocalBase = true

                            /*newZYXELEndDeviceList.add(
                                    DevicesInfoObject
                                    (
                                            Active = true,
                                            HostName = GlobalData.getCurrentGatewayInfo().getName(),
                                            IPAddress = GlobalData.getCurrentGatewayInfo().IP,
                                            PhysAddress = GlobalData.getCurrentGatewayInfo().MAC,
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

                            getWanInfoTask()
                        }
                        catch(e: JSONException)
                        {
                            e.printStackTrace()

                            hideLoading()
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
                            LogUtil.d(TAG,"wanInfo:$wanInfo")
                            GlobalData.gatewayWanInfo = wanInfo.copy()
                            getGuestWiFiEnableTask()
                        }
                        catch(e: JSONException)
                        {
                            e.printStackTrace()

                            hideLoading()
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
                            LogUtil.d(TAG,"guestWiFiInfo:$guestWiFiInfo")
                            GlobalData.guestWiFiStatus = guestWiFiInfo.Object.Enable
                            getFSecureInfoTask()
                        }
                        catch(e: JSONException)
                        {
                            e.printStackTrace()

                            hideLoading()
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
                            LogUtil.d(TAG,"fSecureInfo:$fSecureInfo")
                            FeatureConfig.FSecureStatus = fSecureInfo.Object.Cyber_Security_FSC
                            getHostNameReplaceInfoTask()
                        }
                        catch(e: JSONException)
                        {
                            e.printStackTrace()

                            hideLoading()
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
                            LogUtil.d(TAG,"hostNameReplaceInfo:$hostNameReplaceInfo")
                            FeatureConfig.hostNameReplaceStatus = hostNameReplaceInfo.Object.Enable
                            getInternetBlockingInfoTask()
                        }
                        catch(e: JSONException)
                        {
                            e.printStackTrace()

                            hideLoading()
                        }
                    }
                }).execute()
    }

    private fun getInternetBlockingInfoTask()
    {
        LogUtil.d(TAG,"getInternetBlockingInfoTask()")
        GatewayApi.GetInternetBlockingInfo()
                .setRequestPageName(TAG)
                .setResponseListener(object: Commander.ResponseListener()
                {
                    override fun onSuccess(responseStr: String)
                    {
                        try
                        {
                            internetBlockingInfo = Gson().fromJson(responseStr, InternetBlockingInfo::class.javaObjectType)
                            LogUtil.d(TAG,"internetBlockingInfo:$internetBlockingInfo")
                            FeatureConfig.internetBlockingStatus = internetBlockingInfo.Object.Enable
                            stopGetAllNeedDeviceInfoTask()
                        }
                        catch(e: JSONException)
                        {
                            e.printStackTrace()

                            hideLoading()
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
                            val status = data.getJSONObject("Object").getString("X_ZYXEL_WPSRunningStatus")
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

    private fun getCloudWPSStatusInfoTask()
    {
        LogUtil.d(TAG,"getCloudWPSStatusInfoTask()")

        P2PAddMeshApi.GetWPSStatus()
                .setRequestPageName(TAG)
                .setResponseListener(object: TUTKP2PResponseCallback()
                {
                    override fun onSuccess(responseStr: String)
                    {
                        try
                        {
                            val data = JSONObject(responseStr)
                            val status = data.getJSONObject("Object").getString("X_ZYXEL_WPSRunningStatus")
                            LogUtil.d(TAG,"WPS status:$status")

                            with(status)
                            {
                                when
                                {
                                    contains("OK", ignoreCase = true) ->
                                    {
                                        getCloudWPSStatusTimer.cancel()
                                        GlobalBus.publish(LoadingTransitionEvent.WPSStatusUpdate(true))
                                    }

                                    contains("Requested", ignoreCase = true) -> {}

                                    else ->
                                    {
                                        getCloudWPSStatusTimer.cancel()
                                        GlobalBus.publish(LoadingTransitionEvent.WPSStatusUpdate(false))
                                    }
                                }
                            }
                        }
                        catch(e: Exception)
                        {
                            e.printStackTrace()
                            getCloudWPSStatusTimer.cancel()
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
                            val uploadResult = data.getJSONObject("Object").getString("UploadSpeedResult")
                            val downloadResult = data.getJSONObject("Object").getString("DownloadSpeedResult")
                            val status = data.getJSONObject("Object").getString("Status")
                            LogUtil.d(TAG,"SpeedTest uploadResult:$uploadResult")
                            LogUtil.d(TAG,"SpeedTest downloadResult:$downloadResult")
                            LogUtil.d(TAG,"SpeedTest status:$status")

                            if(AppConfig.SpeedTestActive && AppConfig.SpeedTestActiveDebug)
                            {
                                val start = data.getJSONObject("Object").getString("Start")
                                val serverIP = data.getJSONObject("Object").getString("ServerIP")
                                val CPE_WANname = data.getJSONObject("Object").getString("CPE_WANname")
                                LogUtil.d(TAG,"SpeedTest start:$start")
                                LogUtil.d(TAG,"SpeedTest serverIP:$serverIP")
                                LogUtil.d(TAG,"SpeedTest CPE_WANname:$CPE_WANname")

                                showLongToast("uploadResult:$uploadResult\ndownloadResult:$downloadResult\nstatus:$status\nstart:$start\nserverIP:$serverIP\nCPE_WANname:$CPE_WANname", "")
                            }

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

                                    contains("Err", ignoreCase = true) ->
                                    {
                                        stopSpeedTest()

                                        runOnUiThread{
                                            MessageDialog(
                                                    this@MainActivity,
                                                    "",
                                                    getString(R.string.speed_test_error),
                                                    arrayOf(getString(R.string.message_dialog_ok)),
                                                    AppConfig.DialogAction.ACT_NONE
                                            ).show()
                                        }

                                        GlobalBus.publish(GatewayEvent.GetSpeedTestComplete("- Mbps", "- Mbps"))
                                    }

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


    private fun startCloudGetAllNeedDeviceInfoTask(style: AppConfig.LoadingStyle)
    {
        cloudDeviceFragTrigger = false

        when(style)
        {
            AppConfig.LoadingStyle.STY_NONE -> {}
            AppConfig.LoadingStyle.STY_ONLY_BG -> ShowLoadingOnlyGrayBG()
            else -> showLoading()
        }

        val gatewayList = mutableListOf<GatewayInfo>()
        val findingDeviceInfo = GatewayInfo()
        gatewayList.add(findingDeviceInfo)

        GlobalData.currentGatewayIndex = 0
        GlobalData.gatewayList = gatewayList.toMutableList()

        getCloudSystemInfoTask()
    }

    private fun getCloudSystemInfoTask()
    {
        LogUtil.d(TAG,"getCloudSystemInfoTask()")
        P2PGatewayApi.GetSystemInfo()
                .setRequestPageName(TAG)
                .setResponseListener(object: TUTKP2PResponseCallback()
                {
                    override fun onSuccess(responseStr: String)
                    {
                        try
                        {
                            val data = JSONObject(responseStr)
                            val name = data.getJSONObject("Object").getString("HostName")
                            LogUtil.d(TAG,"HostName:$name")
                            GlobalData.getCurrentGatewayInfo().UserDefineName = name
                            getCloudFWVersionInfoTask()
                        }
                        catch(e: JSONException)
                        {
                            e.printStackTrace()

                            hideLoading()
                        }
                    }
                }).execute()
    }

    private fun getCloudFWVersionInfoTask()
    {
        LogUtil.d(TAG,"getCloudFWVersionInfoTask()")
        P2PGatewayApi.GetFWVersionInfo()
                .setRequestPageName(TAG)
                .setResponseListener(object: TUTKP2PResponseCallback()
                {
                    override fun onSuccess(responseStr: String)
                    {
                        try
                        {
                            gateDetailInfo = Gson().fromJson(responseStr, GatewayDetailInfo::class.javaObjectType)
                            LogUtil.d(TAG,"gateDetailInfo:$gateDetailInfo")
                            GlobalData.getCurrentGatewayInfo().SoftwareVersion = gateDetailInfo.Object.SoftwareVersion
                            GlobalData.getCurrentGatewayInfo().ModelName = gateDetailInfo.Object.ModelName
                            GlobalData.getCurrentGatewayInfo().SerialNumber = gateDetailInfo.Object.SerialNumber
                            getCloudLanIPInfoTask()
                        }
                        catch(e: JSONException)
                        {
                            e.printStackTrace()

                            hideLoading()
                        }
                    }
                }).execute()
    }

    private fun getCloudLanIPInfoTask()
    {
        LogUtil.d(TAG,"getCloudLanIPInfoTask()")
        P2PGatewayApi.GetLanIPInfo()
                .setRequestPageName(TAG)
                .setResponseListener(object: TUTKP2PResponseCallback()
                {
                    override fun onSuccess(responseStr: String)
                    {
                        try
                        {
                            ipInterfaceInfo = Gson().fromJson(responseStr, IPInterfaceInfo::class.javaObjectType)
                            LogUtil.d(TAG,"ipInterfaceInfo:$ipInterfaceInfo")
                            for(item in ipInterfaceInfo.Object)
                            {
                                if( (item.X_ZYXEL_IfName == "br0") && (item.IPv4Address.isNotEmpty()) )
                                {
                                    GlobalData.getCurrentGatewayInfo().IP = item.IPv4Address[0].IPAddress
                                    break
                                }
                            }
                            getCustomerInfoTask()
                        }
                        catch(e: JSONException)
                        {
                            e.printStackTrace()

                            hideLoading()
                        }
                    }
                }).execute()
    }

    private fun getCustomerInfoTask()
    {
        LogUtil.d(TAG,"getCustomerInfoTask()")
        P2PGatewayApi.GetCustomerInfo()
                .setRequestPageName(TAG)
                .setResponseListener(object: TUTKP2PResponseCallback()
                {
                    override fun onSuccess(responseStr: String)
                    {
                        try
                        {
                            customerInfo = Gson().fromJson(responseStr, CustomerInfo::class.javaObjectType)
                            LogUtil.d(TAG,"customerInfo:$customerInfo")
                            GlobalData.customerLogo = customerInfo.Object.X_ZYXEL_APP_Customer
                            getCloudChangeIconNameInfoTask(AppConfig.LoadingStyle.STY_NONE)
                        }
                        catch(e: JSONException)
                        {
                            e.printStackTrace()

                            hideLoading()
                        }
                    }
                }).execute()
    }

    private fun getCloudChangeIconNameInfoTask(style: AppConfig.LoadingStyle)
    {
        when(style)
        {
            AppConfig.LoadingStyle.STY_NONE -> {}
            AppConfig.LoadingStyle.STY_ONLY_BG -> ShowLoadingOnlyGrayBG()
            else -> showLoading()
        }

        LogUtil.d(TAG,"getCloudChangeIconNameInfoTask()")
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
                            getCloudDeviceInfoTask()
                        }
                        catch(e: JSONException)
                        {
                            e.printStackTrace()

                            hideLoading()
                        }
                    }
                }).execute()
    }

    private fun getCloudDeviceInfoTask()
    {
        LogUtil.d(TAG,"getCloudDeviceInfoTask()")
        P2PDevicesApi.GetDevicesInfo()
                .setRequestPageName(TAG)
                .setResponseListener(object: TUTKP2PResponseCallback()
                {
                    override fun onSuccess(responseStr: String)
                    {
                        try
                        {
                            /*val responseStr2 =
                                    "{\n" +
                                            "       \"requested_path\": \"Device.Hosts.Host.\",\n" +
                                            "       \"oper_status\": \"Success\",\n" +
                                            "       \"Object\": [\n" +
                                            "         {\n" +
                                            "           \"PhysAddress\": \"\",\n" +
                                            "           \"IPAddress\": \"\",\n" +
                                            "           \"IPAddress6\": \"\",\n" +
                                            "           \"IPLinkLocalAddress6\": \"\",\n" +
                                            "           \"AddressSource\": \"\",\n" +
                                            "           \"DHCPClient\": \"\",\n" +
                                            "           \"LeaseTimeRemaining\": 0,\n" +
                                            "           \"AssociatedDevice\": \"\",\n" +
                                            "           \"Layer1Interface\": \"\",\n" +
                                            "           \"Layer3Interface\": \"\",\n" +
                                            "           \"VendorClassID\": \"\",\n" +
                                            "           \"ClientID\": \"\",\n" +
                                            "           \"UserClassID\": \"\",\n" +
                                            "           \"HostName\": \"\",\n" +
                                            "           \"Active\": \"\",\n" +
                                            "           \"X_ZYXEL_DeleteLease\": \"\",\n" +
                                            "           \"X_ZYXEL_ConnectionType\": \"\",\n" +
                                            "           \"X_ZYXEL_ConnectedAP\": \"\",\n" +
                                            "           \"X_ZYXEL_HostType\": \"\",\n" +
                                            "           \"X_ZYXEL_CapabilityType\": \"\",\n" +
                                            "           \"X_ZYXEL_PhyRate\": 0,\n" +
                                            "           \"X_ZYXEL_WiFiStatus\": \"\",\n" +
                                            "           \"X_ZYXEL_SignalStrength\": 0,\n" +
                                            "           \"X_ZYXEL_SNR\": 0,\n" +
                                            "           \"X_ZYXEL_RSSI\": 0,\n" +
                                            "           \"X_ZYXEL_SoftwareVersion\": \"\",\n" +
                                            "           \"X_ZYXEL_Address6Source\": \"\",\n" +
                                            "           \"X_ZYXEL_DHCP6Client\": \"\",\n" +
                                            "           \"X_ZYXEL_BytesSent\": 0,\n" +
                                            "           \"X_ZYXEL_BytesReceived\": 0,\n" +
                                            "           \"ClientDuid\": \"\",\n" +
                                            "           \"ExpireTime\": \"\",\n" +
                                            "           \"X_ZYXEL_Neighbor\": \"\",\n" +
                                            "           \"X_ZYXEL_Conn_Guest\": 0,\n" +
                                            "           \"X_ZYXEL_Band\": 0,\n" +
                                            "           \"X_ZYXEL_Channel_24G\": 0,\n" +
                                            "           \"X_ZYXEL_Channel_5G\": 0,\n" +
                                            "           \"X_ZYXEL_DHCPLeaseTime\": 0,\n" +
                                            "           \"X_ZYXEL_RSSI_STAT\": \"\"\n" +
                                            "         },\n" +
                                            "{\n" +
                                            "           \"PhysAddress\": \"b8:d5:26:4d:85:cb\",\n" +
                                            "           \"IPAddress\": \"192.168.1.245\",\n" +
                                            "           \"IPAddress6\": \"\",\n" +
                                            "           \"IPLinkLocalAddress6\": \"fe80::bad5:26ff:fe4d:85cb\",\n" +
                                            "           \"AddressSource\": \"DHCP\",\n" +
                                            "           \"DHCPClient\": \"\",\n" +
                                            "           \"LeaseTimeRemaining\": 67506,\n" +
                                            "           \"AssociatedDevice\": \"\",\n" +
                                            "           \"Layer1Interface\": \"Device.Ethernet.Interface.4\",\n" +
                                            "           \"Layer3Interface\": \"Device.IP.Interface.1\",\n" +
                                            "           \"VendorClassID\": \"*\",\n" +
                                            "           \"ClientID\": \"\",\n" +
                                            "           \"UserClassID\": \"\",\n" +
                                            "           \"HostName\": \"WX3310-B0_28850\",\n" +
                                            "           \"Active\": true,\n" +
                                            "           \"X_ZYXEL_DeleteLease\": false,\n" +
                                            "           \"X_ZYXEL_ConnectionType\": \"Ethernet\",\n" +
                                            "           \"X_ZYXEL_ConnectedAP\": \"\",\n" +
                                            "           \"X_ZYXEL_HostType\": \"AP\",\n" +
                                            "           \"X_ZYXEL_CapabilityType\": \"L2Device\",\n" +
                                            "           \"X_ZYXEL_PhyRate\": 1000,\n" +
                                            "           \"X_ZYXEL_WiFiStatus\": true,\n" +
                                            "           \"X_ZYXEL_SignalStrength\": 0,\n" +
                                            "           \"X_ZYXEL_SNR\": 0,\n" +
                                            "           \"X_ZYXEL_RSSI\": 0,\n" +
                                            "           \"X_ZYXEL_SoftwareVersion\": \"V1.00(ABSF.0)b3\",\n" +
                                            "           \"X_ZYXEL_Address6Source\": \"\",\n" +
                                            "           \"X_ZYXEL_DHCP6Client\": \"\",\n" +
                                            "           \"X_ZYXEL_BytesSent\": 0,\n" +
                                            "           \"X_ZYXEL_BytesReceived\": 0,\n" +
                                            "           \"ClientDuid\": \"\",\n" +
                                            "           \"ExpireTime\": \"\",\n" +
                                            "           \"X_ZYXEL_DHCPLeaseTime\": 1584073586,\n" +
                                            "           \"X_ZYXEL_RSSI_STAT\": \"Too Close\",\n" +
                                            "           \"X_ZYXEL_Neighbor\": \"\",\n" +
                                            "           \"X_ZYXEL_Conn_Guest\": 0,\n" +
                                            "           \"X_ZYXEL_Band\": 0,\n" +
                                            "           \"X_ZYXEL_Channel_24G\": 0,\n" +
                                            "           \"X_ZYXEL_Channel_5G\": 0\n" +
                                            "         },\n" +
                                            "         {\n" +
                                            "           \"PhysAddress\": \"40:4e:36:b3:fd:15\",\n" +
                                            "           \"IPAddress\": \"192.168.1.36\",\n" +
                                            "           \"IPAddress6\": \"\",\n" +
                                            "           \"IPLinkLocalAddress6\": \"fe80::424e:36ff:feb3:fd15\",\n" +
                                            "           \"AddressSource\": \"DHCP\",\n" +
                                            "           \"DHCPClient\": \"\",\n" +
                                            "           \"LeaseTimeRemaining\": 86035,\n" +
                                            "           \"AssociatedDevice\": \"WiFi.AccessPoint.5.AssociatedDevice.1\",\n" +
                                            "           \"Layer1Interface\": \"\",\n" +
                                            "           \"Layer3Interface\": \"Device.IP.Interface.1\",\n" +
                                            "           \"VendorClassID\": \"android-dhcp-9\",\n" +
                                            "           \"ClientID\": \"\",\n" +
                                            "           \"UserClassID\": \"\",\n" +
                                            "           \"HostName\": \"android-e5784d2ba14b34c5\",\n" +
                                            "           \"Active\": true,\n" +
                                            "           \"X_ZYXEL_DeleteLease\": false,\n" +
                                            "           \"X_ZYXEL_ConnectionType\": \"Wi-Fi\",\n" +
                                            "           \"X_ZYXEL_ConnectedAP\": \"02:16:77:01:00:02\",\n" +
                                            "           \"X_ZYXEL_HostType\": \"Desktop\",\n" +
                                            "           \"X_ZYXEL_CapabilityType\": \"Client\",\n" +
                                            "           \"X_ZYXEL_PhyRate\": 761,\n" +
                                            "           \"X_ZYXEL_WiFiStatus\": false,\n" +
                                            "           \"X_ZYXEL_SignalStrength\": 5,\n" +
                                            "           \"X_ZYXEL_SNR\": 50,\n" +
                                            "           \"X_ZYXEL_RSSI\": -34,\n" +
                                            "           \"X_ZYXEL_SoftwareVersion\": \"\",\n" +
                                            "           \"X_ZYXEL_Address6Source\": \"\",\n" +
                                            "           \"X_ZYXEL_DHCP6Client\": \"\",\n" +
                                            "           \"X_ZYXEL_BytesSent\": 271,\n" +
                                            "           \"X_ZYXEL_BytesReceived\": 701,\n" +
                                            "           \"ClientDuid\": \"\",\n" +
                                            "           \"ExpireTime\": \"\",\n" +
                                            "           \"X_ZYXEL_DHCPLeaseTime\": 1584341644,\n" +
                                            "           \"X_ZYXEL_Neighbor\": \"02:16:77:01:00:02\",\n" +
                                            "           \"X_ZYXEL_Conn_Guest\": 0,\n" +
                                            "           \"X_ZYXEL_Band\": 2,\n" +
                                            "           \"X_ZYXEL_Channel_24G\": 0,\n" +
                                            "           \"X_ZYXEL_Channel_5G\": 149,\n" +
                                            "           \"X_ZYXEL_RSSI_STAT\": \"Too Close\"\n" +
                                            "         }\n" +
                                            "       ]\n" +
                                            "     }\n"*/

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

                            if(cloudDeviceFragTrigger)
                            {
                                cloudDeviceFragTrigger = false
                                GlobalBus.publish(DevicesEvent.GetCloudDeviceInfoComplete())
                            }
                            else
                                getCloudWanInfoTask()
                        }
                        catch(e: JSONException)
                        {
                            e.printStackTrace()

                            hideLoading()
                        }
                    }
                }).execute()
    }

    private fun getCloudWanInfoTask()
    {
        LogUtil.d(TAG,"getCloudWanInfoTask()")
        P2PGatewayApi.GetWanInfo()
                .setRequestPageName(TAG)
                .setResponseListener(object: TUTKP2PResponseCallback()
                {
                    override fun onSuccess(responseStr: String)
                    {
                        try
                        {
                            wanInfo = Gson().fromJson(responseStr, WanInfo::class.javaObjectType)
                            LogUtil.d(TAG,"wanInfo:$wanInfo")
                            GlobalData.gatewayWanInfo = wanInfo.copy()
                            GlobalData.getCurrentGatewayInfo().MAC = wanInfo.Object.MAC
                            getCloudGuestWiFiEnableTask()
                        }
                        catch(e: JSONException)
                        {
                            e.printStackTrace()

                            hideLoading()
                        }
                    }
                }).execute()
    }

    private fun getCloudGuestWiFiEnableTask()
    {
        LogUtil.d(TAG,"getCloudGuestWiFiEnableTask()")
        P2PWiFiSettingApi.GetGuestWiFi24GInfo()
                .setRequestPageName(TAG)
                .setResponseListener(object: TUTKP2PResponseCallback()
                {
                    override fun onSuccess(responseStr: String)
                    {
                        try
                        {
                            guestWiFiInfo = Gson().fromJson(responseStr, GuestWiFiInfo::class.javaObjectType)
                            LogUtil.d(TAG,"guestWiFiInfo:$guestWiFiInfo")
                            GlobalData.guestWiFiStatus = guestWiFiInfo.Object.Enable
                            getCloudFSecureInfoTask()
                        }
                        catch(e: JSONException)
                        {
                            e.printStackTrace()

                            hideLoading()
                        }
                    }
                }).execute()
    }

    private fun getCloudFSecureInfoTask()
    {
        LogUtil.d(TAG,"getCloudFSecureInfoTask()")
        P2PGatewayApi.GetFSecureInfo()
                .setRequestPageName(TAG)
                .setResponseListener(object: TUTKP2PResponseCallback()
                {
                    override fun onSuccess(responseStr: String)
                    {
                        try
                        {
                            fSecureInfo = Gson().fromJson(responseStr, FSecureInfo::class.javaObjectType)
                            LogUtil.d(TAG,"fSecureInfo:$fSecureInfo")
                            FeatureConfig.FSecureStatus = fSecureInfo.Object.Cyber_Security_FSC
                            getCloudHostNameReplaceInfoTask()
                        }
                        catch(e: JSONException)
                        {
                            e.printStackTrace()

                            hideLoading()
                        }
                    }
                }).execute()
    }

    private fun getCloudHostNameReplaceInfoTask()
    {
        LogUtil.d(TAG,"getCloudHostNameReplaceInfoTask()")
        P2PGatewayApi.GetHostNameReplaceInfo()
                .setRequestPageName(TAG)
                .setResponseListener(object: TUTKP2PResponseCallback()
                {
                    override fun onSuccess(responseStr: String)
                    {
                        try
                        {
                            hostNameReplaceInfo = Gson().fromJson(responseStr, HostNameReplaceInfo::class.javaObjectType)
                            LogUtil.d(TAG,"hostNameReplaceInfo:$hostNameReplaceInfo")
                            FeatureConfig.hostNameReplaceStatus = hostNameReplaceInfo.Object.Enable
                            if(GlobalData.notiUid.isNotEmpty() && GlobalData.notiMac.isNotEmpty())
                                checkNotiFlowFinalStep()
                            else
                                GlobalBus.publish(HomeEvent.GetCloudDeviceInfoComplete())
                        }
                        catch(e: JSONException)
                        {
                            e.printStackTrace()

                            hideLoading()
                        }
                    }
                }).execute()
    }

    private fun getUserInfo()
    {
        val accessToken by SharedPreferencesUtil(this, AppConfig.SHAREDPREF_TUTK_ACCESS_TOKEN_KEY, "")

        val header = HashMap<String, Any>()
        header["authorization"] = "${GlobalData.tokenType} $accessToken"

        AMDMApi.GetUserInfo()
                .setRequestPageName(TAG)
                .setHeaders(header)
                .setResponseListener(object: TUTKCommander.ResponseListener()
                {
                    override fun onSuccess(responseStr: String)
                    {
                        try
                        {
                            userInfo = Gson().fromJson(responseStr, TUTKUserInfo::class.javaObjectType)
                            LogUtil.d(TAG,"userInfo:$userInfo")
                            GlobalData.currentEmail = userInfo.email
                            getAllDevice()
                        }
                        catch(e: JSONException)
                        {
                            e.printStackTrace()

                            hideLoading()
                        }
                    }
                }).execute()
    }

    private fun getAllDevice()
    {
        LogUtil.d(TAG,"getAllDevice()")

        val accessToken by SharedPreferencesUtil(this, AppConfig.SHAREDPREF_TUTK_ACCESS_TOKEN_KEY, "")

        val header = HashMap<String, Any>()
        header["authorization"] = "${GlobalData.tokenType} $accessToken"

        AMDMApi.GetAllDevice()
                .setRequestPageName(TAG)
                .setHeaders(header)
                .setResponseListener(object: TUTKCommander.ResponseListener()
                {
                    override fun onSuccess(responseStr: String)
                    {
                        try
                        {
                            GlobalData.cloudGatewayListInfo = Gson().fromJson(responseStr, TUTKAllDeviceInfo::class.javaObjectType)
                            LogUtil.d(TAG,"allDeviceInfo:${GlobalData.cloudGatewayListInfo}")

                            if(GlobalData.notiUid.isNotEmpty() && GlobalData.notiMac.isNotEmpty())
                            {
                                for(item in GlobalData.cloudGatewayListInfo.data)
                                {
                                    if(item.udid == GlobalData.notiUid)
                                    {
                                        GlobalData.currentCredential = item.credential
                                        break
                                    }
                                }

                                connectP2PFromNoti()
                            }
                            else
                                switchToFragContainer(CloudGatewayListFragment())
                        }
                        catch(e: JSONException)
                        {
                            e.printStackTrace()

                            hideLoading()
                        }
                    }
                }).execute()
    }

    private fun refreshToken(isInSetupFlow: Boolean)
    {
        var refreshToken by SharedPreferencesUtil(this, AppConfig.SHAREDPREF_TUTK_REFRESH_TOKEN_KEY, "")
        var accessToken by SharedPreferencesUtil(this, AppConfig.SHAREDPREF_TUTK_ACCESS_TOKEN_KEY, "")

        if(refreshToken == "" || accessToken == "")
        {
            val bundle = Bundle().apply{
                putBoolean("isInSetupFlow", isInSetupFlow)
            }
            switchToFragContainer(CloudLoginFragment().apply{ arguments = bundle })
        }
        else
        {
            val header = HashMap<String, Any>()
            header["authorization"] = "Basic ${BuildConfig.TUTK_DM_AUTHORIZATION}"
            header["content-type"] = "application/x-www-form-urlencoded"

            val body = HashMap<String, Any>()
            body["grant_type"] = "refresh_token"
            body["refresh_token"] = refreshToken

            AMDMApi.RefreshToken()
                    .setRequestPageName(TAG)
                    .setHeaders(header)
                    .setFormBody(body)
                    .setResponseListener(object: TUTKCommander.ResponseListener()
                    {
                        override fun onSuccess(responseStr: String)
                        {
                            try
                            {
                                tokenInfo = Gson().fromJson(responseStr, TUTKTokenInfo::class.javaObjectType)
                                LogUtil.d(TAG,"refreshTokenInfo:$tokenInfo")
                                refreshToken = tokenInfo.refresh_token
                                accessToken = tokenInfo.access_token
                                GlobalData.tokenType = tokenInfo.token_type
                                LogUtil.d(TAG, "refreshToken:$refreshToken")
                                LogUtil.d(TAG, "accessToken:$accessToken")
                                getUserInfo()
                            }
                            catch(e: JSONException)
                            {
                                e.printStackTrace()

                                hideLoading()
                            }
                        }
                    }).execute()
        }
    }

    private fun connectP2PFromNoti()
    {
        showLoading()

        var name = ""
        for(item in GlobalData.cloudGatewayListInfo.data)
        {
            if(item.udid == GlobalData.notiUid)
            {
                name = item.displayName
                break
            }
        }

        doAsync{
            TUTKP2PBaseApi.stopSession()
            if(TUTKP2PBaseApi.initIOTCRDT() >= 0)
            {
                if(TUTKP2PBaseApi.startSession(GlobalData.notiUid) >= 0)
                {
                    GlobalData.currentDisplayName = name
                    GlobalData.currentUID = GlobalData.notiUid
                    verifyCloudAgentTask()
                }
                else
                    gotoTroubleShooting()
            }
            else
                gotoTroubleShooting()
        }
    }

    private fun checkNotiFlowFinalStep()
    {
        hideLoading()

        var isZyxelEndDevice = false
        var isExist = false
        var deviceInfo = DevicesInfoObject()

        for(item in GlobalData.ZYXELEndDeviceList)
        {
            if(GlobalData.notiMac == item.PhysAddress)
            {
                isExist = true
                isZyxelEndDevice = true
                deviceInfo = item
                break
            }
        }

        for(item in GlobalData.homeEndDeviceList)
        {
            if(GlobalData.notiMac == item.PhysAddress)
            {
                isExist = true
                isZyxelEndDevice = false
                deviceInfo = item
                break
            }
        }

        GlobalData.notiMac = ""
        GlobalData.notiUid = ""

        if(isExist)
        {
            if(isZyxelEndDevice)
            {
                val bundle = Bundle().apply{
                    putBoolean("GatewayMode", false)
                    putSerializable("GatewayInfo", GlobalData.getCurrentGatewayInfo())
                    putSerializable("WanInfo", GlobalData.gatewayWanInfo)
                    putSerializable("DevicesInfo", deviceInfo)
                }
                switchToFragContainer(CloudZYXELEndDeviceDetailFragment().apply{ arguments = bundle })
            }
            else
            {
                val bundle = Bundle().apply{
                    putSerializable("DevicesInfo", deviceInfo)
                    putString("Search", "")
                    putBoolean("FromSearch", false)
                }
                switchToFragContainer(CloudEndDeviceDetailFragment().apply{ arguments = bundle })
            }
        }
        else
            switchToFragContainer(CloudEndDeviceDetailOfflineFragment())
    }

    private fun gotoTroubleShooting()
    {
        TUTKP2PBaseApi.forceStopSession()

        hideLoading()

        val bundle = Bundle().apply{
            putSerializable("pageMode", AppConfig.TroubleshootingPage.PAGE_P2P_INIT_FAIL_IN_GATEWAY_LIST)
        }

        switchToFragContainer(SetupConnectTroubleshootingFragment().apply{ arguments = bundle })
    }

    private fun syncNoti()
    {
        LogUtil.d(TAG,"syncNoti()")

        doAsync{
            val siteList = db.getSiteInfoDao().queryByNoti(true)
            if(siteList.isNotEmpty())
            {
                val paramsArray = JSONArray()
                for(item in siteList)
                {
                    val params = JSONObject()
                    params.put("uid", item.uid)
                    params.put("interval", AppConfig.NOTI_INTERVAL)
                    params.put("format", AppConfig.NOTI_FORMAT)
                    paramsArray.put(params)
                }
                LogUtil.d(TAG,"paramsArray:$paramsArray")

                val base64Encoded = Base64.encodeToString(paramsArray.toString().toByteArray(), Base64.DEFAULT)
                LogUtil.d(TAG,"base64Encoded:$base64Encoded")

                mappingSyncNoti(base64Encoded)
            }
            else
            {
                LogUtil.d(TAG,"no noti enable in DB!!")
                getUserInfo()
            }
        }
    }

    private fun registerNoti()
    {
        LogUtil.d(TAG,"registerNoti()")

        val phoneUdid = Settings.System.getString(this.contentResolver, Settings.Secure.ANDROID_ID)
        val notificationToken by SharedPreferencesUtil(this, AppConfig.SHAREDPREF_NOTIFICATION_TOKEN, "")

        val header = HashMap<String, Any>()
        val body = HashMap<String, Any>()
        body["cmd"] = "client"
        body["os"] = "android"
        body["appid"] = AppConfig.NOTI_BUNDLE_ID
        body["udid"] = phoneUdid
        body["token"] = notificationToken
        body["lang"] = "enUS"
        body["dev"] = 0

        NotificationApi.Common(this)
                .setRequestPageName(TAG)
                .setHeaders(header)
                .setFormBody(body)
                .setResponseListener(object: TUTKCommander.ResponseListener()
                {
                    override fun onSuccess(responseStr: String)
                    {
                        LogUtil.d(TAG,"NotificationApi Register:$responseStr")
                        syncNoti()
                    }
                }).execute()
    }

    private fun mappingSyncNoti(map: String)
    {
        LogUtil.d(TAG,"mappingSyncNoti()")

        val phoneUdid = Settings.System.getString(this.contentResolver, Settings.Secure.ANDROID_ID)

        val header = HashMap<String, Any>()
        val body = HashMap<String, Any>()
        body["cmd"] = "mapsync"
        body["os"] = "android"
        body["appid"] = AppConfig.NOTI_BUNDLE_ID
        body["udid"] = phoneUdid
        body["map"] = map

        NotificationApi.Common(this)
                .setRequestPageName(TAG)
                .setHeaders(header)
                .setFormBody(body)
                .setResponseListener(object: TUTKCommander.ResponseListener()
                {
                    override fun onSuccess(responseStr: String)
                    {
                        LogUtil.d(TAG,"NotificationApi Map Sync:$responseStr")
                    }
                }).execute()
    }

    private fun verifyCloudAgentTask()
    {
        LogUtil.d(TAG,"verifyCloudAgentTask()")

        val params = ",\"credential\":\"${GlobalData.currentCredential}\""

        P2PGatewayApi.VerifyCloudAgent()
                .setRequestPageName(TAG)
                .setRequestPayload(params)
                .setResponseListener(object: TUTKP2PResponseCallback()
                {
                    override fun onSuccess(responseStr: String)
                    {
                        try
                        {
                            val data = JSONObject(responseStr)
                            val result = data.get("oper_status").toString()
                            if(result.equals("Success", ignoreCase = true))
                                startCloudGetAllNeedDeviceInfoTask(AppConfig.LoadingStyle.STY_NONE)
                            else
                                gotoTroubleShooting()
                        }
                        catch(e: Exception)
                        {
                            e.printStackTrace()
                            hideLoading()
                        }
                    }
                }).execute()
    }
}