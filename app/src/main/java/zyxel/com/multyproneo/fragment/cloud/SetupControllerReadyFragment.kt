package zyxel.com.multyproneo.fragment.cloud

import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewpager.widget.ViewPager
import kotlinx.android.synthetic.main.fragment_setup_controller_ready.*
import org.jetbrains.anko.doAsync
import zyxel.com.multyproneo.R
import zyxel.com.multyproneo.adapter.SetupControllerReadyHelpAdapter
import zyxel.com.multyproneo.api.cloud.*
import zyxel.com.multyproneo.dialog.SetupControllerReadyHelpDialog
import zyxel.com.multyproneo.event.GlobalBus
import zyxel.com.multyproneo.event.MainEvent
import zyxel.com.multyproneo.util.*
import java.util.*

class SetupControllerReadyFragment : Fragment()
{
    private val TAG = javaClass.simpleName
    private lateinit var helpDlg: SetupControllerReadyHelpDialog

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
    {
        return inflater.inflate(R.layout.fragment_setup_controller_ready, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)

        val helpAdapter = SetupControllerReadyHelpAdapter(activity!!)
        setup_controller_ready_help_pager.adapter = helpAdapter
        setup_controller_ready_help_pager.offscreenPageLimit = 3
        setup_controller_ready_help_pager.addOnPageChangeListener(object: ViewPager.OnPageChangeListener
        {
            override fun onPageScrollStateChanged(state: Int)
            {
            }

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int)
            {
            }

            override fun onPageSelected(position: Int)
            {
            }

        })
        setup_controller_ready_help_pager_indicator.attachTo(setup_controller_ready_help_pager)

        setClickListener()

        GlobalBus.publish(MainEvent.ShowLoading())
        doAsync{
            TUTKP2PBaseApi.stopSession()
            GlobalBus.publish(MainEvent.HideLoading())
        }
    }

    override fun onResume()
    {
        super.onResume()
        GlobalBus.publish(MainEvent.HideBottomToolbar())
    }

    override fun onPause()
    {
        super.onPause()
    }

    override fun onDestroyView()
    {
        super.onDestroyView()
    }

    private val clickListener = View.OnClickListener{ view ->
        when(view)
        {
            setup_controller_ready_help_image ->
            {
                helpDlg = SetupControllerReadyHelpDialog(activity!!)
                helpDlg.show()
            }

            setup_controller_ready_next_image -> GlobalBus.publish(MainEvent.SwitchToFrag(SetupConnectControllerFragment()))
        }
    }

    private fun setClickListener()
    {
        setup_controller_ready_help_image.setOnClickListener(clickListener)
        setup_controller_ready_next_image.setOnClickListener(clickListener)
    }

    fun notificationTest()
    {
        var phoneUdid = Settings.System.getString(activity!!.contentResolver, Settings.Secure.ANDROID_ID)
        var notificationToken by SharedPreferencesUtil(activity!!, AppConfig.SHAREDPREF_NOTIFICATION_TOKEN, "")

        val header = HashMap<String, Any>()
        val body = HashMap<String, Any>()
        body["cmd"] = "client"
        body["os"] = "android"
        body["appid"] = AppConfig.NOTI_BUNDLE_ID
        body["udid"] = phoneUdid
        body["token"] = notificationToken
        body["lang"] = "enUS"
        body["bgfetch"] = 1
        body["dev"] = 0

        NotificationApi.Common(activity!!)
                .setRequestPageName(TAG)
                .setHeaders(header)
                .setFormBody(body)
                .setResponseListener(object: TUTKCommander.ResponseListener()
                {
                    override fun onSuccess(responseStr: String)
                    {
                        LogUtil.d(TAG,"NotificationApi Register:$responseStr")
                        mapping()
                    }
                }).execute()
    }

    fun mapping()
    {
        LogUtil.d(TAG,"mapping()")

        var phoneUdid = Settings.System.getString(activity!!.contentResolver, Settings.Secure.ANDROID_ID)

        val header = HashMap<String, Any>()
        val body = HashMap<String, Any>()
        body["cmd"] = "mapping"
        body["os"] = "android"
        body["appid"] = AppConfig.NOTI_BUNDLE_ID
        body["uid"] = "EBKUAX3MUD7M9G6GU1CJ"
        body["udid"] = phoneUdid
        body["format"] = "e2Rldl9uYW1lfSB7ZXZlbnRfdHlwZX0=" //{dev_name} {event_type}
        body["interval"] = 3
        //body["customized_payload"] = "eyJjb250ZW50LWF2YWlsYWJsZSI6MSwiYWxlcnQiOnsidGl0bGUiOnslTVlUT1BJQyV9LCJib2R5Ijp7JU1ZQk9EWSV9fSwiZGF0YSI6eyJMb3VpcyI6eyVMTEwlfSwiQW15Ijp7JUFBQSV9fX0==="

        NotificationApi.Common(activity!!)
                .setRequestPageName(TAG)
                .setHeaders(header)
                .setFormBody(body)
                .setResponseListener(object: TUTKCommander.ResponseListener()
                {
                    override fun onSuccess(responseStr: String)
                    {
                        LogUtil.d(TAG,"NotificationApi Mapping:$responseStr")
                    }
                }).execute()
    }

    fun removeMapping()
    {
        LogUtil.d(TAG,"removeMapping()")

        var phoneUdid = Settings.System.getString(activity!!.contentResolver, Settings.Secure.ANDROID_ID)

        val header = HashMap<String, Any>()
        val body = HashMap<String, Any>()
        body["cmd"] = "rm_mapping"
        body["os"] = "android"
        body["appid"] = AppConfig.NOTI_BUNDLE_ID
        body["uid"] = "EBKUAX3MUD7M9G6GU1CJ"
        body["udid"] = phoneUdid

        NotificationApi.Common(activity!!)
                .setRequestPageName(TAG)
                .setHeaders(header)
                .setFormBody(body)
                .setResponseListener(object: TUTKCommander.ResponseListener()
                {
                    override fun onSuccess(responseStr: String)
                    {
                        LogUtil.d(TAG,"NotificationApi removeMapping:$responseStr")
                    }
                }).execute()
    }
}