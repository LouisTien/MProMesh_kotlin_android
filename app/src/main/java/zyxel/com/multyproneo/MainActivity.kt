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
import zyxel.com.multyproneo.event.GlobalBus
import zyxel.com.multyproneo.event.MainEvent
import zyxel.com.multyproneo.fragment.FindingDeviceFragment
import zyxel.com.multyproneo.util.LogUtil

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
    private var currentFrag: String = ""

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
}
