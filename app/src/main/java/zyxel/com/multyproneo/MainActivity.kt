package zyxel.com.multyproneo

import android.app.Dialog
import android.content.Context
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentTransaction
import android.support.v7.app.AlertDialog
import android.view.View
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.activity_main.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import zyxel.com.multyproneo.event.DevicesEvent
import zyxel.com.multyproneo.event.GlobalBus
import zyxel.com.multyproneo.event.HomeEvent
import zyxel.com.multyproneo.event.MainEvent
import zyxel.com.multyproneo.fragment.FindingDeviceFragment
import zyxel.com.multyproneo.model.EndDeviceProfile
import zyxel.com.multyproneo.model.WanInfoProfile
import zyxel.com.multyproneo.util.AppConfig
import zyxel.com.multyproneo.util.GlobalData
import zyxel.com.multyproneo.util.LogUtil
import java.util.*
import kotlin.concurrent.schedule

class MainActivity : AppCompatActivity()
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
    private lateinit var loadingDlg: Dialog
    private lateinit var deviceTimer: Timer
    private var currentFrag = ""

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        loadingDlg = createLoadingDlg(this)
        listenEvent()
        switchToFragContainer(FindingDeviceFragment())
    }

    override fun onResume()
    {
        super.onResume()
    }

    override fun onDestroy()
    {
        super.onDestroy()
        disposeEvent()
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

    private fun createLoadingDlg(context: Context): Dialog
    {
        val builder = AlertDialog.Builder(context, R.style.loadingStyle)
        builder.setCancelable(false)
        builder.setView(getLayoutInflater().inflate(R.layout.dialog_loading, null))
        return builder.create()
    }

    private fun listenEvent()
    {
        switchFrgDisposable = GlobalBus.listen(MainEvent.SwitchToFrag::class.java).subscribe{
            switchToFragContainer(it.frag)
        }

        showLoadingDisposable = GlobalBus.listen(MainEvent.ShowLoading::class.java).subscribe{
            showLoading()
        }

        hideLoadingDisposable = GlobalBus.listen(MainEvent.HideLoading::class.java).subscribe{
            hideLoading()
        }

        showBottomToolbarDisposable = GlobalBus.listen(MainEvent.ShowBottomToolbar::class.java).subscribe{
            bottom_toolbar.visibility = View.VISIBLE
        }

        hideBottomToolbarDisposable = GlobalBus.listen(MainEvent.HideBottomToolbar::class.java).subscribe{
            bottom_toolbar.visibility = View.GONE
        }

        setHomeIconFocusDisposable = GlobalBus.listen(MainEvent.SetHomeIconFocus::class.java).subscribe{
            disSelectToolBarIcons()
            home_image.isSelected = true
            home_text.isSelected = true
        }

        startGetDeviceInfoTaskDisposable = GlobalBus.listen(MainEvent.StartGetDeviceInfoTask::class.java).subscribe{
            deviceTimer = Timer()
            deviceTimer.schedule(0, (AppConfig.endDeviceListUpdateTime * 1000).toLong()){
                getDeviceInfoTask()
            }
        }

        stopGetDeviceInfoTaskDisposable = GlobalBus.listen(MainEvent.StopGetDeviceInfoTask::class.java).subscribe{
            deviceTimer.cancel()
        }
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

    private fun showLoading()
    {
        runOnUiThread{ Runnable{ if(!loadingDlg.isShowing) loadingDlg.show() }.run() }
    }

    private fun hideLoading()
    {
        runOnUiThread{ Runnable{ if(loadingDlg.isShowing) loadingDlg.dismiss() }.run() }
    }

    private fun getDeviceInfoTask()
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
                if(item.CapabilityType.equals("L2Device"))
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
    }
}
