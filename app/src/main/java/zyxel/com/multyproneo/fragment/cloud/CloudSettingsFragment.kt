package zyxel.com.multyproneo.fragment.cloud

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.gson.Gson
import kotlinx.android.synthetic.main.fragment_cloud_settings.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import org.json.JSONException
import zyxel.com.multyproneo.BuildConfig
import zyxel.com.multyproneo.R
import zyxel.com.multyproneo.api.cloud.P2PWiFiSettingApi
import zyxel.com.multyproneo.api.cloud.TUTKP2PResponseCallback
import zyxel.com.multyproneo.database.room.DatabaseSiteInfoEntity
import zyxel.com.multyproneo.event.GlobalBus
import zyxel.com.multyproneo.event.MainEvent
import zyxel.com.multyproneo.model.WiFiSettingInfo
import zyxel.com.multyproneo.util.DatabaseCloudUtil
import zyxel.com.multyproneo.util.GlobalData
import zyxel.com.multyproneo.util.LogUtil

class CloudSettingsFragment : Fragment()
{
    private val TAG = javaClass.simpleName
    private lateinit var db: DatabaseCloudUtil
    private lateinit var WiFiSettingInfoSet: WiFiSettingInfo
    private var currentDBInfo: DatabaseSiteInfoEntity? = null
    private var preserveSettingsEnable = false
    private var WiFiName = ""
    private var WiFiPwd = ""

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
    {
        return inflater.inflate(R.layout.fragment_cloud_settings, container, false)
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
        GlobalBus.publish(MainEvent.ShowCloudBottomToolbar())
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
            settings_notification_relative -> GlobalBus.publish(MainEvent.SwitchToFrag(CloudSettingsNotificationDetailFragment()))

            settings_preserve_settings_switch_image -> {
                preserveSettingsEnable = !preserveSettingsEnable

                doAsync{
                    if(currentDBInfo != null)
                    {
                        currentDBInfo!!.backup = preserveSettingsEnable
                        db.getSiteInfoDao().insert(currentDBInfo!!)
                        uiThread{ updateUI() }
                    }
                    else
                    {
                        GlobalBus.publish(MainEvent.ShowLoading())
                        getWiFiSettingInfoTask()
                    }
                }
            }

            settings_cloud_account_relative -> GlobalBus.publish(MainEvent.SwitchToFrag(CloudSettingsCloudAccountDetailFragment()))

            settings_privacy_policy_relative -> GlobalBus.publish(MainEvent.SwitchToFrag(CloudSettingsPrivacyPolicyFragment()))

            settings_troubleshooting_relative -> GlobalBus.publish(MainEvent.SwitchToFrag(CloudSettingsTroubleshootingFragment()))
        }
    }

    private fun setClickListener()
    {
        settings_notification_relative.setOnClickListener(clickListener)
        settings_preserve_settings_switch_image.setOnClickListener(clickListener)
        settings_troubleshooting_relative.setOnClickListener(clickListener)
        settings_cloud_account_relative.setOnClickListener(clickListener)
        settings_privacy_policy_relative.setOnClickListener(clickListener)
    }

    private fun getInfoFromDB()
    {
        LogUtil.d(TAG,"MAC:${GlobalData.getCurrentGatewayInfo().MAC}")
        doAsync{
            currentDBInfo = db.getSiteInfoDao().queryByMac(GlobalData.getCurrentGatewayInfo().MAC)
            preserveSettingsEnable = currentDBInfo?.backup?:false
            LogUtil.d(TAG,"preserveSettingsEnable:$preserveSettingsEnable")
            uiThread{ updateUI() }
        }
    }

    private fun updateUI()
    {
        settings_preserve_settings_switch_image.setImageResource(if(preserveSettingsEnable) R.drawable.switch_on else R.drawable.switch_off)
        settings_app_version_value_text.text = "V${BuildConfig.VERSION_NAME}"
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
                    preserveSettingsEnable
            )
            db.getSiteInfoDao().insert(currentDBInfo!!)
            uiThread{ updateUI() }
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
                            /*WiFiSecurity = WiFiSettingInfoSet.Object.AccessPoint[0].Security.ModeEnabled
                            WiFiName5g = WiFiSettingInfoSet.Object.SSID[4].SSID
                            WiFiPwd5g = WiFiSettingInfoSet.Object.AccessPoint[4].Security.KeyPassphrase
                            WiFiSecurity5g = WiFiSettingInfoSet.Object.AccessPoint[4].Security.ModeEnabled
                            guestWiFiName = WiFiSettingInfoSet.Object.SSID[1].SSID
                            guestWiFiPwd = WiFiSettingInfoSet.Object.AccessPoint[1].Security.KeyPassphrase
                            guestWiFiSecurity = WiFiSettingInfoSet.Object.AccessPoint[1].Security.ModeEnabled
                            guestWiFiStatus = WiFiSettingInfoSet.Object.SSID[1].Enable*/
                            addToDB()
                            GlobalBus.publish(MainEvent.HideLoading())
                        }
                        catch(e: JSONException)
                        {
                            GlobalBus.publish(MainEvent.HideLoading())
                            e.printStackTrace()
                        }
                    }
                }).execute()

    }
}