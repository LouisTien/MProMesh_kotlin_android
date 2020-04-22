package zyxel.com.multyproneo.fragment.cloud

import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.gson.Gson
import kotlinx.android.synthetic.main.fragment_settings_notification_detail.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import org.json.JSONException
import zyxel.com.multyproneo.R
import zyxel.com.multyproneo.api.cloud.NotificationApi
import zyxel.com.multyproneo.api.cloud.P2PWiFiSettingApi
import zyxel.com.multyproneo.api.cloud.TUTKCommander
import zyxel.com.multyproneo.api.cloud.TUTKP2PResponseCallback
import zyxel.com.multyproneo.database.room.DatabaseSiteInfoEntity
import zyxel.com.multyproneo.event.GlobalBus
import zyxel.com.multyproneo.event.MainEvent
import zyxel.com.multyproneo.model.WiFiSettingInfo
import zyxel.com.multyproneo.util.*
import java.util.HashMap

class CloudSettingsNotificationDetailFragment : Fragment()
{
    private val TAG = javaClass.simpleName
    private lateinit var db: DatabaseCloudUtil
    private lateinit var WiFiSettingInfoSet: WiFiSettingInfo
    private var currentDBInfo: DatabaseSiteInfoEntity? = null
    private var notificationEnable = false
    private var WiFiName = ""
    private var WiFiPwd = ""

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
    {
        return inflater.inflate(R.layout.fragment_settings_notification_detail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)
        db = DatabaseCloudUtil.getInstance(activity!!)!!
        setClickListener()
        getInfoFromDB()
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
            settings_notification_detail_back_image -> GlobalBus.publish(MainEvent.EnterCloudSettingsPage())

            settings_notification_detail_switch_image -> {
                notificationEnable = !notificationEnable

                GlobalBus.publish(MainEvent.ShowLoading())
                if(notificationEnable)
                    registerNoti()
                else
                    removeMappingNoti()
            }
        }
    }

    private fun setClickListener()
    {
        settings_notification_detail_back_image.setOnClickListener(clickListener)
        settings_notification_detail_switch_image.setOnClickListener(clickListener)
    }

    private fun getInfoFromDB()
    {
        LogUtil.d(TAG,"MAC:${GlobalData.getCurrentGatewayInfo().MAC}")
        doAsync{
            currentDBInfo = db.getSiteInfoDao().queryByMac(GlobalData.getCurrentGatewayInfo().MAC)
            notificationEnable = currentDBInfo?.notification?:false
            LogUtil.d(TAG,"notificationEnable:$notificationEnable")
            uiThread{ updateUI() }
        }
    }

    private fun updateUI()
    {
        GlobalBus.publish(MainEvent.HideLoading())
        settings_notification_detail_switch_image.setImageResource(if(notificationEnable) R.drawable.switch_on else R.drawable.switch_off)
    }

    private fun addToDB()
    {
        doAsync{
            currentDBInfo = DatabaseSiteInfoEntity(
                    GlobalData.getCurrentGatewayInfo().MAC,
                    GlobalData.currentUID,
                    GlobalData.currentDisplayName,
                    "N/A",
                    WiFiName,
                    WiFiPwd,
                    false,
                    notificationEnable
            )
            db.getSiteInfoDao().insert(currentDBInfo!!)
            uiThread{ updateUI() }
        }
    }

    private fun updateToDB()
    {
        doAsync{
            if(currentDBInfo != null)
            {
                LogUtil.d(TAG,"MAC in DB :${GlobalData.getCurrentGatewayInfo().MAC}")
                currentDBInfo!!.notification = notificationEnable
                db.getSiteInfoDao().insert(currentDBInfo!!)
                uiThread{ updateUI() }
            }
            else
            {
                LogUtil.d(TAG,"MAC not in DB :${GlobalData.getCurrentGatewayInfo().MAC}")
                getWiFiSettingInfoTask()
            }
        }
    }

    private fun getWiFiSettingInfoTask()
    {
        LogUtil.d(TAG,"getWiFiSettingInfoTask()")
        P2PWiFiSettingApi.GetWiFiSettingInfo()
                .setRequestPageName(TAG)
                .setResponseListener(object: TUTKP2PResponseCallback()
                {
                    override fun onSuccess(responseStr: String)
                    {
                        try
                        {
                            WiFiSettingInfoSet = Gson().fromJson(responseStr, WiFiSettingInfo::class.javaObjectType)
                            LogUtil.d(TAG,"wiFiSettingInfo:$WiFiSettingInfoSet")

                            WiFiName = WiFiSettingInfoSet.Object.SSID[0].SSID
                            WiFiPwd = WiFiSettingInfoSet.Object.AccessPoint[0].Security.KeyPassphrase
                            addToDB()
                        }
                        catch(e: JSONException)
                        {
                            e.printStackTrace()

                            GlobalBus.publish(MainEvent.HideLoading())
                        }
                    }
                }).execute()
    }

    private fun registerNoti()
    {
        LogUtil.d(TAG,"registerNoti()")

        val phoneUdid = Settings.System.getString(activity!!.contentResolver, Settings.Secure.ANDROID_ID)
        val notificationToken by SharedPreferencesUtil(activity!!, AppConfig.SHAREDPREF_NOTIFICATION_TOKEN, "")

        val header = HashMap<String, Any>()
        val body = HashMap<String, Any>()
        body["cmd"] = "client"
        body["os"] = "android"
        body["appid"] = AppConfig.NOTI_BUNDLE_ID
        body["udid"] = phoneUdid
        body["token"] = notificationToken
        body["lang"] = "enUS"
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
                        mappingNoti()
                    }
                }).execute()
    }

    private fun mappingNoti()
    {
        LogUtil.d(TAG,"mappingNoti()")

        val phoneUdid = Settings.System.getString(activity!!.contentResolver, Settings.Secure.ANDROID_ID)

        val header = HashMap<String, Any>()
        val body = HashMap<String, Any>()
        body["cmd"] = "mapping"
        body["os"] = "android"
        body["appid"] = AppConfig.NOTI_BUNDLE_ID
        body["uid"] = GlobalData.currentUID
        body["udid"] = phoneUdid
        body["format"] = AppConfig.NOTI_FORMAT
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
                        updateToDB()
                    }
                }).execute()
    }

    private fun removeMappingNoti()
    {
        LogUtil.d(TAG,"removeMappingNoti()")

        val phoneUdid = Settings.System.getString(activity!!.contentResolver, Settings.Secure.ANDROID_ID)

        val header = HashMap<String, Any>()
        val body = HashMap<String, Any>()
        body["cmd"] = "rm_mapping"
        body["os"] = "android"
        body["appid"] = AppConfig.NOTI_BUNDLE_ID
        body["uid"] = GlobalData.currentUID
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
                        updateToDB()
                    }
                }).execute()
    }
}