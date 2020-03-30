package zyxel.com.multyproneo.fragment.cloud

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_cloud_settings.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import zyxel.com.multyproneo.BuildConfig
import zyxel.com.multyproneo.R
import zyxel.com.multyproneo.database.room.DatabaseSiteInfoEntity
import zyxel.com.multyproneo.event.GlobalBus
import zyxel.com.multyproneo.event.MainEvent
import zyxel.com.multyproneo.util.DatabaseCloudUtil
import zyxel.com.multyproneo.util.GlobalData
import zyxel.com.multyproneo.util.LogUtil

class CloudSettingsFragment : Fragment()
{
    private val TAG = javaClass.simpleName
    private lateinit var db: DatabaseCloudUtil
    private var currentDBInfo: DatabaseSiteInfoEntity? = null
    private var preserveSettingsEnable = false

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
                    }
                    uiThread{ updateUI() }
                }
            }

            settings_cloud_account_relative -> GlobalBus.publish(MainEvent.SwitchToFrag(CloudSettingsCloudAccountDetailFragment()))

            settings_privacy_policy_relative -> GlobalBus.publish(MainEvent.SwitchToFrag(CloudSettingsPrivacyPolicyFragment()))
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
        doAsync{
            currentDBInfo = db.getSiteInfoDao().queryByMac(GlobalData.getCurrentGatewayInfo().MAC)
            preserveSettingsEnable = currentDBInfo?.backup?:false
            LogUtil.e(TAG,"preserveSettingsEnable:$preserveSettingsEnable")
            uiThread{ updateUI() }
        }
    }

    private fun updateUI()
    {
        settings_preserve_settings_switch_image.setImageResource(if(preserveSettingsEnable) R.drawable.switch_on else R.drawable.switch_off)
        settings_app_version_value_text.text = "V${BuildConfig.VERSION_NAME}"
    }
}