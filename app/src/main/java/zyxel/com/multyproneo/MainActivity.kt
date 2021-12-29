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
import com.google.gson.Gson
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
import zyxel.com.multyproneo.model.cloud.TUTKAllDeviceInfo
import zyxel.com.multyproneo.model.cloud.TUTKTokenInfo
import zyxel.com.multyproneo.model.cloud.TUTKUserInfo
import zyxel.com.multyproneo.tool.CryptTool
import zyxel.com.multyproneo.util.*
import zyxel.com.multyproneo.wifichart.WiFiChannelChartListener
import java.io.File
import java.util.*
import kotlin.concurrent.schedule

class MainActivity : AppCompatActivity(), WiFiChannelChartListener
{
    private val TAG = "MainActivity"
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
    private lateinit var progressBar: ProgressBar
    private lateinit var progressBarHint: ProgressBar
    private lateinit var progressHintText: TextView
    private lateinit var getSpeedTestStatusTimer: CountDownTimer
    private lateinit var userInfo: TUTKUserInfo
    private lateinit var tokenInfo: TUTKTokenInfo
    private lateinit var db: DatabaseCloudUtil
    private var screenTimer = Timer()
    private var getWPSStatusTimer = Timer()
    private var getCloudWPSStatusTimer = Timer()

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        db = DatabaseCloudUtil.getInstance(this)!!

        if(AppConfig.SaveLog)
        {
            SaveLogUtil.oldFilePath = this.getExternalFilesDir(null)
            //path:/storage/emulated/0/Android/data/zyxel.com.multyproneo/files
            //user can find the file using file explorer

            val filepath = this.filesDir
                //path:/data/user/0/zyxel.com.multyproneo/files
                //user can not find the file using file explorer
            val logDir = File(filepath, "log")
                //path:/data/user/0/zyxel.com.multyproneo/files/log
            if(!logDir.exists())
            {
                logDir.mkdir()
            }
            SaveLogUtil.filePath = logDir
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

        if(GlobalData.isSupportMultiObjects())
        {
            if(GlobalData.currentFrag != "WiFiSettingsFragment")
                switchToFragContainer(WiFiSettingsFragment())
        }
        else
        {
            if(GlobalData.currentFrag != "WiFiSettingsOldFragment")
                switchToFragContainer(WiFiSettingsOldFragment())
        }
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
        val msgString = when (msg) {
            "Timeout" -> getString(R.string.commander_error_timeout)
            "Server is disconnect." -> getString(R.string.commander_error_disconnect)
            else -> msg
        }
        runOnUiThread{
            errorMsgDlg.description = msgString
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

    private fun stopGetAllNeedDeviceInfoTask()
    {
        hideLoading()
        GlobalBus.publish(HomeEvent.GetDeviceInfoComplete())
        GlobalBus.publish(DevicesEvent.GetDeviceInfoComplete())
        GlobalBus.publish(DevicesDetailEvent.GetDeviceInfoComplete())
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
                            {
                                P2PApiHandler().execute(
                                        P2PApiHandler.API_RES_EVENT.API_RES_EVENT_NOTI_ENTER,
                                        arrayListOf
                                        (
                                                P2PApiHandler.API_REF.API_GET_SYSTEM_INFO,
                                                P2PApiHandler.API_REF.API_GET_FW_VERSION,
                                                P2PApiHandler.API_REF.API_GET_LAN_IP,
                                                P2PApiHandler.API_REF.API_GET_APP_CUSTOM_INFO,
                                                P2PApiHandler.API_REF.API_GET_CHANGE_ICON_NAME,
                                                P2PApiHandler.API_REF.API_GET_DEVICE_INFO,
                                                P2PApiHandler.API_REF.API_GET_WAN_INFO,
                                                P2PApiHandler.API_REF.API_GET_GUEST_WIFI_ENABLE,
                                                P2PApiHandler.API_REF.API_GET_FSECURE_INFO,
                                                P2PApiHandler.API_REF.API_GET_HOSTNAME_REPLACE_INFO,
                                                P2PApiHandler.API_REF.API_CHECK_NOTI_FLOW
                                        )
                                )
                            }
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